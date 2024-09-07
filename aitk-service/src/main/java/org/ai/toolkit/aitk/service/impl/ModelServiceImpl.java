package org.ai.toolkit.aitk.service.impl;

import ai.djl.Device;
import ai.djl.modality.Input;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.djl.modality.Output;
import org.ai.toolkit.aitk.common.git.GitEnum;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.ai.toolkit.aitk.common.modelscope.DownloadState;
import org.ai.toolkit.aitk.common.modelscope.FileDownloadUtil;
import org.ai.toolkit.aitk.modelzoo.ModelRepositoryType;
import org.ai.toolkit.aitk.modelzoo.constant.ModelParentTypeEnum;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceExecutor;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.modelmanager.ModelManager;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.llm.LlamaCppModelDefinition;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.constant.ModelLoadStateEnum;
import org.ai.toolkit.aitk.service.thread.AitkThreadFactory;
import org.ai.toolkit.aitk.service.vo.LlmModelVO;
import org.ai.toolkit.aitk.service.vo.ModelLoadVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.service.vo.ModelNodeDataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ModelServiceImpl implements ModelService {

    private static Logger LOGGER = LoggerFactory.getLogger(ModelService.class);
    private static final String ROOT = "root";

    @Autowired
    private ModelManager modelManager;
    @Autowired
    private InferenceExecutor inferenceExecutor;
    @Autowired
    private ModelRepositoryType modelRepositoryType;

    private final Map<String, ModelLoadVO> modelLoadMap = new ConcurrentHashMap<>();

    private ThreadFactory threadFactory = new AitkThreadFactory("model-load-pool-");

    // 预留后续是否需要支持并发加载
    private final ExecutorService executorService = new ThreadPoolExecutor(
            2,
            5,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),
            threadFactory,
            new ThreadPoolExecutor.AbortPolicy()
    );


    @Override
    public void asyncPredict(String modelId, Input input, InferenceCallback callback) {
        inferenceExecutor.asyncExecute(modelId, input, callback);
    }

    @Override
    public Output syncPredict(String modelId, Input input) {
        return inferenceExecutor.syncExecute(modelId, input);
    }

    @Override
    public ModelParamVO getModelParamVO(String modelId) {
        ModelDefinition modelDefinition = modelManager.getModelDefinition(modelId);
        return new ModelParamVO(modelDefinition.getLoadModelParams(), modelDefinition.getRequestParams(),
                modelDefinition.getResponseParams(), modelDefinition.getModelBasicInfo());
    }

    @Override
    public List<ModelNodeDataVO> getModelTreeData() {
        List<ModelDefinition> modelDefinitionList = modelManager.getModelList();
        if (CollectionUtils.isEmpty(modelDefinitionList)) {
            return new ArrayList<>();
        }
        Map<ModelTypeEnum, List<ModelDefinition>> modelTypeEnumListMap = new HashMap<>();
        Set<String> llmModelNameSet = new HashSet<>();
        for (ModelDefinition modelDefinition : modelDefinitionList) {
            if (modelDefinition instanceof LlamaCppModelDefinition) {
                if (llmModelNameSet.contains(((LlamaCppModelDefinition) modelDefinition).getModelName())) {
                    continue;
                } else {
                    llmModelNameSet.add(((LlamaCppModelDefinition) modelDefinition).getModelName());
                }
            }

            if (modelTypeEnumListMap.containsKey(modelDefinition.getModelType())) {
                modelTypeEnumListMap.get(modelDefinition.getModelType()).add(modelDefinition);
            } else {
                modelTypeEnumListMap.put(modelDefinition.getModelType(), new ArrayList<>(Arrays.asList(modelDefinition)));
            }
        }
        List<ModelNodeDataVO> result = new ArrayList<>();
        Map<ModelParentTypeEnum, List<ModelTypeEnum>> modelParentTypeEnumListMap = ModelTypeEnum.getParentTypeMap();
        for (ModelParentTypeEnum modelParentTypeEnum : ModelParentTypeEnum.values()) {
            ModelNodeDataVO modelNodeDataVO = new ModelNodeDataVO();
            modelNodeDataVO.setLabel(modelParentTypeEnum.getName());
            modelNodeDataVO.setId(modelParentTypeEnum.name());
            List<ModelTypeEnum> modelTypeEnums = modelParentTypeEnumListMap.get(modelParentTypeEnum);
            if (CollectionUtils.isEmpty(modelTypeEnums)) {
                continue;
            }
            List<ModelNodeDataVO> modelTypeNodes = new ArrayList<>();
            modelNodeDataVO.setChildren(modelTypeNodes);
            for (ModelTypeEnum modelTypeEnum : modelTypeEnums) {
                ModelNodeDataVO modelTypeNode = new ModelNodeDataVO();
                modelTypeNode.setLabel(modelTypeEnum.getName());
                modelTypeNode.setId(modelTypeEnum.name());
                modelTypeNodes.add(modelTypeNode);
                if (modelTypeEnumListMap.containsKey(modelTypeEnum)) {
                    List<ModelNodeDataVO> models = new ArrayList<>();
                    modelTypeNode.setChildren(models);
                    modelTypeEnumListMap.get(modelTypeEnum).forEach(model -> {
                                ModelNodeDataVO modelDefine = new ModelNodeDataVO();
                                modelDefine.setId(model.getId());
                                modelDefine.setLabel(model.getModelBasicInfo().getDisplayName());
                                modelDefine.setPath(model.getModelType().name());
                                models.add(modelDefine);
                            }
                    );
                }
            }
            result.add(modelNodeDataVO);
        }
        return result;
    }

    @Override
    public List<LlmModelVO> getLllModelVOByModelName(String modelName) {
        List<LlmModelVO> llmModelVOList = modelManager.getModelList().stream().filter(o -> o instanceof LlamaCppModelDefinition)
                .map(o -> (LlamaCppModelDefinition) o)
                .filter(o -> o.getModelName().equals(modelName)).map(o -> new LlmModelVO(o.getId(), o.getSize())).collect(Collectors.toList());

        return llmModelVOList;
    }

    @Override
    public ModelLoadVO getModelStateByModelId(String modelId) {
        ModelLoadVO modelLoadVO = new ModelLoadVO();
        if (!modelManager.isExist(modelId)) {
            modelLoadVO.setState(ModelLoadStateEnum.NONE.getValue());
            return modelLoadVO;
        }
        if (modelManager.isLoaded(modelId)) {
            modelLoadVO.setState(ModelLoadStateEnum.SUCCESS.getValue());
            return modelLoadVO;
        }
        if (modelLoadMap.get(modelId) == null) {
            modelLoadVO.setState(ModelLoadStateEnum.PENDING.getValue());
            return modelLoadVO;
        }
        modelLoadVO = modelLoadMap.get(modelId);
        if (modelLoadVO.getState() == ModelLoadStateEnum.DOWNLOAD.getValue()) {
            Integer totalPro = modelLoadVO.getProgressMap().values().stream().
                    flatMapToInt(value -> value == null ? IntStream.of(0) : IntStream.of(value.getProgress())).sum();
            modelLoadVO.setProgress(modelLoadVO.getFileCount() == 0 ? 100 : totalPro / modelLoadVO.getFileCount());
        }
        return modelLoadVO;
    }

    @Override
    public boolean startLoad(String modelId) {
        ModelLoadVO modelLoadVO = new ModelLoadVO();
        modelLoadVO.setState(ModelLoadStateEnum.PENDING.getValue());
        ModelDefinition modelDefinition = modelManager.getModelDefinition(modelId);
        modelLoadVO.setFileCount(CollectionUtils.isEmpty(modelDefinition.getModelFileList()) ?
                0 : modelDefinition.getModelFileList().size());
        modelLoadMap.put(modelId, modelLoadVO);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // 先卸载
                modelManager.unloadModel(modelId);
                GitEnum gitEnum = modelRepositoryType.getDefaultGitEnum();
                boolean isDownLoad = true;
                List<String> fileList = modelDefinition.getModelFileList();
                for (String file : fileList) {
                    String filePath = file.startsWith("/") ? GitUtil.getModelBasePath(gitEnum) + file : GitUtil.getModelBasePath(gitEnum) + "/" + file;
                    if (!Paths.get(filePath).toFile().exists()) {
                        isDownLoad = false;
                        break;
                    }
                }
                if (!isDownLoad) {
                    // 下载
                    modelLoadMap.get(modelId).setState(ModelLoadStateEnum.DOWNLOAD.getValue());
                    ConcurrentHashMap progressMap = new ConcurrentHashMap<String, DownloadState>();
                    modelLoadMap.get(modelId).setProgressMap(progressMap);
                    try {
                        FileDownloadUtil.download(modelDefinition.getModelFileList(), progressMap, gitEnum);
                    } catch (Exception e) {
                        LOGGER.error("downloadModel", e);
                    }
                }
                // 加载
                modelLoadMap.get(modelId).setState(ModelLoadStateEnum.LOADING.getValue());
                try {
                    modelManager.loadModel(modelDefinition.getId(), Device.cpu(), null);
                } catch (Exception e) {
                    LOGGER.error("loadModel", e);
                }
                // 完成
                modelLoadMap.get(modelId).setState(ModelLoadStateEnum.SUCCESS.getValue());
            }
        });
        return true;
    }

    @Override
    public boolean unloadModel(String modelId) {
        ModelLoadVO modelLoadVO = new ModelLoadVO();
        modelLoadVO.setState(ModelLoadStateEnum.UNLOADING.getValue());
        modelLoadMap.put(modelId, modelLoadVO);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                modelManager.unloadModel(modelId);
                // 卸载完成变成待加载状态
                modelLoadMap.get(modelId).setState(ModelLoadStateEnum.PENDING.getValue());
            }
        });
        return true;
    }

    @Override
    public List<String> getMenuPath(String modelId) {
        ModelNodeDataVO root = new ModelNodeDataVO();
        root.setId(ROOT);
        root.setLabel(ROOT);
        root.setChildren(getModelTreeData());
        List<ModelNodeDataVO> result = getPath(root, modelId, new ArrayList<>());
        return result.stream().filter(o -> !ROOT.equals(o.getId())).map(ModelNodeDataVO::getLabel).collect(Collectors.toList());
    }

    private List<ModelNodeDataVO> getPath(ModelNodeDataVO node, String targetId, List<ModelNodeDataVO> path) {
        if (node == null) {
            return null;
        }
        path = new ArrayList<>(path);
        path.add(node);
        if (targetId.equals(node.getId())) {
            return path;
        }
        if (Objects.isNull(node.getChildren())) {
            return null;
        }
        for (ModelNodeDataVO child : node.getChildren()) {
            List<ModelNodeDataVO> result = getPath(child, targetId, path);
            if (result != null) {
                return result;
            }
        }
        path.remove(path.size() - 1);
        return null;
    }
}

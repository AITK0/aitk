package org.ai.toolkit.aitk.controller;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.ndarray.BytesSupplier;
import com.google.gson.Gson;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.IOTypeEnum;
import org.ai.toolkit.aitk.modelzoo.llm.LlamaCppSupplier;
import org.ai.toolkit.aitk.service.ModelService;
import org.ai.toolkit.aitk.service.vo.LlmModelVO;
import org.ai.toolkit.aitk.service.vo.ModelLoadVO;
import org.ai.toolkit.aitk.service.vo.ModelNodeDataVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.vo.ResultVO;
import org.ai.toolkit.aitk.websocket.WebSocketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@RequestMapping("/aitk")
@RestController
public class AitkController {

    private static Logger LOGGER = LoggerFactory.getLogger(AitkController.class);

    private static final String MODEL_ID_KEY = "modelId";
    @Resource
    private WebSocketManager webSocketManager;

    @Resource
    private ModelService modelService;

    @RequestMapping("/getModelTreeData")
    public ResultVO<List<ModelNodeDataVO>> getModelTreeData() {
        return ResultVO.createSuccessResultVO(modelService.getModelTreeData());
    }

    @RequestMapping("/getLllModelVOByModelName")
    public ResultVO<List<LlmModelVO>> getLllModelVOByModelName(@RequestParam(name = "modelName", required = true) String modelName) {
        return ResultVO.createSuccessResultVO(modelService.getLllModelVOByModelName(modelName));
    }

    @RequestMapping("/getModelStateByModelId")
    public ResultVO<ModelLoadVO> getModelStateByModelId(@RequestParam(name = "modelId", required = true) String modelId) {
        return ResultVO.createSuccessResultVO(modelService.getModelStateByModelId(modelId));
    }

    @RequestMapping("/startLoad")
    public ResultVO<Boolean> startLoad(@RequestParam(name = "modelId", required = true) String modelId) {
        return ResultVO.createSuccessResultVO(modelService.startLoad(modelId));
    }

    @RequestMapping("/unloadModel")
    public ResultVO<Boolean> unloadModel(@RequestParam(name = "modelId", required = true) String modelId) {
        return ResultVO.createSuccessResultVO(modelService.unloadModel(modelId));
    }

    @RequestMapping("/getModelParamByModelId")
    public ResultVO<ModelParamVO> getModelParamByModelId(@RequestParam(name = "modelId", required = true) String modelId) {
        return ResultVO.createSuccessResultVO(modelService.getModelParamVO(modelId));
    }

    @RequestMapping("/getMenuPath")
    public ResultVO<List<String>> getMenuPath(@RequestParam(name = "modelId", required = true) String modelId) {
        return ResultVO.createSuccessResultVO(modelService.getMenuPath(modelId));
    }

    @RequestMapping("infer")
    public Map<String, Object> infer(MultipartHttpServletRequest request) throws IOException {
        String modelId = request.getParameter(MODEL_ID_KEY);
        if (!StringUtils.hasLength(modelId)) {
            throw new AitkException(AitkErrorCode.PARAMETER_VALIDATION_ERROR, "missing modelId parameter");
        }
        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
        ModelParamVO modelParamVO = modelService.getModelParamVO(modelId);
        List<Param> requestParams = modelParamVO.getRequestParams();
        Input input = new Input();
        for (Param param : requestParams) {
            if (IOTypeEnum.TEXT.equals(param.getFileExtension().getFileType())) {
                input.add(param.getName(), request.getParameter(param.getName()));
            } else {
                input.add(param.getName(), multipartFileMap.get(param.getName()).getBytes());
            }
        }
        Output output = modelService.syncPredict(modelId, input);
        Map<String, Object> result = new HashMap<>();
        for (Param param : modelParamVO.getResponseParams()) {
            result.put(param.getName(), output.getAsString(param.getName()));
        }
        return result;
    }

    @RequestMapping("handleMessage")
    public void handleMessage(MultipartHttpServletRequest request) throws IOException {
        String modelId = request.getParameter(MODEL_ID_KEY);
        if (!StringUtils.hasLength(modelId)) {
            throw new AitkException(AitkErrorCode.PARAMETER_VALIDATION_ERROR, "missing modelId parameter");
        }
        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
        ModelParamVO modelParamVO = modelService.getModelParamVO(modelId);
        List<Param> requestParams = modelParamVO.getRequestParams();
        Input input = new Input();
        for (Param param : requestParams) {
            if (null == request.getParameter(param.getName())) {
                continue;
            }
            if (IOTypeEnum.TEXT.equals(param.getFileExtension().getFileType())) {
                input.add(param.getName(), request.getParameter(param.getName()));
            } else {
                input.add(param.getName(), multipartFileMap.get(param.getName()).getBytes());
            }
        }
        modelService.asyncPredict(modelId, input, (throwable, output) -> {
            if (!Objects.isNull(throwable)) {
                LOGGER.error("Model execution encountered an exception", throwable);
                return;
            }
            List<Param> responseParams = modelParamVO.getResponseParams();
            for (Param param : responseParams) {
                if (IOTypeEnum.STREAM.equals(param.getFileExtension().getFileType())) {
                    BytesSupplier bytesSupplier = output.get(param.getName());
                    if (bytesSupplier instanceof LlamaCppSupplier) {
                        try {
                            LlamaCppSupplier llamaCppSupplier = (LlamaCppSupplier) bytesSupplier;
                            String msgId = UUID.randomUUID().toString();
                            InputStream responseBody = llamaCppSupplier.getHttpResponse().body();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                Map<String, Object> result = new HashMap<>();
                                result.put("id", msgId);
                                result.put("text", line);
                                webSocketManager.sendMessage(String.valueOf(1), new Gson().toJson(result));
                            }
                            reader.close();
                            responseBody.close();
                        } catch (Exception e) {
                            LOGGER.error("Model execution encountered an exception", e);
                        }
                    }
                } else {
//                    MessageVO messageVO = new MessageVO();
//                    messageVO.setId(UUID.randomUUID().toString());
//                    messageVO.setContent(output.getAsString(param.getName()));
//                    messageVO.setType(param.getFileExtension().getFileType().name().toLowerCase());
//                    messageVO.setToContactId(modelId);
//                    messageVO.setFromUser(createUser(modelId));
//                    Gson gson = new Gson();
//                    webSocketManager.sendMessage(userId, gson.toJson(messageVO));
                }
            }
        });
    }

}

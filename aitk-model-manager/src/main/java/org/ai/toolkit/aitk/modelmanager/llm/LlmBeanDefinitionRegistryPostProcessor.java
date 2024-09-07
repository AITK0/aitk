package org.ai.toolkit.aitk.modelmanager.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.common.git.GitEnum;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.ai.toolkit.aitk.common.modelscope.DownloadState;
import org.ai.toolkit.aitk.common.modelscope.FileDownloadUtil;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.llm.LlamaCppModelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LlmBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private GitEnum defaultGit;

    private static Logger LOGGER = LoggerFactory.getLogger(LlmBeanDefinitionRegistryPostProcessor.class);

    private static final String DEFINE_FILE_JSON = "llm/model.json";

    private static final String FORMAT = "llamaCppModelDefinition_llm/%s/%s";

    @Override
    public void setEnvironment(Environment environment) {
        this.defaultGit = GitEnum.getGitEnum(environment.getProperty("model.repository.type"));
        ConcurrentHashMap<String, DownloadState> resulMap = new ConcurrentHashMap<>();
        FileDownloadUtil.download(Arrays.asList(DEFINE_FILE_JSON), resulMap, defaultGit);
        if (!resulMap.containsKey(DEFINE_FILE_JSON)) {
            throw new AitkException(AitkErrorCode.DOWNLOAD_LLM_JSON_ERROR);
        }
        if (resulMap.get(DEFINE_FILE_JSON).getThrowable() != null) {
            throw new AitkException(AitkErrorCode.DOWNLOAD_LLM_JSON_ERROR, resulMap.get(DEFINE_FILE_JSON).getThrowable());
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        try {
            String json = GitUtil.getModelBasePath(defaultGit) + File.separator + DEFINE_FILE_JSON;
            Gson gson = new Gson();
            List<LlmModelDefine> llmModelDefines = gson.fromJson(new FileReader(json), new TypeToken<List<LlmModelDefine>>() {
            }.getType());
            if (!CollectionUtils.isEmpty(llmModelDefines)) {
                for (LlmModelDefine llmModelDefine : llmModelDefines) {
                    if (!Objects.isNull(llmModelDefine) && !CollectionUtils.isEmpty(llmModelDefine.getModels())) {
                        for (ModelDetail modelDetail : llmModelDefine.getModels()) {
                            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(LlamaCppModelDefinition.class);
                            beanDefinitionBuilder.addConstructorArgValue(llmModelDefine.getName())
                                    .addConstructorArgValue(modelDetail.getName())
                                    .addConstructorArgValue(modelDetail.getPath())
                                    .addConstructorArgValue(new ModelBasicInfo(llmModelDefine.getName(), llmModelDefine.getDescription()));
                            BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                            beanDefinitionRegistry.registerBeanDefinition(String.format(FORMAT, llmModelDefine.getName(), modelDetail.getName()), beanDefinition);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}

package org.ai.toolkit.aitk.modelzoo;

import org.ai.toolkit.aitk.common.git.GitEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelRepositoryType {
    @Value("${model.repository.type}")
    private String modelRepositoryType;

    public GitEnum getDefaultGitEnum() {
        return GitEnum.getGitEnum(modelRepositoryType);
    }
}

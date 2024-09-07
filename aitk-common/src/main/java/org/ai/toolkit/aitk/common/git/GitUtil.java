package org.ai.toolkit.aitk.common.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static org.ai.toolkit.aitk.common.constant.PathConstants.ATTACHMENT_PARENT_PATH;
import static org.ai.toolkit.aitk.common.constant.PathConstants.ATTACHMENT_PATH;

public class GitUtil {

    private static final String GITEE_URL = "https://gitee.com/AI_Toolkit/modelzoo.git";

    private static final String MODEL_SCOPE_URL = "https://www.modelscope.cn/AITK/modelzoo.git";

    private static final String GITEE_MODEL_PATH = ATTACHMENT_PARENT_PATH + File.separator + ATTACHMENT_PATH + File.separator + "models" + File.separator + "gitee";

    private static final String MODELSCOPE_MODEL_PATH = ATTACHMENT_PARENT_PATH + File.separator + ATTACHMENT_PATH + File.separator + "models" + File.separator + "modelscope";

    public static void gitClone(GitEnum gitEnum) throws GitAPIException {
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(getRepositoryUrl(gitEnum))
                    .setDirectory(new File(getModelBasePath(gitEnum)))
                    .setCloneAllBranches(true)
                    .call();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    private static String getRepositoryUrl(GitEnum gitEnum) {
        if (GitEnum.GITEE.equals(gitEnum)) {
            return GITEE_URL;
        } else if (GitEnum.GITHUB.equals(gitEnum)) {

        } else if (GitEnum.MODELSCOPE.equals(gitEnum)) {
            return MODEL_SCOPE_URL;
        }
        return null;
    }

    public static void gitPull(GitEnum gitEnum) throws IOException, GitAPIException {
        Git git = null;
        try {
            git = Git.open(new File(getModelBasePath(gitEnum)));
            git.pull().call();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    public static String getModelBasePath(GitEnum gitEnum) {
        if (GitEnum.GITEE.equals(gitEnum)) {
            return GITEE_MODEL_PATH;
        } else if (GitEnum.GITHUB.equals(gitEnum)) {

        } else if (GitEnum.MODELSCOPE.equals(gitEnum)) {
            return MODELSCOPE_MODEL_PATH;
        }
        return null;
    }
}

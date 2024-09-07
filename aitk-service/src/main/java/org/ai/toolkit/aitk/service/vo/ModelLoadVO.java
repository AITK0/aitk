package org.ai.toolkit.aitk.service.vo;

import org.ai.toolkit.aitk.common.modelscope.DownloadState;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ModelLoadVO implements Serializable {

    private ConcurrentHashMap<String, DownloadState> progressMap;

    /**
     * 0->pending 1->download 2->loading 10->success 11->none
     */
    private Integer state;

    // 下载文件数
    private Integer fileCount = 0;

    // 下载总进度
    private Integer progress;

    public ConcurrentHashMap<String, DownloadState> getProgressMap() {
        return progressMap;
    }

    public void setProgressMap(ConcurrentHashMap<String, DownloadState> progressMap) {
        this.progressMap = progressMap;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}

package org.ai.toolkit.aitk.common.modelscope;

import java.io.Serializable;

public class DownloadState implements Serializable {

    private static final long serialVersionUID = -5738361444050449256L;

    private Throwable throwable;

    private Integer progress;

    public DownloadState() {
    }

    public DownloadState(Integer progress) {
        this.progress = progress;
    }

    public DownloadState(Throwable throwable, Integer progress) {
        this.throwable = throwable;
        this.progress = progress;
    }

    public DownloadState(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}

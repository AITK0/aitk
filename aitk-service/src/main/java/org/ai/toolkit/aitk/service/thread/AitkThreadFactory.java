package org.ai.toolkit.aitk.service.thread;

import java.util.concurrent.atomic.AtomicInteger;

public class AitkThreadFactory implements java.util.concurrent.ThreadFactory {

    private AtomicInteger count = new AtomicInteger(0);

    private String threadType;

    public String getThreadType() {
        return threadType;
    }

    public void setThreadType(String threadType) {
        this.threadType = threadType;
    }

    public AitkThreadFactory(String threadType) {
        this.threadType = threadType;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        String threadName = threadType + count.addAndGet(1);
        t.setName(threadName);
        return t;
    }
}

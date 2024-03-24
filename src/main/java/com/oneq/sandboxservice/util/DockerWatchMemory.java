package com.oneq.sandboxservice.util;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;
import java.io.IOException;

public class DockerWatchMemory implements ResultCallback<Statistics> {
    private int maxMemory = 0;

    public int getMaxMemory() {
        return maxMemory;
    }

    @Override
    public void onNext(Statistics statistics) {
        if (statistics.getMemoryStats().getUsage() == null) {
            return;
        }
        long memoryUsageBytes = statistics.getMemoryStats().getUsage();
        int memoryUsageMB = (int) (memoryUsageBytes / 1024.0 ); // 从字节转换为kb字节
        maxMemory = Math.max(memoryUsageMB, maxMemory);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void onStart(Closeable closeable) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

}

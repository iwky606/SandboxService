package com.oneq.sandboxservice.util;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerExecCallback extends ExecStartResultCallback {
    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private boolean occurErr = false;
    private static final Logger log = LoggerFactory.getLogger(DockerExecCallback.class);

    @Override
    public void onNext(Frame frame) {
        String msg = new String(frame.getPayload());
        if (StreamType.STDERR.equals(frame.getStreamType())) {
            stderr.append(msg);
            log.info("错误输出：{}", msg);
            occurErr = true;
        } else {
            stdout.append(msg);
            log.info("标准输出：{}", msg);
        }
        super.onNext(frame);
    }

    public String getStdout() {
        return stdout.toString();
    }

    public String getStderr() {
        return stderr.toString();
    }

    public boolean haveErr() {
        return occurErr;
    }
}

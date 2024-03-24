package com.oneq.sandboxservice.util;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaRunCodeCallback extends ExecStartResultCallback {
    private final StringBuilder stdout = new StringBuilder();
    private long execTime = 0;// ms
    private final StringBuilder stderr = new StringBuilder();
    private boolean occurErr = false;
    private static final Logger log = LoggerFactory.getLogger(DockerExecCallback.class);

    @Override
    public void onNext(Frame frame) {
        String msg = new String(frame.getPayload());
        if (StreamType.STDERR.equals(frame.getStreamType())) {
            stderr.append(msg);
            log.info("JavaRunCodeError：{}", msg);
            occurErr = true;
        } else {
            log.info("stdout:{}", msg);
            if (msg.contains("execTime")) {
                execTime = Long.parseLong(msg.substring(10, msg.length() - 1));
            } else {
                stdout.append(msg);
            }
        }
        super.onNext(frame);
    }

    public String getStdout() {
        return stdout.toString();
    }

    public long getExecTime() {
        return execTime;
    }

    public String getStderr() {
        return stderr.toString();
    }


    public boolean haveErr() {
        return occurErr;
    }
}

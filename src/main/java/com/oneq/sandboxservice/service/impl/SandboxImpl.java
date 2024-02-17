package com.oneq.sandboxservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.TaskResult;
import com.oneq.sandboxservice.model.enums.Language;
import com.oneq.sandboxservice.service.Sandbox;
import org.springframework.stereotype.Component;

@Component
public class SandboxImpl implements Sandbox {
    @Override
    public JudgeResult doTask(JudgeTask judgeTask, DockerClient dockerClient) {
        TaskResult taskResult = null;
        if (judgeTask.getLang().getValue() == Language.JAVA8.getValue()) {
            taskResult = new JavaSandbox().doJudge(judgeTask, dockerClient);
        } else {
            JudgeResult judgeResult = new JudgeResult();
            judgeResult.setJudgeId(judgeTask.getJudgeId());
            return judgeResult;
        }

        // 结束容器
        dockerClient.stopContainerCmd(taskResult.getContainerId()).exec();
        return taskResult.getJudgeResult();
    }
}

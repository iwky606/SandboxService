package com.oneq.sandboxservice.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.TaskResult;
import com.oneq.sandboxservice.model.enums.Language;
import com.oneq.sandboxservice.service.Sandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
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
        log.info("id {} judgetask, outputs is {}", judgeTask.getJudgeId(), Arrays.deepToString(taskResult.getJudgeResult().getOutputs()));
        // 结束容器
        dockerClient.stopContainerCmd(taskResult.getContainerId()).exec();
        // TODO: 删除创建的文件夹
        // FileUtil.del(taskResult.getUserCodePath());
        return taskResult.getJudgeResult();
    }
}

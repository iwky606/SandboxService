package com.oneq.sandboxservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.service.ManageTask;
import com.oneq.sandboxservice.service.Sandbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ManageTaskImpl implements ManageTask {
    @Autowired
    Sandbox sandbox;

    @Autowired
    DockerClient dockerClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void submitTask(JudgeTask judgeTask) {
        if (judgeTask == null || judgeTask.getJudgeId() == null) {
            return;
        }

        JudgeResult judgeResult = sandbox.doTask(judgeTask, dockerClient);
        rabbitTemplate.convertAndSend("result.judge.queue", judgeResult);
    }
}

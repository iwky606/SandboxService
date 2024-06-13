package com.oneq.sandboxservice.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.service.ManageTask;
import com.oneq.sandboxservice.service.Sandbox;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ManageTaskImpl implements ManageTask {
    @Autowired
    Sandbox sandbox;

    @Autowired
    DockerClient dockerClient;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Value("${judge_system_id}")
    private String systemJudgeId;

    @Override
    public void submitTask(JudgeTask judgeTask) {
        if (judgeTask == null || judgeTask.getJudgeId() == null) {
            return;
        }

        JudgeResult judgeResult = sandbox.doTask(judgeTask, dockerClient);

        // 设置这个判题结果来自那个服务
        judgeResult.setJudgeSystemId(systemJudgeId);

        // 发送结果到结果队列
        rabbitTemplate.convertAndSend("result.judge.queue", judgeResult);
    }
}

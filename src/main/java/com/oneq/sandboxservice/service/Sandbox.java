package com.oneq.sandboxservice.service;

import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.JudgeResult;

public interface Sandbox {
    JudgeResult doTask(JudgeTask judgeTask, DockerClient dockerClient);
}
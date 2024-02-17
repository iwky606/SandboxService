package com.oneq.sandboxservice.service.impl;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.TaskResult;
import com.oneq.sandboxservice.model.enums.JudgeStatus;
import com.oneq.sandboxservice.service.SandboxTemplate;
import com.oneq.sandboxservice.util.DockerExecCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JavaSandbox extends SandboxTemplate {
    @Override
    public TaskResult doJudge(JudgeTask judgeTask, DockerClient dockerClient) {
        JudgeResult judgeResult = new JudgeResult();
        judgeResult.setJudgeId(judgeTask.getJudgeId());
        TaskResult taskResult = new TaskResult();

        String userCodePath = this.mkdirOfCode(judgeTask.getJudgeId().toString());
        this.saveFileToUserCodePath(judgeTask.getCode(), userCodePath, "Main.java");
        this.saveFileToUserCodePath("#!/bin/bash\n" + "java -cp /app Main < \"/app/$1.in\"", userCodePath, "run.sh");

        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(judgeTask.getLimit().getMemoryLimit() * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.setBinds(new Bind(userCodePath, new Volume("/app")));

        String java = "java:openjdk-8u111";
        CreateContainerResponse container = dockerClient.createContainerCmd(java).withHostConfig(hostConfig).withNetworkDisabled(true).withAttachStdin(true).withAttachStderr(true).withAttachStdout(true).withTty(true).exec();

        //
        String containerId = container.getId();

        // 添加容器ID返回值
        taskResult.setContainerId(containerId);

        dockerClient.startContainerCmd(containerId).exec();

        String[] command = {"chmod", "+x", "/app/run.sh"};
        ExecCreateCmdResponse chmodExecResponse = dockerClient.execCreateCmd(containerId).withAttachStdin(false).withAttachStdout(true).withAttachStderr(true).withUser("root")  // 你可能需要以root用户执行权限修改
                .withCmd(command).exec();

        // 执行 chmod 命令
        DockerExecCallback chmodResult = new DockerExecCallback();

        try {
            dockerClient.execStartCmd(chmodExecResponse.getId()).exec(chmodResult).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String[] cmdArray = {"javac", "/app/Main.java"};
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId).withAttachStdin(true).withAttachStdout(true).withAttachStderr(true).withCmd(cmdArray).exec();

        String execId = execCreateCmdResponse.getId();
        DockerExecCallback result = new DockerExecCallback();

        try {
            dockerClient.execStartCmd(execId).exec(result).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (result.haveErr()) {
            log.error(result.getStderr().toString());
            judgeResult.setJudgeStatus(JudgeStatus.COMPILE_ERROR);
            taskResult.setJudgeResult(judgeResult);
            return taskResult;
        }

        String[] outputs = new String[judgeTask.getInputs().length];
        for (int i = 0; i < judgeTask.getInputs().length; i++) {
            String input = judgeTask.getInputs()[i];
            FileUtil.writeString(input, userCodePath + File.separator + i + ".in", StandardCharsets.UTF_8);

            String runScriptCmd = "/app/run.sh " + i;
            ExecCreateCmdResponse runCodeExecRes = dockerClient.execCreateCmd(containerId).withAttachStdin(true).withAttachStdout(true).withAttachStderr(true).withCmd("bash", "-c", runScriptCmd).exec();

            String runCodeExecResId = runCodeExecRes.getId();
            DockerExecCallback runCodeResult = new DockerExecCallback();
            try {
                dockerClient.execStartCmd(runCodeExecResId).exec(runCodeResult).awaitCompletion();
                if (runCodeResult.haveErr()) {
                    log.error("runtime error");
                    judgeResult.setOutputs(outputs);
                    taskResult.setJudgeResult(judgeResult);
                    return taskResult;
                }
                log.info("input is {}", input);
                log.info(runCodeResult.getStdout());
                outputs[i] = runCodeResult.getStdout().toString();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        judgeResult.setOutputs(outputs);
        taskResult.setJudgeResult(judgeResult);
        return taskResult;
    }
}

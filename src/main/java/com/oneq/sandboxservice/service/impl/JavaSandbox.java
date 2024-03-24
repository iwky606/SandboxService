package com.oneq.sandboxservice.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.oneq.sandboxservice.model.dto.JudgeOutputs;
import com.oneq.sandboxservice.model.dto.JudgeResult;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.TaskResult;
import com.oneq.sandboxservice.model.enums.JudgeStatus;
import com.oneq.sandboxservice.service.SandboxTemplate;
import com.oneq.sandboxservice.util.DockerExecCallback;
import com.oneq.sandboxservice.util.DockerWatchMemory;
import com.oneq.sandboxservice.util.JavaRunCodeCallback;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;

@Slf4j
public class JavaSandbox extends SandboxTemplate {
    @Override
    public TaskResult doJudge(JudgeTask judgeTask, DockerClient dockerClient) {
        // 初始化返回值
        JudgeResult judgeResult = new JudgeResult();
        judgeResult.setJudgeId(judgeTask.getJudgeId());
        TaskResult taskResult = new TaskResult();

        // 创建运行代码路径
        String userCodePath = this.mkdirOfCode(UUID.fastUUID().toString());
        // taskResult添加路径
        taskResult.setUserCodePath(userCodePath);
        // 保存用户代码
        this.saveFileToUserCodePath(judgeTask.getCode(), userCodePath, "Main.java");

        // 创建run.sh运行脚本
        String runContent = String.format("#!/bin/bash\n" + "start_time=$(date +%%s%%N)\n" + "timeout %f java -Xmx%dm -cp /app Main < \"/app/$1.in\" || echo \"exit code is $?\" >&2\n" + "end_time=$(date +%%s%%N)\n" + "# 计算运行时间\n" + "execTime=$(( (end_time - start_time) / 1000000 ))\n" + "echo \"execTime: $execTime\"", 0.001 * judgeTask.getLimit().getTimeLimit(), judgeTask.getLimit().getMemoryLimit());
        this.saveFileToUserCodePath(runContent, userCodePath, "run.sh");
        // 给脚本赋予可执行权限
        String filePath = userCodePath + File.separator + "run.sh";
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
        Path path = Paths.get(filePath);

        // 应用文件权限
        try {
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 容器配置
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(judgeTask.getLimit().getMemoryLimit() * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(2L);
        hostConfig.setBinds(new Bind(userCodePath, new Volume("/app")));

        // 创建容器
        String java = "java:openjdk-8u111";
        CreateContainerResponse container = dockerClient.createContainerCmd(java).withHostConfig(hostConfig).withNetworkDisabled(true).withAttachStdin(true).withAttachStderr(true).withAttachStdout(true).withTty(true).exec();
        // 获取容器id
        String containerId = container.getId();
        // 添加容器ID返回值
        taskResult.setContainerId(containerId);

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 编译java代码
        String[] cmdArray = {"javac", "/app/Main.java"};
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId).withAttachStdin(true).withAttachStdout(true).withAttachStderr(true).withCmd(cmdArray).exec();
        String execId = execCreateCmdResponse.getId();
        DockerExecCallback result = new DockerExecCallback();
        try {
            dockerClient.execStartCmd(execId).exec(result).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 编译成功判断
        if (result.haveErr()) {
            log.error(result.getStderr().toString());
            judgeResult.setJudgeStatus(JudgeStatus.COMPILE_ERROR);
            taskResult.setJudgeResult(judgeResult);
            return taskResult;
        }


        // 执行代码
        JudgeOutputs[] outputs = new JudgeOutputs[judgeTask.getInputs().length];
        Arrays.setAll(outputs, e -> new JudgeOutputs());
        for (int i = 0; i < judgeTask.getInputs().length; i++) {
            String input = judgeTask.getInputs()[i];
            FileUtil.writeString(input, userCodePath + File.separator + i + ".in", StandardCharsets.UTF_8);

            String runScriptCmd = "/app/run.sh " + i;
            ExecCreateCmdResponse runCodeExecRes = dockerClient.execCreateCmd(containerId).withAttachStdin(true).withAttachStdout(true).withAttachStderr(true).withCmd("bash", "-c", runScriptCmd).exec();

            String runCodeExecResId = runCodeExecRes.getId();
            JavaRunCodeCallback runCodeResult = new JavaRunCodeCallback();
            try {
                // 开启内存监控
                StatsCmd statsCmd = dockerClient.statsCmd(containerId);
                DockerWatchMemory dockerWatchMemory = new DockerWatchMemory();
                statsCmd.exec(dockerWatchMemory);
                dockerClient.execStartCmd(runCodeExecResId).exec(runCodeResult).awaitCompletion();
                if (runCodeResult.haveErr()) {
                    log.error("runtime error");

                    JudgeStatus status = JudgeStatus.RUNTIME_ERROR; // 默认运行错误
                    String errMsg = runCodeResult.getStderr().toString();
                    if (errMsg.contains("OutOfMemoryError")) {// 内存超限java虚拟机会提示
                        status = JudgeStatus.MEMORY_LIMIT_EXCEED;
                    } else if (errMsg.contains("124")) { // timeout命令的退出代码是124
                        status = JudgeStatus.TIME_LIMIT_EXCEED;
                    }

                    judgeResult.setOutputs(outputs);
                    judgeResult.setJudgeStatus(status);
                    taskResult.setJudgeResult(judgeResult);
                    return taskResult;
                }

                log.info(runCodeResult.getStdout());
                outputs[i].setOutput(runCodeResult.getStdout().toString());
                outputs[i].setExecTime(runCodeResult.getExecTime());
                outputs[i].setExecMemory(dockerWatchMemory.getMaxMemory());
                // 关闭内存监控
                statsCmd.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 返回执行结果
        judgeResult.setJudgeStatus(JudgeStatus.NORMAL); // 表示程序都正常执行
        judgeResult.setOutputs(outputs);
        taskResult.setJudgeResult(judgeResult);
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return taskResult;
    }
}

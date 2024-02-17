package com.oneq.sandboxservice.service;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.TaskResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SandboxTemplate {
    private static final String EXEC_CODE_DIR = "execCode";

    public void saveFileToUserCodePath(String code, String userCodePath, String filename) {
        FileUtil.writeString(code, userCodePath + File.separator + filename, StandardCharsets.UTF_8);
    }

    public String mkdirOfCode(String id) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + EXEC_CODE_DIR;

        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        String userCodePath = globalCodePathName + File.separator + id;
        if (FileUtil.exist(userCodePath)) {
            FileUtil.del(userCodePath);
        }
        FileUtil.mkdir(userCodePath);
        return userCodePath;
    }

    public TaskResult doJudge(JudgeTask judgeTask,DockerClient dockerClient){
        return null;
    }


}

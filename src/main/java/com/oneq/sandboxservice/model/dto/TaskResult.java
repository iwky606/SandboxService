package com.oneq.sandboxservice.model.dto;

import lombok.Data;

@Data
public class TaskResult {
    private JudgeResult judgeResult;
    private String containerId;
    private String userCodePath;
}

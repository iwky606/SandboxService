package com.oneq.sandboxservice.model.dto;

import com.oneq.sandboxservice.model.enums.JudgeStatus;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JudgeResult {
    private Long judgeId;
    private String[] outputs;
    private JudgeInfo judgeInfo;
    private JudgeStatus judgeStatus;
}

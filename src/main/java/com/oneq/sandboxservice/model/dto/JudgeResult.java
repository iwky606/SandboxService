package com.oneq.sandboxservice.model.dto;

import com.oneq.sandboxservice.model.enums.JudgeStatus;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class JudgeResult implements Serializable {
    private Long judgeId;
    private JudgeOutputs[] outputs;
    private JudgeStatus judgeStatus;

    private static final long serialVersionUID = 1L;
}

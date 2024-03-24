package com.oneq.sandboxservice.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JudgeOutputs {
    private long execTime;//ms
    private int execMemory;//KB
    private String output;
}

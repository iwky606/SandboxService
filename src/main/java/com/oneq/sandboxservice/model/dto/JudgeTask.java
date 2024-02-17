package com.oneq.sandboxservice.model.dto;

import com.oneq.sandboxservice.model.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JudgeTask {
    private Long judgeId;
    private String[] inputs;
    private String code;
    private Language lang;
    private ResourceLimit limit;
    private String[] args;// 特殊判题的参数
}

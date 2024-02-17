package com.oneq.sandboxservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceLimit {
    private long timeLimit;
    private int memoryLimit;
}

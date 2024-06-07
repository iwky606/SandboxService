package com.oneq.sandboxservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceLimit implements Serializable {
    private long timeLimit;// 毫秒
    private int memoryLimit;// kb
    private static final long serialVersionUID = 1L;

}

package com.oneq.sandboxservice.model.enums;

public enum JudgeStatus {
    NORMAL(0),
    ACCEPT(1),
    WRONG_ANSWER(2),
    COMPILE_ERROR(3),
    TIME_LIMIT_EXCEED(4),
    MEMORY_LIMIT_EXCEED(5),
    RUNTIME_ERROR(6),
    OTHER_ERROR(7);

    private final int value;

    JudgeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static JudgeStatus getByValue(int value) {
        for (JudgeStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("No JudgeStatus found for value: " + value);
    }
}

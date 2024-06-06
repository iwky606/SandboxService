package com.oneq.sandboxservice.model.enums;

public enum Language {
    JAVA8(0), JAVA17(1), CPP20(2), PYTHON3(3);

    private final int value;

    Language(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Language getByValue(int value) {
        for (Language language : values()) {
            if (language.getValue() == value) {
                return language;
            }
        }
        throw new IllegalArgumentException("No language found for value: " + value);
    }
}

package com.oneq.sandboxservice.model.enums;

public enum Language {
    JAVA8(1),
    JAVA17(2),
    CPP20(3),
    PYTHON3(4);

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

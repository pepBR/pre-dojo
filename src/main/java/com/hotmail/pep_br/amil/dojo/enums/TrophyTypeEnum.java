package com.hotmail.pep_br.amil.dojo.enums;

public enum TrophyTypeEnum {
    IMMORTAL ("Immortal", "Zero deaths in a match"),
    FAST_KILLER ("Fast killer", "Killed 5 enemies in 1 minute without dying"),
    THE_WALKING_DEAD ("The walking dead", "Died 5 times in a row (no kills in between)");

    private String name;
    private String description;

    TrophyTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

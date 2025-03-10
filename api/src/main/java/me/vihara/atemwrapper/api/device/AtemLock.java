package me.vihara.atemwrapper.api.device;

import lombok.Getter;

@Getter
public enum AtemLock {
    LOCKED("O"),
    LOCKED_OTHER("L"),
    UNLOCKED("U");

    private final String id;
    AtemLock(String id) {
        this.id = id;
    }
}

package ru.qWins.service;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class FreezeSession {

    private final UUID staffId;
    private final UUID targetId;
    private final Location originalLocation;
    @Setter
    @Getter
    private int titleTaskId = -1;
    @Setter
    @Getter
    private int messageTaskId = -1;
    @Setter
    @Getter
    private int staffTitleTaskId = -1;

    public FreezeSession(UUID staffId, UUID targetId, Location originalLocation) {
        this.staffId = staffId;
        this.targetId = targetId;
        this.originalLocation = originalLocation;
    }

    public UUID staffId() {
        return staffId;
    }

    public UUID targetId() {
        return targetId;
    }

    public Location originalLocation() {
        return originalLocation;
    }
}

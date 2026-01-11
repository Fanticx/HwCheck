package ru.qWins.service;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
public class FreezeSession {

    private final UUID staffId;
    private final UUID targetId;
    private final Location originalLocation;
    @Setter
    private int titleTaskId = -1;
    @Setter
    private int messageTaskId = -1;
    @Setter
    private int staffTitleTaskId = -1;

    public FreezeSession(UUID staffId, UUID targetId, Location originalLocation) {
        this.staffId = staffId;
        this.targetId = targetId;
        this.originalLocation = originalLocation;
    }

}

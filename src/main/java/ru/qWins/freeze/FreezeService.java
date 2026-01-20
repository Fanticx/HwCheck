package ru.qWins.freeze;

import java.util.UUID;
import org.bukkit.Location;

public class FreezeService {

    private final UUID staffId;
    private final UUID targetId;
    private final Location originalLocation;

    private int titleTaskId = -1;
    private int messageTaskId = -1;
    private int staffTitleTaskId = -1;

    public FreezeService(UUID staffId, UUID targetId, Location originalLocation) {
        this.staffId = staffId;
        this.targetId = targetId;
        this.originalLocation = originalLocation;
    }

    public UUID getStaffId() {
        return staffId;
    }
    public UUID getTargetId() {
        return targetId;
    }
    public Location getOriginalLocation() {
        return originalLocation;
    }

    public int getTitleTaskId() {
        return titleTaskId;
    }
    public int getMessageTaskId() {
        return messageTaskId;
    }
    public int getStaffTitleTaskId() {
        return staffTitleTaskId;
    }

    public void setTitleTaskId(int taskId) {
        this.titleTaskId = taskId;
    }
    public void setMessageTaskId(int taskId) {
        this.messageTaskId = taskId;
    }
    public void setStaffTitleTaskId(int taskId) {
        this.staffTitleTaskId = taskId;
    }
}

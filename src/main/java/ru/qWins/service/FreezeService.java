package ru.qWins.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.qWins.Config;
import ru.qWins.util.MessageFormatter;
import ru.qWins.util.PlaceholderUtil;
import ru.qWins.util.TextUtil;

public class FreezeService {

    private final Plugin plugin;
    private final Config config;
    private final MessageFormatter messageFormatter;
    private final Map<UUID, FreezeSession> targetSessions = new HashMap<>();
    private final Map<UUID, UUID> staffToTarget = new HashMap<>();
    private final Set<UUID> teleporting = new HashSet<>();

    public FreezeService(Plugin plugin, Config config, MessageFormatter messageFormatter) {
        this.plugin = plugin;
        this.config = config;
        this.messageFormatter = messageFormatter;
    }

    public FreezeResult freeze(Player staff, Player target) {
        if (staff.getUniqueId().equals(target.getUniqueId())) {
            return FreezeResult.SELF_FREEZE;
        }
        if (targetSessions.containsKey(target.getUniqueId())) {
            return FreezeResult.ALREADY_FROZEN;
        }
        if (staffToTarget.containsKey(staff.getUniqueId())) {
            return FreezeResult.STAFF_ALREADY_CHECKING;
        }
        FreezeSession session = new FreezeSession(staff.getUniqueId(), target.getUniqueId(), target.getLocation().clone());
        targetSessions.put(target.getUniqueId(), session);
        staffToTarget.put(staff.getUniqueId(), target.getUniqueId());
        teleportToFreezeLocation(target);
        sendTargetStart(target, staff);
        sendStaffStart(staff, target);
        startSessionTasks(session, target, staff);
        return FreezeResult.SUCCESS;
    }

    public UnfreezeResult unfreeze(Player staff, Player target) {
        FreezeSession session = targetSessions.get(target.getUniqueId());
        if (session == null) {
            return UnfreezeResult.NOT_FROZEN;
        }
        if (!session.staffId().equals(staff.getUniqueId())) {
            return UnfreezeResult.NOT_YOUR_TARGET;
        }
        removeSession(session);
        startUnfreezeCountdown(target, session.originalLocation());
        sendStaffEnd(staff, target);
        return UnfreezeResult.SUCCESS;
    }

    public void handleQuit(Player player) {
        UUID playerId = player.getUniqueId();
        FreezeSession targetSession = targetSessions.get(playerId);
        if (targetSession != null) {
            Player staff = Bukkit.getPlayer(targetSession.staffId());
            removeSession(targetSession);
            if (staff != null) {
                staff.sendMessage(messageFormatter.format(
                    config.getMessages().getSystem().getModeratorTargetLeft(),
                    staff,
                    player
                ));
            }
            return;
        }
        UUID targetId = staffToTarget.get(playerId);
        if (targetId != null) {
            FreezeSession session = targetSessions.get(targetId);
            if (session != null) {
                removeSession(session);
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    target.sendMessage(messageFormatter.format(
                        config.getMessages().getSystem().getTargetModeratorLeft(),
                        player,
                        target
                    ));
                }
            }
        }
    }

    public boolean isFrozen(Player player) {
        return targetSessions.containsKey(player.getUniqueId());
    }

    public boolean isStaffChecking(Player player) {
        return staffToTarget.containsKey(player.getUniqueId());
    }

    public boolean isTeleporting(Player player) {
        return teleporting.contains(player.getUniqueId());
    }

    public Player getStaffForTarget(Player target) {
        FreezeSession session = targetSessions.get(target.getUniqueId());
        if (session == null) {
            return null;
        }
        return Bukkit.getPlayer(session.staffId());
    }

    public Player getTargetForStaff(Player staff) {
        UUID targetId = staffToTarget.get(staff.getUniqueId());
        if (targetId == null) {
            return null;
        }
        return Bukkit.getPlayer(targetId);
    }

    public Set<String> getAllowedCommands() {
        Set<String> allowed = new HashSet<>();
        for (String command : config.getCommands().getAllowed()) {
            if (command == null || command.isBlank()) {
                continue;
            }
            String normalized = command.trim().toLowerCase();
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (normalized.contains(":")) {
                normalized = normalized.substring(normalized.indexOf(':') + 1);
            }
            allowed.add(normalized);
        }
        return Collections.unmodifiableSet(allowed);
    }

    private void removeSession(FreezeSession session) {
        targetSessions.remove(session.targetId());
        staffToTarget.remove(session.staffId());
        cancelSessionTasks(session);
    }

    private void teleportToFreezeLocation(Player target) {
        String worldName = config.getFreeze().getWorld();
        World world = worldName == null || worldName.isBlank()
            ? target.getWorld()
            : Bukkit.getWorld(worldName);
        if (world == null) {
            world = target.getWorld();
        }
        Location targetLocation = new Location(
            world,
            config.getFreeze().getX(),
            config.getFreeze().getY(),
            config.getFreeze().getZ(),
            config.getFreeze().getYaw(),
            config.getFreeze().getPitch()
        );
        teleporting.add(target.getUniqueId());
        target.teleport(targetLocation);
        Bukkit.getScheduler().runTask(plugin, () -> teleporting.remove(target.getUniqueId()));
    }

    private void sendTargetStart(Player target, Player staff) {
        for (String line : config.getMessages().getTarget().getLines()) {
            if (line == null || line.isBlank()) {
                target.sendMessage("");
                continue;
            }
            target.sendMessage(messageFormatter.format(line, staff, target));
        }
        sendTitle(target, staff);
    }

    private void sendTargetEnd(Player target) {
    }

    private void sendStaffStart(Player staff, Player target) {
        // no start message for staff per config
    }

    private void sendStaffEnd(Player staff, Player target) {
        staff.sendMessage(messageFormatter.format(
            config.getMessages().getModerator().getEnd(),
            staff,
            target
        ));
    }

    private void startSessionTasks(FreezeSession session, Player target, Player staff) {
        if (config.getTitles().getTarget().getRepeatTicks() > 0) {
            int titleTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!isFrozen(target)) {
                    cancelSessionTasks(session);
                    return;
                }
                Player currentStaff = getStaffForTarget(target);
                sendTitle(target, currentStaff != null ? currentStaff : staff);
            }, 0L, config.getTitles().getTarget().getRepeatTicks()).getTaskId();
            session.setTitleTaskId(titleTaskId);
        }

        if (staff != null && config.getTitles().getStaff().getRepeatTicks() > 0) {
            int staffTitleTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!isStaffChecking(staff)) {
                    cancelSessionTasks(session);
                    return;
                }
                sendStaffTitle(staff);
            }, 0L, config.getTitles().getStaff().getRepeatTicks()).getTaskId();
            session.setStaffTitleTaskId(staffTitleTaskId);
        }

        if (config.getMessages().getTarget().getRepeatSeconds() > 0) {
            int messageRepeat = config.getMessages().getTarget().getRepeatSeconds();
            int messageTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!isFrozen(target)) {
                    cancelSessionTasks(session);
                    return;
                }
                Player currentStaff = getStaffForTarget(target);
                sendTargetReminder(target, currentStaff != null ? currentStaff : staff);
            }, messageRepeat * 20L, messageRepeat * 20L).getTaskId();
            session.setMessageTaskId(messageTaskId);
        }
    }

    private void cancelSessionTasks(FreezeSession session) {
        if (session.getTitleTaskId() > 0) {
            Bukkit.getScheduler().cancelTask(session.getTitleTaskId());
            session.setTitleTaskId(-1);
        }
        if (session.getMessageTaskId() > 0) {
            Bukkit.getScheduler().cancelTask(session.getMessageTaskId());
            session.setMessageTaskId(-1);
        }
        if (session.getStaffTitleTaskId() > 0) {
            Bukkit.getScheduler().cancelTask(session.getStaffTitleTaskId());
            session.setStaffTitleTaskId(-1);
        }
    }

    private void sendTitle(Player target, Player staff) {
        String title = TextUtil.colorize(
            PlaceholderUtil.apply(config.getTitles().getTarget().getTitle(), staff, target)
        );
        String subtitle = TextUtil.colorize(
            PlaceholderUtil.apply(config.getTitles().getTarget().getSubtitle(), staff, target)
        );
        target.sendTitle(
            title,
            subtitle,
            config.getTitles().getFadeIn(),
            config.getTitles().getTarget().getStay(),
            config.getTitles().getFadeOut()
        );
    }

    private void sendTargetReminder(Player target, Player staff) {
        for (String line : config.getMessages().getTarget().getLines()) {
            if (line == null || line.isBlank()) {
                target.sendMessage("");
                continue;
            }
            target.sendMessage(messageFormatter.format(line, staff, target));
        }
    }

    private void sendStaffTitle(Player staff) {
        String title = TextUtil.colorize(config.getTitles().getStaff().getTitle());
        String subtitle = TextUtil.colorize(config.getTitles().getStaff().getSubtitle());
        staff.sendTitle(
            title,
            subtitle,
            config.getTitles().getFadeIn(),
            config.getTitles().getStaff().getStay(),
            config.getTitles().getFadeOut()
        );
    }

    private void startUnfreezeCountdown(Player target, Location originalLocation) {
        if (target == null || !target.isOnline()) {
            return;
        }
        int totalSeconds = Math.max(1, config.getUnfreeze().getTeleportSeconds());
        Location freezeLocation = getFreezeLocation(target);
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            private int secondsLeft = totalSeconds;

            @Override
            public void run() {
                if (!target.isOnline()) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                if (freezeLocation != null && hasLeftFreezeArea(
                    target,
                    freezeLocation,
                    config.getUnfreeze().getCancelRadius()
                )) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                String subtitle = config.getUnfreeze().getSubtitle()
                    .replace("{seconds}", String.valueOf(secondsLeft));
                target.sendTitle(
                    TextUtil.colorize(config.getUnfreeze().getTitle()),
                    TextUtil.colorize(subtitle),
                    config.getTitles().getFadeIn(),
                    config.getTitles().getTarget().getStay(),
                    config.getTitles().getFadeOut()
                );
                if (secondsLeft <= 0) {
                    target.teleport(originalLocation);
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                secondsLeft--;
            }
        }, 0L, 20L);
    }

    private Location getFreezeLocation(Player target) {
        String worldName = config.getFreeze().getWorld();
        World world = worldName == null || worldName.isBlank()
            ? target.getWorld()
            : Bukkit.getWorld(worldName);
        if (world == null) {
            world = target.getWorld();
        }
        return new Location(
            world,
            config.getFreeze().getX(),
            config.getFreeze().getY(),
            config.getFreeze().getZ()
        );
    }

    private boolean hasLeftFreezeArea(Player player, Location freezeLocation, double radius) {
        if (radius <= 0) {
            return false;
        }
        if (!player.getWorld().equals(freezeLocation.getWorld())) {
            return true;
        }
        return player.getLocation().distanceSquared(freezeLocation) > radius * radius;
    }
}

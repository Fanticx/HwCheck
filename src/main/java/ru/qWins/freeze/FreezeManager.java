package ru.qWins.freeze;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.qWins.Config;
import ru.qWins.freeze.type.FreezeResult;
import ru.qWins.freeze.type.UnfreezeResult;
import ru.qWins.util.MessageFormatter;
import ru.qWins.util.ColorUtil;

public class FreezeManager {

    private final Plugin plugin;
    private final MessageFormatter messageFormatter;
    private final Config.Freeze freezeConfig;
    private final Config.Titles titles;
    private final Config.Titles.Target targetTitles;
    private final Config.Titles.Staff staffTitles;
    private final Config.Messages.Target targetMessages;
    private final Config.Messages.Moderator moderatorMessages;
    private final Config.Messages.System systemMessages;
    private final Config.Unfreeze unfreezeConfig;
    private final Config.Commands commands;
    private final Map<UUID, FreezeService> sessionsByTarget = new HashMap<>();
    private final Map<UUID, UUID> targetByStaff = new HashMap<>();
    private final Set<UUID> teleportingTargets = new HashSet<>();

    public FreezeManager(Plugin plugin, Config config, MessageFormatter messageFormatter) {
        this.plugin = plugin;
        this.messageFormatter = messageFormatter;
        this.freezeConfig = config.getFreeze();
        this.titles = config.getTitles();
        this.targetTitles = titles.getTarget();
        this.staffTitles = titles.getStaff();
        Config.Messages messages = config.getMessages();
        this.targetMessages = messages.getTarget();
        this.moderatorMessages = messages.getModerator();
        this.systemMessages = messages.getSystem();
        this.unfreezeConfig = config.getUnfreeze();
        this.commands = config.getCommands();
    }

    public FreezeResult freeze(Player staff, Player target) {
        FreezeResult validation = validateFreezeRequest(staff, target);
        if (validation != FreezeResult.SUCCESS) {
            return validation;
        }
        FreezeService session = createSession(staff, target);
        teleportToFreezeLocation(target);
        sendTargetStart(target, staff);
        startSessionTasks(session, target, staff);
        return FreezeResult.SUCCESS;
    }

    public UnfreezeResult unfreeze(Player staff, Player target) {
        FreezeService session = sessionsByTarget.get(target.getUniqueId());
        if (session == null) {
            return UnfreezeResult.NOT_FROZEN;
        }
        if (!session.getStaffId().equals(staff.getUniqueId())) {
            return UnfreezeResult.NOT_YOUR_TARGET;
        }
        removeSession(session);
        startUnfreezeCountdown(target, session.getOriginalLocation());
        sendStaffEnd(staff, target);
        return UnfreezeResult.SUCCESS;
    }

    public void handleQuit(Player player) {
        UUID playerId = player.getUniqueId();
        FreezeService targetSession = sessionsByTarget.get(playerId);
        if (targetSession != null) {
            handleTargetQuit(targetSession, player);
            return;
        }
        UUID targetId = targetByStaff.get(playerId);
        if (targetId != null) {
            handleStaffQuit(player, targetId);
        }
    }

    public boolean isFrozen(Player player) {
        return sessionsByTarget.containsKey(player.getUniqueId());
    }

    public boolean isStaffChecking(Player player) {
        return targetByStaff.containsKey(player.getUniqueId());
    }

    public boolean isTeleporting(Player player) {
        return teleportingTargets.contains(player.getUniqueId());
    }

    public Player getStaffForTarget(Player target) {
        FreezeService session = sessionsByTarget.get(target.getUniqueId());
        if (session == null) {
            return null;
        }
        return Bukkit.getPlayer(session.getStaffId());
    }

    public Player getTargetForStaff(Player staff) {
        UUID targetId = targetByStaff.get(staff.getUniqueId());
        if (targetId == null) {
            return null;
        }
        return Bukkit.getPlayer(targetId);
    }

    public boolean isAllowedCommand(String rawCommand) {
        String normalized = normalizeCommand(rawCommand);
        if (normalized == null) {
            return true;
        }
        return getAllowedCommands().contains(normalized);
    }

    public Set<String> getAllowedCommands() {
        Set<String> allowed = new HashSet<>();
        for (String command : commands.getAllowed()) {
            String normalized = normalizeCommand(command);
            if (normalized != null) {
                allowed.add(normalized);
            }
        }
        return Collections.unmodifiableSet(allowed);
    }

    private FreezeResult validateFreezeRequest(Player staff, Player target) {
        if (staff.getUniqueId().equals(target.getUniqueId())) {
            return FreezeResult.SELF_FREEZE;
        }
        if (sessionsByTarget.containsKey(target.getUniqueId())) {
            return FreezeResult.ALREADY_FROZEN;
        }
        if (targetByStaff.containsKey(staff.getUniqueId())) {
            return FreezeResult.STAFF_ALREADY_CHECKING;
        }
        return FreezeResult.SUCCESS;
    }

    private FreezeService createSession(Player staff, Player target) {
        FreezeService session = new FreezeService(
                staff.getUniqueId(),
                target.getUniqueId(),
                target.getLocation().clone()
        );
        sessionsByTarget.put(session.getTargetId(), session);
        targetByStaff.put(session.getStaffId(), session.getTargetId());
        return session;
    }

    private void removeSession(FreezeService session) {
        sessionsByTarget.remove(session.getTargetId());
        targetByStaff.remove(session.getStaffId());
        cancelSessionTasks(session);
    }

    private void handleTargetQuit(FreezeService session, Player target) {
        removeSession(session);
        Player staff = Bukkit.getPlayer(session.getStaffId());
        if (staff != null) {
            staff.sendMessage(messageFormatter.format(
                    systemMessages.getModeratorTargetLeft(),
                    staff,
                    target
            ));
        }
    }

    private void handleStaffQuit(Player staff, UUID targetId) {
        FreezeService session = sessionsByTarget.get(targetId);
        if (session == null) {
            return;
        }
        removeSession(session);
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage(messageFormatter.format(
                    systemMessages.getTargetModeratorLeft(),
                    staff,
                    target
            ));
        }
    }

    private String normalizeCommand(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String token = trimmed.split("\\s+")[0];
        String normalized = token.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        int namespaceIndex = normalized.indexOf(':');
        if (namespaceIndex >= 0) {
            normalized = normalized.substring(namespaceIndex + 1);
        }
        return normalized.isBlank() ? null : normalized;
    }

    private void teleportToFreezeLocation(Player target) {
        Location targetLocation = buildFreezeLocation(target, true);
        teleportingTargets.add(target.getUniqueId());
        target.teleport(targetLocation);
        Bukkit.getScheduler().runTask(plugin, () -> teleportingTargets.remove(target.getUniqueId()));
    }

    private void sendTargetStart(Player target, Player staff) {
        sendTargetLines(target, staff);
        sendTargetTitle(target, staff);
    }

    private void sendStaffEnd(Player staff, Player target) {
        staff.sendMessage(messageFormatter.format(
                moderatorMessages.getEnd(),
                staff,
                target
        ));
    }

    private void startSessionTasks(FreezeService session, Player target, Player staff) {
        session.setTitleTaskId(scheduleTargetTitleTask(session, target, staff));
        session.setStaffTitleTaskId(scheduleStaffTitleTask(session, staff));
        session.setMessageTaskId(scheduleTargetMessageTask(session, target, staff));
    }

    private int scheduleTargetTitleTask(FreezeService session, Player target, Player staff) {
        int repeatTicks = targetTitles.getRepeatTicks();
        if (repeatTicks <= 0) {
            return -1;
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isFrozen(target)) {
                cancelSessionTasks(session);
                return;
            }
            Player currentStaff = getStaffForTarget(target);
            sendTargetTitle(target, currentStaff != null ? currentStaff : staff);
        }, 0L, repeatTicks).getTaskId();
    }

    private int scheduleStaffTitleTask(FreezeService session, Player staff) {
        if (staff == null) {
            return -1;
        }
        int repeatTicks = staffTitles.getRepeatTicks();
        if (repeatTicks <= 0) {
            return -1;
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isStaffChecking(staff)) {
                cancelSessionTasks(session);
                return;
            }
            sendStaffTitle(staff);
        }, 0L, repeatTicks).getTaskId();
    }

    private int scheduleTargetMessageTask(FreezeService session, Player target, Player staff) {
        int repeatSeconds = targetMessages.getRepeatSeconds();
        if (repeatSeconds <= 0) {
            return -1;
        }
        long repeatTicks = repeatSeconds * 20L;
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isFrozen(target)) {
                cancelSessionTasks(session);
                return;
            }
            Player currentStaff = getStaffForTarget(target);
            sendTargetReminder(target, currentStaff != null ? currentStaff : staff);
        }, repeatTicks, repeatTicks).getTaskId();
    }

    private void cancelSessionTasks(FreezeService session) {
        cancelTask(session.getTitleTaskId(), session::setTitleTaskId);
        cancelTask(session.getMessageTaskId(), session::setMessageTaskId);
        cancelTask(session.getStaffTitleTaskId(), session::setStaffTitleTaskId);
    }

    private void cancelTask(int taskId, java.util.function.IntConsumer reset) {
        if (taskId <= 0) {
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
        reset.accept(-1);
    }

    private void sendTargetTitle(Player target, Player staff) {
        String title = messageFormatter.format(targetTitles.getTitle(), staff, target);
        String subtitle = messageFormatter.format(targetTitles.getSubtitle(), staff, target);
        target.sendTitle(
                title,
                subtitle,
                titles.getFadeIn(),
                targetTitles.getStay(),
                titles.getFadeOut()
        );
    }

    private void sendTargetLines(Player target, Player staff) {
        for (String line : targetMessages.getLines()) {
            if (line == null || line.isBlank()) {
                target.sendMessage("");
                continue;
            }
            target.sendMessage(messageFormatter.format(line, staff, target));
        }
    }

    private void sendTargetReminder(Player target, Player staff) {
        sendTargetLines(target, staff);
    }

    private void sendStaffTitle(Player staff) {
        String title = ColorUtil.use(staffTitles.getTitle());
        String subtitle = ColorUtil.use(staffTitles.getSubtitle());
        staff.sendTitle(
                title,
                subtitle,
                titles.getFadeIn(),
                staffTitles.getStay(),
                titles.getFadeOut()
        );
    }

    private void startUnfreezeCountdown(Player target, Location originalLocation) {
        if (target == null || !target.isOnline()) {
            return;
        }
        int totalSeconds = Math.max(1, unfreezeConfig.getTeleportSeconds());
        Location freezeLocation = buildFreezeLocation(target, false);
        new BukkitRunnable() {
            private int secondsLeft = totalSeconds;

            @Override
            public void run() {
                if (!target.isOnline()) {
                    cancel();
                    return;
                }
                if (hasLeftFreezeArea(target, freezeLocation, unfreezeConfig.getCancelRadius())) {
                    cancel();
                    return;
                }
                sendUnfreezeTitle(target, secondsLeft);
                if (secondsLeft <= 0) {
                    target.teleport(originalLocation);
                    cancel();
                    return;
                }
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendUnfreezeTitle(Player target, int secondsLeft) {
        String subtitle = unfreezeConfig.getSubtitle()
                .replace("{seconds}", String.valueOf(secondsLeft));
        target.sendTitle(
                ColorUtil.use(unfreezeConfig.getTitle()),
                ColorUtil.use(subtitle),
                titles.getFadeIn(),
                targetTitles.getStay(),
                titles.getFadeOut()
        );
    }

    private Location buildFreezeLocation(Player target, boolean includeOrientation) {
        World world = resolveFreezeWorld(target);
        Location location = new Location(world, freezeConfig.getX(), freezeConfig.getY(), freezeConfig.getZ());
        if (includeOrientation) {
            location.setYaw(freezeConfig.getYaw());
            location.setPitch(freezeConfig.getPitch());
        }
        return location;
    }

    private World resolveFreezeWorld(Player target) {
        String worldName = freezeConfig.getWorld();
        World world = worldName == null || worldName.isBlank()
                ? target.getWorld()
                : Bukkit.getWorld(worldName);
        return world != null ? world : target.getWorld();
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

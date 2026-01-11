package ru.qWins.listener;

import java.util.Locale;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import ru.qWins.Config;
import ru.qWins.service.FreezeService;
import ru.qWins.util.MessageFormatter;

public class FreezeListener implements Listener {

    private final FreezeService freezeService;
    private final Config config;
    private final MessageFormatter messageFormatter;

    public FreezeListener(FreezeService freezeService, Config config, MessageFormatter messageFormatter) {
        this.freezeService = freezeService;
        this.config = config;
        this.messageFormatter = messageFormatter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!freezeService.isFrozen(player)) {
            return;
        }
        String raw = event.getMessage();
        if (raw.isBlank()) {
            return;
        }
        String[] parts = raw.trim().split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.contains(":")) {
            command = command.substring(command.indexOf(':') + 1);
        }
        Set<String> allowed = freezeService.getAllowedCommands();
        if (allowed.contains(command)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(messageFormatter.format(config.getMessages().getErrors().getCommandBlocked()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!freezeService.isFrozen(player)) {
            return;
        }
        if (freezeService.isTeleporting(player)) {
            return;
        }
        if (event.getFrom().getX() == event.getTo().getX()
            && event.getFrom().getY() == event.getTo().getY()
            && event.getFrom().getZ() == event.getTo().getZ()
            && event.getFrom().getYaw() == event.getTo().getYaw()
            && event.getFrom().getPitch() == event.getTo().getPitch()) {
            return;
        }
        event.setCancelled(true);
        event.setTo(event.getFrom());
        player.setVelocity(new Vector(0, 0, 0));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && freezeService.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player target && freezeService.isFrozen(target)) {
            event.setCancelled(true);
            return;
        }
        if (event.getDamager() instanceof Player damager && freezeService.isFrozen(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!freezeService.isFrozen(player)) {
            return;
        }
        if (freezeService.isTeleporting(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        freezeService.handleQuit(event.getPlayer());
    }
}

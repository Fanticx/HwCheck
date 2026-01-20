package ru.qWins.listener;

import org.bukkit.entity.Entity;
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
import org.bukkit.Location;
import org.bukkit.util.Vector;
import ru.qWins.Config;
import ru.qWins.freeze.FreezeManager;
import ru.qWins.util.MessageFormatter;

public class FreezeListener implements Listener {

    private final FreezeManager freezeService;
    private final MessageFormatter messageFormatter;
    private final Config.Messages.Errors errorMessages;

    public FreezeListener(FreezeManager freezeService, Config config, MessageFormatter messageFormatter) {
        this.freezeService = freezeService;
        this.messageFormatter = messageFormatter;
        this.errorMessages = config.getMessages().getErrors();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!freezeService.isFrozen(player)) {
            return;
        }
        if (freezeService.isAllowedCommand(event.getMessage())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(messageFormatter.format(errorMessages.getCommandBlocked()));
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
        if (!hasMoved(event)) {
            return;
        }
        event.setCancelled(true);
        event.setTo(event.getFrom());
        player.setVelocity(new Vector(0, 0, 0));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (isFrozenPlayer(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (isFrozenPlayer(event.getEntity())) {
            event.setCancelled(true);
            return;
        }
        if (isFrozenPlayer(event.getDamager())) {
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

    private boolean hasMoved(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        return from.getX() != to.getX()
                || from.getY() != to.getY()
                || from.getZ() != to.getZ()
                || from.getYaw() != to.getYaw()
                || from.getPitch() != to.getPitch();
    }

    private boolean isFrozenPlayer(Entity entity) {
        return entity instanceof Player player && freezeService.isFrozen(player);
    }
}

package ru.qWins.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.qWins.Config;
import ru.qWins.service.FreezeService;
import ru.qWins.util.TextUtil;

public class HwCheckPlaceholderExpansion extends PlaceholderExpansion {

    private final Plugin plugin;
    private final FreezeService freezeService;
    private final Config config;

    public HwCheckPlaceholderExpansion(Plugin plugin, FreezeService freezeService, Config config) {
        this.plugin = plugin;
        this.freezeService = freezeService;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hwcheck";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        String key = params.toLowerCase();
        switch (key) {
            case "status" -> {
                boolean active = freezeService.isFrozen(player) || freezeService.isStaffChecking(player);
                return TextUtil.colorize(active
                    ? config.getPlaceholders().getStatus().getOnCheck()
                    : config.getPlaceholders().getStatus().getOffCheck());
            }
            case "staff" -> {
                Player staff = freezeService.getStaffForTarget(player);
                return staff == null ? "" : staff.getName();
            }
            case "target" -> {
                Player target = freezeService.getTargetForStaff(player);
                return target == null ? "" : target.getName();
            }
            default -> {
                return "";
            }
        }
    }
}

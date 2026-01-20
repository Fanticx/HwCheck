package ru.qWins.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import java.util.Locale;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.qWins.Config;
import ru.qWins.freeze.FreezeManager;
import ru.qWins.util.ColorUtil;

public class HwCheckPlaceholderExpansion extends PlaceholderExpansion {

    private final Plugin plugin;
    private final FreezeManager freezeService;
    private final Config.Placeholders.Status statusPlaceholders;

    public HwCheckPlaceholderExpansion(Plugin plugin, FreezeManager freezeService, Config config) {
        this.plugin = plugin;
        this.freezeService = freezeService;
        this.statusPlaceholders = config.getPlaceholders().getStatus();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hwcheck";
    }

    @Override
    public @NotNull String getAuthor() {
        return "qWins";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
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
        String key = params.toLowerCase(Locale.ROOT);
        switch (key) {
            case "status" -> {
                boolean active = freezeService.isFrozen(player) || freezeService.isStaffChecking(player);
                return ColorUtil.use(active
                    ? statusPlaceholders.getOnCheck()
                    : statusPlaceholders.getOffCheck());
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

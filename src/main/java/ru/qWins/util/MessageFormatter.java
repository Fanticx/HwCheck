package ru.qWins.util;

import org.bukkit.entity.Player;
import ru.qWins.Config;

public record MessageFormatter(Config config) {

    public String format(String text) {
        return TextUtil.colorize(text);
    }

    public String format(String text, Player staff, Player target) {
        return TextUtil.colorize(PlaceholderUtil.apply(text, staff, target));
    }
}

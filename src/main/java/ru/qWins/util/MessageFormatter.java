package ru.qWins.util;

import org.bukkit.entity.Player;

public class MessageFormatter {

    public String format(String text) {
        return ColorUtil.use(text);
    }

    public String format(String text, Player staff, Player target) {
        return ColorUtil.use(applyPlaceholders(text, staff, target));
    }

    private String applyPlaceholders(String text, Player staff, Player target) {
        if (text == null) {
            return "";
        }
        String result = text;
        if (staff != null) {
            result = result.replace("{staff}", staff.getName());
        }
        if (target != null) {
            result = result.replace("{target}", target.getName());
        }
        return result;
    }
}

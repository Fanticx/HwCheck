package ru.qWins.util;

import org.bukkit.entity.Player;

public final class PlaceholderUtil {

    private PlaceholderUtil() {
    }

    public static String apply(String text, Player staff, Player target) {
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

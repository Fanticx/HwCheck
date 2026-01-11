package ru.qWins.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

public final class TextUtil {

    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorize(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }
}

package ru.qWins;

import com.annotations.qwins.annotations.core.ApiVersion;
import com.annotations.qwins.annotations.core.Authors;
import com.annotations.qwins.annotations.core.Name;
import com.annotations.qwins.annotations.core.Version;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.message.LiteMessages;
import dev.rollczi.litecommands.message.Message;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import java.io.File;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.qWins.command.FreezeCommand;
import ru.qWins.command.UnfreezeCommand;
import ru.qWins.listener.FreezeListener;
import ru.qWins.placeholder.HwCheckPlaceholderExpansion;
import ru.qWins.service.FreezeService;
import ru.qWins.util.MessageFormatter;

@Name("HwCheck")
@Version("1.0.0")
@ApiVersion(ApiVersion.Target.v1_16)
@Authors("qWins || Fanticx")
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    private Config config;
    private LiteCommands<CommandSender> liteCommands;
    private MessageFormatter messageFormatter;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.config = ConfigManager.create(Config.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(getDataFolder(), "config.yml"));
            it.saveDefaults();
            it.load(true);
        });
        this.messageFormatter = new MessageFormatter(config);
        FreezeService freezeService = new FreezeService(this, config, messageFormatter);
        this.liteCommands = LiteBukkitFactory.builder(this)
            .commands(
                new FreezeCommand(freezeService, config, messageFormatter),
                new UnfreezeCommand(freezeService, config, messageFormatter)
            )
            .message(LiteMessages.INVALID_USAGE, Message.of(invalidUsage -> {
                String usage = invalidUsage.getSchematic().first();
                String text = config.getMessages().getSystem().getInvalidUsage().replace("{usage}", usage);
                return messageFormatter.format(text);
            }))
            .message(LiteBukkitMessages.PLAYER_NOT_FOUND, Message.of(input -> {
                String name = input == null ? "" : input;
                String text = config.getMessages().getSystem().getPlayerNotFound().replace("{input}", name);
                return messageFormatter.format(text);
            }))
            .build();
        getServer().getPluginManager().registerEvents(new FreezeListener(freezeService, config, messageFormatter), this);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new HwCheckPlaceholderExpansion(this, freezeService, config).register();
        }
        getLogger().info("[HwCheck] плагин включен.");
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) {
            liteCommands.unregister();
        }
    }
}

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
import ru.qWins.freeze.FreezeManager;
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
        this.messageFormatter = new MessageFormatter();
        FreezeManager freezeService = new FreezeManager(this, config, messageFormatter);
        Config.Messages.System systemMessages = config.getMessages().getSystem();
        this.liteCommands = LiteBukkitFactory.builder(this)
            .commands(
                new FreezeCommand(freezeService, config, messageFormatter),
                new UnfreezeCommand(freezeService, config, messageFormatter)
            )
            .message(LiteMessages.INVALID_USAGE, Message.of(invalidUsage ->
                messageFormatter.format(systemMessages.getInvalidUsage()
                    .replace("{usage}", invalidUsage.getSchematic().first()))
            ))
            .message(LiteBukkitMessages.PLAYER_NOT_FOUND, Message.of(input ->
                messageFormatter.format(systemMessages.getPlayerNotFound()
                    .replace("{input}", input == null ? "" : input))
            ))
            .build();
        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new FreezeListener(freezeService, config, messageFormatter), this);
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new HwCheckPlaceholderExpansion(this, freezeService, config).register();
        }
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) {
            liteCommands.unregister();
        }
    }
}

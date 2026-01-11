package ru.qWins.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.context.Sender;
import org.bukkit.entity.Player;
import ru.qWins.Config;
import ru.qWins.service.FreezeService;
import ru.qWins.service.UnfreezeResult;
import ru.qWins.util.MessageFormatter;

@Command(name = "unfreezing", aliases = {"unfrz"})
public class UnfreezeCommand {

    private final FreezeService freezeService;
    private final Config config;
    private final MessageFormatter messageFormatter;

    public UnfreezeCommand(FreezeService freezeService, Config config, MessageFormatter messageFormatter) {
        this.freezeService = freezeService;
        this.config = config;
        this.messageFormatter = messageFormatter;
    }

    @Execute
    @Permission("hwcheck.unfreeze")
    public void execute(@Sender Player sender, @Arg("player") Player target) {
        UnfreezeResult result = freezeService.unfreeze(sender, target);
        switch (result) {
            case SUCCESS -> {
            }
            case NOT_FROZEN -> sender.sendMessage(messageFormatter.format(
                config.getMessages().getErrors().getNotFrozen()
            ));
            case NOT_YOUR_TARGET -> sender.sendMessage(messageFormatter.format(
                config.getMessages().getErrors().getNotYourTarget()
            ));
        }
    }
}

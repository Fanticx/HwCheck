package ru.qWins.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.context.Sender;
import org.bukkit.entity.Player;
import ru.qWins.Config;
import ru.qWins.freeze.type.FreezeResult;
import ru.qWins.freeze.FreezeManager;
import ru.qWins.util.MessageFormatter;

@Command(name = "freezing", aliases = {"frz"})
public class FreezeCommand {

    private final FreezeManager freezeService;
    private final MessageFormatter messageFormatter;
    private final Config.Messages.Errors errorMessages;

    public FreezeCommand(FreezeManager freezeService, Config config, MessageFormatter messageFormatter) {
        this.freezeService = freezeService;
        this.messageFormatter = messageFormatter;
        this.errorMessages = config.getMessages().getErrors();
    }

    @Execute
    @Permission("hwcheck.freeze")
    public void execute(@Sender Player sender, @Arg("player") Player target) {
        FreezeResult result = freezeService.freeze(sender, target);
        switch (result) {
            case SUCCESS -> {
            }
            case SELF_FREEZE -> sender.sendMessage(messageFormatter.format(
                errorMessages.getSelfFreeze()
            ));
            case STAFF_ALREADY_CHECKING -> {
                Player currentTarget = freezeService.getTargetForStaff(sender);
                Player placeholderTarget = currentTarget != null ? currentTarget : target;
                sender.sendMessage(messageFormatter.format(
                    errorMessages.getModeratorBusy(),
                    sender,
                    placeholderTarget
                ));
            }
            case ALREADY_FROZEN -> {
                Player currentStaff = freezeService.getStaffForTarget(target);
                if (currentStaff != null) {
                    sender.sendMessage(messageFormatter.format(
                        errorMessages.getTargetHasModerator(),
                        currentStaff,
                        target
                    ));
                } else {
                    sender.sendMessage(messageFormatter.format(
                        errorMessages.getAlreadyFrozen()
                    ));
                }
            }
        }
    }
}

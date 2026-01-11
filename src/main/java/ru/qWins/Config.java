package ru.qWins;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class Config extends OkaeriConfig {

    private Freeze freeze = new Freeze();
    @Comment("")
    private Titles titles = new Titles();
    @Comment("")
    private Messages messages = new Messages();
    @Comment("")
    private Unfreeze unfreeze = new Unfreeze();
    @Comment("")
    private Placeholders placeholders = new Placeholders();
    @Comment("")
    private Commands commands = new Commands();

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Freeze extends OkaeriConfig {
        private String world = "world";
        private double x = 0.5;
        private double y = 64.0;
        private double z = 0.5;
        private float yaw = 0.0f;
        private float pitch = 0.0f;
    }

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Titles extends OkaeriConfig {
        private Target target = new Target();
        private Staff staff = new Staff();
        private int fadeIn = 10;
        private int fadeOut = 10;

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Target extends OkaeriConfig {
            private String title = "&x&F&B&0&0&0&0ПРОВЕРКА НА ЧИТЫ!";
            private String subtitle = "&x&F&B&0&0&0&0Следите за чатом и не выходите из игры!";
            private int stay = 20;
            private int repeatTicks = 40;
        }

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Staff extends OkaeriConfig {
            private String title = "";
            private String subtitle = "&fНа проверке";
            private int stay = 20;
            private int repeatTicks = 30;
        }
    }

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Messages extends OkaeriConfig {
        private Target target = new Target();
        private Moderator moderator = new Moderator();
        private Errors errors = new Errors();
        private System system = new System();

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Target extends OkaeriConfig {
            private List<String> lines = Arrays.asList(
                "&x&F&B&0&0&0&0▶ &fВы подозреваетесь в &x&F&B&0&0&0&0использовании читов, &fпоэтому",
                "&f были заморожены!",
                "",
                "&6Чтобы разморозить себя и не получить бан, &f Вас есть &67",
                "&6мин., &fчтобы отправить свой логин &x&F&B&0&0&0&0&nAnyDesk&f в чат",
                "&7(Кликабельно: &x&F&B&0&0&0&0www.anydesk.com/ru &fи пройти проверку)",
                "",
                "&&x&F&B&0&0&0&0В случае отказа/выхода/игнора &f- &x&F&B&0&0&0&0блокировка аккаунта",
                ""
            );
            private int repeatSeconds = 30;
        }

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Moderator extends OkaeriConfig {
            private String end = "&x&F&B&0&0&0&0▶ &fПроверка игрока &f{target}&f завершена.";
        }

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Errors extends OkaeriConfig {
            private String alreadyFrozen = "&x&F&B&0&0&0&0▶ &fЭтот игрок уже на проверке.";
            private String notFrozen = "&x&F&B&0&0&0&0▶ &fЭтот игрок не на проверке.";
            private String moderatorBusy = "&x&F&B&0&0&0&0▶ &fТы уже проверяешь &f{target}&f.";
            private String targetHasModerator = "&x&F&B&0&0&0&0▶ &fЭтого игрока уже проверяет &f{staff}&f.";
            private String selfFreeze = "&x&F&B&0&0&0&0▶ &fНельзя вызвать на проверку самого себя.";
            private String notYourTarget = "&x&F&B&0&0&0&0▶ &fЭтот игрок не на твоей проверке.";
            private String commandBlocked = "&x&F&B&0&0&0&0▶ &fНельзя использовать команды во время проверки.";
        }

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class System extends OkaeriConfig {
            private String moderatorTargetLeft = "&x&F&B&0&0&0&0▶ &fИгрок &f{target}&f вышел с сервера.";
            private String targetModeratorLeft = "&x&F&B&0&0&0&0▶ &fПроверяющий вышел. Проверка отменена.";
            private String invalidUsage = "&x&F&B&0&0&0&0▶ &fИспользование: &f{usage}";
            private String playerNotFound = "&x&F&B&0&0&0&0▶ &fИгрок &f{input}&f не найден.";
        }
    }

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Unfreeze extends OkaeriConfig {
        private String title = "&fВы &aразморожены&f!";
        private String subtitle = "&fТелепортация обратно через: &a{seconds}&f. Покиньте область проверки для &cотмены&f.";
        private int teleportSeconds = 10;
        private double cancelRadius = 3.0;
    }

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Placeholders extends OkaeriConfig {
        private Status status = new Status();

        @Getter
        @Setter
        @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
        public static class Status extends OkaeriConfig {
            private String onCheck = "&x&b&4&2&4&c&fНа проверке";
            private String offCheck = "&r";
        }
    }

    @Getter
    @Setter
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class Commands extends OkaeriConfig {
        private List<String> allowed = Arrays.asList("msg", "r", "reply", "tell", "w");
    }
}

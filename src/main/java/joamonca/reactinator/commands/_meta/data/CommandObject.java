package joamonca.reactinator.commands._meta.data;

import joamonca.reactinator.commands._meta.BotCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;

public record CommandObject(
        String name,
        String description,
        BotCommand command,
        boolean HiddenFromHelp,
        boolean userInstallable, // whether to register as a user-installable app
        List<OptionData> options // for slash commands
) {
    public CommandObject {
        options = (options == null) ? List.of() : List.copyOf(options);
    }

    // all my homies love varargs
    public CommandObject(String name, String description, BotCommand command, boolean hiddenFromHelp, OptionData... options) {
        this(name, description, command, hiddenFromHelp, false, List.of(options));
    }

    // same but for user-installable commands
    public CommandObject(String name, String description, BotCommand command, boolean hiddenFromHelp, boolean userInstallable, OptionData... options) {
        this(name, description, command, hiddenFromHelp, userInstallable, List.of(options));
    }
}

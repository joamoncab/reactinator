package joamonca.reactinator.commands._meta.data;

import joamonca.reactinator.commands._meta.BotCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;

public record CommandObject(
        String name,
        String description,
        BotCommand command,
        List<OptionData> options // for slash commands
) {
    public CommandObject {
        options = (options == null) ? List.of() : List.copyOf(options);
    }

    // all my homies love varargs
    public CommandObject(String name, String description, BotCommand command, OptionData... options) {
        this(name, description, command, List.of(options));
    }
}

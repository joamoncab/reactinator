package joamonca.reactinator.commands.settings;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class OptOut implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        String type = event.getOption("type").getAsString();
        long userId = event.getUser().getIdLong();

        boolean current = data.database().getOptout(userId, type);
        data.database().setOptout(userId, type, !current);
        replySlash(event, (current ? "re-enabled " : "opted out of ") + type + ".", true);
    }
}

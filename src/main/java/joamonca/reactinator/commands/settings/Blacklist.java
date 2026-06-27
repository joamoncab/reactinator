package joamonca.reactinator.commands.settings;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class Blacklist implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        if (!event.getUser().getId().equals(data.ownerID())) {
            replySlash(event, "you are not authorized to use this command.", true);
            return;
        }

        long userId = event.getOption("user").getAsUser().getIdLong();
        data.database().ensureUser(userId);
        boolean current = data.database().isBlacklisted(userId);
        data.database().setBlacklisted(userId, !current);
        replySlash(event, (current ? "un" : "") + "blacklisted user.", true);
    }
}

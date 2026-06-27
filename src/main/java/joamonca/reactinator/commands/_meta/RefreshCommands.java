package joamonca.reactinator.commands._meta;

import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class RefreshCommands implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();

        if (!event.getUser().getId().equals(data.ownerID())) {
            replySlash(event, "you are not authorized to use this command.", true);
            return;
        }

        event.deferReply(true).queue();

        try {
            event.getGuild().updateCommands().addCommands(
                    CommandsList.getCommands().entrySet().stream()
                            .map(entry -> Commands.slash(entry.getKey(), entry.getValue().description())
                                    .addOptions(entry.getValue().options()))
                            .toList()
            ).queue(
                success -> replySlash(event, "slash commands successfully refreshed!", true),
                failure -> replySlash(event, "failed to refresh slash commands: " + failure.getMessage(), true)
            );
        } catch (Exception e) {
            replySlash(event, "an error occurred: " + e.getMessage(), true);
        }
    }
}

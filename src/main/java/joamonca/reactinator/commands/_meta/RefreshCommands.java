package joamonca.reactinator.commands._meta;

import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import static joamonca.reactinator.util.send.Messages.reply;

public class RefreshCommands implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();

        if (!event.getUser().getId().equals(data.ownerID())) {
            reply(event, "you are not authorized to use this command.", true);
            return;
        }

        try {
            event.getGuild().updateCommands().addCommands(
                    CommandsList.getCommands().entrySet().stream()
                            .map(entry -> Commands.slash(entry.getKey(), entry.getValue().description())
                                    .addOptions(entry.getValue().options()))
                            .toList()
            ).queue(
                success -> reply(event, "slash commands successfully refreshed!", true),
                failure -> reply(event, "failed to refresh slash commands: " + failure.getMessage(), true)
            );
        } catch (Exception e) {
            reply(event, "an error occurred: " + e.getMessage(), true);
        }
    }
}

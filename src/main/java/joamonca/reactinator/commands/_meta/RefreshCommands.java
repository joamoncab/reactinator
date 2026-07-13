package joamonca.reactinator.commands._meta;

import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.commands._meta.data.CommandObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Map;

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

        Map<String, CommandObject> allCommands = CommandsList.getCommands();

        try {
            // guild-only commands (not user-installable)
            event.getGuild().updateCommands().addCommands(
                    allCommands.entrySet().stream()
                            .filter(entry -> !entry.getValue().userInstallable())
                            .map(entry -> Commands.slash(entry.getKey(), entry.getValue().description())
                                    .addOptions(entry.getValue().options()))
                            .toList()
            ).queue(
                success -> reply(event, "guild commands successfully refreshed!", true),
                failure -> reply(event, "failed to refresh guild commands: " + failure.getMessage(), true)
            );

            // user-installable commands (registered globally with both integration types)
            event.getJDA().updateCommands().addCommands(
                    allCommands.entrySet().stream()
                            .filter(entry -> entry.getValue().userInstallable())
                            .map(entry -> {
                                SlashCommandData cmd = Commands.slash(entry.getKey(), entry.getValue().description())
                                        .addOptions(entry.getValue().options());
                                cmd.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL);
                                cmd.setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL);
                                return cmd;
                            })
                            .toList()
            ).queue(
                success -> reply(event, "global user-installable commands successfully refreshed!", true),
                failure -> reply(event, "failed to refresh global commands: " + failure.getMessage(), true)
            );
        } catch (Exception e) {
            reply(event, "an error occurred: " + e.getMessage(), true);
        }
    }
}


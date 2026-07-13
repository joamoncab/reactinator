package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class UserLeaderboard implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        List<long[]> entries = data.database().getUserReactionLeaderboard(
                event.getGuild().getIdLong(), 10
        );

        if (entries.isEmpty()) {
            event.getHook().sendMessage("no reaction data yet.").queue();
            return;
        }

        StringBuilder sb = new StringBuilder("**most reacted users**\n");
        int rank = 1;
        for (long[] entry : entries) {
            String display;
            try {
                User user = event.getJDA().retrieveUserById(entry[0]).complete();
                display = "**" + user.getName() + "**";
            } catch (ErrorResponseException e) {
                display = e.getErrorResponse().getMeaning() + " (<@" + entry[0] + ">)";
            }
            sb.append(rank++).append(". ").append(display)
                    .append(" — **").append(entry[1]).append("** reactions\n");
        }
        sb.append("total: **").append(data.database().getUserReactionGrandTotal(event.getGuild().getIdLong())).append("** reactions\n\n");

        event.getHook().sendMessage(sb.toString()).queue();
    }
}


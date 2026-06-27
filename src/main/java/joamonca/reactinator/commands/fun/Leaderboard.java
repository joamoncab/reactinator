package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class Leaderboard implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        List<long[]> entries = data.database().getEmojiLeaderboard(
                event.getGuild().getIdLong(), 10
        );

        if (entries.isEmpty()) {
            event.getHook().sendMessage("no emoji usage data yet.").queue();
            return;
        }

        StringBuilder sb = new StringBuilder("**emoji leaderboard**\n");
        int rank = 1;
        for (long[] entry : entries) {
            RichCustomEmoji emoji = event.getGuild().getEmojiById(entry[0]);
            String display = emoji != null ? emoji.getAsMention() : "unknown (" + entry[0] + ")";
            sb.append(rank++).append(". ").append(display)
                    .append(" — used **").append(entry[1]).append("** times\n");
        }

        event.getHook().sendMessage(sb.toString()).queue();
    }
}

package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class UserStats implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        User targetUser = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getInteraction().getUser();
        long userId = targetUser.getIdLong();
        long guildId = event.getGuild().getIdLong();
        String name = "**"+targetUser.getName()+"**";

        int total = data.database().getUserReactionTotal(userId, guildId);
        if (total == 0) {
            event.getHook().sendMessage(name + " has no reactions yet.").queue();
            return;
        }

        List<long[]> emojiStats = data.database().getUserEmojiStats(userId, guildId, 10);

        StringBuilder sb = new StringBuilder("reaction stats for ").append(name).append("\n");

        if (!emojiStats.isEmpty()) {
            int rank = 1;
            for (long[] entry : emojiStats) {
                RichCustomEmoji emoji = event.getGuild().getEmojiById(entry[0]);
                String display = emoji != null ? emoji.getAsMention() : "unknown (" + entry[0] + ")";
                sb.append(rank++).append(". ").append(display)
                        .append(" — **").append(entry[1]).append("** times\n");
            }
        }

        sb.append("total: **").append(total).append("** reactions\n\n");

        event.getHook().sendMessage(sb.toString()).queue();
    }
}

package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.util.get.ReactLeaderboardEntry;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class ReactLeaderboard implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        List<ReactLeaderboardEntry> entries = data.database().getReactLeaderboard(
                event.getGuild().getIdLong(), 10
        );

        if (entries.isEmpty()) {
            event.getHook().sendMessage("no message reaction data yet. yet...").queue();
            return;
        }

        StringBuilder sb = new StringBuilder("**reactboard leaderboard**\n");
        int rank = 1;
        for (ReactLeaderboardEntry entry : entries) {
            RichCustomEmoji emoji = event.getGuild().getEmojiById(entry.emojiId());
            String display = emoji != null ? emoji.getAsMention() : "unknown";
            String jumpUrl = "https://discord.com/channels/" + event.getGuild().getIdLong() + "/" + entry.channelId() + "/" + entry.messageId();
            
            sb.append(rank++).append(". ").append(display)
                    .append(" **").append(entry.count()).append("** | ")
                    .append(jumpUrl).append("\n");
        }

        event.getHook().sendMessage(sb.toString()).queue();
    }
}

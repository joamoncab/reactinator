package joamonca.reactinator.events;

import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.util.get.ReactboardEntry;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReactionHandler extends ListenerAdapter {
    private final Database database;

    public ReactionHandler(Database database) {
        this.database = database;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;

        // here we only track human reactions, reactinator has another table for that
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        // ban is ban heh
        if (database.isBlacklisted(event.getUserIdLong())) return;

        if (event.getEmoji().getType() != Emoji.Type.CUSTOM) return;

        long emojiId = event.getEmoji().asCustom().getIdLong();

        if (database.isEmojiBlacklisted(emojiId)) return;

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long messageId = event.getMessageIdLong();

        Long reactinatorEmojiId = database.getReactionEmoji(messageId);
        if (reactinatorEmojiId == null || reactinatorEmojiId != emojiId) {
            return;
        }

        database.upsertReaction(messageId, emojiId);

        ReactboardEntry reactboard = database.getReactboardForGuild(guildId);
        if (reactboard != null) {
            int count = database.getReactionCount(messageId);
            if (count == reactboard.threshold()) {
                GuildMessageChannel target = event.getGuild().getChannelById(GuildMessageChannel.class, reactboard.channel());
                if (target != null) {
                    String jumpUrl = "https://discord.com/channels/" + guildId + "/" + channelId + "/" + messageId;
                    target.sendMessage(
                            event.getEmoji().asCustom().getAsMention() + " **" + count + "** | " + jumpUrl
                    ).queue();
                }
            }
        }
    }
}

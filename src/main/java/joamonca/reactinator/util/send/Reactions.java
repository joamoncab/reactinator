package joamonca.reactinator.util.send;

import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.util.get.ReactboardEntry;
import joamonca.reactinator.util.process.Cats;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Reactions {
    public static void react(MessageReceivedEvent event, List<RichCustomEmoji> customEmojiList, Database database) {
        if (customEmojiList == null) customEmojiList = event.getGuild().getEmojis();
        if (customEmojiList.isEmpty()) return;

        List<RichCustomEmoji> available = customEmojiList;
        if (database != null) {
            available = customEmojiList.stream()
                    .filter(emoji -> !database.isEmojiBlacklisted(emoji.getIdLong())) // todo this is a lot of database requests isn't it
                    .toList();
            if (available.isEmpty()) return;
        }

        tryReact(event, available, database, 3);
    }

    private static void tryReact(MessageReceivedEvent event, List<RichCustomEmoji> available, Database database, int attemptsLeft) {
        if (available.isEmpty() || attemptsLeft <= 0) return;

        RichCustomEmoji emojiToUse = available.get(ThreadLocalRandom.current().nextInt(available.size()));

        event.getMessage().addReaction(emojiToUse).queue(
            success -> {
                // Track in database if available
                if (database != null) {
                    long guildId = event.getGuild().getIdLong();
                    long channelId = event.getChannel().getIdLong();
                    long messageId = event.getMessage().getIdLong();
                    long emojiId = emojiToUse.getIdLong();

                    database.ensureChannel(channelId, guildId);
                    database.ensureEmoji(emojiId, guildId);
                    database.incrementEmojiUsage(emojiId);
                    database.insertMessage(messageId, channelId);
                    database.upsertReaction(messageId, emojiId);

                    // Check reactboard threshold
                    ReactboardEntry reactboard = database.getReactboardForGuild(guildId);
                    if (reactboard != null) {
                        int count = database.getReactionCount(messageId);
                        if (count == reactboard.threshold()) {
                            GuildMessageChannel target = event.getGuild().getChannelById(GuildMessageChannel.class, reactboard.channel());
                            if (target != null) {
                                target.sendMessage(
                                        emojiToUse.getAsMention() + " **" + count + "** | " + event.getMessage().getJumpUrl()
                                ).queue();
                            }
                        }
                    }
                }
            },
            throwable -> {
                if (throwable instanceof net.dv8tion.jda.api.exceptions.ErrorResponseException e) {
                    if (e.getErrorResponse() == net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI) {
                        List<RichCustomEmoji> remaining = new java.util.ArrayList<>(available);
                        remaining.remove(emojiToUse);
                        tryReact(event, remaining, database, attemptsLeft - 1);
                        return;
                    }
                }
                // Print other errors to stderr
                System.err.println("Failed to react: " + throwable.getMessage());
            }
        );
    }

    public static void reactWithCats(MessageReceivedEvent event, Database database) {
        react(event, Cats.getCatEmojis(event.getGuild().getEmojis()), database);
    }
}

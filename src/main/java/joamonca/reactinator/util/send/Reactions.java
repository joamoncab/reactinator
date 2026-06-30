package joamonca.reactinator.util.send;

import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.util.get.ReactboardEntry;
import joamonca.reactinator.util.process.Cats;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static joamonca.reactinator.util.send.Messages.reply;

public class Reactions {
    public static void react(MessageReceivedEvent event, List<RichCustomEmoji> customEmojiList, Database database) {
        if (customEmojiList == null) customEmojiList = event.getGuild().getEmojis();
        if (customEmojiList.isEmpty()) return;

        List<RichCustomEmoji> available = customEmojiList;
        if (database != null) {
            Set<Long> blacklisted = database.getBlacklistedEmojiIds(event.getGuild().getIdLong());
            available = customEmojiList.stream()
                    .filter(RichCustomEmoji::isAvailable) // we don't care if it's blacklisted if there are not enough boosts to use it
                    .filter(emoji -> !blacklisted.contains(emoji.getIdLong()))
                    .toList();
            if (available.isEmpty()) return;
        }

        tryReact(event, available, database);
    }

    private static void tryReact(MessageReceivedEvent event, List<RichCustomEmoji> available, Database database) {
        if (available.isEmpty()) return;

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
                    if (throwable instanceof ErrorResponseException e) {
                        if (e.getErrorResponse() == ErrorResponse.REACTION_BLOCKED) {
                            event.getChannel().sendMessage(event.getMessage().getAuthor().getAsMention() + " is a meanie and has blocked me :(").queue();
                        }
                    }
                }
        );
    }

    public static void reactWithCats(MessageReceivedEvent event, Database database) {
        react(event, Cats.getCatEmojis(event.getGuild().getEmojis()), database);
    }
}

package joamonca.reactinator.events;

import joamonca.reactinator.json.Reader;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

public class MessageHandler extends ListenerAdapter {
    Reader reader;
    Random random;
    public MessageHandler(Reader reader) {
        this.reader = reader;
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        // The user who sent the message
        User author = event.getAuthor();

        this.random = new Random(event.getAuthor().getIdLong() +
                System.currentTimeMillis() +
                event.getMessage().getIdLong() +
                event.getChannel().getIdLong());

        // Check whether the message was sent in a guild / server
        if (event.isFromGuild())
        {
            // This is a message from a server
            if (reader.isTargetID(author.getId()))
            {
                if (random.nextFloat() >= reader.getSpawnChance(author.getId())) {
                    return;
                }
                var emojiData = reader.getEmoji(author.getId());
                if (emojiData != null) {
                    event.getMessage().addReaction(
                            Emoji.fromCustom(emojiData.getEmojiName(),
                                            emojiData.getEmojiID(),
                                            emojiData.isAnimated()
                            )
                    ).queue();
                }
            }
        }
    }
}

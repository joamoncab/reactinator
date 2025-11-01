package joamonca.reactinator.events;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class MessageHandler extends ListenerAdapter {
    Random random;
    float chance;
    public MessageHandler(float chance) {
        this.chance = chance;
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
           // fixed chance to react
            if (this.random.nextFloat() <= chance) {
                List<RichCustomEmoji> emojis = event.getGuild().getEmojis();
                float whichEmoji = this.random.nextFloat();
                if (!emojis.isEmpty()) {
                    int emojiIndex = (int) (whichEmoji * emojis.size());
                    RichCustomEmoji emojiToUse = emojis.get(emojiIndex);
                    event.getMessage().addReaction(emojiToUse).queue();
                }
            }
        }
    }
}

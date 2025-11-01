package joamonca.reactinator.events;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class MessageHandler extends ListenerAdapter {
    private final Random random = new Random();
    private final float chance;

    public MessageHandler(float chance) {
        this.chance = chance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        if (random.nextFloat() > chance) {
            return;
        }

        List<RichCustomEmoji> emojis = event.getGuild().getEmojis();
        if (!emojis.isEmpty()) {
            RichCustomEmoji emojiToUse = emojis.get(random.nextInt(emojis.size()));
            event.getMessage().addReaction(emojiToUse).queue();
        }
    }
}
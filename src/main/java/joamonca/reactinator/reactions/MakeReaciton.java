package joamonca.reactinator.reactions;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MakeReaciton {
    private MakeReaciton() {} // utility class

    public static void react(MessageReceivedEvent event, List<RichCustomEmoji> customEmojiList) {
        if (customEmojiList == null) customEmojiList = event.getGuild().getEmojis();
        if (!customEmojiList.isEmpty()) {
            RichCustomEmoji emojiToUse = customEmojiList.get(ThreadLocalRandom.current().nextInt(customEmojiList.size()));
            event.getMessage().addReaction(emojiToUse).queue();
        }
    }
}


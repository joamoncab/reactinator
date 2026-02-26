package joamonca.reactinator.reactions;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Random;

public class MakeReaciton {
    MessageReceivedEvent event;
    Random random = new Random();
    public MakeReaciton(MessageReceivedEvent event) {
        this.event = event;
    }
    public void react(List<RichCustomEmoji> customEmojiList) {
        if (customEmojiList == null) customEmojiList = event.getGuild().getEmojis();
        if (!customEmojiList.isEmpty()) {
            RichCustomEmoji emojiToUse = customEmojiList.get(random.nextInt(customEmojiList.size()));
            event.getMessage().addReaction(emojiToUse).queue();
        }
    }
}

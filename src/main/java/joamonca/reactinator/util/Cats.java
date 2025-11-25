package joamonca.reactinator.util;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.ArrayList;
import java.util.List;

public class Cats {
    public static boolean isCatChannel(String channelName) {
        return
                channelName.toLowerCase().contains("-cat") ||
                channelName.toLowerCase().contains("cat-") ||
                channelName.toLowerCase().contains("-cats") ||
                channelName.toLowerCase().contains("cats-");
    }

    public static List<RichCustomEmoji> getCatEmojis(List<RichCustomEmoji> emojis) {
        List<RichCustomEmoji> catEmojis;
        catEmojis = emojis.stream().filter(emoji ->
                emoji.getName().toLowerCase().contains("cat")
        ).toList();
        return catEmojis.isEmpty() ? emojis : catEmojis;
    }
}

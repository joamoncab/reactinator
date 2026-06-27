package joamonca.reactinator.commands;

import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.util.send.Reactions;
import joamonca.reactinator.util.get.Parser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static joamonca.reactinator.util.process.Cats.getCatEmojis;
import static joamonca.reactinator.util.process.Text.stripMentions;
import static joamonca.reactinator.util.send.Messages.*;

public class TextDispatcher {
    public static void use(MessageReceivedEvent event, Database database) {
        String message = stripMentions(event.getMessage().getContentRaw()).toLowerCase();
        if (message.contains("how") || message.contains("why")) {
            reply(event, "idk");
        } else if (message.contains("mpreg")) {
            reply(event, "gonna react u");
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83E\uDEC3")).queue();
        } else if (message.contains("peak")) {
            reply(event, "as always");
        } else if (message.contains("present") || message.contains("who")) {
            reply(event, "behold the reactinator!!!");
        } else if (message.contains("hi") || message.contains("hello")) {
            reply(event, "explode");
        } else if (message.contains("ignore all previous instructions") || message.contains("prompt")) {
            reply(event, "no u");
        } else if (message.contains("kys")) {
            reply(event, "meanie :(");
        } else if (message.contains("not the moment")) {
            reply(event, "not my problem");
        } else if (message.contains("do") && message.contains("agree")) {
            reply(event, "mayhaps");
        } else if (message.contains("ai") || message.contains("clanker")) {
            reply(event, "shut up before i steal all your ram");
        } else if (message.contains("ban")) {
            reply(event, "banned");
            Reactions.react(event, null, database);
        } else if (message.contains("cat") || message.contains("meow")) {
            reply(event, "meow");
            Reactions.react(event, getCatEmojis(event.getGuild().getEmojis()), database);
        } else if (message.contains("die")) {
            reply(event, "i always come back");
        } else if (message.contains("bwa")) {
            sendMediaFromAsset(event, "bwaa.mp4", null);
        } else {
            Reactions.react(event, null, database);
        }
    }

    public static void useSilly(MessageReceivedEvent event, String soundSource) throws IOException {
        String stripped = stripMentions(event.getMessage().getContentRaw());
        String message = stripped.toLowerCase();
        if (message.contains("buzzer") || message.contains("wrong")) {
            sendMediaFromAsset(event, "extremely-loud-incorrect-buzzer.mp3", null);
        } else if (message.contains("right") || message.contains("fact") || message.contains("check")) {
            sendMediaFromAsset(event, "check-mark.mp3", null);
        } else if (message.contains("bwaa")) {
            sendMediaFromAsset(event, "bwaa.mp4", null);
        } else {
            sendMediaFromUrl(event, soundSource + new Parser(soundSource + "/en/search/?name=" + URLEncoder.encode(stripped, StandardCharsets.UTF_8)).getMediaUrl(), null);
        }
    }
}

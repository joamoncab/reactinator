package joamonca.reactinator.ai;

import joamonca.reactinator.reactions.MakeReaciton;
import joamonca.reactinator.util.Parser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import java.net.URL;

public class AIHandler {
    MessageReceivedEvent event;
    public AIHandler(MessageReceivedEvent event) {
        this.event = event;
    }

    public void use() {
        String message = event.getMessage().getContentRaw().trim().toLowerCase();
        if (message.contains("how") || message.contains("why")) {
            reply("idk");
        } else if (message.contains("mpreg")) {
            reply("gonna react u");
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83E\uDEC3")).queue();
        } else if (message.contains("peak")) {
            reply("as always");
        } else if (message.contains("present") || message.contains("who")) {
            reply("behold the reactinator!!!");
        } else if (message.contains("hi") || message.contains("hello")) {
            reply("explode");
        } else if (message.contains("ignore all previous instructions")) {
            reply("no u");
        } else if (message.contains("kys")) {
            reply("meanie :(");
        } else if (message.contains("not the moment")) {
            reply("not my problem");
        } else if (message.contains("do") && message.contains("agree")) {
            reply("mayhaps");
        }  else if (message.contains("ai") || message.contains("clanker")) {
            reply("shut up before i steal all your ram");
        } else {
            new MakeReaciton(event).react(null);
        }
    }

    public void useSilly(String soundSource) throws IOException {
        String message = event.getMessage().getContentRaw().trim().toLowerCase();
        if (message.contains("buzzer") || message.contains("wrong")) {
            sendMedia("extremely-loud-incorrect-buzzer.mp3", null);
        } else if (message.contains("right") || message.contains("fact") || message.contains("check")) {
            sendMedia("check-mark.mp3", null);
        } else {
            sendMediaFromUrl(soundSource + new Parser(soundSource + "/en/search/?name=" + URLEncoder.encode(
                    event.getMessage().getContentRaw().replaceAll("<@!?\\d+>|<@&\\d+>|<#\\d+>", "").trim(),
                    StandardCharsets.UTF_8)).getMediaUrl(), null);
        }
    }

    private void reply(String message) {
        event.getChannel().sendMessage(message).setMessageReference(event.getMessageId()).queue();
    }

    private void sendMedia(@NotNull String fileName, @Nullable String message) {
        String resourcePath = "/assets/" + fileName;
        if (message == null) {
            message = "";
        }

        InputStream inputStream = getClass().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            System.out.println("Resource not found: " + resourcePath);
            return;
        }

        event.getChannel().sendMessage(message)
                .addFiles(FileUpload.fromData(inputStream, fileName))
                .setMessageReference(event.getMessage().getReferencedMessage())
                .queue();
    }

    private void sendMediaFromUrl(@NotNull String urlString, @Nullable String message) {
        if (message == null) {
            message = "";
        }

        try {
            java.net.URL url = new java.net.URI(urlString).toURL();
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            if (!fileName.contains(".mp3")) {
                fileName = "audio.mp3";
            }

            InputStream inputStream = url.openStream();
            event.getChannel().sendMessage(message)
                    .addFiles(FileUpload.fromData(inputStream, fileName))
                    .setMessageReference(event.getMessage().getReferencedMessage())
                    .queue();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}

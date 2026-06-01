package joamonca.reactinator.ai;

import joamonca.reactinator.reactions.MakeReaciton;
import joamonca.reactinator.util.Parser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AIHandler {
    private AIHandler() {} // utility class

    public static void use(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().trim().toLowerCase();
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
        } else if (message.contains("ignore all previous instructions")) {
            reply(event, "no u");
        } else if (message.contains("kys")) {
            reply(event, "meanie :(");
        } else if (message.contains("not the moment")) {
            reply(event, "not my problem");
        } else if (message.contains("do") && message.contains("agree")) {
            reply(event, "mayhaps");
        }  else if (message.contains("ai") || message.contains("clanker")) {
            reply(event, "shut up before i steal all your ram");
        } else {
            MakeReaciton.react(event, null);
        }
    }

    public static void useSilly(MessageReceivedEvent event, String soundSource) throws IOException {
        String message = event.getMessage().getContentRaw().trim().toLowerCase();
        if (message.contains("buzzer") || message.contains("wrong")) {
            sendMedia(event, "extremely-loud-incorrect-buzzer.mp3", null);
        } else if (message.contains("right") || message.contains("fact") || message.contains("check")) {
            sendMedia(event, "check-mark.mp3", null);
        } else {
            sendMediaFromUrl(event, soundSource + new Parser(soundSource + "/en/search/?name=" + URLEncoder.encode(
                    event.getMessage().getContentRaw().replaceAll("<@!?\\\\d+>|<@&\\\\d+>|<#\\\\d+>", "").trim(),
                    StandardCharsets.UTF_8)).getMediaUrl(), null);
        }
    }

    private static void reply(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessage(message).setMessageReference(event.getMessageId()).queue();
    }

    private static void sendMedia(MessageReceivedEvent event, @NotNull String fileName, @Nullable String message) {
        String resourcePath = "/assets/" + fileName;
        if (message == null) {
            message = "";
        }

        try (InputStream inputStream = AIHandler.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.out.println("Resource not found: " + resourcePath);
                return;
            }

            byte[] data = inputStream.readAllBytes();
            event.getChannel().sendMessage(message)
                    .addFiles(FileUpload.fromData(data, fileName))
                    .setMessageReference(event.getMessage().getReferencedMessage())
                    .queue();
        } catch (IOException e) {
            System.out.println("Error reading resource: " + e.getMessage());
        }
    }

    private static void sendMediaFromUrl(MessageReceivedEvent event, @NotNull String urlString, @Nullable String message) {
        if (message == null) {
            message = "";
        }

        try {
            java.net.URL url = new java.net.URI(urlString).toURL();
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            if (!fileName.contains(".mp3")) {
                fileName = "audio.mp3";
            }

            try (InputStream inputStream = url.openStream()) {
                byte[] data = inputStream.readAllBytes();
                event.getChannel().sendMessage(message)
                        .addFiles(FileUpload.fromData(data, fileName))
                        .setMessageReference(event.getMessage().getReferencedMessage())
                        .queue();
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

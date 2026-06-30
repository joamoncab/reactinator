package joamonca.reactinator.util.send;

import joamonca.reactinator.commands.TextDispatcher;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Messages {
    public static void reply(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessage(message).setMessageReference(event.getMessageId()).queue();
    }

    public static void reply(SlashCommandInteractionEvent event, String message, boolean ephemeral) {
        event.getHook().sendMessage(message).setEphemeral(ephemeral).queue();
    }

    public static void replyError(Event event, String message) {
        message = message + "\n<@807170846497570848> do better!!!"; //blaming mud for this btw
        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            reply(messageReceivedEvent, message);
        } else if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
            reply(slashCommandInteractionEvent, message, true);
        }
    }

    public static void sendMediaFromAsset(Event event, @NotNull String fileName, @Nullable String message) {
        String resourcePath = "/assets/" + fileName;
        if (message == null) {
            message = "";
        }

        try (InputStream inputStream = TextDispatcher.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.out.println("Resource not found: " + resourcePath);
                return;
            }
            sendMedia(event, inputStream, message, fileName);
        } catch (IOException e) {
            System.out.println("Error reading resource: " + e.getMessage());
        }
    }

    public static void sendMediaFromUrl(Event event, @NotNull String urlString, @Nullable String message) {
        if (message == null) {
            message = "";
        }

        try {
            java.net.URL url = new java.net.URI(urlString).toURL();
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            if (!fileName.contains(".mp3")) {
                fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8) + ".mp3";
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    sendMedia(event, inputStream, message, fileName);
                }
            } else {
                System.out.println("Error: " + connection.getResponseMessage());
                replyError(event, "couldn't find that weird media you wanted");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sendMedia(Event event, InputStream inputStream, String message, String fileName) throws IOException {
        byte[] data = inputStream.readAllBytes();
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
            slashCommandInteractionEvent.getHook().sendMessage(message)
                    .addFiles(FileUpload.fromData(data, fileName))
                    .queue();
        } else if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            messageReceivedEvent.getChannel().sendMessage(message)
                    .addFiles(FileUpload.fromData(data, fileName))
                    .setMessageReference(messageReceivedEvent.getMessage().getReferencedMessage())
                    .queue();
        }
    }
}

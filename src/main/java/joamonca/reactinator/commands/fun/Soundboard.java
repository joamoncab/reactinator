package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.util.get.Parser;
import joamonca.reactinator.util.send.Messages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static joamonca.reactinator.util.process.Text.stripMentions;
import static joamonca.reactinator.util.send.Messages.replySlash;

public class Soundboard implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        String soundOption = event.getOption("sound").getAsString();
        String stripped = stripMentions(soundOption);
        String message = stripped.toLowerCase();
        String soundSource = data.soundsSource();

        if (soundSource == null || soundSource.isEmpty()) {
            replySlash(event, "soundboard source is not configured. blame someone else, not me, ey?", false);
            return;
        }

        try {
            if (message.contains("buzzer") || message.contains("wrong")) {
                Messages.sendMediaFromAsset(event, "extremely-loud-incorrect-buzzer.mp3", null);
            } else if (message.contains("right") || message.contains("fact") || message.contains("check")) {
                Messages.sendMediaFromAsset(event, "check-mark.mp3", null);
            } else if (message.contains("bwaa")) {
                Messages.sendMediaFromAsset(event, "bwaa.mp4", null);
            } else {
                String searchUrl = soundSource + "/en/search/?name=" + URLEncoder.encode(stripped, StandardCharsets.UTF_8);
                Parser parser = new Parser(searchUrl);
                String mediaUrl = parser.getMediaUrl();
                if (mediaUrl == null || mediaUrl.isEmpty()) {
                    replySlash(event, "could not find any sound matching that query.", true);
                    return;
                }
                Messages.sendMediaFromUrl(event, soundSource + mediaUrl, null);
            }
        } catch (Exception e) {
            System.out.println("Error playing soundboard command: " + e.getMessage());
            replySlash(event, "failed to play or find the specified sound.", true);
        }
    }
}

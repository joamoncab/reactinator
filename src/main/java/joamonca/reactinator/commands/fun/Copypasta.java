package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.util.exceptions.CopypastaNotFoundException;
import joamonca.reactinator.util.send.Messages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static joamonca.reactinator.util.process.Text.stripMentions;
import static joamonca.reactinator.util.send.Messages.reply;
import static joamonca.reactinator.util.send.Messages.replyError;

public class Copypasta implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();

        String copypastaSource = data.copypastaSource();
        if (copypastaSource == null || copypastaSource.isEmpty()) {
            event.reply("copypasta source is not configured. blame someone else, not me, ey?").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        String copypastaOption = event.getOption("copypasta").getAsString();
        String stripped = stripMentions(copypastaOption);

        try {
            try {
                Messages.sendCopypastaFromUrl(event, copypastaSource + URLEncoder.encode(stripped, StandardCharsets.UTF_8));
            } catch (CopypastaNotFoundException e) {
                replyError(event, e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error playing copypasta command: " + e.getMessage());
            reply(event, "failed to play or find the specified copypasta.", false);
        }
    }
}

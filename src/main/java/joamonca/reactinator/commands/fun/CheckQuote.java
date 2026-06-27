package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.util.get.QuoteTamperInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class CheckQuote implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();
        OptionMapping signatureOpt = event.getOption("signature");

        if (signatureOpt == null) {
            replySlash(event, "please specify a signature ID.", true);
            return;
        }

        long signature = signatureOpt.getAsLong();

        QuoteTamperInfo info = data.database().getQuoteTamper(signature);

        if (info == null) {
            event.getHook().sendMessage("could not find a quote with signature #" + signature + ".").queue();
            return;
        }

        String creatorMention = "<@" + info.userId() + ">";
        String messageLink = String.format("https://discord.com/channels/%d/%d/%d",
                info.guildId(), info.channelId(), info.messageId());

        String response = String.format("quote signature #%d was created by %s.\nOriginal message: %s",
                signature, creatorMention, messageLink);

        event.getHook().sendMessage(response).queue();
    }
}

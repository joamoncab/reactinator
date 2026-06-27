package joamonca.reactinator.commands.chances;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class GetChance implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();
        OptionMapping channelOpt = event.getOption("channel");

        long channelId = (channelOpt != null) ? channelOpt.getAsChannel().getIdLong() : event.getChannel().getIdLong();
        String channelMention = (channelOpt != null) ? channelOpt.getAsChannel().getAsMention() : event.getChannel().getAsMention();

        int channelChance = data.database().getChannelChances(channelId);
        int guildChance = data.database().getGuildChances(event.getGuild().getIdLong());

        if (channelChance >= 0) {
            replySlash(event, "current chance in " + channelMention + " is " + channelChance + "%", true);
        } else if (guildChance >= 0) {
            replySlash(event, "no override set for " + channelMention + ". It uses the guild default (" + guildChance + "%).", true);
        } else {
            replySlash(event, "failed to retrieve chances.", true);
        }
    }
}

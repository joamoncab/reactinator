package joamonca.reactinator.commands.chances;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class SetChanceChannel implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();
        if (!(event.getUser().getId().equals(data.ownerID()) || event.getMember().hasPermission(Permission.MESSAGE_MANAGE))) {
            replySlash(event, "you are not authorized to use this command.", true);
            return;
        }

        OptionMapping channelOpt = event.getOption("channel");
        long channelId = (channelOpt != null) ? channelOpt.getAsChannel().getIdLong() : event.getChannel().getIdLong();
        String channelMention = (channelOpt != null) ? channelOpt.getAsChannel().getAsMention() : event.getChannel().getAsMention();
        int chance = event.getOption("chance").getAsInt();
        long guildId = event.getGuild().getIdLong();

        if (chance < 0 || chance > 100) {
            replySlash(event, "chance must be between 0 and 100.", true);
            return;
        }

        data.database().ensureChannel(channelId, guildId);
        if (data.database().setChannelChances(channelId, chance)) {
            replySlash(event, "channel chances for " + channelMention + " updated to " + chance + "%", true);
        } else {
            replySlash(event, "failed to update channel chances.", true);
        }
    }
}

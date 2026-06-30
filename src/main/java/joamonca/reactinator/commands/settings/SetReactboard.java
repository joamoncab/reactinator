package joamonca.reactinator.commands.settings;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static joamonca.reactinator.util.send.Messages.reply;

public class SetReactboard implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();
        if (!(event.getUser().getId().equals(data.ownerID()) || event.getMember().hasPermission(Permission.MESSAGE_MANAGE))) {
            reply(event, "you are not authorized to use this command.", true);
            return;
        }

        long channelId = event.getOption("channel").getAsChannel().getIdLong();
        int threshold = event.getOption("threshold").getAsInt();
        long guildId = event.getGuild().getIdLong();

        if (threshold <= 0) {
            data.database().removeReactboard(channelId);
            reply(event, "reactboard removed.", true);
        } else {
            data.database().ensureChannel(channelId, guildId);
            data.database().setReactboard(channelId, threshold);
            reply(event, "reactboard set to <#" + channelId + "> with threshold " + threshold + ".", true);
        }
    }
}

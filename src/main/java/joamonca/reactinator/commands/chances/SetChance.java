package joamonca.reactinator.commands.chances;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static joamonca.reactinator.util.send.Messages.reply;

public class SetChance implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();
        if (!(event.getUser().getId().equals(data.ownerID()) || event.getMember().hasPermission(Permission.MESSAGE_MANAGE))) {
            reply(event, "you are not authorized to use this command.", true);
            return;
        }
        int newChance = event.getOption("chance").getAsInt();
        if (newChance < 0 || newChance > 100) {
            reply(event, "chance must be between 0 and 100.", true);
            return;
        }
        if (data.database().setGuildChances(event.getGuild().getIdLong(), newChance)) {
            reply(event, "chances updated to " + newChance + "%", true);
        } else {
            reply(event, "failed to update chances.", true);
        }
    }
}

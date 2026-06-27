package joamonca.reactinator.commands;

import joamonca.reactinator.commands._meta.CommandsList;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.commands._meta.data.CommandObject;
import joamonca.reactinator.util.get.Database;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashDispatcher {
    public SlashDispatcher(SlashCommandInteractionEvent event, Database database, String ownerID, String soundsSource) {
        CommandObject command = CommandsList.getCommand(event.getName());
        if (command == null) {
            event.getInteraction().reply("how did you do this?").setEphemeral(true).queue();
        } else {
            command.command().execute(new CommandDataObject(event, database, ownerID, soundsSource));
        }
    }
}

package joamonca.reactinator.events;

import joamonca.reactinator.commands.SlashDispatcher;
import joamonca.reactinator.util.get.Database;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static joamonca.reactinator.util.send.Messages.replySlash;

public class SlashCommandsHandler extends ListenerAdapter {
    private final Database database;
    private final String authorized;
    private final String soundsSource;

    public SlashCommandsHandler(Database database, String authorized, String soundsSource) {
        this.authorized = authorized;
        this.database = database;
        this.soundsSource = soundsSource;
    }
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (database.isBlacklisted(event.getUser().getIdLong())) {
            replySlash(event, "you are blacklisted from using this bot. what have you done?", true);
            return;
        }
        new SlashDispatcher(event, database, authorized, soundsSource);
    }
}

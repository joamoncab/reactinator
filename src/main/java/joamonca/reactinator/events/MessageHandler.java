package joamonca.reactinator.events;

import joamonca.reactinator.commands.TextDispatcher;
import joamonca.reactinator.commands._meta.CommandsList;
import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.util.send.Reactions;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static joamonca.reactinator.util.process.Cats.isCatChannel;

public class MessageHandler extends ListenerAdapter {
    private final Database database;
    private final String soundsSource;
    private final String slashVersion;

    public MessageHandler(Database database, String soundsSource, String slashVersion) {
        this.database = database;
        this.soundsSource = soundsSource;
        this.slashVersion = slashVersion;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        int guildChance = database.getGuildChances(guildId);

        // no guild records, create all data
        if (guildChance == -1) {
            database.ensureGuild(guildId);
            if (slashVersion != null) {
                database.setSlashVer(guildId, slashVersion);
            }
            updateGuildSlashCommands(event);
            return;
        }

        // update slash commands, if needed (discord is just like that idk)
        if (slashVersion != null) {
            String dbVer = database.getSlashVer(guildId);
            if (dbVer == null || !dbVer.equals(slashVersion)) {
                database.setSlashVer(guildId, slashVersion);
                updateGuildSlashCommands(event);
            }
        }

        // haha blacklisted
        if (database.isBlacklisted(event.getAuthor().getIdLong())) return;

        if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())) {
            if (event.getMessage().getAuthor().equals(event.getJDA().getSelfUser())) return; // ignore self but not other bots (very important for the silly)
            if (event.getMessage().getReferencedMessage() != null && !event.getMessage().getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser())) {
                try {
                    TextDispatcher.useSilly(event, soundsSource);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    TextDispatcher.use(event, database);
                }
            } else {
                TextDispatcher.use(event, database);
            }
        }

        if (database.getOptout(event.getAuthor().getIdLong(), "react")) return;


        int channelChance = database.getChannelChances(event.getChannel().getIdLong());
        int effectiveChance = (channelChance >= 0) ? channelChance : guildChance;

        if (ThreadLocalRandom.current().nextInt(100) >= effectiveChance) {
            return;
        }

        // uses only cat emojis in cat channels (meow)
        if (isCatChannel(event.getChannel().getName())) {
            Reactions.reactWithCats(event, database);
        } else {
            Reactions.react(event, null, database);
        }
    }

    private void updateGuildSlashCommands(MessageReceivedEvent event) {
        event.getGuild().updateCommands().addCommands(
                CommandsList.getCommands().entrySet().stream()
                        .map(entry -> Commands.slash(entry.getKey(), entry.getValue().description())
                                .addOptions(entry.getValue().options()))
                        .toList()
        ).queue();
    }
}

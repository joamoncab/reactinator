package joamonca.reactinator.events;

import joamonca.reactinator.ai.AIHandler;
import joamonca.reactinator.database.ReactDB;
import joamonca.reactinator.reactions.MakeReaciton;
import joamonca.reactinator.util.Slash;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static joamonca.reactinator.util.Cats.getCatEmojis;
import static joamonca.reactinator.util.Cats.isCatChannel;

public class MessageHandler extends ListenerAdapter {
    private final ReactDB reactDB;
    private final String authorized;
    private final String soundsSource;

    public MessageHandler(ReactDB reactDB, String authorized, String soundsSource) {
        this.authorized = authorized;
        this.reactDB = reactDB;
        this.soundsSource = soundsSource;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        // Register slash command (only needs to be done once per guild)
        // discord DOES NOT like it when you do this on every message

        float chance = reactDB.getChances(event.getGuild().getIdLong());

        if (chance == -1f) {
            event.getGuild().updateCommands().addCommands(
                    Commands.slash("setchance", "Set the chance of reacting to a message.")
                            .addOption(OptionType.NUMBER, "chance", "The chance (%) of reacting to a message.", true),
                    Commands.slash("chances", "Get the current chance of reacting to a message.")
            ).queue();
            reactDB.setChances(event.getGuild().getIdLong(), 0.01f); // set default chance to 1%
            return;
        }

        if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())) {
            if (event.getMessage().getAuthor().equals(event.getJDA().getSelfUser())) return; // ignore self but not other bots
            if (event.getMessage().getReferencedMessage() != null && !event.getMessage().getReferencedMessage().getAuthor().equals(event.getJDA().getSelfUser())) {
                try {
                    AIHandler.useSilly(event, soundsSource);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    AIHandler.use(event);
                }
            } else {
                AIHandler.use(event);
            }
        }

        if (ThreadLocalRandom.current().nextFloat() > chance) {
            return;
        }

        // uses only cat emojis in cat channels
        if (isCatChannel(event.getChannel().getName())) {
            List<RichCustomEmoji> emojis = event.getGuild().getEmojis();
            // filter only cat emojis
            MakeReaciton.react(event, getCatEmojis(emojis));
        } else {
            MakeReaciton.react(event, null);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        switch (event.getName()) {
            case "setchance" -> {
                Slash.setChance(event, reactDB, authorized);
            }
            case "chances" -> {
                Slash.getChance(event, reactDB);
            }
        }
    }
}
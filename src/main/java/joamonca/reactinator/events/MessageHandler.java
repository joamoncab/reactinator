package joamonca.reactinator.events;

import joamonca.reactinator.database.ReactDB;
import joamonca.reactinator.util.Slash;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static net.dv8tion.jda.api.entities.Activity.customStatus;

public class MessageHandler extends ListenerAdapter {
    private final Random random = new Random();
    private ReactDB reactDB;
    private String authorized;

    public MessageHandler(ReactDB reactDB, String authorized) {
        this.authorized = authorized;
        this.reactDB = reactDB;
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

        if (random.nextFloat() > chance) {
            return;
        }

        List<RichCustomEmoji> emojis = event.getGuild().getEmojis();
        if (!emojis.isEmpty()) {
            RichCustomEmoji emojiToUse = emojis.get(random.nextInt(emojis.size()));
            event.getMessage().addReaction(emojiToUse).queue();
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
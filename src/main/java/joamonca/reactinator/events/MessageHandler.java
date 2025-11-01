package joamonca.reactinator.events;

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
    private float chance;
    private String authorized;

    public MessageHandler(float chance, String authorized) {
        this.authorized = authorized;
        this.chance = chance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        // Register slash command (only needs to be done once per guild), discord DOES NOT like it when you do this on every message
        /*event.getGuild().updateCommands().addCommands(
                Commands.slash("setchance", "Set the chance of reacting to a message.")
                        .addOption(OptionType.NUMBER, "chance", "The chance (%) of reacting to a message.", true)
        ).queue();*/

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
        if (event.getName().equals("setchance")) {
            if (!event.getUser().getId().equals(authorized)) {
                event.reply("You are not authorized to use this command.").setEphemeral(true).queue();
                return;
            }
            float newChance = (float) event.getOption("chance").getAsDouble() / 100;
            if (newChance < 0 || newChance > 1) {
                event.reply("Chance must be between 0 and 100.").setEphemeral(true).queue();
                return;
            }
            // Note: This change is not persistent and will reset when the bot restarts
            chance = newChance;
            event.reply("Chance updated to " + (newChance * 100) + "%").setEphemeral(true).queue();
            event.getJDA().getSelfUser().getJDA().getPresence().setActivity(
                    Activity.customStatus("behold the reactinator!!! | chances: " + (newChance * 100) + "%")
            );
        }
    }
}
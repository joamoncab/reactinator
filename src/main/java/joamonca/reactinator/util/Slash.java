package joamonca.reactinator.util;

import joamonca.reactinator.database.ReactDB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Slash {
    public static void setChance(SlashCommandInteractionEvent event, ReactDB reactDB, String authorized) {
        if (!(event.getUser().getId().equals(authorized) || event.getMember().hasPermission(Permission.MESSAGE_MANAGE))) {
            event.reply("You are not authorized to use this command.").setEphemeral(true).queue();
            return;
        }
        float newChance = (float) event.getOption("chance").getAsDouble() / 100;
        if (newChance < 0 || newChance > 1) {
            event.reply("Chance must be between 0 and 100.").setEphemeral(true).queue();
            return;
        }
        if (reactDB.setChances(event.getGuild().getIdLong(), newChance)) {
            event.reply("Chances updated to " + (newChance * 100) + "%").setEphemeral(true).queue();
        } else {
            event.reply("Failed to update chances.").setEphemeral(true).queue();
        }
    }

    public static void getChance(SlashCommandInteractionEvent event, ReactDB reactDB) {
        float chance = reactDB.getChances(event.getGuild().getIdLong());
        if (chance >= 0) {
            event.reply("Current chance is " + (chance * 100) + "%").setEphemeral(true).queue();
        } else {
            event.reply("Failed to retrieve chances.").setEphemeral(true).queue();
        }
    }
}

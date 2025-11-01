package joamonca.reactinator;

import joamonca.reactinator.events.MessageHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Main
{
    private static final float CHANCE = 0.01f;

    public static void main(String[] args) {
        // we keep the bot token in an environment variable for security reasons
        String token = System.getenv("TOKEN");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                // Enables MessageReceivedEvent for guild (also known as servers)
                GatewayIntent.GUILD_MESSAGES,
                // Allows access to server emojis and stickers
                GatewayIntent.GUILD_EXPRESSIONS
        );

        try
        {
            // By using createLight(token, intents), we use a minimalistic cache profile (lower ram usage)
            // and only enable the provided set of intents. All other intents are disabled, so you won't receive events for those.
            JDA jda = JDABuilder.createLight(token, intents)
                    // On this builder, you are adding all your event listeners and session configuration
                    .addEventListeners(new MessageHandler(CHANCE))
                    .setActivity(Activity.watching("messages to react"))
                    .setStatus(OnlineStatus.IDLE)
                    .build();

            jda.getRestPing().queue(ping ->
                    // shows ping in milliseconds
                    System.out.println("Logged in with ping: " + ping)
            );

            // If you want to access the cache, you can use awaitReady() to block the main thread until the jda instance is fully loaded
            jda.awaitReady();
        }
        catch (InterruptedException e)
        {
            // Thrown if the awaitReady() call is interrupted
            e.printStackTrace();
        }
    }
}
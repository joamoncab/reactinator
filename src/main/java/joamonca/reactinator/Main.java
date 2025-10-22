package joamonca.reactinator;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.EnumSet;

public class Main extends ListenerAdapter
{
    public static final Emoji EMOJI = Emoji.fromCustom("cb", 1430584363082584278L, false);
    static String targetID;

    public static void main(String[] args) throws IOException
    {
        String token = System.getenv("TOKEN");
        targetID = System.getenv("TARGET_ID");

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
                    .addEventListeners(new Main())
                    .setActivity(Activity.watching("messages to react"))
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

    // This overrides the method called onMessageReceived in the ListenerAdapter class
    // Your IDE (such as intellij or eclipse) can automatically generate this override for you, by simply typing "onMessage" and auto-completing it!
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        // The user who sent the message
        User author = event.getAuthor();

        // Check whether the message was sent in a guild / server
        if (event.isFromGuild())
        {
            // This is a message from a server
            if (author.getId().equals(targetID))
                event.getMessage().addReaction(EMOJI).queue();
        }
    }
}
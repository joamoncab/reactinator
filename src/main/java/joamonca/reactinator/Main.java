package joamonca.reactinator;

import joamonca.reactinator.util.get.Database;
import joamonca.reactinator.events.MessageHandler;
import joamonca.reactinator.events.ReactionHandler;
import joamonca.reactinator.events.SlashCommandsHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class Main
{
    public static void main(String[] args) {
        // we keep the bot token in an environment variable for security reasons
        String token = System.getenv("TOKEN");
        String authorizedUser = System.getenv("AUTHORIZED_USER");
        String dbUri = System.getenv("DB_URI");
        String soundsSource = System.getenv("FETCH_URL");
        String slashVer = System.getenv("SLASH_COMMANDS_VERSION");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        );

        try
        {
            Database database = new Database(dbUri);

            Runtime.getRuntime().addShutdownHook(new Thread(database::close));

            JDA jda = JDABuilder.createLight(token, intents)
                    .enableCache(CacheFlag.EMOJI)
                    .addEventListeners(new MessageHandler(database, soundsSource, slashVer))
                    .addEventListeners(new SlashCommandsHandler(database, authorizedUser, soundsSource))
                    .addEventListeners(new ReactionHandler(database))
                    .setActivity(Activity.customStatus("behold the reactinator!!!"))
                    .setStatus(OnlineStatus.IDLE)
                    .build();

            jda.getRestPing().queue(ping ->
                    System.out.println("Logged in with ping: " + ping)
            );

            jda.awaitReady();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
package joamonca.reactinator.commands._meta;

import joamonca.reactinator.commands.chances.GetChance;
import joamonca.reactinator.commands.chances.SetChance;
import joamonca.reactinator.commands.chances.SetChanceChannel;
import joamonca.reactinator.commands.fun.CheckQuote;
import joamonca.reactinator.commands.fun.Leaderboard;
import joamonca.reactinator.commands.fun.Mpreg;
import joamonca.reactinator.commands.fun.Quotify;
import joamonca.reactinator.commands.fun.ReactLeaderboard;
import joamonca.reactinator.commands.fun.Soundboard;
import joamonca.reactinator.commands.settings.Blacklist;
import joamonca.reactinator.commands.settings.BlacklistEmoji;
import joamonca.reactinator.commands.settings.OptOut;
import joamonca.reactinator.commands.settings.SetReactboard;
import joamonca.reactinator.commands._meta.data.CommandObject;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashMap;
import java.util.Map;

public class CommandsList {
    /*
    * command list: this will store all info related to commands. since it contains dynamic links to java classes, it cannot live on the database
    * */
    public static final Map<String, CommandObject> commands = new HashMap<>();

    static {
        commands.put("chances", new CommandObject(
                "get chances",
                "get the current guild or channel reaction chances",
                new GetChance(),
                new OptionData(OptionType.CHANNEL, "channel", "channel to get chances for", false)
        ));
        commands.put("setchance", new CommandObject(
                "set chances",
                "set the current guild reaction chances",
                new SetChance(),
                new OptionData(OptionType.INTEGER, "chance", "chance (%) of reacting to a message", true)
        ));
        commands.put("setchancechannel", new CommandObject(
                "set channel chances",
                "set per-channel reaction chances (overrides guild default)",
                new SetChanceChannel(),
                new OptionData(OptionType.INTEGER, "chance", "chance (%) of reacting in this channel", true),
                new OptionData(OptionType.CHANNEL, "channel", "channel to set chances for (will take actual channel if none provided)", false)
        ));
        commands.put("mpreg", new CommandObject(
                "mpreginator",
                "put someone's pfp on discord's mpreg emoji",
                new Mpreg(),
                new OptionData(OptionType.MENTIONABLE, "user", "user mention to mpreg", true)
        ));
        commands.put("quotify", new CommandObject(
                "quotify",
                "creates a quote image from a message",
                new Quotify(),
                new OptionData(OptionType.STRING, "message", "the message ID or link to quote", true)
        ));
        commands.put("optout", new CommandObject(
                "opt out",
                "toggle opt-out for bot features",
                new OptOut(),
                new OptionData(OptionType.STRING, "type", "what to opt out of", true)
                        .addChoice("mpreg", "mpreg")
                        .addChoice("react", "react")
                        .addChoice("quote", "quote")
        ));
        commands.put("blacklistemoji", new CommandObject(
                "blacklist emoji",
                "toggle blacklist on a custom emoji",
                new BlacklistEmoji(),
                new OptionData(OptionType.STRING, "emoji", "the custom emoji to blacklist", true)
        ));
        commands.put("reactboard", new CommandObject(
                "reactboard",
                "configure the reactboard channel and threshold",
                new SetReactboard(),
                new OptionData(OptionType.CHANNEL, "channel", "channel to post reactboard messages in", true),
                new OptionData(OptionType.INTEGER, "threshold", "reaction count threshold (0 to disable reactboard)", true)
        ));
        commands.put("blacklist", new CommandObject(
                "blacklist user",
                "toggle blacklist on a user (bot staff only)",
                new Blacklist(),
                new OptionData(OptionType.USER, "user", "user to blacklist", true)
        ));
        commands.put("leaderboard", new CommandObject(
                "leaderboard",
                "show the top emojis by usage in this server",
                new Leaderboard()
        ));
        commands.put("soundboard", new CommandObject(
                "soundboard",
                "play a soundboard effect",
                new Soundboard(),
                new OptionData(OptionType.STRING, "sound", "the soundboard effect or query to play", true)
        ));
        commands.put("reactleaderboard", new CommandObject(
                "reactleaderboard",
                "show the top reacted messages in this server",
                new ReactLeaderboard()
        ));
        commands.put("checkquote", new CommandObject(
                "check quote signature",
                "checks a quote signature to verify its authenticity",
                new CheckQuote(),
                new OptionData(OptionType.INTEGER, "signature", "the signature number from the bottom right of the image", true)
        ));
        commands.put("refreshcommands", new CommandObject(
                "refresh commands",
                "refresh the slash commands for this guild (bot staff only)",
                new RefreshCommands()
        ));
    }

    public static CommandObject getCommand(String commandName) {
        return commands.get(commandName);
    }
    public static  Map<String, CommandObject> getCommands() {
        return commands;
    }
}

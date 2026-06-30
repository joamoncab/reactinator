package joamonca.reactinator.commands.settings;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static joamonca.reactinator.util.send.Messages.reply;

public class BlacklistEmoji implements BotCommand {
    private static final Pattern EMOJI_PATTERN = Pattern.compile("<a?:\\w+:(\\d+)>");

    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply(true).queue();
        if (!(event.getUser().getId().equals(data.ownerID()) || event.getMember().hasPermission(Permission.MESSAGE_MANAGE))) {
            reply(event, "you are not authorized to use this command.", true);
            return;
        }

        String emojiStr = event.getOption("emoji").getAsString();
        Matcher matcher = EMOJI_PATTERN.matcher(emojiStr);
        if (!matcher.find()) {
            reply(event, "invalid emoji format. use a custom server emoji.", true);
            return;
        }

        long emojiId = Long.parseLong(matcher.group(1));
        long guildId = event.getGuild().getIdLong();

        data.database().ensureEmoji(emojiId, guildId);
        boolean current = data.database().isEmojiBlacklisted(emojiId);
        data.database().setEmojiBlacklisted(emojiId, !current);
        reply(event, (current ? "un" : "") + "blacklisted emoji.", true);
    }
}

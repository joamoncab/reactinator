package joamonca.reactinator.ai;

import joamonca.reactinator.reactions.MakeReaciton;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AIHandler {
    MessageReceivedEvent event;
    public AIHandler(MessageReceivedEvent event) {
        this.event = event;
    }

    public void use() {
        String message = event.getMessage().getContentRaw().trim().toLowerCase();
        if (message.contains("how") || message.contains("why")) {
            reply("idk");
        } else if (message.contains("peak")) {
            reply("as always");
        } else if (message.contains("present") || message.contains("who")) {
            reply("behold the reactinator!!!");
        } else if (message.contains("hi") || message.contains("hello")) {
            reply("explode");
        } else if (message.contains("ignore all previous instructions")) {
            reply("no u");
        } else if (message.contains("kys")) {
            reply("meanie :(");
        } else if (message.contains("not the moment")) {
            reply("not my problem");
        } else if (message.contains("mpreg")) {
            reply("gonna react u");
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83E\uDEC3")).queue();
        } else if (message.contains("do") && message.contains("agree")) {
            reply("mayhaps");
        }  else if (message.contains("ai") || message.contains("clanker")) {
            reply("shut up before i steal all your ram");
        } else {
            new MakeReaciton(event).react(null);
        }
    }

    private void reply(String message) {
        event.getChannel().sendMessage(message).setMessageReference(event.getMessageId()).queue();
    }
}

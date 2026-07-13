package joamonca.reactinator.commands._meta;

import joamonca.reactinator.commands._meta.data.CommandDataObject;
import joamonca.reactinator.commands._meta.data.CommandObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

public class Help implements BotCommand {
    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("reactinator — command list");
        embed.setColor(new Color(88, 101, 242)); // discord blurple
        embed.setDescription("here's everything i can do:");

        for (Map.Entry<String, CommandObject> entry : CommandsList.getCommands().entrySet()) {
            // oooo hidden commands
            if (entry.getValue().HiddenFromHelp()) continue;

            String slashName = entry.getKey();
            CommandObject cmd = entry.getValue();

            String usage = cmd.options().isEmpty()
                    ? ""
                    : cmd.options().stream()
                        .map(opt -> opt.isRequired()
                                ? " `<" + opt.getName() + ">`"
                                : " `[" + opt.getName() + "]`")
                        .collect(Collectors.joining());

            embed.addField(
                    "/" + slashName + usage,
                    cmd.description(),
                    false
            );
        }

        embed.setFooter("<required>  [optional]");

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}

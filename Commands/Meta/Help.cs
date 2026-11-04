using NetCord;
using NetCord.Rest;

namespace Reactinator.Commands.Meta;

public class Help : IBotCommand
{
    public Task Execute(CommandContext context)
    {
        var fields = CommandsList.Commands
            .Where(entry => !entry.Value.HiddenFromHelp) // oooo hidden commands
            .OrderBy(entry => entry.Key)
            .Select(entry => new EmbedFieldProperties
            {
                Name = "/" + entry.Key + string.Concat(entry.Value.Options.Select(option =>
                    option.Required == true ? $" `<{option.Name}>`" : $" `[{option.Name}]`")),
                Value = entry.Value.Description,
            });

        var embed = new EmbedProperties
        {
            Title = "reactinator — command list",
            Color = new Color(88, 101, 242), // discord blurple
            Description = "here's everything i can do:",
            Fields = fields,
            Footer = new EmbedFooterProperties { Text = "<required>  [optional]" },
        };

        return context.Respond(new InteractionMessageProperties { Embeds = [embed] }, ephemeral: true);
    }
}

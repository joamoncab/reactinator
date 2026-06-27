package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import static joamonca.reactinator.util.process.Image.createCircularCrop;
import static joamonca.reactinator.util.process.Image.encodeImage;
import static joamonca.reactinator.util.send.Messages.replySlash;

public class Mpreg implements BotCommand {
    private static final int AVATAR_X = 253;
    private static final int AVATAR_Y = 15;
    private static final int AVATAR_SIZE = 450;
    private static final int AVATAR_DOWNLOAD_SIZE = 512;
    private static final String TEMPLATE_PATH = "/assets/mpreg.png";

    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();
        event.deferReply().queue();

        User user = event.getOption("user").getAsUser();

        // Respect mpreg opt-out
        if (data.database().getOptout(user.getIdLong(), "mpreg")) {
            event.getHook().sendMessage("that user has opted out of mpreg.").setEphemeral(true).queue();
            return;
        }

        String avatarUrl = user.getEffectiveAvatar().getUrl(AVATAR_DOWNLOAD_SIZE);

        try (InputStream templateStream = Mpreg.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                replySlash(event, "hey so i lost the mpreg", false); // honestly too funny to not be seen
                return;
            }

            BufferedImage template = ImageIO.read(templateStream);
            BufferedImage avatar = ImageIO.read(new URI(avatarUrl).toURL());
            BufferedImage circularAvatar = createCircularCrop(avatar);

            compositeAvatar(template, circularAvatar);

            byte[] imageBytes = encodeImage(template);
            event.getHook().sendFiles(FileUpload.fromData(imageBytes, "mpreg_result.png")).queue();

        } catch (Exception e) {
            Logger.getLogger(Mpreg.class.getName()).log(Level.SEVERE, "Failed to process mpreg image", e);
            replySlash(event, "uh oh something went wrong", false);
        }
    }

    private static void compositeAvatar(BufferedImage template, BufferedImage avatar) {
        Graphics2D g = template.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(avatar, AVATAR_X, AVATAR_Y, AVATAR_SIZE, AVATAR_SIZE, null);
        } finally {
            g.dispose();
        }
    }


}

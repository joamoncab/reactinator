package joamonca.reactinator.commands.fun;

import joamonca.reactinator.commands._meta.BotCommand;
import joamonca.reactinator.commands._meta.data.CommandDataObject;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static joamonca.reactinator.util.process.Image.createCircularCrop;
import static joamonca.reactinator.util.process.Image.encodeImage;
import static joamonca.reactinator.util.send.Messages.reply;

public class Quotify implements BotCommand {
    private static final int AVATAR_DOWNLOAD_SIZE = 256;

    @Override
    public void execute(CommandDataObject data) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) data.event();

        String messageInput = event.getOption("message").getAsString();
        TargetMessageRef ref = parseMessageInput(event, messageInput);

        if (ref == null) {
            event.reply("invalid message ID or link format. what did you put in there?").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        MessageChannel channel = event.getGuild().getChannelById(GuildMessageChannel.class, ref.channelId);
        if (channel == null) {
            channel = event.getChannel();
        }

        channel.retrieveMessageById(ref.messageId).queue(
                message -> {
                    // Respect quote opt-out
                    if (data.database().getOptout(message.getAuthor().getIdLong(), "quote")) {
                        reply(event, "that user has opted out of being quoted.", false);
                        return;
                    }
                    try {
                        long signature = data.database().insertQuoteTamper(
                                event.getUser().getIdLong(),
                                message.getIdLong(),
                                message.getChannel().getIdLong(),
                                message.getGuild().getIdLong()
                        );
                        if (signature == -1) {
                            reply(event, "failed to generate a signature for this quote (SIG# watermark)", false);
                            return;
                        }
                        generateQuoteImage(event, message, signature);
                    } catch (Exception e) {
                        reply(event, "failed to process the quote card image, blaming you for this btw", false);
                    }
                },
                failure -> reply(event, "could not find or retrieve the specified message. trying to hack someone or something?", false)
        );
    }

    private void generateQuoteImage(SlashCommandInteractionEvent event, Message message, long signature) throws IOException {
        User author = message.getAuthor();
        String avatarUrl = author.getEffectiveAvatar().getUrl(AVATAR_DOWNLOAD_SIZE);

        BufferedImage image = new BufferedImage(900, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // stupit antialiasing does not work
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(28, 28, 33),
                900, 300, new Color(14, 14, 16)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, 900, 300);

        g.setColor(new Color(255, 255, 255, 15));
        g.setStroke(new BasicStroke(2));
        g.drawRect(5, 5, 890, 290);

        BufferedImage avatar = null;
        try {
            avatar = ImageIO.read(new URI(avatarUrl).toURL());
        } catch (Exception e) {
            e.printStackTrace(); // logging should be more robust blah blah blah
        }

        if (avatar != null) {
            BufferedImage circularAvatar = createCircularCrop(avatar);
            g.drawImage(circularAvatar, 50, 70, 160, 160, null);
        } else {
            g.setColor(new Color(60, 60, 65));
            g.fillOval(50, 70, 160, 160);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 72));
            String initial = author.getName().substring(0, 1).toUpperCase();
            FontMetrics initialMetrics = g.getFontMetrics();
            int initialX = 50 + (160 - initialMetrics.stringWidth(initial)) / 2;
            int initialY = 70 + ((160 - initialMetrics.getHeight()) / 2) + initialMetrics.getAscent();
            g.drawString(initial, initialX, initialY);
        }

        // avatar ring
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 255, 255, 50));
        g.drawOval(50, 70, 160, 160);

        // text, todo process something more than plain messages (emojis, attachments and whatnot)
        String rawText = message.getContentRaw();
        String quoteText = !rawText.isBlank() ? rawText.strip() : "[Media/Attachment]";
        if (!quoteText.startsWith("“") && !quoteText.startsWith("\"")) {
            quoteText = "“" + quoteText + "”";
        }

        String authorName = message.getMember() != null ? message.getMember().getEffectiveName() : author.getEffectiveName();
        int year = message.getTimeCreated().getYear();
        String authorLine = "— " + authorName + ", " + year;

        int maxTextWidth = 590;
        int spacing = 20;

        Font authorFont = new Font("SansSerif", Font.PLAIN, 20);
        g.setFont(authorFont);
        FontMetrics authorMetrics = g.getFontMetrics();
        int authorHeight = authorMetrics.getHeight();

        int fontSize = 28;
        List<String> lines = null;
        FontMetrics quoteMetrics = null;
        int quoteLineHeight = 0;
        int totalHeight = 0;

        while (fontSize >= 14) {
            Font quoteFont = new Font("Georgia", Font.ITALIC, fontSize);
            g.setFont(quoteFont);
            quoteMetrics = g.getFontMetrics();
            lines = wrapText(quoteText, quoteMetrics, maxTextWidth);
            quoteLineHeight = quoteMetrics.getHeight();
            totalHeight = (lines.size() * quoteLineHeight) + spacing + authorHeight;
            if (totalHeight <= 260) {
                break;
            }
            fontSize -= 2;
        }

        int startY = (300 - totalHeight) / 2;
        g.setFont(new Font("Georgia", Font.ITALIC, fontSize)); // oooo fancy font
        g.setColor(new Color(240, 240, 245));
        int currentY = startY + quoteMetrics.getAscent();
        for (String line : lines) {
            g.drawString(line, 260, currentY);
            currentY += quoteLineHeight;
        }

        g.setFont(authorFont);
        g.setColor(new Color(160, 160, 165));
        currentY += spacing - quoteLineHeight + authorMetrics.getAscent();
        g.drawString(authorLine, 260, currentY);

        String sigText = "SIG #" + signature;
        Font sigFont = new Font("SansSerif", Font.PLAIN, 12);
        g.setFont(sigFont);
        g.setColor(new Color(255, 255, 255, 60));
        FontMetrics sigMetrics = g.getFontMetrics(sigFont);
        int sigWidth = sigMetrics.stringWidth(sigText);
        int sigX = 890 - sigWidth;
        int sigY = 290 - sigMetrics.getDescent();
        g.drawString(sigText, sigX, sigY);

        g.dispose(); // we don't do memleaks here (voluntarily)

        byte[] imageBytes = encodeImage(image);
        event.getHook().sendFiles(FileUpload.fromData(imageBytes, "quote_result.png")).queue();
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.isEmpty()) {
                currentLine.append(word);
            } else {
                String testLine = currentLine + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    currentLine.append(" ").append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private TargetMessageRef parseMessageInput(SlashCommandInteractionEvent event, String input) {
        if (input == null) return null;
        input = input.trim();

        if (input.matches("\\d+")) {
            try {
                long msgId = Long.parseLong(input);
                return new TargetMessageRef(event.getChannel().getIdLong(), msgId);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (input.contains("/channels/")) {
            String[] parts = input.split("/");
            if (parts.length >= 3) {
                try {
                    String msgStr = parts[parts.length - 1];
                    String chanStr = parts[parts.length - 2];
                    long msgId = Long.parseLong(msgStr);
                    long chanId = Long.parseLong(chanStr);
                    return new TargetMessageRef(chanId, msgId);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    private record TargetMessageRef(long channelId, long messageId) {
    }
}

package joamonca.reactinator.util.process;

public class Text {
    public static String stripMentions(String raw) {
        return raw.replaceAll("<@[!&]?\\d+>|<#\\d+>", "").trim();
    }
}

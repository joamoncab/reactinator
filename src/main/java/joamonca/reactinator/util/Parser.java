package joamonca.reactinator.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private Document doc;
    public Parser(String url) throws IOException {
        doc = Jsoup.connect(url).get();
    }

    public String getMediaUrl() {
        Element button = doc.selectFirst("button.webshare");

        if (button != null) {
            String onclick = button.attr("onclick");

            Pattern pattern = Pattern.compile("'([^']+\\.mp3)'");
            Matcher matcher = pattern.matcher(onclick);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}

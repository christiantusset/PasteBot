package net.notfab.pastebot.scrapers;

import net.notfab.pastebot.PasteScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4Scraper extends PasteScraper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public IPv4Scraper() {
        super("IPv4");
    }

    private Pattern ipv4Pattern = Pattern.compile("^([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
            "(?<!172\\.(16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31))(?<!127)(?<!^10)(?<!^0)\\." +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?<!192\\.168)(?<!172\\.(16|17|18|19|20|" +
            "21|22|23|24|25|26|27|28|29|30|31))\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?<!\\.255$)$");

    /**
     * Searches for IPv4 addresses in the paste.
     * @param content - The paste content.
     * @param url - The paste url.
     * @return if we should process other rules.
     */
    @Override
    public Boolean apply(String content, String url) {
        Matcher matcher = ipv4Pattern.matcher(content);
        if(matcher.find()) {
            logger.info(matcher.group(0) + " - " + url);
            return !this.savePaste(content, url, matcher.group(0));
        }
        return true;
    }

}
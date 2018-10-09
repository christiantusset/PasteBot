package net.notfab.pastebot.scrapers;

import net.notfab.pastebot.PasteScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordScraper extends PasteScraper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public KeywordScraper() {
        super("Keyword");
    }

    private String[] keywords = new String[]{"udemy", "steam", "password", "login", "username", "netflix", "spotify",
            "#extm3u", "jdbc", ".amazonaws", "senha", "crunchyroll", "ubisoft", "humblebundle", "nuuvem"};

    /**
     * Searches for keywords in the paste.
     * @param content - The paste content.
     * @param url - The paste url.
     * @return if we should process other rules.
     */
    @Override
    public Boolean apply(String content, String url) {
        for(String key: keywords) {
            if(content.toLowerCase().contains(key)) {
                logger.info(key + " - " + url);
                return !this.savePaste(content, url, key);
            }
        }
        return true;
    }

}

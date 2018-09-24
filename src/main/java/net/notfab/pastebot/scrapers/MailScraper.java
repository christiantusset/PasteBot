package net.notfab.pastebot.scrapers;

import net.notfab.pastebot.PasteScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailScraper extends PasteScraper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MailScraper() {
        super("Mail");
    }

    private final Pattern emailPattern = Pattern.compile("((?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}" +
            "~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\" +
            "[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)" +
            "+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
            "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b" +
            "\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)]))");

    /**
     * Searches for email addresses in the paste.
     * @param content - The paste content.
     * @param url - The paste url.
     * @return if we should process other rules.
     */
    @Override
    public Boolean apply(String content, String url) {
        Matcher matcher = emailPattern.matcher(content);
        if(matcher.find()) {
            logger.info(matcher.group(1) + " - " + url);
            return !this.savePaste(content, url, matcher.group(1));
        }
        return true;
    }

}
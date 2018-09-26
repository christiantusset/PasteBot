package net.notfab.pastebot;

import net.notfab.pastebot.scrapers.IPv4Scraper;
import net.notfab.pastebot.scrapers.KeywordScraper;
import net.notfab.pastebot.scrapers.MailScraper;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PasteBot {

    private List<PasteScraper> consumers = new ArrayList<>();
    private Deque<String> toCheck = new ArrayDeque<>();
    private List<String> used = new ArrayList<>();
    private ProxyFetcher fetcher = new ProxyFetcher();
    private ScheduledExecutorService service = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(22);
    private Logger logger = LoggerFactory.getLogger(PasteBot.class);
    private Long lastRun = System.currentTimeMillis();

    private int success = 0;
    private int failed = 0;

    public PasteBot() {
        this.consumers.add(new IPv4Scraper());
        this.consumers.add(new MailScraper());
        this.consumers.add(new KeywordScraper());
        this.toCheck.add("https://pastebin.com/B5XQiMPM");
        this.service.scheduleAtFixedRate(() -> {
            if((System.currentTimeMillis() - lastRun) >= TimeUnit.SECONDS.toMillis(15)) {
                this.schedule();
                logger.warn("[Metrics] Detected dead task, starting again...");
            }
            logger.info("[Metrics] Success = " + success + " / Failed = " + failed + " / Queue = " + toCheck.size());
        }, 1, 5, TimeUnit.SECONDS);
        this.schedule();
    }

    private void schedule() {
        service.scheduleAtFixedRate(() -> {
            lastRun = System.currentTimeMillis();
            String url = toCheck.pollFirst();
            if (used.contains(url)) return;
            try {
                if(url == null) {
                    Thread.sleep(1000);
                    return;
                }
                boolean result = this.scan(url);
                if (result) {
                    this.used.add(url);
                    success++;
                } else {
                    this.toCheck.addLast(url);
                    failed++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private boolean scan(String url) {
        Proxy proxy = fetcher.next();
        try {
            if (proxy == null) {
                return false;
            }
            Document document = Jsoup.connect(url)
                    .proxy(proxy)
                    .validateTLSCertificates(false)
                    .timeout(3000)
                    .header("User-Agent", fetcher.nextUserAgent()).get();
            if (document == null)
                return true;
            List<String> others = extractLinks(document);
            this.toCheck.addAll(others);
            // -- Parse text
            Element content = document.getElementById("paste_code");
            if(content == null) return true;
            String string = content.text();
            consumers.forEach(consumer -> {
                try {
                    consumer.apply(string, url);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            return true;
        } catch (HttpStatusException exception) {
            if (exception.getStatusCode() == 403) {
                fetcher.remove(proxy, 0);
            } else {
                fetcher.remove(proxy, 2);
            }
            return false;
        } catch (SocketException | SocketTimeoutException exception) {
            fetcher.remove(proxy, 1);
            return false;
        } catch (IOException e) {
            if (e.getMessage().contains("too many")) {
                fetcher.remove(proxy, 0);
            } else {
                fetcher.remove(proxy, 2);
            }
            return false;
        }
    }

    private List<String> extractLinks(Document document) {
        List<String> urls = new ArrayList<>();
        Element menu_2 = document.getElementById("menu_2");
        for (Element a : menu_2.getElementsByTag("a")) {
            String url = "https://pastebin.com" + a.attr("href");
            if (!this.used.contains(url) && !this.toCheck.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

}
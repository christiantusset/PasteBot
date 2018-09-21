package net.notfab.pastebot;

import net.notfab.pastebot.consumers.ComboConsumer;
import net.notfab.pastebot.consumers.EmailConsumer;
import net.notfab.pastebot.consumers.IPv4Consumer;
import net.notfab.pastebot.consumers.KeywordConsumer;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class PasteBot {

    private List<BiConsumer<String, String>> consumers = new ArrayList<>();
    private Deque<String> toCheck = new ArrayDeque<>();
    private List<String> used = new ArrayList<>();
    private ProxyFetcher fetcher = new ProxyFetcher();

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    int success = 0;
    int failed = 0;

    public PasteBot() {
        this.consumers.add(new KeywordConsumer());
        this.consumers.add(new EmailConsumer());
        this.consumers.add(new ComboConsumer());
        this.consumers.add(new IPv4Consumer());

        this.toCheck.add("https://pastebin.com/B5XQiMPM");

        service.scheduleAtFixedRate(() -> {
            System.out.println("[Metrics] Success = " + success + " / Failed = " + failed + " / Queue = " + toCheck.size());
        }, 1, 5, TimeUnit.SECONDS);
        service.scheduleAtFixedRate(() -> {
            String url = toCheck.pollFirst();
            if (used.contains(url)) return;
            try {
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

    private boolean scan(String url) throws InterruptedException {
        Proxy proxy = fetcher.next();
        try {
            if (proxy == null) {
                // Readd \/
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
            if (others.isEmpty()) {
                Thread.sleep(10000);
            }
            this.toCheck.addAll(others);
            // -- Parse text
            Element content = document.getElementById("paste_code");
            String string = content.text();
            consumers.forEach(consumer -> {
                try {
                    consumer.accept(string, url);
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
            if(e.getMessage().contains("too many")) {
                fetcher.remove(proxy, 0);
            } else {
                fetcher.remove(proxy, 2);
            }
            System.out.println("Failed to parse paste " + url + " - " + e.getMessage());
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
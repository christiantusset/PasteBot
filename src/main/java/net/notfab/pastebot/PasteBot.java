package net.notfab.pastebot;

import net.notfab.pastebot.consumers.ComboConsumer;
import net.notfab.pastebot.consumers.EmailConsumer;
import net.notfab.pastebot.consumers.SpecialConsumer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
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

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    int total = 0;
    int failed = 0;

    public PasteBot() {
        this.consumers.add(new SpecialConsumer());
        this.consumers.add(new EmailConsumer());
        this.consumers.add(new ComboConsumer());

        this.toCheck.add("https://pastebin.com/x7mBgCsf");

        service.scheduleAtFixedRate(() -> {
            System.out.println("[Metrics] Total = " + total + " / Failed = " + failed + " / Queue = " + toCheck.size());
        }, 1, 5, TimeUnit.SECONDS);
        service.scheduleAtFixedRate(() -> {
            String url = toCheck.poll();
            if (used.contains(url)) return;
            try {
                boolean result = this.scan(url);
                if (!result) {
                    Thread.sleep(10000);
                } else {
                    total++;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            failed++;
        }, 1, 1, TimeUnit.SECONDS);
    }

    private boolean scan(String url) throws InterruptedException {
        try {
            Document document = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0").get();
            if (document == null)
                return false;
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
        } catch (IOException e) {
            System.out.println("Failed to parse paste " + url + " - " + e.getMessage());
            return false;
        }
    }

    private List<String> extractLinks(Document document) {
        List<String> urls = new ArrayList<>();
        Element menu_2 = document.getElementById("menu_2");
        for (Element a : menu_2.getElementsByTag("a")) {
            String url = "https://pastebin.com" + a.attr("href");
            if (!this.used.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

}
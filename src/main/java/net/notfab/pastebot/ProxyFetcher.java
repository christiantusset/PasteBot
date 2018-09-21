package net.notfab.pastebot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProxyFetcher {

    private List<Proxy> working = new ArrayList<>();
    private List<String> userAgent = new ArrayList<>();
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private int current = 0;

    public ProxyFetcher() {
        try {
            File file = new File("proxy.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            reader.lines().forEach(line -> {
                String ip = line.split(":")[0];
                int port = Integer.parseInt(line.split(":")[1]);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
                working.add(proxy);
            });
            reader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // --
        try {
            File file = new File("user-agent.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            reader.lines().forEach(userAgent::add);
            reader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // --
        service.scheduleAtFixedRate(() -> {
            int current = working.size();
            System.out.println("[ProxyFetcher] Adding some proxies...");
            this.working.addAll(getSomeProxies());
            System.out.println("[ProxyFetcher] Added " + (working.size() - current) + " proxies.");
        }, 5, 10, TimeUnit.MINUTES);
    }

    public String nextUserAgent() {
        return this.userAgent.get(new Random().nextInt(this.userAgent.size()));
    }

    public Proxy next() {
        current++;
        if(this.working.isEmpty()) return null;
        if (this.working.size() <= current)
            current = 0;
        return this.working.get(current);
    }

    public void remove(Proxy proxy, int reason) {
        if (reason == 0) {
            // HTTP 403 (Banned)
            System.out.println("[Proxy] Banned " + proxy.toString());
        } else if (reason == 1) {
            // Timed Out
            System.out.println("[Proxy] Offline " + proxy.toString());
            this.working.remove(proxy);
        } else {
            // Unknown (PKIX?)
            this.working.remove(proxy);
        }
    }

    public void saveProxies() {
        List<String> list = new ArrayList<>();
        working.forEach(x -> {
            String ip = ((InetSocketAddress)x.address()).getAddress().getHostAddress();
            String port = ((InetSocketAddress)x.address()).getPort() + "";
            list.add(ip + ":" + port);
        });
        try {
            FileWriter writer = new FileWriter("proxy-working.txt");
            list.forEach(x -> {
                try {
                    writer.write(x + "\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Proxy> getSomeProxies() {
        List<Proxy> allFound = new ArrayList<>();
        allFound.addAll(getFromHideMyNA());
        allFound.addAll(getFromSSLProxies());
        return allFound;
    }

    private List<Proxy> getFromSSLProxies() {
        List<Proxy> proxies = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://www.sslproxies.org")
                    .header("User-Agent", this.nextUserAgent())
                    .get();
            Element tbody = document.getElementsByTag("tbody").get(0);
            tbody.getElementsByTag("tr").forEach(tr -> {
                try {
                    String ip = tr.getElementsByTag("td").get(0).text();
                    String port = tr.getElementsByTag("td").get(1).text();
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port)));
                    proxies.add(proxy);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
        return proxies;
    }

    private List<Proxy> getFromHideMyNA() {
        List<Proxy> proxies = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://hidemyna.me/en/proxy-list/")
                    .header("User-Agent", this.nextUserAgent())
                    .get();
            Elements proxy__t = document.getElementsByClass("proxy__t");
            Element tbody = null;
            for (Element element : proxy__t) {
                Elements tbodies = element.getElementsByTag("tbody");
                if(!tbodies.isEmpty()) {
                    tbody = tbodies.get(0);
                }
            }
            if(tbody == null) return new ArrayList<>();
            tbody.getElementsByTag("tr").forEach(tr -> {
                try {
                    String ip = tr.getElementsByTag("td").get(0).text();
                    String port = tr.getElementsByTag("td").get(1).text();
                    String type = tr.getElementsByTag("td").get(4).text();
                    Proxy.Type typex = type.toLowerCase().contains("sock") ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                    Proxy proxy = new Proxy(typex, new InetSocketAddress(ip, Integer.parseInt(port)));
                    proxies.add(proxy);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
        return proxies;
    }

}
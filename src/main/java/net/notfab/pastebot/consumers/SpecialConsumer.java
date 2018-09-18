package net.notfab.pastebot.consumers;

import java.util.function.BiConsumer;

public class SpecialConsumer implements BiConsumer<String, String> {

    @Override
    public void accept(String string, String url) {
        if(string.toLowerCase().contains("netflix")) {
            System.out.println("[Special] Netflix @ " + url);
        } else if(string.toLowerCase().contains("spotify")) {
            System.out.println("[Special] Spotify @ " + url);
        } else if(string.toLowerCase().contains("steam")) {
            System.out.println("[Special] Steam @ " + url);
        } else if(string.toLowerCase().contains("#extm3u")) {
            System.out.println("[Special] TV @ " + url);
        }
    }

}
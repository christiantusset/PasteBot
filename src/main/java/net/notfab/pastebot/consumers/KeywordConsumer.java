package net.notfab.pastebot.consumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiConsumer;

public class KeywordConsumer implements BiConsumer<String, String> {

    private String[] keywords = new String[]{"udemy", "steam", "password", "login", "username", "netflix", "spotify",
    "#extm3u", "jdbc", ".amazonaws", "senha"};

    @Override
    public void accept(String string, String url) {
        for(String key: keywords) {
            if(string.toLowerCase().contains(key)) {
                try {
                    FileWriter fileWriter = new FileWriter(new File("Keywords.txt"), true);
                    fileWriter.write(key.toUpperCase() + " - " + url + "\n");
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[Keyword] " + key + " - " + url);
            }
        }
    }

}

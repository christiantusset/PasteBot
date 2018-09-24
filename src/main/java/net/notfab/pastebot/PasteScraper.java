package net.notfab.pastebot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiFunction;

public abstract class PasteScraper implements BiFunction<String, String, Boolean> {

    private String name;

    public PasteScraper(String name) {
        this.name = name;
    }

    private String getName() {
        return this.name;
    }

    protected boolean savePaste(String content, String url, String notes) {
        try {
            String id = url.substring(url.lastIndexOf('/'));
            File file = new File("scraper/" + getName() + "/" + id + ".txt");
            FileWriter writer = new FileWriter(file);
            writer.write("-----------------------------------------------------------");
            writer.write("\n");
            writer.write("URL: " + url);
            writer.write("\n");
            writer.write("Notes: " + notes);
            writer.write("\n");
            writer.write("-----------------------------------------------------------");
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
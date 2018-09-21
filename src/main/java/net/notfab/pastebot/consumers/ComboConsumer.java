package net.notfab.pastebot.consumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComboConsumer implements BiConsumer<String, String> {

    private Pattern pattern = Pattern.compile("((?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{" +
            "|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\" +
            "[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]" +
            "(?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4]" +
            "[0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]" +
            "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)]):(\\w+))");

    @Override
    public void accept(String string, String url) {
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            try {
                FileWriter fileWriter = new FileWriter(new File("ComboConsumer.txt"), true);
                fileWriter.write(matcher.group(1) + ":" + matcher.group(2) + " - " + url + "\n");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[Combo] Found password combo -> " + matcher.group(1) + ":" + matcher.group(2) + " @ " + url);
        }
    }

}

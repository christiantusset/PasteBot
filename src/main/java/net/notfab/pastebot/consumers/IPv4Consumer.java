package net.notfab.pastebot.consumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4Consumer implements BiConsumer<String, String> {

    private Pattern ipv4Pattern = Pattern.compile("^([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])" +
            "(?<!172\\.(16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31))(?<!127)(?<!^10)(?<!^0)\\." +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?<!192\\.168)(?<!172\\.(16|17|18|19|20|" +
            "21|22|23|24|25|26|27|28|29|30|31))\\.([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\." +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?<!\\.255$)$");

    @Override
    public void accept(String string, String url) {
        Matcher matcher = ipv4Pattern.matcher(string);
        if(matcher.find()) {
            try {
                FileWriter fileWriter = new FileWriter(new File("IPs.txt"), true);
                fileWriter.write(matcher.group(0) + " - " + url + "\n");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[IPv4] " + matcher.group(0) + " - " + url);
        }
    }

}

package me.chrommob.baritoneremover.webhook;

import me.chrommob.baritoneremover.config.ConfigManager;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Sender {
    private final ConfigManager configManager;
    private String url;
    private boolean enabled;

    public Sender(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void load() {
        ConfigManager configManager = ConfigManager.getInstance();
        url = configManager.webHookUrl();
        enabled = configManager.webHookEnabled();
    }

    public void send(String message) {
        if (!enabled) return;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.getOutputStream().write(message.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.getInputStream().close();
            connection.disconnect();

            Map<String, List<String>> map = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println("Key : " + entry.getKey() +
                        " ,Value : " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package me.chrommob.baritoneremover.webhook;

import me.chrommob.baritoneremover.config.ConfigManager;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.*;

public class Sender {
    private final ConfigManager configManager;
    private String url;
    private boolean enabled;
    private int taskID = -1;

    public Sender(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void load() {
        url = configManager.webHookUrl();
        enabled = configManager.webHookEnabled();
        if (enabled) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(configManager.plugin(), this::sendMessages, 20, 20);
        } else {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    private int remaining = -1;
    private long remainingReset = 0;
    private final List<WebHookMessage> priorityMessages = new ArrayList<>();
    private final List<WebHookMessage> messages = new ArrayList<>();
    private final Map<String, Long> playerLastMessage = new HashMap<>();
    public void add(String message, String username, boolean priority) {
        if (!enabled) return;
        if (priority) {
            priorityMessages.add(new WebHookMessage(message, username));
        } else {
            messages.add(new WebHookMessage(message, username));
        }
    }

    private void send(WebHookMessage message) {
        playerLastMessage.put(message.username, System.currentTimeMillis());
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.getOutputStream().write(message.content.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.getInputStream().close();
            connection.disconnect();

            Map<String, List<String>> map = connection.getHeaderFields();
            if (map.containsKey("X-RateLimit-Remaining"))
                remaining = Integer.parseInt(map.get("X-RateLimit-Remaining").get(0));
            else remaining = -1;
            if (map.containsKey("X-RateLimit-Reset"))
                remainingReset = Long.parseLong(map.get("X-RateLimit-Reset").get(0)) * 1000;
            else remainingReset = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        //Create list of usernames that have queued messages and have not sent a message at all or in the last time
        List<WebHookMessage> priorityMessages = new ArrayList<>(this.priorityMessages);

        List<WebHookMessage> toSend = new ArrayList<>(priorityMessages);

        this.priorityMessages.removeAll(priorityMessages);
        List<WebHookMessage> messages = new ArrayList<>(this.messages);
        for (WebHookMessage message : messages) {
            if (playerLastMessage.containsKey(message.username)) {
                continue;
            }
            toSend.add(message);
            this.messages.remove(message);
        }
        messages = new ArrayList<>(this.messages);
        messages.sort(Comparator.comparingLong(o -> playerLastMessage.get(o.username)));
        toSend.addAll(messages);
        this.messages.removeAll(messages);

        for (WebHookMessage message : toSend) {
            if (remaining == 0 && remainingReset > System.currentTimeMillis()) {
                continue;
            }
            send(message);
        }
    }
}

class WebHookMessage {
    public final String content;
    public final String username;
    public final long timestamp = System.currentTimeMillis();
    public WebHookMessage(String content, String username) {
        this.content = content;
        this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WebHookMessage)) return false;
        WebHookMessage message = (WebHookMessage) obj;
        return message.content.equals(content) && message.username.equals(username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, username);
    }
}
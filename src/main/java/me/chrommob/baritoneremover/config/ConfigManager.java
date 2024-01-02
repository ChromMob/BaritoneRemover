package me.chrommob.baritoneremover.config;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.webhook.Sender;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfigManager {
    private final File configFile;
    private File debugFile;
    private final Yaml yaml;
    private static ConfigManager instance;
    private final Checks checks;
    private final Sender sender = new Sender(this);
    private final Map<Class<? extends Check>, ConfigData> configDataMap = new HashMap<>();
    private Component prefix;
    private double minTps;
    private boolean webHookEnabled;
    private String webHookUrl;
    private LinkedHashMap<String, Object> config;
    private final BukkitAudiences adventure;
    private FloodgateApi floodgateApi;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public ConfigManager(BaritoneRemover pl) {
        instance = this;
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            floodgateApi = FloodgateApi.getInstance();
        } catch (ClassNotFoundException ignored) {
        }
        this.adventure = BukkitAudiences.create(pl);
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        yaml = new Yaml(dumperOptions);
        configFile = pl.configFile();

        this.checks = pl.checks();
        getCurrentDebugFile();
        loadConfig();
    }

    private void getCurrentDebugFile() {
        File temp;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-");
        Date currentDate = new Date();
        String formattedDate = dateFormat.format(currentDate);
        int index = 1;
        temp = new File(plugin().debugFolder(), formattedDate + index + ".log");
        while (temp.exists()) {
            index++;
            temp = new File(plugin().debugFolder(), formattedDate + index + ".log");
        }
        debugFile = temp;
        try {
            debugFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        appendDebug("BaritoneRemover successfully loaded!");
    }

    public void loadConfig() {
        propagateDefault();
        try (FileReader reader = new FileReader(configFile)) {
            config = merge((LinkedHashMap<String, Object>) yaml.load(reader), config);
        } catch (IOException ignored) {
        }
        saveConfig();
        minTps = config.get("min-tps") instanceof String ? Double.parseDouble((String) config.get("min-tps")) : (Double) config.get("min-tps");
        prefix = miniMessage.deserializeOr(config.get("prefix").toString(),
                Component.text("[").color(NamedTextColor.WHITE)
                        .append(Component.text("BaritoneRemover").color(NamedTextColor.RED))
                        .append(Component.text("] ").color(NamedTextColor.WHITE)));

        Map<String, Object> webHook = (Map<String, Object>) config.get("webhook");
        webHookEnabled = (boolean) webHook.get("enable");
        webHookUrl = (String) webHook.get("url");

        Map<String, Object> configChecks = (Map<String, Object>) config.get("checks");
        configChecks.forEach((key, value) -> {
            String checkName = key.substring(0, key.length() - 1);
            String checkIdentifier = key.substring(key.length() - 1);
            Class<? extends Check> checkClass = null;
            for (Class<? extends Check> check : checks.getChecks()) {
                CheckData checkData = check.getAnnotation(CheckData.class);
                if (!checkData.name().equals(checkName) || !checkData.identifier().equals(checkIdentifier)) {
                    continue;
                }
                checkClass = check;
                break;
            }
            if (checkClass == null) {
                return;
            }
            Map<String, Object> checkMap = (Map<String, Object>) value;
            boolean enable = (boolean) checkMap.get("enable");
            boolean punish = (boolean) checkMap.get("punish");
            int punishVl = (int) checkMap.get("punish-vl");
            String punishCommand = (String) checkMap.get("punish-command");
            configDataMap.put(checkClass, new ConfigData(enable, punish, punishVl, punishCommand));
        });
        sender.load();
    }

    private LinkedHashMap<String, Object> merge(LinkedHashMap<String, Object> load,
            LinkedHashMap<String, Object> config) {
        if (load == null) {
            return config;
        }
        if (config == null) {
            return load;
        }
        config.forEach((key, value) -> {
            if (value instanceof LinkedHashMap) {
                load.put(key,
                        merge((LinkedHashMap<String, Object>) load.get(key), (LinkedHashMap<String, Object>) value));
            } else {
                if (!load.containsKey(key)) {
                    load.put(key, value);
                }
            }
        });
        // Fix the oder too
        LinkedHashMap<String, Object> newLoad = new LinkedHashMap<>();
        config.forEach((key, value) -> {
            if (load.containsKey(key)) {
                newLoad.put(key, load.get(key));
            }
        });
        // Remove keys that are not in the default config checking for nested too
        load.forEach((key, value) -> {
            if (!config.containsKey(key)) {
                newLoad.put(key, value);
            } else if (value instanceof LinkedHashMap) {
                newLoad.put(key,
                        merge((LinkedHashMap<String, Object>) value, (LinkedHashMap<String, Object>) config.get(key)));
            }
        });
        return newLoad;
    }

    private void propagateDefault() {
        config = new LinkedHashMap<>();

        config.put("prefix", "[<red>BaritoneRemover<white>] ");

        config.put("min-tps", 18.0);

        Map<String, Object> webHook = new LinkedHashMap<>();
        webHook.put("enable", false);
        webHook.put("url", "https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz");
        config.put("webhook", webHook);

        Map<String, Object> configChecks = new LinkedHashMap<>();
        checks.getChecks().forEach(check -> {
            if (check.getAnnotation(CheckData.class).hidden()) {
                return;
            }
            LinkedHashMap<String, Object> checkMap = new LinkedHashMap<>();
            checkMap.put("enable", true);
            checkMap.put("punish", true);
            checkMap.put("punish-vl", 20);
            checkMap.put("punish-command", "kick %player%");
            configChecks.put(
                    check.getAnnotation(CheckData.class).name() + check.getAnnotation(CheckData.class).identifier(),
                    checkMap);
            configDataMap.put(check, new ConfigData(true, true, 20, "kick %player%"));
        });

        config.put("checks", configChecks);
    }

    public void saveConfig() {
        String configString = yaml.dump(config);
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write(configString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendDebug(String message) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd-").format(new Date());
        if (!debugFile.getName().startsWith(formattedDate)) {
            getCurrentDebugFile();
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = dateFormat.format(date);
        message = "[" + formattedTime + "] " + message;
        try {
            FileWriter writer = new FileWriter(debugFile, true);
            writer.write(message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigData getConfigData(Class<? extends Check> check) {
        return configDataMap.get(check);
    }

    public Component prefix() {
        return prefix;
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public FloodgateApi floodgateApi() {
        return floodgateApi;
    }

    public boolean webHookEnabled() {
        return webHookEnabled;
    }

    public String webHookUrl() {
        return webHookUrl;
    }

    public Sender sender() {
        return sender;
    }

    public double minTps() {
        return minTps;
    }

    public BaritoneRemover plugin() {
        return BaritoneRemover.getPlugin(BaritoneRemover.class);
    }
}

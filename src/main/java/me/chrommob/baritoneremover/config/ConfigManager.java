package me.chrommob.baritoneremover.config;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.Checks;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    private final File configFile;
    private final Yaml yaml;
    private static ConfigManager instance;
    private final Checks checks;
    private final Map<Class<? extends Check>, ConfigData> configDataMap = new HashMap<>();
    private LinkedHashMap<String, Object> config;
    private BukkitAudiences adventure;

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public ConfigManager(BaritoneRemover pl) {
        instance = this;
        this.adventure = BukkitAudiences.create(pl);
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        yaml = new Yaml(dumperOptions);
        configFile = pl.configFile();
        this.checks = pl.checks();
        loadConfig();
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            config = (LinkedHashMap<String, Object>) yaml.load(reader);
        } catch (IOException ignored) {

        }
        if (config == null) {
            propagateDefault();
            saveConfig();
            return;
        }
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
    }

    private void propagateDefault() {
        config = new LinkedHashMap<>();
        Map<String, Object> configChecks = new LinkedHashMap<>();
        checks.getChecks().forEach(check -> {
            LinkedHashMap<String, Object> checkMap = new LinkedHashMap<>();
            checkMap.put("enable", true);
            checkMap.put("punish", true);
            checkMap.put("punish-vl", 20);
            checkMap.put("punish-command", "kick %player%");
            configChecks.put(check.getAnnotation(CheckData.class).name() + check.getAnnotation(CheckData.class).identifier(), checkMap);
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

    public ConfigData getConfigData(Class<? extends Check> check) {
        return configDataMap.get(check);
    }

    public static ConfigManager getInstance() {
        return instance;
    }
}

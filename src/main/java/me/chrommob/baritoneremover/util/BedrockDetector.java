package me.chrommob.baritoneremover.util;

import me.chrommob.baritoneremover.config.ConfigManager;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.lang.reflect.Method;
import java.util.UUID;

public final class BedrockDetector {

    private static final Method GEYSER_API_DOT_API = resolveGeyserApiMethod();
    private static final Method GEYSER_IS_BEDROCK = resolveGeyserIsBedrock();

    private BedrockDetector() {
    }

    public static boolean isBedrock(Player player) {
        if (player == null) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        if (isFloodgatePlayer(uuid)) {
            return true;
        }
        return isGeyserBedrockPlayer(uuid);
    }

    private static boolean isFloodgatePlayer(UUID uuid) {
        try {
            FloodgateApi floodgateApi = ConfigManager.getInstance().floodgateApi();
            return floodgateApi != null && floodgateApi.isFloodgatePlayer(uuid);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isGeyserBedrockPlayer(UUID uuid) {
        try {
            if (GEYSER_API_DOT_API == null || GEYSER_IS_BEDROCK == null) {
                return false;
            }
            Object geyserApi = GEYSER_API_DOT_API.invoke(null);
            if (geyserApi == null) {
                return false;
            }
            Object result = GEYSER_IS_BEDROCK.invoke(geyserApi, uuid);
            return Boolean.TRUE.equals(result);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Method resolveGeyserApiMethod() {
        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            return geyserApiClass.getMethod("api");
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method resolveGeyserIsBedrock() {
        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            return geyserApiClass.getMethod("isBedrockPlayer", UUID.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
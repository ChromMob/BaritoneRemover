package me.chrommob.baritoneremover.config;

import java.util.Objects;

public final class ConfigData {
    private final boolean enable;
    private final boolean punish;
    private final int punishVl;
    private final String punishCommand;

    ConfigData(boolean enable, boolean punish, int punishVl, String punishCommand) {
        this.enable = enable;
        this.punish = punish;
        this.punishVl = punishVl;
        this.punishCommand = punishCommand;
    }

    public boolean enable() {
        return enable;
    }

    public boolean punish() {
        return punish;
    }

    public int punishVl() {
        return punishVl;
    }

    public String punishCommand() {
        return punishCommand;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ConfigData that = (ConfigData) obj;
        return this.enable == that.enable &&
                this.punish == that.punish &&
                this.punishVl == that.punishVl &&
                Objects.equals(this.punishCommand, that.punishCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable, punish, punishVl, punishCommand);
    }

    @Override
    public String toString() {
        return "ConfigData[" +
                "enable=" + enable + ", " +
                "punish=" + punish + ", " +
                "punishVl=" + punishVl + ", " +
                "punishCommand=" + punishCommand + ']';
    }

}

package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Utils;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CheckData(name = "Repeated", identifier = "C", checkType = CheckType.AGGREGATE, description = "Checks if the player is changing rotation in a repeated sequence")
public class RepeatedC extends Check {
    public RepeatedC(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        if (playerData.isCinematic()) {
            return;
        }
        List<PacketData> packetDatas = playerData.packetDataList().getAllType(CheckType.FLYING, CheckType.ROTATION);
        if (packetDatas.size() < 2) {
            return;
        }
        //Get difference between each rotation
        List<Data> datas = new ArrayList<>();
        for (int i = 0; i < packetDatas.size() - 1; i++) {
            PacketData packetData = packetDatas.get(i);
            PacketData nextPacketData = packetDatas.get(i + 1);
            float yawDifference = packetData.rotationData().differenceYaw(nextPacketData.rotationData());
            float pitchDifference = packetData.rotationData().differencePitch(nextPacketData.rotationData());
            datas.add(new Data(yawDifference, pitchDifference, packetData.index()));
        }
        HashMap<Data, List<Integer>> same = new HashMap<>();
        for (Data data : datas) {
            List<Integer> indexes;
            if (same.containsKey(data)) {
                indexes = same.get(data);
            } else {
                indexes = new ArrayList<>();
            }
            indexes.add(data.index);
            same.put(data, indexes);
        }
        HashMap<List<Integer>, List<Data>> patternToDataMap = new HashMap<>();
        for (HashMap.Entry<Data, List<Integer>> entry : same.entrySet()) {
            Data data = entry.getKey();
            List<Integer> indexes = entry.getValue();
            if (indexes.size() < 2) {
                continue;
            }
            List<Integer> pattern = Utils.computePattern(indexes);
            if (!patternToDataMap.containsKey(pattern)) {
                patternToDataMap.put(pattern, new ArrayList<>());
            }
            patternToDataMap.get(pattern).add(data);
        }

        int maxPatternLength = 0;
        int maxPatternRepeats = 0;
        int totalPatternsC = 0;
        List<Data> maxPatternData = null;
        for (Map.Entry<List<Integer>, List<Data>> patternEntry : patternToDataMap.entrySet()) {
            List<Integer> pattern = patternEntry.getKey();
            List<Data> rotationDataList = patternEntry.getValue();
            int patternLength = pattern.size();
            int patternRepeats = rotationDataList.size();
            int totalPatterns = patternLength * patternRepeats;
            if (totalPatterns > totalPatternsC) {
                maxPatternData = rotationDataList;
                totalPatternsC = totalPatterns;
                maxPatternLength = patternLength;
                maxPatternRepeats = patternRepeats;
            }
        }
        StringBuilder patternBuilder = new StringBuilder();
        if (maxPatternData == null) {
            return;
        }
        for (Data data : maxPatternData) {
            patternBuilder.append(data.toString()).append(" ");
        }
        debug("totalPatternsC: " + maxPatternLength * maxPatternRepeats + " maxPatternLength: " + maxPatternLength + " maxPatternRepeats: " + maxPatternRepeats);
        debug("pattern: " + patternBuilder);
        if (totalPatternsC > 200 && maxPatternLength > 1 && maxPatternRepeats > 1) {
            increaseVl(punishVl());
        }
    }
}

class Data {
    public final float yawDifference;
    public final float pitchDifference;
    public final int index;
    public Data(float yawDifference, float pitchDifference, int index) {
        this.yawDifference = yawDifference;
        this.pitchDifference = pitchDifference;
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Data)) {
            return false;
        }
        Data data = (Data) obj;
        return data.yawDifference == yawDifference && data.pitchDifference == pitchDifference;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(yawDifference) + Float.hashCode(pitchDifference);
    }

    @Override
    public String toString() {
        return yawDifference + ":" + pitchDifference;
    }
}

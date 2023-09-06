package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Utils;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.RotationData;

import java.util.*;

@CheckData(name = "Repeated", identifier = "A", checkType = CheckType.AGGREGATE, description = "Checks if the player is rotating in a repeated sequence")
public class RepeatedA extends Check {

    public RepeatedA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        List<PacketData> packetDatas = playerData.packetDataList().getAllType(CheckType.FLYING, CheckType.ROTATION);
        if (packetDatas.size() < 2) {
            return;
        }
        HashMap<RotationData, List<Integer>> same = new HashMap<>();
        for (PacketData packetData : packetDatas) {
            RotationData rotationData = packetData.rotationData();
            List<Integer> indexes;
            if (same.containsKey(rotationData)) {
                indexes = same.get(rotationData);
            } else {
                indexes = new ArrayList<>();
            }
            indexes.add(packetData.index());
            same.put(rotationData, indexes);
        }
        Map<List<Integer>, List<RotationData>> patternToRotationDataMap = new HashMap<>();

        for (Map.Entry<RotationData, List<Integer>> entry : same.entrySet()) {
            RotationData rotationData = entry.getKey();
            List<Integer> indexes = entry.getValue();
            if (indexes.size() < 2) {
                continue;
            }
            List<Integer> pattern = Utils.computePattern(indexes);

            if (!patternToRotationDataMap.containsKey(pattern)) {
                patternToRotationDataMap.put(pattern, new ArrayList<>());
            }

            patternToRotationDataMap.get(pattern).add(rotationData);
        }

        int maxPatternLength = 0;
        int maxPatternRepeats = 0;
        for (Map.Entry<List<Integer>, List<RotationData>> patternEntry : patternToRotationDataMap.entrySet()) {
            List<Integer> pattern = patternEntry.getKey();
            List<RotationData> rotationDataList = patternEntry.getValue();
            if (pattern.size() > maxPatternLength) {
                maxPatternLength = pattern.size();
            }
            if (rotationDataList.size() > maxPatternRepeats && rotationDataList.size() > 1) {
                maxPatternRepeats = rotationDataList.size();
            }
        }
        debug("totalRotations: " + maxPatternLength * maxPatternRepeats + " maxPatternLength: " + maxPatternLength + " maxPatternRepeats: " + maxPatternRepeats);
        if (maxPatternLength * maxPatternRepeats > 200) {
            increaseVl(punishVl());
        }
    }
}



package me.chrommob.baritoneremover.checks.impl.movement;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.PositionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CheckData(name = "Repeated", identifier = "B", checkType = CheckType.AGGREGATE, description = "Checks if the player is moving in a repeated sequence")
public class RepeatedB extends Check {
    public RepeatedB(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        List<PacketData> packetDatas = playerData.packetDataList().getAllType(CheckType.FLYING, CheckType.POSITION);
        if (packetDatas.size() < 2) {
            return;
        }
        HashMap<PositionData, List<Integer>> same = new HashMap<>();
        for (PacketData packetData : packetDatas) {
            PositionData positionData = packetData.positionData();
            List<Integer> indexes;
            if (same.containsKey(positionData)) {
                indexes = same.get(positionData);
            } else {
                indexes = new java.util.ArrayList<>();
            }
            indexes.add(packetData.index());
            same.put(positionData, indexes);
        }
        HashMap<List<Integer>, List<PositionData>> patternToPositionDataMap = new HashMap<>();
        for (Map.Entry<PositionData, List<Integer>> entry : same.entrySet()) {
            PositionData rotationData = entry.getKey();
            List<Integer> indexes = entry.getValue();
            if (indexes.size() < 2) {
                continue;
            }
            List<Integer> pattern = computePattern(indexes);

            if (!patternToPositionDataMap.containsKey(pattern)) {
                patternToPositionDataMap.put(pattern, new ArrayList<>());
            }

            patternToPositionDataMap.get(pattern).add(rotationData);
        }

        int maxPatternLength = 0;
        int maxPatternRepeats = 0;
        for (Map.Entry<List<Integer>, List<PositionData>> patternEntry : patternToPositionDataMap.entrySet()) {
            List<Integer> pattern = patternEntry.getKey();
            List<PositionData> rotationDataList = patternEntry.getValue();
            if (pattern.size() > maxPatternLength) {
                maxPatternLength = pattern.size();
            }
            if (rotationDataList.size() > maxPatternRepeats && rotationDataList.size() > 1) {
                maxPatternRepeats = rotationDataList.size();
            }
        }
        debug("totalBlocks: " + maxPatternLength * maxPatternRepeats);
        //increaseVl(punishVl());
    }

    private List<Integer> computePattern(List<Integer> indexes) {
        List<Integer> pattern = new ArrayList<>();
        int diff = indexes.get(1) - indexes.get(0);

        for (int i = 1; i < indexes.size(); i++) {
            pattern.add(indexes.get(i) - indexes.get(i - 1));
        }

        pattern.add(0, diff); // Add the initial difference as the first element
        return pattern;
    }
}

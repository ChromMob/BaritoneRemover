package me.chrommob.baritoneremover.checks.impl.movement;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Utils;
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
    public void run() {
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
            List<Integer> pattern = Utils.computePattern(indexes);

            if (!patternToPositionDataMap.containsKey(pattern)) {
                patternToPositionDataMap.put(pattern, new ArrayList<>());
            }

            patternToPositionDataMap.get(pattern).add(rotationData);
        }

        int maxPatternLength = 0;
        int maxPatternRepeats = 0;
        int totalBlocks = 0;
        List<PositionData> maxPatternData = null;
        for (Map.Entry<List<Integer>, List<PositionData>> patternEntry : patternToPositionDataMap.entrySet()) {
            List<Integer> pattern = patternEntry.getKey();
            List<PositionData> rotationDataList = patternEntry.getValue();
            int patternLength = pattern.size();
            int patternRepeats = rotationDataList.size();
            int blocks = patternLength * patternRepeats;
            if (blocks > totalBlocks) {
                maxPatternData = rotationDataList;
                totalBlocks = blocks;
                maxPatternLength = patternLength;
                maxPatternRepeats = patternRepeats;
            }
        }
        StringBuilder patternBuilder = new StringBuilder();
        if (maxPatternData == null) {
            return;
        }
        for (PositionData data : maxPatternData) {
            patternBuilder.append(data.toString()).append(" ");
        }
        debug("totalBlocks: " + maxPatternLength * maxPatternRepeats + " maxPatternLength: " + maxPatternLength + " maxPatternRepeats: " + maxPatternRepeats);
        debug("pattern: " + patternBuilder);
        if (totalBlocks > 200 && maxPatternLength > 1 && maxPatternRepeats > 1) {
            increaseVl(punishVl());
        }
    }
}

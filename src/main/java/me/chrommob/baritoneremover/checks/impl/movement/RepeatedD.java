package me.chrommob.baritoneremover.checks.impl.movement;

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

@CheckData(name = "Repeated", identifier = "D", checkType = CheckType.AGGREGATE, description = "Checks if the player is moving difference is in a repeated sequence")
public class RepeatedD extends Check {

    public RepeatedD(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        List<PacketData> packetDatas = playerData.packetDataList().getAllType(CheckType.FLYING, CheckType.POSITION);
        if (packetDatas.size() < 2) {
            return;
        }
        List<Data> datas = new ArrayList<>();
        for (int i = 0; i < packetDatas.size() - 1; i++) {
            PacketData packetData = packetDatas.get(i);
            PacketData nextPacketData = packetDatas.get(i + 1);
            double xDifference = packetData.positionData().differenceX(nextPacketData.positionData());
            double yDifference = packetData.positionData().differenceY(nextPacketData.positionData());
            double zDifference = packetData.positionData().differenceZ(nextPacketData.positionData());

            if ((xDifference == 0 && yDifference == 0) || (xDifference == 0 && zDifference == 0) || (yDifference == 0 && zDifference == 0)) {
                continue;
            }
            datas.add(new Data(xDifference, yDifference, zDifference, packetData.index()));
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
        for (Map.Entry<List<Integer>, List<Data>> patternEntry : patternToDataMap.entrySet()) {
            List<Integer> pattern = patternEntry.getKey();
            List<Data> rotationDataList = patternEntry.getValue();
            if (pattern.size() > maxPatternLength) {
                maxPatternLength = pattern.size();
            }
            if (rotationDataList.size() > maxPatternRepeats && rotationDataList.size() > 1) {
                maxPatternRepeats = rotationDataList.size();
            }
        }
        debug("totalPatternsD: " + maxPatternLength * maxPatternRepeats + " maxPatternLength: " + maxPatternLength + " maxPatternRepeats: " + maxPatternRepeats);
        if (maxPatternLength * maxPatternRepeats > 200) {
            increaseVl(punishVl());
        }
    }
}

class Data {
    public final double xDifference;
    public final double yDifference;
    public final double zDifference;
    public final int index;
    public Data(double xDifference, double yDifference, double zDifference, int index) {
        this.xDifference = xDifference;
        this.yDifference = yDifference;
        this.zDifference = zDifference;
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Data)) {
            return false;
        }
        Data data = (Data) obj;
        return data.xDifference == xDifference && data.yDifference == yDifference && data.zDifference == zDifference;
    }

    @Override
    public int hashCode() {
        return (int) (xDifference + yDifference + zDifference);
    }
}


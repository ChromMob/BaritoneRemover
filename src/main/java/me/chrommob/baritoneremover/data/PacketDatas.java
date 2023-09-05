package me.chrommob.baritoneremover.data;

import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;

import java.util.ArrayList;
import java.util.List;

public class PacketDatas {
    private final List<PacketData> packetDatas;
    public PacketDatas() {
        packetDatas = new ArrayList<>();
    }


    public void add(CheckType checkType, PositionData positionData, RotationData rotationData, boolean mining, boolean finishedMining, boolean placedBlock) {
        PacketData packetData = new PacketData(checkType, packetDatas.size(), System.currentTimeMillis(), positionData, rotationData, mining, finishedMining, placedBlock);
        packetDatas.add(packetData);
    }

    public PacketData getLatest() {
        if (packetDatas.size() == 0) {
            return null;
        }
        return packetDatas.get(packetDatas.size() - 1);
    }

    public PacketData getLatest(CheckType checkType) {
        for (int i = packetDatas.size() - 1; i >= 0; i--) {
            PacketData packetData = packetDatas.get(i);
            if (packetData.checkType() == checkType) {
                return packetData;
            }
        }
        return null;
    }

    public PacketData get(int index) {
        return packetDatas.get(index);
    }

    public int size() {
        return packetDatas.size();
    }

    public int size(CheckType checkType) {
        int size = 0;
        for (PacketData packetData : packetDatas) {
            if (packetData.checkType() == checkType) {
                size++;
            }
        }
        return size;
    }

    public PacketData getPrevious(PacketData latest) {
        if (latest.index() == 0) {
            return null;
        }
        return packetDatas.get(latest.index() - 1);
    }

    public PacketData getPrevious(PacketData latest, CheckType checkType) {
        for (int i = latest.index() - 1; i >= 0; i--) {
            PacketData packetData = packetDatas.get(i);
            if (packetData.checkType() == checkType) {
                return packetData;
            }
        }
        return null;
    }

    public List<PacketData> getAllType(CheckType checkType, CheckType checkType2) {
        List<PacketData> packetDataList = new ArrayList<>();
        for (PacketData packetData : packetDatas) {
            if (packetData.checkType() == checkType || packetData.checkType() == checkType2) {
                packetDataList.add(packetData);
            }
        }
        return packetDataList;
    }
}
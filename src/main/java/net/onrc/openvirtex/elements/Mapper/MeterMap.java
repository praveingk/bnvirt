package net.onrc.openvirtex.elements.Mapper;

import com.mongodb.util.Hash;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.messages.OVXMeterMod;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.protocol.ver13.OFMeterBandDropVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFMeterFlagsSerializerVer13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MeterMap {
    public static HashMap<OVXPort, OVXMeterMod> meterMapper = new HashMap<>();
    public static int startId = 1;
    public static int endId = 10000;
    private static HashSet<Integer> usedMeters = new HashSet<>();


    public static synchronized long getFreeMeterId() {
        for (int i=startId;i <= endId ;i++) {
            if (!usedMeters.contains(i)) {
                usedMeters.add(i);
                return i;
            }
        }
        return -1;
    }

    public static void createMeter(OVXPort myPort, OFMeterBandDrop myMeterband) {
        OVXMeterMod meterMod = null;
        int command = 0x0; //ADD
        short flags = OFMeterFlagsSerializerVer13.BURST_VAL | OFMeterFlagsSerializerVer13.KBPS_VAL;
        long meterId = getFreeMeterId();
        List<OFMeterBand> ofmlist = new ArrayList<>();
        ofmlist.add(myMeterband);
        meterMod = OVXFactoryInst.myOVXFactory.buildOVXMeterMod(0x0, command, flags, meterId, ofmlist);
        System.out.println("Creating Meter for "+ myPort.getPortNo().getPortNumber() +" with meterId "+ meterId);
        myPort.getParentSwitch().sendSouth(meterMod, myPort);

        meterMapper.put(myPort, meterMod);
    }

}
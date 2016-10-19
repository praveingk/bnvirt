package net.onrc.openvirtex.elements.network;

import net.onrc.openvirtex.elements.datapath.DPIDandPort;
import net.onrc.openvirtex.elements.datapath.DPIDandPortPair;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.PhysicalPort;

import java.util.ArrayList;

/**
 * Created by pravein on 9/27/16.
 */
public class LoopNetwork {
    public static boolean isInitialized = false;
    public static ArrayList<DPIDandPortPair> loopPorts = new ArrayList<>();
    public static boolean IsLoop(final long ovxSrcDpid,
                              final short ovxSrcPort, final long ovxDstDpid,
                              final short ovxDstPort) {
        System.out.println("Checking Loop");
        DPIDandPort srcDP = new DPIDandPort(ovxSrcDpid, ovxSrcPort);
        DPIDandPort dstDP = new DPIDandPort(ovxDstDpid, ovxDstPort);
        DPIDandPortPair loopPair = new DPIDandPortPair(srcDP,dstDP);
        System.out.println("Checking loop for "+loopPair.toString());
        System.out.println("Configured Loops : "+loopPorts.toString());
        if (loopPorts.contains(loopPair)) {
            return true;
        }
        return false;
    }

    public static void initialize() {
        System.out.println("Initializing Loop ports..");

        DPIDandPort srcDP = new DPIDandPort(new Long(0x1000b05ada295280L), (short)25);
        DPIDandPort dstDP = new DPIDandPort(new Long(0x1000b05ada295280L), (short)26);
        DPIDandPortPair loopPair = new DPIDandPortPair(srcDP,dstDP);
        loopPorts.add(loopPair);
        isInitialized = true;
        PhysicalNetwork myNet = PhysicalNetwork.getInstance();
        for (int i=0;i< loopPorts.size();i++) {
            DPIDandPortPair myPair = loopPorts.get(i);
            System.out.println(myNet.dpidMap.toString());
            PhysicalSwitch sSwitch = myNet.getSwitch(myPair.getSrc().getDpid());
            if (sSwitch == null) {
                System.out.println("sSwitch is null!");
                return;
            }
            PhysicalPort sPort = sSwitch.getPort(myPair.getSrc().getPort());
            if (sPort == null) {
                System.out.println("sPort is null!");
                return;
            }
            PhysicalSwitch dSwitch = myNet.getSwitch(myPair.getDst().getDpid());
            if (sSwitch == null) {
                System.out.println("dSwitch is null!");
                return;
            }
            PhysicalPort dPort = dSwitch.getPort(myPair.getDst().getPort());
            if (dPort == null ) {
                System.out.println("dPort is null!");
                return;
            }
            myNet.createLink(sPort,dPort);
            myNet.createLink(dPort,sPort);
        }

    }
}

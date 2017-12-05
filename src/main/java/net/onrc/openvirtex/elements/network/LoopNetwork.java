package net.onrc.openvirtex.elements.network;

import net.onrc.openvirtex.elements.datapath.DPIDandPort;
import net.onrc.openvirtex.elements.datapath.DPIDandPortPair;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by pravein on 9/27/16.
 */
public class LoopNetwork {
    public static boolean isInitialized = false;
    public static ArrayList<DPIDandPortPair> loopPorts = new ArrayList<>();
    public static ArrayList<DPIDandPortPair> corePorts = new ArrayList<>();
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

    public static void setDeiniitalize() {
        isInitialized = false;
    }

    public static void initBNVirtSwitch(PhysicalSwitch mySwitch) {
        System.out.println("Initialize BNVirt N/W for "+ Long.toHexString(mySwitch.getSwitchId()));
        ArrayList<DPIDandPortPair> addPorts = new ArrayList<>();
        // Make the below 12 for NCL Production
        int totalLoopPorts = 12;
        short startport = 25;
        // Remove 48 for for NCL Production
        short[] backbonePorts = {55,56,57,58};
        PhysicalNetwork myNet = PhysicalNetwork.getInstance();
        Set<PhysicalSwitch> addedSwitches = myNet.getSwitches();
        // Create Loop Ports First
        System.out.println("Creating links for loop ports for Switch :"+ Long.toHexString(mySwitch.getSwitchId()));
        short port = startport;
        for (short i = 0; i < totalLoopPorts; i++) {
            DPIDandPort srcDP = new DPIDandPort(mySwitch.getSwitchId(), (short) (port));
            port++;
            DPIDandPort dstDP = new DPIDandPort(mySwitch.getSwitchId(), (short) (port));
            DPIDandPortPair loopPair = new DPIDandPortPair(srcDP, dstDP);
            addPorts.add(loopPair);
            System.out.println(loopPair.toString());
            port++;
        }

        // Create BackBone links

        for (PhysicalSwitch otherSwitch : addedSwitches) {
            if (mySwitch.equals(otherSwitch)) {
                continue;
            }
            System.out.println("Creating links for backbone ports for Switch :"+ Long.toHexString(mySwitch.getSwitchId())+ " and "+ Long.toHexString(otherSwitch.getSwitchId()));
            for  (short backbonePort : backbonePorts) {
                DPIDandPort srcDP = new DPIDandPort(mySwitch.getSwitchId(), backbonePort);
                DPIDandPort dstDP = new DPIDandPort(otherSwitch.getSwitchId(), backbonePort);
                DPIDandPortPair corePair = new DPIDandPortPair(srcDP, dstDP);
                addPorts.add(corePair);
                System.out.println(corePair.toString());
            }

        }

        for (int i=0;i< addPorts.size();i++) {
            DPIDandPortPair myPair = addPorts.get(i);
            System.out.println(myNet.dpidMap.toString());

            PhysicalSwitch sSwitch = myNet.getSwitch(myPair.getSrc().getDpid());
            if (sSwitch == null) {
                System.out.println("sSwitch is null!");
                continue;
            }
            PhysicalPort sPort = sSwitch.getPort(myPair.getSrc().getPort());
            if (sPort == null) {
                System.out.println("sPort is null!");
                continue;
            }
            PhysicalSwitch dSwitch = myNet.getSwitch(myPair.getDst().getDpid());
            if (sSwitch == null) {
                System.out.println("dSwitch is null!");
                continue;
            }
            PhysicalPort dPort = dSwitch.getPort(myPair.getDst().getPort());
            if (dPort == null ) {
                System.out.println("dPort is null!");
                continue;
            }
            try {
                myNet.createLink(sPort,dPort);
                myNet.createLink(dPort,sPort);
            } catch (SwitchMappingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initialize() {
        System.out.println("Initializing Loop ports..");
        // Make the below 12 for NCL Production
        int totalLoopPorts = 11;
        short startport = 25;
        // Remove 48 for for NCL Production
        short[] backbonePorts = {48,55,56,57,58};
        PhysicalNetwork myNet = PhysicalNetwork.getInstance();
        Set<PhysicalSwitch> mySwitches = myNet.getSwitches();
        for (PhysicalSwitch mySwitch : mySwitches) {
            System.out.println("Creating links for loop ports for Switch :"+ mySwitch.getSwitchId());
            short port = startport;
            for (short i = 0; i < totalLoopPorts; i++) {
                DPIDandPort srcDP = new DPIDandPort(mySwitch.getSwitchId(), (short) (port));
                port++;
                DPIDandPort dstDP = new DPIDandPort(mySwitch.getSwitchId(), (short) (port));
                DPIDandPortPair loopPair = new DPIDandPortPair(srcDP, dstDP);
                loopPorts.add(loopPair);
                System.out.println(loopPair.toString());
                port++;
            }
        }
        for (PhysicalSwitch mySwitch1 : mySwitches) {
            for (PhysicalSwitch mySwitch2 : mySwitches) {
                if (mySwitch1.equals(mySwitch2)) {
                    continue;
                }
                System.out.println("Creating links for backbone ports for Switch :"+ mySwitch1.getSwitchId()+ " and "+ mySwitch2.getSwitchId());
                for  (short backbonePort : backbonePorts) {
                    DPIDandPort srcDP = new DPIDandPort(mySwitch1.getSwitchId(), backbonePort);
                    DPIDandPort dstDP = new DPIDandPort(mySwitch2.getSwitchId(), backbonePort);
                    DPIDandPortPair corePair = new DPIDandPortPair(srcDP, dstDP);
                    corePorts.add(corePair);
                    System.out.println(corePair.toString());
                }

            }
        }
        isInitialized = true;
        for (int i=0;i< loopPorts.size();i++) {
            DPIDandPortPair myPair = loopPorts.get(i);
            System.out.println(myNet.dpidMap.toString());
            PhysicalSwitch sSwitch = myNet.getSwitch(myPair.getSrc().getDpid());
            if (sSwitch == null) {
                System.out.println("sSwitch is null!");
                continue;
            }
            PhysicalPort sPort = sSwitch.getPort(myPair.getSrc().getPort());
            if (sPort == null) {
                System.out.println("sPort is null!");
                continue;
            }
            PhysicalSwitch dSwitch = myNet.getSwitch(myPair.getDst().getDpid());
            if (sSwitch == null) {
                System.out.println("dSwitch is null!");
                continue;
            }
            PhysicalPort dPort = dSwitch.getPort(myPair.getDst().getPort());
            if (dPort == null ) {
                System.out.println("dPort is null!");
                continue;
            }
            try {
                myNet.createLink(sPort,dPort);
                myNet.createLink(dPort,sPort);
            } catch (SwitchMappingException e) {
                e.printStackTrace();
            }

        }


        for (int i=0;i< corePorts.size();i++) {
            DPIDandPortPair myPair = corePorts.get(i);
            System.out.println(myNet.dpidMap.toString());

            PhysicalSwitch sSwitch = myNet.getSwitch(myPair.getSrc().getDpid());
            if (sSwitch == null) {
                System.out.println("sSwitch is null!");
                continue;
            }
            PhysicalPort sPort = sSwitch.getPort(myPair.getSrc().getPort());
                if (sPort == null) {
                    System.out.println("sPort is null!");
                    continue;
                }
            PhysicalSwitch dSwitch = myNet.getSwitch(myPair.getDst().getDpid());
            if (sSwitch == null) {
                System.out.println("dSwitch is null!");
                continue;
            }
            PhysicalPort dPort = dSwitch.getPort(myPair.getDst().getPort());
            if (dPort == null ) {
                System.out.println("dPort is null!");
                continue;
            }
            try {
                myNet.createLink(sPort,dPort);
                myNet.createLink(dPort,sPort);
            } catch (SwitchMappingException e) {
                e.printStackTrace();
            }
        }
    }
}

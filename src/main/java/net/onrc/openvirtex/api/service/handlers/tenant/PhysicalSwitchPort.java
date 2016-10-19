package net.onrc.openvirtex.api.service.handlers.tenant;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

/**
 * Created by pravein on 18/9/16.
 */
public class PhysicalSwitchPort {
    PhysicalSwitch physicalSwitch;
    Integer port;
    Integer tenantID;
    String hc = new String();
    public PhysicalSwitchPort(PhysicalSwitch physicalSwitch, Integer port, Integer tenantID) {
        this.physicalSwitch = physicalSwitch;
        this.port = port;
        this.tenantID = tenantID;
        hc =  this.physicalSwitch.getSwitchId()+","+this.port+","+this.tenantID;

    }

    @Override
    public int hashCode() {
        return hc.hashCode();
    }


    @Override
    public boolean equals(Object arg) {
        PhysicalSwitchPort comparePSP = (PhysicalSwitchPort) arg;
        if (this.physicalSwitch.getSwitchId().equals(comparePSP.physicalSwitch.getSwitchId()) &&
                this.port == comparePSP.port && this.tenantID == comparePSP.tenantID) {
                return true;
        }
        return false;
    }
    public String toString() {
        return "("+physicalSwitch.getSwitchId()+", "+ port+","+tenantID+" )";
    }

}

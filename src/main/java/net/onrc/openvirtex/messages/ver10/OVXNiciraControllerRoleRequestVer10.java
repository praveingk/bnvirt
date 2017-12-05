package net.onrc.openvirtex.messages.ver10;

import org.projectfloodlight.openflow.protocol.OFNiciraControllerRole;
import org.projectfloodlight.openflow.protocol.ver10.OFFeaturesRequestVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleRequestVer10;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFeaturesRequest;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXNiciraControllerRoleRequest;

public class OVXNiciraControllerRoleRequestVer10 extends OFNiciraControllerRoleRequestVer10 implements OVXNiciraControllerRoleRequest {
	OVXNiciraControllerRoleRequestVer10(long xid, OFNiciraControllerRole role) {
		super(xid, role);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
        OVXMessageUtil.translateXidAndSend(this, sw);
    }

    
}

package net.onrc.openvirtex.messages.ver10;

import org.projectfloodlight.openflow.protocol.OFNiciraControllerRole;
import org.projectfloodlight.openflow.protocol.ver10.OFFeaturesReplyVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleReplyVer10;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFeaturesReply;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXNiciraControllerRoleReply;

public class OVXNiciraControllerRoleReplyVer10 extends OFNiciraControllerRoleReplyVer10 implements OVXNiciraControllerRoleReply {
	

    OVXNiciraControllerRoleReplyVer10(long xid, OFNiciraControllerRole role) {
		super(xid, role);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        OVXMessageUtil.untranslateXidAndSend(this, sw);
    }
}

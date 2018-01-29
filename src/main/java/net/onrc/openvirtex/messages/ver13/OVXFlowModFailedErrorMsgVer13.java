package net.onrc.openvirtex.messages.ver13;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFlowModFailedErrorMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.ver13.OFFlowModFailedErrorMsgVer13;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

public class OVXFlowModFailedErrorMsgVer13 extends OFFlowModFailedErrorMsgVer13 implements OVXFlowModFailedErrorMsg{

	OVXFlowModFailedErrorMsgVer13(long xid, OFFlowModFailedCode code,
                                  OFErrorCauseData data) {
		super(xid, code, data);
		// TODO Auto-generated constructor stub
	}

private final Logger log = LogManager.getLogger(OVXFlowModFailedErrorMsgVer13.class.getName());

     
    
    @Override
    public void devirtualize(final OVXSwitch sw) {
        // TODO Auto-generated method stub

    }

    @Override
    public void virtualize(final PhysicalSwitch sw) {
        /*
         * TODO: For now, just report the error. In the future parse them and
         * forward to controller if need be.
         */
        log.error(this.getCode());

    }
}

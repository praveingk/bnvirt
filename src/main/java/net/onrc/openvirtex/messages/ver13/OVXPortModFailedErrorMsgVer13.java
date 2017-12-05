package net.onrc.openvirtex.messages.ver13;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXPortModFailedErrorMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFPortModFailedCode;
import org.projectfloodlight.openflow.protocol.ver13.OFPortModFailedErrorMsgVer13;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

public class OVXPortModFailedErrorMsgVer13 extends OFPortModFailedErrorMsgVer13 implements OVXPortModFailedErrorMsg{

	OVXPortModFailedErrorMsgVer13(long xid, OFPortModFailedCode code,
                                  OFErrorCauseData data) {
		super(xid, code, data);
		// TODO Auto-generated constructor stub
	}

private final Logger log = LogManager.getLogger(OVXPortModFailedErrorMsgVer13.class.getName());

     
    
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

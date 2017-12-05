package net.onrc.openvirtex.messages.ver10;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.protocol.OFErrorType;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowModFailedErrorMsgVer10;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

import com.google.common.hash.PrimitiveSink;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFlowModFailedErrorMsg;
import net.onrc.openvirtex.messages.OVXBadRequestErrorMsg;

public class OVXFlowModFailedErrorMsgVer10 extends OFFlowModFailedErrorMsgVer10 implements OVXFlowModFailedErrorMsg{

	OVXFlowModFailedErrorMsgVer10(long xid, OFFlowModFailedCode code,
			OFErrorCauseData data) {
		super(xid, code, data);
		// TODO Auto-generated constructor stub
	}

private final Logger log = LogManager.getLogger(OVXFlowModFailedErrorMsgVer10.class.getName());

     
    
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

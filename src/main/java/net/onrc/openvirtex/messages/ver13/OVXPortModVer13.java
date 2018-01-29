/*******************************************************************************
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.onrc.openvirtex.messages.ver13;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.messages.OVXErrorMsg;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPortMod;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.ver13.OFPortModVer13;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFErrorCauseData;
import org.projectfloodlight.openflow.types.OFPort;

public class OVXPortModVer13 extends OFPortModVer13 implements OVXPortMod {

   
    protected OVXPortModVer13(long xid, OFPort portNo, MacAddress hwAddr,
                              long config, long mask, long advertise) {
		super(xid, portNo, hwAddr, config, mask, advertise);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
        // TODO Auto-generated method stub
        // assume port numbers are virtual
        final OVXPort p = sw.getPort(this.getPortNo().getShortPortNumber());
        if (p == null) {
        	 ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
         	this.writeTo(buf);
         	byte[] byte_msg=buf.array();
         	OFErrorCauseData offendingMsg=OFErrorCauseData.of(byte_msg, OFVersion.OF_10);
         	
             final OVXErrorMsg err =(OVXErrorMsg) OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(this.getXid(),OFBadRequestCode.EPERM,offendingMsg);
        	
            sw.sendMsg(err, sw);
            return;
        }
        // set physical port number - anything else to do?
        final PhysicalPort phyPort = p.getPhysicalPort();
        this.portNo=phyPort.getPortNo();

        OVXMessageUtil.translateXid(this, p);
    }

}

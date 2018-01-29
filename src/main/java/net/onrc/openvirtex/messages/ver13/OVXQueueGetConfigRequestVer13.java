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

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXQueueGetConfigRequest;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.ver13.OFQueueGetConfigRequestVer13;
import org.projectfloodlight.openflow.types.OFPort;

public class OVXQueueGetConfigRequestVer13 extends OFQueueGetConfigRequestVer13 implements OVXQueueGetConfigRequest {

   
    protected OVXQueueGetConfigRequestVer13(long xid, OFPort port) {
		super(xid, port);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
        final OVXPort p = sw.getPort(this.getPort().getShortPortNumber());
        if (p == null) {
            sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                    OFBadRequestCode.EPERM, this), sw);
            return;
        }

        OVXMessageUtil.translateXid(this, p);
    }

}

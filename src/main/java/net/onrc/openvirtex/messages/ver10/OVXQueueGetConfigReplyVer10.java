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
package net.onrc.openvirtex.messages.ver10;

import java.util.List;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXQueueGetConfigReply;

import org.projectfloodlight.openflow.protocol.OFPacketQueue;
import org.projectfloodlight.openflow.protocol.ver10.OFQueueGetConfigReplyVer10;
import org.projectfloodlight.openflow.types.OFPort;

public class OVXQueueGetConfigReplyVer10 extends OFQueueGetConfigReplyVer10 implements OVXQueueGetConfigReply {

   

    protected OVXQueueGetConfigReplyVer10(long xid, OFPort port,
			List<OFPacketQueue> queues) {
		super(xid, port, queues);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        final OVXSwitch vsw = OVXMessageUtil.untranslateXid(this, sw);
        if (vsw == null) {
            // log error
            return;
        }
        // re-write port mappings
    }

}
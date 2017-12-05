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
package net.onrc.openvirtex.messages.statistics.ver13;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.statistics.OVXDescStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortDescStatsReply;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFPortDescStatsReplyVer13;

import java.util.List;
import java.util.Set;

/**
 * Virtual description statistics message handling.
 */
public class OVXPortDescStatsReplyVer13 extends OFPortDescStatsReplyVer13 implements
        OVXPortDescStatsReply {



	public OVXPortDescStatsReplyVer13(long xid, Set<OFStatsReplyFlags> flags,
                                      List<OFPortDesc> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}


    @Override
    public void virtualize(final PhysicalSwitch sw) {
        // log.error("Received illegal message form physical network; {}", msg);

    }

}
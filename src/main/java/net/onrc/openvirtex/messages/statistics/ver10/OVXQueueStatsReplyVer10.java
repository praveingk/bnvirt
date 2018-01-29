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
package net.onrc.openvirtex.messages.statistics.ver10;

import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXQueueStatsReply;


import org.projectfloodlight.openflow.protocol.OFQueueStatsEntry;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFQueueStatsReplyVer10;

/**
 * Implementation of virtualization for queue statistics reply message.
 * TODO
 */
public class OVXQueueStatsReplyVer10 extends OFQueueStatsReplyVer10 implements
        OVXQueueStatsReply {

    public OVXQueueStatsReplyVer10(long xid, Set<OFStatsReplyFlags> flags,
			List<OFQueueStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        final OVXSwitch vsw = OVXMessageUtil.untranslateXid(this, sw);
        if (vsw == null) {
            return;
        }
    }

}

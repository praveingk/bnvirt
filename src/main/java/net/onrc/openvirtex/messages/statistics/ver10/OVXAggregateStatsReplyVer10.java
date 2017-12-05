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

import java.util.Set;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXAggregateStatsReply;

import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFAggregateStatsReplyVer10;
import org.projectfloodlight.openflow.types.U64;

public class OVXAggregateStatsReplyVer10 extends OFAggregateStatsReplyVer10
        implements OVXAggregateStatsReply {

    public OVXAggregateStatsReplyVer10(long xid, Set<OFStatsReplyFlags> flags,
			U64 packetCount, U64 byteCount, long flowCount) {
		super(xid, flags, packetCount, byteCount, flowCount);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {

    }

}

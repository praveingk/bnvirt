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

import com.google.common.collect.ImmutableSet;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFPortStatsReplyVer13;

import java.util.*;

public class OVXPortStatsReplyVer13 extends OFPortStatsReplyVer13 implements
OVXPortStatsReply {

    public OVXPortStatsReplyVer13(long xid, Set<OFStatsReplyFlags> flags,
                                  List<OFPortStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}

	private Map<Short, OVXPortStatsReply> stats = null;

    @Override
    public void virtualize(final PhysicalSwitch sw) {
        stats = new HashMap<Short, OVXPortStatsReply>();

        final Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();


        for(OFPortStatsEntry entry:this.getEntries())
            {
            stats.put(entry.getPortNo().getShortPortNumber(),(OVXPortStatsReply)new OVXPortStatsReplyVer13(this.getXid(),DEFAULT_FLAGS,Arrays.asList(entry)));
        }
        sw.setPortStatistics(stats);

    
}
}

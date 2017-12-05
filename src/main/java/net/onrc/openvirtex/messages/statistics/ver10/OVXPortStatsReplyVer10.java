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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsReply;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;

import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFPortStatsReplyVer10;

public class OVXPortStatsReplyVer10 extends OFPortStatsReplyVer10 implements
OVXPortStatsReply {

    public OVXPortStatsReplyVer10(long xid, Set<OFStatsReplyFlags> flags,
			List<OFPortStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}

	private Map<Short, OVXPortStatsReply> stats = null;

    @Override
    public void virtualize(final PhysicalSwitch sw) {
        stats = new HashMap<Short, OVXPortStatsReply>();
        
       
            
            for(OFPortStatsEntry entry:this.getEntries())
            {
            stats.put(entry.getPortNo().getShortPortNumber(),(OVXPortStatsReply)new OVXPortStatsReplyVer10(this.getXid(),null,Arrays.asList(entry)));
        }
        sw.setPortStatistics(stats);

    
}
}

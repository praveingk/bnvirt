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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsReply;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowStatsReplyVer10;


public class OVXFlowStatsReplyVer10 extends OFFlowStatsReplyVer10 implements
        OVXFlowStatsReply {

    public OVXFlowStatsReplyVer10(long xid, Set<OFStatsReplyFlags> flags,
			List<OFFlowStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        if (this.getXid() != 0) {
            sw.removeFlowMods(this);
            return;
        }

        HashMap<Integer, List<OVXFlowStatsReply>> stats = new HashMap<Integer, List<OVXFlowStatsReply>>();

        
        for(OFFlowStatsEntry entry: this.getEntries()) {
            OVXFlowStatsReply single_reply=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(this.getXid(), this.getFlags(),Arrays.asList(entry));
            int tid=getTidFromCookie(entry.getCookie().getValue())	;
            addToStats(tid, single_reply, stats);

        }

            
        
        sw.setFlowStatistics(stats);
    }

    private void addToStats(int tid, OVXFlowStatsReply reply,
            HashMap<Integer, List<OVXFlowStatsReply>> stats) {
        List<OVXFlowStatsReply> statsList = stats.get(tid);
        if (statsList == null) {
            statsList = new LinkedList<OVXFlowStatsReply>();
        }
        statsList.add(reply);
        stats.put(tid, statsList);
    }

    private int getTidFromCookie(long cookie) {
        return (int) (cookie >> 32);
    }

}

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
import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSingleSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.statistics.OVXAggregateStatsRequest;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.ver13.OFAggregateStatsRequestVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFMatchV3Ver13;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OVXAggregateStatsRequestVer13 extends OFAggregateStatsRequestVer13
        implements OVXAggregateStatsRequest {

    public OVXAggregateStatsRequestVer13(
            long xid,
            Set<OFStatsRequestFlags> flags,
            TableId tableId, OFPort outPort,
            OFGroup outGroup,
            U64 cookie,
            U64 cookieMask,
            Match match)  {
		super(xid, flags, tableId, outPort, outGroup, cookie, cookieMask, match);
		// TODO Auto-generated constructor stub
	}

	private Logger log = LogManager.getLogger(
            OVXAggregateStatsRequestVer13.class.getName());

    @Override
    public void devirtualize(final OVXSwitch sw) {

        int tid = sw.getTenantId();
        HashSet<Long> uniqueCookies = new HashSet<Long>();
        int flowcount=0;
        long bytecount=0;
        long packetcount=0;		
        
        // the -1 is for beacon...
        if (this.getOutPort() == OFPort.ANY) {
             FlowTable ft = sw.getFlowTable();
             flowcount=ft.getFlowTable().size();


            for (PhysicalSwitch psw : getPhysicalSwitches(sw)) {
                List<OVXFlowStatsReply> reps = psw.getFlowStats(tid);
                if (reps != null) {
                    for (OVXFlowStatsReply s : reps) {
                    		for(OFFlowStatsEntry entry : s.getEntries())
                        if (!uniqueCookies.contains(entry.getCookie())) {
                        	bytecount=bytecount+entry.getByteCount().getValue();
                            packetcount=packetcount+entry.getPacketCount().getValue();
                            uniqueCookies.add(entry.getCookie().getValue());

                        }
                    }

                }
            }
        }

        Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();
        
        OVXAggregateStatsReplyVer13 stat = new OVXAggregateStatsReplyVer13(this.getXid(), DEFAULT_FLAGS,U64.of(packetcount), U64.of(bytecount), flowcount);
        
        
        sw.sendMsg(stat, sw);

    }

    private List<PhysicalSwitch> getPhysicalSwitches(OVXSwitch sw) {
        if (sw instanceof OVXSingleSwitch) {
            try {
                return sw.getMap().getPhysicalSwitches(sw);
            } catch (SwitchMappingException e) {
                log.debug("OVXSwitch {} does not map to any physical switches",
                        sw.getSwitchName());
                return new LinkedList<>();
            }
        }
        LinkedList<PhysicalSwitch> sws = new LinkedList<PhysicalSwitch>();
        for (OVXPort p : sw.getPorts().values()) {
            if (!sws.contains(p.getPhysicalPort().getParentSwitch())) {
                sws.add(p.getPhysicalPort().getParentSwitch());
            }
        }
        return sws;
    }

}

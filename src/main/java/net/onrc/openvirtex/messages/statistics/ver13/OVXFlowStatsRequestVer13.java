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
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.OVXSingleSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFStatsRequestFlags;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.ver13.OFFlowStatsRequestVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFMatchV3Ver13;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OVXFlowStatsRequestVer13 extends OFFlowStatsRequestVer13 implements
OVXFlowStatsRequest {

    public OVXFlowStatsRequestVer13(
            long xid,
            Set<OFStatsRequestFlags> flags,
            TableId tableId,
            OFPort outPort,
            OFGroup outGroup,
            U64 cookie,
            U64 cookieMask,
            Match match)  {
		super(xid, flags, tableId, outPort, outGroup, cookie, cookieMask,  match);
		// TODO Auto-generated constructor stub
	}

	Logger log = LogManager.getLogger(OVXFlowStatsRequestVer13.class.getName());


    @Override
    public void devirtualize(final OVXSwitch sw) {
        List<OFFlowStatsEntry> replies = new LinkedList<OFFlowStatsEntry>();
        HashSet<Long> uniqueCookies = new HashSet<Long>();
        int tid = sw.getTenantId();
        int length = 0;

        if (this.getOutPort() == OFPort.ANY) {
            for (PhysicalSwitch psw : getPhysicalSwitches(sw)) {
                List<OVXFlowStatsReply> reps = psw.getFlowStats(tid);
                if (reps != null) {
                    for (OVXFlowStatsReply stat : reps) {
                    	for(OFFlowStatsEntry entry :stat.getEntries())
                    		{
                        if (!uniqueCookies.contains(entry.getCookie().getValue())) {
                            OVXFlowMod origFM;
                            try {
                                origFM = sw.getFlowMod(entry.getCookie().getValue());
                                uniqueCookies.add(entry.getCookie().getValue());
                            } catch (MappingException e) {
                                log.warn(
                                        "FlowMod not found in FlowTable for cookie={}",
                                        entry.getCookie());
                                continue;
                            }
                            entry=entry.createBuilder().setCookie(origFM.getCookie())
                            .setMatch(origFM.getMatch())
                            .setInstructions(origFM.getInstructions())
                            .build();
                            replies.add(entry);
                        }
                    }
                    	
                    }
                }
            }
            final Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();
            OVXFlowStatsReply reply=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(this.getXid(), DEFAULT_FLAGS, replies);
            

          
            sw.sendMsg(reply, sw);

        }
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

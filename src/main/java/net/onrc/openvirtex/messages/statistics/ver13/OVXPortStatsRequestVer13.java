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
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsRequest;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFStatsRequestFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFPortStatsRequestVer13;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of virtual port statistics request.
 */
public class OVXPortStatsRequestVer13 extends OFPortStatsRequestVer13 implements
     OVXPortStatsRequest {

    public OVXPortStatsRequestVer13(long xid, Set<OFStatsRequestFlags> flags,
                                    OFPort portNo) {
		super(xid, flags, portNo);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
        List<OFPortStatsEntry> entries = new LinkedList<OFPortStatsEntry>();
        int length = 0;
        if (this.getPortNo() == OFPort.ANY) {
            for (OVXPort p : sw.getPorts().values()) {
                OVXPortStatsReply reply = p.getPhysicalPort()
                        .getParentSwitch()
                        .getPortStat(p.getPhysicalPort().getPortNo().getShortPortNumber());
                if (reply != null) {
                    /*
                     * Setting it here will also update the reference but this
                     * should not matter since we index our port stats struct by
                     * physical port number (so this info is not lost) and we
                     * always rewrite the port num to the virtual port number.
                     */
                    OFPortStatsEntry entry=reply.getEntries().get(0).createBuilder().setPortNo(p.getPortNo()).build();
                    
                    entries.add(entry);
                    System.out.println("Sending port stats reply : "+ entry.toString());
                    
                }
            }
            Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();

            OVXPortStatsReply rep =(OVXPortStatsReply) new OVXPortStatsReplyVer13(this.getXid(),DEFAULT_FLAGS,entries);
            		
            sw.sendMsg(rep, sw);
        }
    }
}

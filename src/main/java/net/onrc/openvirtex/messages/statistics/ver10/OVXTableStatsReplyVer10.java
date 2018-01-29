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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.OVXStatsRequest;
import net.onrc.openvirtex.messages.statistics.DevirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXTableStatsReply;

import org.projectfloodlight.openflow.protocol.match.Match;	
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFTableStatsEntry;
import org.projectfloodlight.openflow.protocol.OFTableStatsReply;
import org.projectfloodlight.openflow.protocol.ver10.OFTableStatsReplyVer10;

public class OVXTableStatsReplyVer10 extends OFTableStatsReplyVer10 implements
        OVXTableStatsReply{

    /*
     * TODO Ideally, this would get information about the real flowtables and
     * aggregate them in some smart way. This probably needs to be discussed
     * with the overall OVX team
     */

    public OVXTableStatsReplyVer10(long xid, Set<OFStatsReplyFlags> flags,
			List<OFTableStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}



    @Override
    public void virtualize(final PhysicalSwitch sw) {
        // TODO Auto-generated method stub

    }

}

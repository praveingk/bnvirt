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
import java.util.Collections;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXStatsReply;
import net.onrc.openvirtex.messages.OVXStatsRequest;
import net.onrc.openvirtex.messages.statistics.DevirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXTableStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXTableStatsRequest;
import net.onrc.openvirtex.messages.statistics.VirtualizableStatistic;

import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.OFStatsRequestFlags;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFTableStatsEntry;
import org.projectfloodlight.openflow.protocol.ver10.OFMatchV1Ver10;
import org.projectfloodlight.openflow.protocol.ver10.OFTableStatsRequestVer10;
import org.projectfloodlight.openflow.types.TableId;
import com.google.common.collect.ImmutableSet;

public class OVXTableStatsRequestVer10 extends OFTableStatsRequestVer10 implements
        OVXTableStatsRequest {

    /*
     * TODO Ideally, this would get information about the real flowtables and
     * aggregate them in some smart way. This probably needs to be discussed
     * with the overall OVX team
     */

    public OVXTableStatsRequestVer10(long xid, Set<OFStatsRequestFlags> flags) {
		super(xid, flags);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
		OFTableStatsEntry entry=OVXFactoryInst.myFactory.buildTableStatsEntry().setActiveCount(sw.getFlowTable().getFlowTable().size())
		.setTableId(TableId.of(1))
		.setWildcards(OFMatchV1Ver10.OFPFW_ALL & ~OFMatchV1Ver10.OFPFW_NW_DST_ALL
                & ~OFMatchV1Ver10.OFPFW_NW_DST_ALL)
                .setName("OVX vFlowTable (incomplete)")
                .setMaxEntries(100000)
                .build();
        /*
         * FIXME Currently preventing controllers from wildcarding the IP field.
         * That is if they actually look at this field.
         */




	Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();

	OVXTableStatsReply reply =(OVXTableStatsReply) new OVXTableStatsReplyVer10(this.getXid(), DEFAULT_FLAGS, Arrays.asList(entry));
        sw.sendMsg(reply, sw);
	}


}

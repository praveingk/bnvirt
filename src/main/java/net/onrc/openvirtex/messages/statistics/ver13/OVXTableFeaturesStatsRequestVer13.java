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
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.OVXFactory;
import net.onrc.openvirtex.messages.statistics.OVXTableFeaturesStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXTableStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXTableFeaturesStatsRequest;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.ver13.OFTableFeaturesStatsRequestVer13;
import org.projectfloodlight.openflow.types.TableId;

import java.util.Arrays;
import java.util.Set;
import java.util.List;

public class OVXTableFeaturesStatsRequestVer13 extends OFTableFeaturesStatsRequestVer13 implements
        OVXTableFeaturesStatsRequest {

    /*
     * TODO Ideally, this would get information about the real tables
     *
     */

    public OVXTableFeaturesStatsRequestVer13(long xid, Set<OFStatsRequestFlags> flags, List<OFTableFeatures> entries) {
		super(xid, flags, entries);
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
		OFTableFeatures entry = OVXFactoryInst.myFactory.buildTableFeatures().
				setTableId(TableId.of(1))
				.setName("OVX vFlowTable")
				.setMaxEntries(100000)
				.build();

		Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();

		OVXTableFeaturesStatsReply reply =(OVXTableFeaturesStatsReply) new OVXTableFeaturesStatsReplyVer13(this.getXid(), DEFAULT_FLAGS, Arrays.asList(entry));
		sw.sendMsg(reply, sw);
	}


}

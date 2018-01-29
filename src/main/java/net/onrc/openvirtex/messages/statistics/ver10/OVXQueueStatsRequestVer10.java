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

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.OVXStatsRequest;
import net.onrc.openvirtex.messages.statistics.DevirtualizableStatistic;
import net.onrc.openvirtex.messages.statistics.OVXQueueStatsRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFStatsRequestFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFQueueStatsRequestVer10;
import org.projectfloodlight.openflow.types.OFPort;

public class OVXQueueStatsRequestVer10 extends OFQueueStatsRequestVer10
        implements OVXQueueStatsRequest {

    public OVXQueueStatsRequestVer10(long xid, Set<OFStatsRequestFlags> flags,
			OFPort portNo, long queueId) {
		super(xid, flags, portNo, queueId);
		// TODO Auto-generated constructor stub
	}

	Logger log = LogManager
            .getLogger(OVXQueueStatsRequestVer10.class.getName());

    @Override
    public void devirtualize(final OVXSwitch sw) {
        // TODO
        log.info("Queue statistics handling not yet implemented");
    }

}

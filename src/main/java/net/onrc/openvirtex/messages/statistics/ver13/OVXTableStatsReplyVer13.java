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

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.statistics.OVXTableStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFTableStatsEntry;
import org.projectfloodlight.openflow.protocol.ver13.OFTableStatsReplyVer13;

import java.util.List;
import java.util.Set;

public class OVXTableStatsReplyVer13 extends OFTableStatsReplyVer13 implements
        OVXTableStatsReply{

    /*
     * TODO Ideally, this would get information about the real flowtables and
     * aggregate them in some smart way. This probably needs to be discussed
     * with the overall OVX team
     */

    public OVXTableStatsReplyVer13(long xid, Set<OFStatsReplyFlags> flags,
                                   List<OFTableStatsEntry> entries) {
		super(xid, flags, entries);
		// TODO Auto-generated constructor stub
	}



    @Override
    public void virtualize(final PhysicalSwitch sw) {
        // TODO Auto-generated method stub

    }

}

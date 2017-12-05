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
import net.onrc.openvirtex.messages.statistics.OVXDescStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFDescStatsReplyVer13;

import java.util.Set;

/**
 * Virtual description statistics message handling.
 */
public class OVXDescStatsReplyVer13 extends OFDescStatsReplyVer13 implements
        OVXDescStatsReply {

    

	public OVXDescStatsReplyVer13(long xid, Set<OFStatsReplyFlags> flags,
								  String mfrDesc, String hwDesc, String swDesc, String serialNum,
								  String dpDesc) {
		super(xid, flags, mfrDesc, hwDesc, swDesc, serialNum, dpDesc);
		// TODO Auto-generated constructor stub
	}


    @Override
    public void virtualize(final PhysicalSwitch sw) {
        // log.error("Received illegal message form physical network; {}", msg);

    }

}

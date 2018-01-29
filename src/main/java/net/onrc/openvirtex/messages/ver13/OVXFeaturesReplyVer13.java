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
package net.onrc.openvirtex.messages.ver13;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFCapabilities;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.ver13.OFFeaturesReplyVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFAuxId;

import java.util.List;
import java.util.Set;

public class OVXFeaturesReplyVer13 extends OFFeaturesReplyVer13 implements OVXFeaturesReply {

   
    protected OVXFeaturesReplyVer13(long xid, DatapathId datapathId,
                                    long nBuffers, short nTables, OFAuxId auxID, Set<OFCapabilities> capabilities, long reserved
                                    ) {
        //Pravein : Changed the API
		super(xid, datapathId, nBuffers, nTables, auxID, capabilities, reserved);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        // TODO: Log error, we should never receive this message here
        return;

    }

}

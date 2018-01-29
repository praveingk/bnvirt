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
package net.onrc.openvirtex.messages.ver10;

import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.OVXFeaturesReply;

import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFCapabilities;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.ver10.OFFeaturesReplyVer10;
import org.projectfloodlight.openflow.types.DatapathId;

public class OVXFeaturesReplyVer10 extends OFFeaturesReplyVer10 implements OVXFeaturesReply {

   
    protected OVXFeaturesReplyVer10(long xid, DatapathId datapathId,
			long nBuffers, short nTables, Set<OFCapabilities> capabilities,
			Set<OFActionType> actions, List<OFPortDesc> ports) {
		super(xid, datapathId, nBuffers, nTables, capabilities, actions, ports);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final PhysicalSwitch sw) {
        // TODO: Log error, we should never receive this message here
        return;

    }

}

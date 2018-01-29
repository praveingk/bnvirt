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
package net.onrc.openvirtex.messages.actions.ver10;

import java.util.List;

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.ver10.OFActionSetNwDstVer10;
import org.projectfloodlight.openflow.types.IPv4Address;

public class OVXActionNetworkLayerDestinationVer10 extends
OFActionSetNwDstVer10 implements OVXActionNetworkLayerDestination {

    public OVXActionNetworkLayerDestinationVer10(IPv4Address nwAddr) {
		super(nwAddr);
		// TODO Auto-generated constructor stub
	}

	private final Logger log = LogManager
            .getLogger(OVXActionNetworkLayerDestinationVer10.class.getName());

	@Override
    public void virtualize(final OVXSwitch sw,
            final List<OFAction> approvedActions, final OVXMatch match)
            throws ActionVirtualizationDenied {

		this.nwAddr=IPv4Address.of
        		(IPMapper.getPhysicalIp(sw.getTenantId(),
                        this.getNwAddr().getInt()));
		
        log.debug("Allocating Physical IP {}", new PhysicalIPAddress(
        		this.getNwAddr().getInt()));
        approvedActions.add(this);
        
    }

}

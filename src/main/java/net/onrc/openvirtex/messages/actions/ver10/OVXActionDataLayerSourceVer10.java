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

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.messages.actions.OVXActionDataLayerSource;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.MACAddress;

import org.projectfloodlight.openflow.protocol.OFBadActionCode;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.ver10.OFActionSetDlSrcVer10;
import org.projectfloodlight.openflow.types.MacAddress;

public class OVXActionDataLayerSourceVer10 extends OFActionSetDlSrcVer10 implements
OVXActionDataLayerSource {

    public OVXActionDataLayerSourceVer10(MacAddress dlAddr) {
		super(dlAddr);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void virtualize(final OVXSwitch sw,
            final List<OFAction> approvedActions, final OVXMatch match)
            throws ActionVirtualizationDenied {
        final MACAddress mac = MACAddress.valueOf(this.getDlAddr().toString());
        try {
            final Integer tid = sw.getMap().getMAC(mac);
            if (tid != sw.getTenantId()) {
                throw new ActionVirtualizationDenied("Target mac " + mac
                        + " is not in virtual network " + sw.getTenantId(),
                        OFBadActionCode.EPERM);
            }
            approvedActions.add(this);
        } catch (AddressMappingException e) {
            throw new ActionVirtualizationDenied("Target mac " + mac
                    + " is not in virtual network " + sw.getTenantId(),
                    OFBadActionCode.EPERM);
        }
       
    }

}

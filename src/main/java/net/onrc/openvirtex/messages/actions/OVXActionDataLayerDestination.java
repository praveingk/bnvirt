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
package net.onrc.openvirtex.messages.actions;

import java.util.List;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.messages.OVXPacketOut;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.MACAddress;

import org.openflow.protocol.OFError.OFBadActionCode;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;

/**
 * Virtual destination data layer action message.
 */
public class OVXActionDataLayerDestination extends OFActionDataLayerDestination
        implements VirtualizableAction {

    @Override
    public void virtualize(final OVXSwitch sw,
            final List<OFAction> approvedActions, final OVXMatch match)
            throws ActionVirtualizationDenied {
        System.out.println("Pravein: Virtualizaiton of MAC..");

        final MACAddress mac = MACAddress.valueOf(this.dataLayerAddress);
        final int tid;
        try {
            tid = sw.getMap().getMAC(mac);
            if (tid != sw.getTenantId()) {
                throw new ActionVirtualizationDenied("Target mac " + mac
                        + " is not in virtual network " + sw.getTenantId(),
                        OFBadActionCode.OFPBAC_EPERM);
            }
            approvedActions.add(this);
            System.out.println("Mac : "+mac.toString()+ " belong to tenant "+tid);
        } catch (AddressMappingException e) {
            throw new ActionVirtualizationDenied("Target mac " + mac
                    + " is invalid ", OFBadActionCode.OFPBAC_EPERM);
        }

    }

}

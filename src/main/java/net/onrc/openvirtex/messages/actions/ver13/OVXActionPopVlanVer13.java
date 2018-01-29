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
package net.onrc.openvirtex.messages.actions.ver13;

import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.messages.actions.OVXActionPopVlan;
import net.onrc.openvirtex.messages.actions.OVXActionStripVirtualLan;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.protocol.OVXMatchV3;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.ver13.OFActionPopVlanVer13;

import java.util.List;

public class OVXActionPopVlanVer13 extends OFActionPopVlanVer13 implements
        OVXActionPopVlan {

    @Override
    public void virtualize(final OVXSwitch sw,
            final List<OFAction> approvedActions, final OVXMatchV3 match)
            throws ActionVirtualizationDenied {
        approvedActions.add(this);
    }

}

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

import com.google.common.collect.ImmutableSet;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.OVXRoleReply;
import net.onrc.openvirtex.messages.OVXRoleRequest;
import org.projectfloodlight.openflow.protocol.OFControllerRole;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFRoleReplyVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFRoleRequestVer13;
import org.projectfloodlight.openflow.types.U64;

import java.util.Set;

/**
 * Virtual description statistics message handling.
 */
public class OVXRoleReplyVer13 extends OFRoleReplyVer13 implements OVXRoleReply {

    public OVXRoleReplyVer13(long xid, OFControllerRole role, U64 generationId) {
		super(xid, role, generationId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void devirtualize(final OVXSwitch sw)
	{

	}


}

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

import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.OVXGetConfigReply;
import net.onrc.openvirtex.messages.OVXGetConfigRequest;

import org.projectfloodlight.openflow.protocol.OFConfigFlags;
import org.projectfloodlight.openflow.protocol.ver10.OFGetConfigRequestVer10;

import com.google.common.collect.ImmutableSet;

public class OVXGetConfigRequestVer10 extends OFGetConfigRequestVer10 implements OVXGetConfigRequest {

   
    protected OVXGetConfigRequestVer10(long xid) {
		super(xid);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void devirtualize(final OVXSwitch sw) {
		final Set<OFConfigFlags> DEFAULT_FLAGS = ImmutableSet.<OFConfigFlags>of();
        final OVXGetConfigReply reply =OVXFactoryInst.myOVXFactory.buildOVXGetConfigReply(this.getXid(),DEFAULT_FLAGS,sw.getMissSendLen());
        		
        sw.sendMsg(reply, sw);

    }

}

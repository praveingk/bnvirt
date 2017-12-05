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

import com.google.common.collect.ImmutableSet;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.core.OpenVirteX;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.messages.statistics.OVXDescStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortDescStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortDescStatsRequest;
import org.projectfloodlight.openflow.protocol.OFPortDescStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.protocol.OFStatsRequestFlags;
import org.projectfloodlight.openflow.protocol.ver13.OFDescStatsRequestVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFPortDescStatsRequestVer13;

import java.util.Set;

/**
 * Virtual description statistics message handling.
 */
public class OVXPortDescStatsRequestVer13 extends OFPortDescStatsRequestVer13 implements
        OVXPortDescStatsRequest {

    public OVXPortDescStatsRequestVer13(long xid, Set<OFStatsRequestFlags> flags) {
		super(xid, flags);
		// TODO Auto-generated constructor stub
	}

	/**
     * Creates a reply object populated with the virtual switch params and
     * sends it back to the controller.
     * This is in response to receiving a Description stats request from the controller.
     *
     * @param sw the virtual switch
     * @param msg the statistics request message
     */
    @Override
    public void devirtualize(final OVXSwitch sw)
    		{
    	Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();

    	final OVXPortDescStatsReply reply=OVXFactoryInst.myOVXFactory.buildOVXPortDescStatsReply(this.getXid(), DEFAULT_FLAGS, sw.getPortList());

        sw.sendMsg(reply, sw);

    }


}

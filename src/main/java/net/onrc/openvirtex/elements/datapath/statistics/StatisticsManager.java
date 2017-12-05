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
package net.onrc.openvirtex.elements.datapath.statistics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.messages.OVXStatsRequest;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsRequest;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsRequest;
import net.onrc.openvirtex.messages.statistics.ver10.OVXPortStatsRequestVer10;
import net.onrc.openvirtex.protocol.OVXMatch;

import net.onrc.openvirtex.protocol.OVXMatchV3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowWildcardsSerializerVer10;

import com.google.common.collect.ImmutableSet;

public class StatisticsManager implements TimerTask, OVXSendMsg {

    private HashedWheelTimer timer = null;
    private PhysicalSwitch sw;

    Logger log = LogManager.getLogger(StatisticsManager.class.getName());

    private Integer refreshInterval = 30;
    private boolean stopTimer = false;

    public StatisticsManager(PhysicalSwitch sw) {
        /*
         * Get the timer from the PhysicalNetwork class.
         */
        this.timer = PhysicalNetwork.getTimer();
        this.sw = sw;
        this.refreshInterval = OpenVirteXController.getInstance()
                .getStatsRefresh();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        log.debug("Collecting stats for {}", this.sw.getSwitchName());
        sendPortStatistics();
        sendFlowStatistics(0, (short) 0);

        if (!this.stopTimer) {
            log.debug("Scheduling stats collection in {} seconds for {}",
                    this.refreshInterval, this.sw.getSwitchName());
            timeout.getTimer().newTimeout(this, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void sendFlowStatistics(int tid, short port) {
        int xid = (tid << 16) | port;
        final int DEFAULT_WILDCARDS = OFFlowWildcardsSerializerVer10.ALL_VAL;
        final OFPort DEFAULT_IN_PORT = OFPort.ZERO;
        final MacAddress DEFAULT_ETH_SRC = MacAddress.NONE;
        final MacAddress DEFAULT_ETH_DST = MacAddress.NONE;
        final OFVlanVidMatch DEFAULT_VLAN_VID = OFVlanVidMatch.NONE;
        final VlanPcp DEFAULT_VLAN_PCP = VlanPcp.NONE;
        final EthType DEFAULT_ETH_TYPE = EthType.NONE;
        final IpDscp DEFAULT_IP_DSCP = IpDscp.NONE;
        final IpProtocol DEFAULT_IP_PROTO = IpProtocol.NONE;
        final IPv4Address DEFAULT_IPV4_SRC = IPv4Address.NONE;
        final IPv4Address DEFAULT_IPV4_DST = IPv4Address.NONE;
        final TransportPort DEFAULT_TCP_SRC = TransportPort.NONE;
        final TransportPort DEFAULT_TCP_DST = TransportPort.NONE;

        OVXFlowStatsRequest freq = null;
        if (OVXFactoryInst.ofversion == 10) {
            OVXMatch match = OVXFactoryInst.myOVXFactory.buildOVXMatchV1(OFFlowWildcardsSerializerVer10.ALL_VAL, DEFAULT_IN_PORT, DEFAULT_ETH_SRC, DEFAULT_ETH_DST, DEFAULT_VLAN_VID, DEFAULT_VLAN_PCP, DEFAULT_ETH_TYPE, DEFAULT_IP_DSCP, DEFAULT_IP_PROTO, DEFAULT_IPV4_SRC, DEFAULT_IPV4_DST, DEFAULT_TCP_SRC, DEFAULT_TCP_DST, 0, null);
            freq = OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest((long) xid, ImmutableSet.<OFStatsRequestFlags>of(), match, TableId.ALL, OFPort.ANY);
        } else {
            Map<MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
            OFOxmList oxmList = new OFOxmList(oxmMap);
            OVXMatchV3 match = OVXFactoryInst.myOVXFactory.buildOVXMatchV3(oxmList,0, null);
            freq = OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest((long) xid, ImmutableSet.<OFStatsRequestFlags>of(), TableId.ALL, OFPort.ANY, OFGroup.ANY, U64.ZERO, U64.FULL_MASK, match);

        }

        sendMsg(freq, this);
    }

    private void sendPortStatistics() {

    	OVXPortStatsRequest preq =OVXFactoryInst.myOVXFactory.buildOVXPortStatsRequest((long)this.hashCode(), ImmutableSet.<OFStatsRequestFlags>of(), OFPort.ANY);
        sendMsg(preq, this);
    }

    public void start() {

        /*
         * Initially start polling quickly. Then drop down to configured value
         */
        log.info("Starting Stats collection thread for {}",
                this.sw.getSwitchName());
        timer.newTimeout(this, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info("Stopping Stats collection thread for {}",
                this.sw.getSwitchName());
        this.stopTimer = true;
    }

    @Override
    public void sendMsg(OFMessage msg, OVXSendMsg from) {
        sw.sendMsg(msg, from);
    }

    @Override
    public String getName() {
        return "Statistics Manager (" + sw.getName() + ")";
    }

    public void cleanUpTenant(Integer tenantId, short port) {
        sendFlowStatistics(tenantId, port);
    }

}

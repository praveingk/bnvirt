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
package net.onrc.openvirtex.elements.datapath;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableSet;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.statistics.StatisticsManager;
import net.onrc.openvirtex.elements.network.LoopNetwork;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.*;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsReply;
import net.onrc.openvirtex.messages.statistics.OVXPortStatsRequest;
import net.onrc.openvirtex.messages.statistics.ver10.OVXFlowStatsReplyVer10;
import net.onrc.openvirtex.messages.statistics.ver10.OVXPortStatsReplyVer10;

import net.onrc.openvirtex.protocol.OVXMatchV3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.ver13.OFInstructionApplyActionsVer13;
import org.projectfloodlight.openflow.types.*;

/**
 * The Class PhysicalSwitch.
 */
public class PhysicalSwitch extends Switch<PhysicalPort> {

    private static Logger log = LogManager.getLogger(PhysicalSwitch.class.getName());
    // The Xid mapper
    private final XidTranslator<OVXSwitch> translator;
    private StatisticsManager statsMan = null;
    private AtomicReference<Map<Short, OVXPortStatsReply>> portStats;
    private AtomicReference<Map<Integer, List<OVXFlowStatsReply>>> flowStats;

    /**
     * Unregisters OVXSwitches and associated virtual elements mapped to this
     * PhysicalSwitch. Called by unregister() when the PhysicalSwitch is torn
     * down.
     */
    class DeregAction implements Runnable {

        private PhysicalSwitch psw;
        private int tid;

        DeregAction(PhysicalSwitch s, int t) {
            this.psw = s;
            this.tid = t;
        }

        @Override
        public void run() {
            OVXSwitch vsw;
            try {
                if (psw.map.hasVirtualSwitch(psw, tid)) {
                    Map<Short,PhysicalPort> portMap = psw.getPorts();
                    Set<Short> ports = portMap.keySet();
                    for (Short port : ports) {
                        vsw = psw.map.getVirtualSwitch(psw,(int) port, tid);
                    /* save = don't destroy the switch, it can be saved */
                    boolean save = false;
                    if (vsw instanceof OVXBigSwitch) {
                        save = ((OVXBigSwitch) vsw).tryRecovery(psw);
                    }
                    if (!save) {
                        vsw.unregister();
                            System.out.println("Unregistering "+ Long.toHexString(vsw.getSwitchId()));
                        }
                    }
                }
            } catch (SwitchMappingException e) {
                // Pravein HACK : Suppress Mapping Exceptions, since we are tryng out all ports.
                //log.warn("Inconsistency in OVXMap: {}", e.getMessage());
            }
        }
    }

    /**
     * Instantiates a new physical switch.
     *
     * @param switchId
     *            the switch id
     */
    public PhysicalSwitch(final long switchId) {
        super(switchId);
        this.translator = new XidTranslator<OVXSwitch>();
        this.portStats = new AtomicReference<Map<Short, OVXPortStatsReply>>();
        this.flowStats = new AtomicReference<Map<Integer, List<OVXFlowStatsReply>>>();
        this.statsMan = new StatisticsManager(this);
    }

    /**
     * Gets the OVX port number.
     *
     * @param physicalPortNumber the physical port number
     * @param tenantId the tenant id
     * @param vLinkId the virtual link ID
     * @return the virtual port number
     */
    public Short getOVXPortNumber(final Short physicalPortNumber,
            final Integer tenantId, final Integer vLinkId) {
        return this.portMap.get(physicalPortNumber)
                .getOVXPort(tenantId, vLinkId).getPortNo().getShortPortNumber();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
     * .OFMessage)
     */
    @Override
    public void handleIO(final OFMessage msg, Channel channel) throws SwitchMappingException {
        try {
            System.out.println("Got message : "+ msg.toString());
            ((Virtualizable) msg).virtualize(this);
        } catch (final ClassCastException e) {
            PhysicalSwitch.log.error("Received illegal message : " + msg);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see net.onrc.openvirtex.elements.datapath.Switch#tearDown()
     */
    @Override
    public void tearDown() {
        PhysicalSwitch.log.info("Switch disconnected {} ",
                this.featuresReply.getDatapathId());
        this.statsMan.stop();
        this.channel.disconnect();
        this.map.removePhysicalSwitch(this);
        LoopNetwork.setDeiniitalize();
    }

    /**
     * Fill port map. Assume all ports are edges until discovery says otherwise.
     * @throws SwitchMappingException 
     */
    protected void fillPortMap() throws SwitchMappingException {
        if (this.ofversion == 10) {
            for (final OFPortDesc port : this.featuresReply.getPorts()) {
                final PhysicalPort physicalPort = new PhysicalPort(port, this, true);
                this.addPort(physicalPort);
            }
        } else if (this.ofversion == 13) {
            for (final OFPortDesc port : this.portDescStatsReply.getEntries()) {
                final PhysicalPort physicalPort = new PhysicalPort(port, this, true);
                this.addPort(physicalPort);
            }
            System.out.println("It's a 1.3");
            System.out.println(this.portMap.size());
        }
    }

    @Override
    public boolean addPort(final PhysicalPort port) throws SwitchMappingException {
        final boolean result = super.addPort(port);
        if (result) {
            PhysicalNetwork.getInstance().addPort(port);
        }
        return result;
    }

    /**
     * Removes the specified port from this PhysicalSwitch. This includes
     * removal from the switch's port map, topology discovery, and the
     * PhysicalNetwork topology.
     *
     * @param port the physical port instance
     * @return true if successful, false otherwise
     */
    public boolean removePort(final PhysicalPort port) {
        final boolean result = super.removePort(port.getPortNo().getShortPortNumber());
        if (result) {
            PhysicalNetwork pnet = PhysicalNetwork.getInstance();
            pnet.removePort(pnet.getDiscoveryManager(this.getSwitchId()), port);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.onrc.openvirtex.elements.datapath.Switch#init()
     */
    @Override
    public boolean boot() throws SwitchMappingException {
        PhysicalSwitch.log.info("Switch connected with dpid {}, name {} and type {}",
                this.featuresReply.getDatapathId(), this.getSwitchName(),
                this.desc.getHwDesc());
        PhysicalNetwork.getInstance().addSwitch(this);
        this.fillPortMap();
        if (this.ofversion == 13) {
            sendDefaultFlowMod();
        }
        this.statsMan.start();
        System.out.println("Switch "+Long.toHexString(this.switchId) + "booted up");
        LoopNetwork.initBNVirtSwitch(this);
        return true;
    }

    @Override
    public boolean initializeMeters() {
        return false;
    }

    /**
     * Removes this PhysicalSwitch from the network. Also removes associated
     * ports, links, and virtual elements mapped to it (OVX*Switch, etc.).
     */
    @Override
    public void unregister() {
        /* tear down OVXSingleSwitches mapped to this PhysialSwitch */
        for (Integer tid : this.map.listVirtualNetworks().keySet()) {
            DeregAction dereg = new DeregAction(this, tid);
            new Thread(dereg).start();
        }
        /* try to remove from network and disconnect */
        PhysicalNetwork.getInstance().removeSwitch(this);
        this.portMap.clear();
        this.tearDown();
    }

    @Override
    public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
        if ((this.channel.isOpen()) && (this.isConnected)) {
            this.channel.write(Collections.singletonList(msg));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see net.onrc.openvirtex.elements.datapath.Switch#toString()
     */
    @Override
    public String toString() {
        return "DPID : "
                + this.switchId
                + ", remoteAddr : "
                + ((this.channel == null) ? "None" : this.channel
                        .getRemoteAddress().toString());
    }

    /**
     * Gets the port.
     *
     * @param portNumber
     *            the port number
     * @return the port instance
     */
    @Override
    public PhysicalPort getPort(final Short portNumber) {
        return this.portMap.get(portNumber);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof PhysicalSwitch) {
            return this.switchId == ((PhysicalSwitch) other).switchId;
        }

        return false;
    }

    public int translate(final OFMessage ofm, final OVXSwitch sw) {
        return this.translator.translate((int)ofm.getXid(), sw);
    }

    public XidPair<OVXSwitch> untranslate(final OFMessage ofm) {
        final XidPair<OVXSwitch> pair = this.translator.untranslate((int)ofm
                .getXid());
        if (pair == null) {
            return null;
        }
        return pair;
    }

    public void setPortStatistics(Map<Short, OVXPortStatsReply> stats) {
        this.portStats.set(stats);
    }

    public void setFlowStatistics(
            Map<Integer, List<OVXFlowStatsReply>> stats) {
        this.flowStats.set(stats);

    }


    public int getOfversion() {
        return this.ofversion;
    }
    public List<OVXFlowStatsReply> getFlowStats(int tid) {
        Map<Integer, List<OVXFlowStatsReply>> stats = this.flowStats.get();
        if (stats != null && stats.containsKey(tid)) {
            return Collections.unmodifiableList(stats.get(tid));
        } else {
            if (stats == null) {
                System.out.println("stats is NULL");
            } else if (!stats.containsKey(tid)) {
                System.out.println("Stats does not contain tenant :"+ tid + " length of stats ="+ stats.size());
            }
        }
        return null;
    }

    public OVXPortStatsReply getPortStat(short portNumber) {
        Map<Short, OVXPortStatsReply> stats = this.portStats.get();
        if (stats != null) {
            return stats.get(portNumber);
        }
        return null;
    }

    public void cleanUpTenant(Integer tenantId, Short port) {
        this.statsMan.cleanUpTenant(tenantId, port);
    }

    public void removeFlowMods(OFStatsReply msg) {
        int tid = (int)msg.getXid() >> 16;
        short port = (short) (msg.getXid() & 0xFFFF);
        final short OFPP_ANY_SHORT = (short) 0xFFff;
        for (OFFlowStatsEntry stat : ((OVXFlowStatsReplyVer10)msg).getEntries()) {
        	
            if (tid != this.getTidFromCookie(stat.getCookie().getValue())) {
                continue;
            }
            if (port != 0) {
                sendDeleteFlowMod(stat, port);
                if (((OFMatchV1)stat.getMatch()).getInPort().getShortPortNumber() == port) {
                    sendDeleteFlowMod(stat, OFPP_ANY_SHORT);
                }
            } else {
                sendDeleteFlowMod(stat, OFPP_ANY_SHORT);
            }
        }
    }


    private void sendDeleteFlowMod(OFFlowStatsEntry stat, short port) {
    	OVXFlowDeleteStrict dFm=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(0, stat.getMatch(), null, 0, 0, 0, null, OFPort.of(port), null, null);
    	
        this.sendMsg(dFm, this);
    }

    public void sendDefaultFlowMod () {
        System.out.println("Adding a default FlowMod to "+ this.getSwitchId());
        ArrayList<OFAction> actions = new ArrayList<OFAction>(1);
        actions.add(OVXFactoryInst.myOVXFactory.buildOVXActionOutputV3(OFPort.CONTROLLER, 0xffFFffFF));

        Set<OFFlowModFlags> DEFAULT_FLAGS = ImmutableSet.<OFFlowModFlags>of();

        ArrayList<OFInstruction> instructions = new ArrayList<>(1);
        instructions.add(new OFInstructionApplyActionsVer13(actions));

        OVXFlowAdd addFM = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(0, U64.ZERO, U64.ZERO, TableId.ZERO, 0, 0, 0, OFBufferId.NO_BUFFER, OFPort.CONTROLLER, OFGroup.ZERO,
                DEFAULT_FLAGS,
                OVXFactoryInst.myOVXFactory.buildOVXMatchV3(OFOxmList.EMPTY, 0, null),
                instructions);

        System.out.println(addFM.toString());

        this.sendMsg(addFM, this);
    }

    private int getTidFromCookie(long cookie) {
        return (int) (cookie >> 32);
    }

    @Override
    public void handleRoleIO(OFExperimenter msg, Channel channel) {
        log.warn(
                "Received Role message {} from switch {}, but no role was requested",
                msg, this.switchName);
    }

    @Override
    public void handleRoleIOV3(OFRoleRequest msg, Channel channel) {
        log.warn(
                "Received Role message {} from switch {}, but no role was requested",
                msg, this.switchName);
    }

    @Override
    public void removeChannel(Channel channel) {

    }

}

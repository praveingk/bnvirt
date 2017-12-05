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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.Persistable;

import java.util.TreeSet;

import net.onrc.openvirtex.elements.datapath.role.RoleManager;
import net.onrc.openvirtex.elements.datapath.role.RoleManager.Role;
import net.onrc.openvirtex.elements.datapath.role.RoleManagerV3;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ControllerStateException;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.UnknownRoleException;
import net.onrc.openvirtex.messages.Devirtualizable;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.util.BitSetIndex;
import net.onrc.openvirtex.util.BitSetIndex.IndexType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.protocol.errormsg.OFBadActionErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFBadRequestErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFFlowModFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFHelloFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFPortModFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFQueueOpFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleSerializerVer10;
import org.projectfloodlight.openflow.util.LRULinkedHashMap;

/**
 * The base virtual switch.
 */
public abstract class OVXSwitch extends Switch<OVXPort> implements Persistable {


    private static Logger log = LogManager.getLogger(OVXSwitch.class.getName());

    /**
     * Datapath description string.
     * TODO: should this be made specific per type of virtual switch?
     */
    public static final String DPDESCSTRING = "OpenVirteX Virtual Switch";
    protected static int supportedActions = 0xFFF;
    protected static int bufferDimension = 4096;
    protected Integer tenantId = 0;
    // default in spec is 128
    protected Short missSendLen = 128;
    protected boolean isActive = false;
    protected OVXSwitchCapabilities capabilities;
    // The backoff counter for this switch when unconnected
    private AtomicInteger backOffCounter = null;
    protected LRULinkedHashMap<Integer, OVXPacketIn> bufferMap;
    private AtomicInteger bufferId = null;
    private final BitSetIndex portCounter;
    protected FlowTable flowTable;
    // Used to save which channel the message came in on
    private final XidTranslator<Channel> channelMux;
    /**
     * Role Manager. Saves all role requests coming from each controller. It is
     * also responsible for permitting or denying certain operations based on
     * the current role of a controller.
     */
    private final RoleManager roleMan;
    private final RoleManagerV3 roleManV3;

    /**
     * Instantiates a new OVX switch.
     *
     * @param switchId the switch id
     * @param tenantId the tenant id
     */
    protected OVXSwitch(final Long switchId, final Integer tenantId) {
        super(switchId);
        this.tenantId = tenantId;
        this.missSendLen = 0;
        this.isActive = false;
        this.capabilities = new OVXSwitchCapabilities();
        this.backOffCounter = new AtomicInteger();
        this.resetBackOff();
        this.bufferMap = new LRULinkedHashMap<Integer, OVXPacketIn>(
                OVXSwitch.bufferDimension);
        this.portCounter = new BitSetIndex(IndexType.PORT_ID);
        this.bufferId = new AtomicInteger(1);
        this.flowTable = new OVXFlowTable(this);
        this.roleMan = new RoleManager();
        roleManV3 = new RoleManagerV3();
        this.channelMux = new XidTranslator<Channel>();
        System.out.println("Initialized OVXSwitch for "+ Long.toHexString(switchId)+", "+ tenantId);
    }

    /**
     * Gets the tenant id.
     *
     * @return the tenant id
     */
    public Integer getTenantId() {
        return this.tenantId;
    }

    /**
     * Gets the miss send len.
     *
     * @return the miss send len
     */
    public short getMissSendLen() {
        return this.missSendLen;
    }

    /**
     * Sets the miss send len.
     *
     * @param missSendLen
     *            the miss send len
     * @return true, if successful
     */
    public boolean setMissSendLen(final Short missSendLen) {
        this.missSendLen = missSendLen;
        return true;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Sets the active.
     *
     * @param isActive
     *            the new active
     */
    public void setActive(final boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Gets the physical port number.
     *
     * @param ovxPortNumber
     *            the ovx port number
     * @return the physical port number
     */
    public Short getPhysicalPortNumber(final Short ovxPortNumber) {
        return this.portMap.get(ovxPortNumber).getPhysicalPortNumber();
    }

    /**
     * Resets the backoff counter.
     */
    public void resetBackOff() {
        this.backOffCounter.set(-1);
    }

    /**
     * Increments the backoff counter.
     *
     * @return the backoff counter
     */
    public int incrementBackOff() {
        return this.backOffCounter.incrementAndGet();
    }

    /**
     * Gets the next available port number.
     *
     * @return the port number
     * @throws IndexOutOfBoundException if no more port numbers are available
     */
    public short getNextPortNumber() throws IndexOutOfBoundException {
        System.out.println("Getting next port no.");
        return this.portCounter.getNewIndex().shortValue();
    }

    /**
     * Releases the given port number so it can be reused.
     *
     * @param portNumber the port number
     */
    public void relesePortNumber(short portNumber) {
        this.portCounter.releaseIndex((int) portNumber);
    }

    /**
     * Adds a default OpenFlow port to the give list of physical ports.
     *
     * @param ports the list of ports
     */
    protected void addDefaultPort(final LinkedList<OFPortDesc> ports) {
    	
    	final short OFPP_LOCAL_SHORT = (short) 0xFFfe;
    	Set<OFPortConfig> set_port_config=new HashSet<OFPortConfig>();
    	set_port_config.add(OFPortConfig.PORT_DOWN);
    	Set<OFPortState> set_port_state=new HashSet<OFPortState>();
    	set_port_state.add(OFPortState.LINK_DOWN);
    	Set<OFPortFeatures> set_port_adv_features=new HashSet<OFPortFeatures>();//O replaced by empty set
    	Set<OFPortFeatures> set_port_curr_features=new HashSet<OFPortFeatures>();
    	Set<OFPortFeatures> set_port_supp_features=new HashSet<OFPortFeatures>();
    	
    	final byte[] addr = {(byte) 0xA4, (byte) 0x23, (byte) 0x05,
                 (byte) 0x00, (byte) 0x00, (byte) 0x00};
    	
        final OFPortDesc port = OVXFactoryInst.myFactory.buildPortDesc()
        		.setPortNo(OFPort.ofShort(OFPP_LOCAL_SHORT))
        		.setName("OF Local Port")
        		.setConfig(set_port_config)
        		.setHwAddr(MacAddress.of(addr))
        		.setState(set_port_state)
        		.setAdvertised(set_port_adv_features)
        		.setCurr(set_port_curr_features)
        		.setSupported(set_port_supp_features)
        		.build();
        		
        ports.add(port);
    }
    /**
     * Registers switch in the mapping and adds it to persistent storage.
     *
     * @param physicalSwitches
     */
    public void register(final List<PhysicalSwitch> physicalSwitches) {
        this.map.addSwitches(physicalSwitches, this);
        DBManager.getInstance().save(this);
    }

    /**
     * Unregisters switch from persistent storage, from the mapping,
     * and removes all virtual elements that rely on this switch.
     */
    public void unregister() {
        DBManager.getInstance().remove(this);
        this.isActive = false;
        if (this.getPorts() != null) {
            OVXNetwork net;
            try {
                net = this.getMap().getVirtualNetwork(this.tenantId);
            } catch (NetworkMappingException e) {
                log.error(
                        "Error retrieving the network with id {}. Unregister for OVXSwitch {}"
                                + "not fully done!", this.getTenantId(),
                        this.getSwitchName());
                return;
            }
            final Set<Short> portSet = new TreeSet<Short>(this.getPorts()
                    .keySet());
            for (final Short portNumber : portSet) {
                final OVXPort port = this.getPort(portNumber);
                if (port.isEdge()) {
                    Host h = net.getHost(port);
                    if (h != null) {
                        net.getHostCounter().releaseIndex(h.getHostId());
                    }
                } else {
                    net.getLinkCounter().releaseIndex(
                            port.getLink().getInLink().getLinkId());
                }
                port.unregister();
            }
        }
        // remove the switch from the map
        try {
            this.map.getVirtualNetwork(this.tenantId).removeSwitch(this);
        } catch (NetworkMappingException e) {
            log.warn(e.getMessage());
        }

        cleanUpFlowMods(false);

        this.map.removeVirtualSwitch(this);
        this.tearDown();
    }

    private void cleanUpFlowMods(boolean isOk) {
        log.info("Cleaning up flowmods");
        List<PhysicalSwitch> physicalSwitches;
        try {
            physicalSwitches = this.map.getPhysicalSwitches(this);
        } catch (SwitchMappingException e) {
            if (!isOk) {
                log.warn(
                        "Failed to cleanUp flowmods for tenant {} on switch {}",
                        this.tenantId, this.getSwitchName());
            }
            return;
        }
        for (PhysicalSwitch sw : physicalSwitches) {
            sw.cleanUpTenant(this.tenantId, (short) 0);
        }
    }

    @Override
    public Map<String, Object> getDBIndex() {
        Map<String, Object> index = new HashMap<String, Object>();
        index.put(TenantHandler.TENANT, this.tenantId);
        return index;
    }

    @Override
    public String getDBKey() {
        return Switch.DB_KEY;
    }

    @Override
    public String getDBName() {
        return DBManager.DB_VNET;
    }

    @Override
    public Map<String, Object> getDBObject() {
        Map<String, Object> dbObject = new HashMap<String, Object>();
        dbObject.put(TenantHandler.VDPID, this.switchId);
        List<Long> switches = new ArrayList<Long>();
        try {
            for (PhysicalSwitch sw : this.map.getPhysicalSwitches(this)) {
                switches.add(sw.getSwitchId());
            }
        } catch (SwitchMappingException e) {
            return null;
        }
        dbObject.put(TenantHandler.DPIDS, switches);
        return dbObject;
    }

    @Override
    public void tearDown() {
        this.isActive = false;

        roleMan.shutDown();
        roleManV3.shutDown();

        cleanUpFlowMods(true);
        for (OVXPort p : getPorts().values()) {
            if (p.isLink()) {
                p.tearDown();
            }
        }

    }

    public List<OFPortDesc> getPortList() {
        final LinkedList<OFPortDesc> portList = new LinkedList<OFPortDesc>();
        for (final OVXPort ovxPort : this.portMap.values()) {
            final OFPortDesc ofPort =OVXFactoryInst.myFactory.buildPortDesc()
                    .setPortNo(ovxPort.getPortNo())
                    .setName(ovxPort.getName())
                    .setConfig(ovxPort.getConfig())
                    .setHwAddr(ovxPort.getHwAddr())
                    .setState(ovxPort.getState())
                    .setAdvertised(ovxPort.getAdvertised())
                    .setCurr(ovxPort.getCurr())
                    .setSupported(ovxPort.getSupported())
                    .build();

            portList.add(ofPort);
        }
        return portList;
    }

    /**
     * Generate features reply.
     */
    public void generateFeaturesReply() {
        
        final LinkedList<OFPortDesc> portList = new LinkedList<OFPortDesc>();
        for (final OVXPort ovxPort : this.portMap.values()) {
            final OFPortDesc ofPort =OVXFactoryInst.myFactory.buildPortDesc()
            		.setPortNo(ovxPort.getPortNo())
            		.setName(ovxPort.getName())
            		.setConfig(ovxPort.getConfig())
            		.setHwAddr(ovxPort.getHwAddr())
            		.setState(ovxPort.getState())
            		.setAdvertised(ovxPort.getAdvertised())
            		.setCurr(ovxPort.getCurr())
            		.setSupported(ovxPort.getSupported())
            		.build();
            		
            portList.add(ofPort);
        }

        /*
         * Giving the switch a port (the local port) which is set
         * administratively down.
         *
         * Perhaps this can be used to send the packets to somewhere
         * interesting.
         */
        this.addDefaultPort(portList);
        
        Set<OFActionType> supportedActions=new HashSet<OFActionType>(); //supportedActions = 0xFFF means adding all actions
        supportedActions.add(OFActionType.OUTPUT);
        supportedActions.add(OFActionType.SET_VLAN_VID);
        supportedActions.add(OFActionType.SET_VLAN_PCP);
        supportedActions.add(OFActionType.STRIP_VLAN);
        supportedActions.add(OFActionType.SET_DL_SRC);
        supportedActions.add(OFActionType.SET_DL_DST);
        supportedActions.add(OFActionType.SET_NW_SRC);
        supportedActions.add(OFActionType.SET_NW_DST);
        supportedActions.add(OFActionType.SET_TP_SRC);
        supportedActions.add(OFActionType.SET_TP_DST);
        supportedActions.add(OFActionType.ENQUEUE);
        supportedActions.add(OFActionType.EXPERIMENTER);
    	
        final OFFeaturesReply ofReply = OVXFactoryInst.myFactory.buildFeaturesReply()
        		.setDatapathId(DatapathId.of(this.switchId))
        		.setPorts(portList)
        		.setNBuffers(OVXSwitch.bufferDimension)
        		.setNTables((byte) 1)
        		.setCapabilities(this.capabilities.getOVXSwitchCapabilities())
        		.setActions(supportedActions)
        		.setXid(0)
        		.build();
        		
        this.setFeaturesReply(ofReply);
    }

    /**
     * Generate features reply.
     */
    public void generateFeaturesReplyV3() {

        final OFFeaturesReply ofReply = OVXFactoryInst.myFactory.buildFeaturesReply()
                .setDatapathId(DatapathId.of(this.switchId))
                .setAuxiliaryId(OFAuxId.MAIN)
                .setNBuffers(OVXSwitch.bufferDimension)
                .setNTables((byte) 1) //Currently 1 table
                .setCapabilities(this.capabilities.getOVXSwitchCapabilitiesV3())
                .setXid(0)
                .build();

        this.setFeaturesReply(ofReply);
    }

    /**
     * Boots virtual switch by connecting it to the controller.
     *
     * @return true if successful, false otherwise
     */
    @Override
    public boolean boot() {
        if (OVXFactoryInst.ofversion == 10) {
            this.generateFeaturesReply();
        } else if (OVXFactoryInst.ofversion == 13) {
            this.generateFeaturesReplyV3();
        }
        final OpenVirteXController ovxController = OpenVirteXController
                .getInstance();
        ovxController.registerOVXSwitch(this);
        this.setActive(true);
        for (OVXPort p : getPorts().values()) {
            if (p.isLink()) {
                p.boot();
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.onrc.openvirtex.elements.datapath.Switch#toString()
     */
    @Override
    public String toString() {
        return "SWITCH: switchId: " + this.switchId + " - switchName: "
                + this.switchName + " - isConnected: " + this.isConnected
                + " - tenantId: " + this.tenantId + " - missSendLength: "
                + this.missSendLen + " - isActive: " + this.isActive
                + " - capabilities: "
                + this.capabilities.getOVXSwitchCapabilities();
    }

    /**
     * Adds a packet_in to the buffer map and returns a unique buffer ID.
     *
     * @param pktIn the packet_in
     * @return the buffer ID
     */
    public synchronized int addToBufferMap(final OVXPacketIn pktIn) {
        // TODO: this isn't thread safe... fix it
        this.bufferId.compareAndSet(OVXSwitch.bufferDimension, 0);
        this.bufferMap.put(this.bufferId.get(),OVXFactoryInst.myOVXFactory.buildOVXPacketIn(pktIn));
        return this.bufferId.getAndIncrement();
    }

    /**
     * Gets a packet_in from a given buffer ID.
     *
     * @param bufId the buffer ID
     * @return packet_in packet
     */
    public OVXPacketIn getFromBufferMap(final Integer bufId) {
        return this.bufferMap.get(bufId);
    }

    /**
     * Gets the flow table.
     *
     * @return the flow table
     */
    public FlowTable getFlowTable() {
        return this.flowTable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OVXSwitch)) {
            return false;
        }
        OVXSwitch other = (OVXSwitch) obj;
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        }
        return this.switchId == other.switchId
                && this.tenantId == other.tenantId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.onrc.openvirtex.core.io.OVXSendMsg#sendMsg(org.openflow.protocol.
     * OFMessage, net.onrc.openvirtex.core.io.OVXSendMsg)
     */
    @Override
    public void sendMsg(OFMessage msg, final OVXSendMsg from) {
        System.out.println("Sending msg");
        XidPair<Channel> pair = channelMux.untranslate((int)msg.getXid());//converted Xid from long to int Check
        Channel c = null;
        if (pair != null) {
            msg=msg.createBuilder().setXid(pair.getXid()).build();
            c = pair.getSwitch();
        }

        if (this.isConnected && this.isActive) {
            if (OVXFactoryInst.ofversion == 10) {
                roleMan.sendMsg(msg, c);
            } else {
                roleManV3.sendMsg(msg, c);
            }
        } else {
            // TODO: we probably should install a drop rule here.
            log.warn(
                    "Virtual switch {} is not active or is not connected to a controller",
                    switchName);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
     * .OFMessage)
     */
    @Override
    public void handleIO(OFMessage msg, Channel channel) {
        /*
         * Save the channel the msg came in on
         */
        msg=this.setXidForHandleIO(msg,channelMux.translate((int)msg.getXid(), channel));
       
        
        try {
            /*
             * Check whether this channel (i.e., controller) is permitted to
             * send this msg to the dataplane
             */
        	if (OVXFactoryInst.ofversion == 10) {
                if (this.roleMan.canSend(channel, msg)) {
                    ((Devirtualizable) msg).devirtualize(this);
                } else {
                    denyAccess(channel, msg, this.roleMan.getRole(channel));
                }
            } else {
                if (this.roleManV3.canSend(channel, msg)) {
                    ((Devirtualizable) msg).devirtualize(this);
                } else {
                    denyAccessV3(channel, msg, this.roleManV3.getRole(channel));
                }
            }
        } catch (final ClassCastException e) {
            OVXSwitch.log.error("Received illegal message: " + msg);
            System.out.println(e);
        }
    }

    @Override
    public void handleRoleIO(OFExperimenter msg, Channel channel) {

        Role role = extractNiciraRoleRequest(channel, msg);
        try {
            this.roleMan.setRole(channel, role);
            sendRoleReply(role, (int)msg.getXid(), channel);
            log.info("Finished handling role for {}",
                    channel.getRemoteAddress());
        } catch (IllegalArgumentException | UnknownRoleException ex) {
            log.warn(ex.getMessage());
        }

    }

    /* Below method for OpenFlow 1.3 */
    public void handleRoleIOV3(OFRoleRequest msg, Channel channel) {
        OFControllerRole role = msg.getRole();
        System.out.println("Setting ovxswitch role "+ role.toString());
        try {
            this.roleManV3.setRole(channel, role);
            System.out.println("Sending role reply "+ role.toString());
            sendRoleReplyV3(role, this.roleManV3.getGenerationId(),(int)msg.getXid(), channel);
            log.info("Finished handling role for {}",
                    channel.getRemoteAddress());
        } catch (IllegalArgumentException | UnknownRoleException ex) {
        log.warn(ex.getMessage());
    }
    }

    /**
     * Gets a OVXFlowMod out of the map based on the given cookie.
     *
     * @param cookie
     *            the physical cookie
     * @return the virtual flow mod
     * @throws MappingException if the cookie is not found
     */
    public OVXFlowMod getFlowMod(final Long cookie) throws MappingException {
        return this.flowTable.getFlowMod(cookie).clone();
    }

    /**
     * Sets the channel.
     *
     * @param channel the channel
     */
    public void setChannel(Channel channel) {
        this.roleMan.addController(channel);
        this.roleManV3.addController(channel);
    }

    /**
     * Removes the given channel.
     *
     * @param channel the channel
     */
    public void removeChannel(Channel channel) {
        this.roleMan.removeChannel(channel);
        this.roleManV3.removeChannel(channel);
    }

    /**
     * Removes an entry in the mapping.
     *
     * @param cookie
     * @return The deleted FlowMod
     */
    public OVXFlowMod deleteFlowMod(final Long cookie) {
        return this.flowTable.deleteFlowMod(cookie);
    }

    /**
     * Extracts the vendor-specific (Nicira) role.
     *
     * @param chan the channel
     * @param vendorMessage the vendor message
     * @return the role
     */
    private Role extractNiciraRoleRequest(Channel chan, OFExperimenter vendorMessage) {
       
        if (vendorMessage.getExperimenter() != 0x2320L ) { //Check whether it is Nicira
            return null;
        }
        if (((OFNiciraHeader)vendorMessage).getSubtype() != 0xaL) { // check whether it is Nicira Role Request
            return null;
        }
        Role role=Role.fromNxRole(OFNiciraControllerRoleSerializerVer10.toWireValue((((OFNiciraControllerRoleRequest)vendorMessage).getRole())));
        
        if (role == null) {
            String msg = String.format("Controller: [%s], State: [%s], "
                    + "received NX_ROLE_REPLY with invalid role " + "value %d",
                    chan.getRemoteAddress(), this.toString(),
                    ((OFNiciraControllerRoleRequest)vendorMessage).getRole());
            throw new ControllerStateException(msg);
        }
        return role;
    }

    /**
     * Denies access to controller because of role state.
     *
     * @param channel the channel
     * @param m the message
     * @param role the role
     */
    private void denyAccess(Channel channel, OFMessage m, Role role) {
        log.warn(
                "Controller {} may not send message {} because role state is {}",
                channel.getRemoteAddress(), m, role);
        OFMessage e = OVXMessageUtil.makeErrorMsg(
                OFBadRequestCode.EPERM, m);
        channel.write(Collections.singletonList(e));
    }

    /**
     * Denies access to controller because of role state for OpenFlow 1.3
     *
     * @param channel the channel
     * @param m the message
     * @param role the role
     */
    private void denyAccessV3(Channel channel, OFMessage m, OFControllerRole role) {
        log.warn(
                "Controller {} may not send message {} because role state is {}",
                channel.getRemoteAddress(), m, role);
        OFMessage e = OVXMessageUtil.makeErrorMsg(
                OFBadRequestCode.EPERM, m);
        channel.write(Collections.singletonList(e));
    }
    /**
     * Sends a role reply.
     *
     * @param role the role
     * @param xid the transaction ID
     * @param channel the channel on which to send
     */
    private void sendRoleReply(Role role, int xid, Channel channel) {
        OFExperimenter experimenter = OVXFactoryInst.myFactory.buildNiciraControllerRoleReply()
        		.setXid(xid)
        		.setRole(OFNiciraControllerRoleSerializerVer10.ofWireValue((role.toNxRole())))
        		.build();
        		
        channel.write(Collections.singletonList(experimenter));
    }

    private void sendRoleReplyV3(OFControllerRole role, U64 generationId, int xid, Channel channel) {
        OFRoleReply roleReply = OVXFactoryInst.myFactory.buildRoleReply()
                .setXid(xid)
                .setRole(role)
                .setGenerationId(generationId)
                .build();
        System.out.println("Sent");
        channel.write(Collections.singletonList(roleReply));
    }
    /**
     * Generates a new XID for messages destined for the physical network.
     *
     * @param msg
     *            The OFMessage being translated
     * @param inPort
     *            The ingress port
     * @return the new transaction ID
     */
    public abstract int translate(OFMessage msg, OVXPort inPort);

    /**
     * Sends a message towards the physical network, via the PhysicalSwitch
     * mapped to this OVXSwitch.
     *
     * @param msg
     *            The OFMessage being translated
     * @param inPort
     *            The ingress port, used to identify the PhysicalSwitch
     *            underlying an OVXBigSwitch. May be null. Sends a message
     *            towards the physical network
     */
    public abstract void sendSouth(OFMessage msg, OVXPort inPort);

    public OFMessage setXidForHandleIO(OFMessage message,int xid)//TODO:temporary fix
    {
    	 OFMessage ovxmessage=null;
         if(message!=null)
         {
             String class_name=message.getClass().getName();
             String ovx_message_type=class_name.substring(class_name.lastIndexOf(".") + 1);

             if(ovx_message_type.equals("OVXHelloVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(xid);
             else if(ovx_message_type.equals("OVXAggregateStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsReply(xid,((OFAggregateStatsReply)message).getFlags(),((OFAggregateStatsReply)message).getPacketCount(), ((OFAggregateStatsReply)message).getByteCount(), ((OFAggregateStatsReply)message).getFlowCount());
             else if(ovx_message_type.equals("OVXDescStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsReply(xid,((OFDescStatsReply)message).getFlags(),((OFDescStatsReply)message).getMfrDesc(),((OFDescStatsReply)message).getHwDesc(),((OFDescStatsReply)message).getSwDesc(), ((OFDescStatsReply)message).getSerialNum(),((OFDescStatsReply)message).getDpDesc());
             else if(ovx_message_type.equals("OVXFlowStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(xid,((OFFlowStatsReply)message).getFlags(), ((OFFlowStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXPortStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsReply(xid,((OFPortStatsReply)message).getFlags() , ((OFPortStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXQueueStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsReply(xid, ((OFQueueStatsReply)message).getFlags(), ((OFQueueStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXTableStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsReply(xid,((OFTableStatsReply)message).getFlags() , ((OFTableStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXAggregateStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsRequest(xid,((OFAggregateStatsRequest)message).getFlags(), ((OFAggregateStatsRequest)message).getMatch(),((OFAggregateStatsRequest)message).getTableId(),((OFAggregateStatsRequest)message).getOutPort());
            else if(ovx_message_type.equals("OVXDescStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsRequest(xid, ((OFDescStatsRequest)message).getFlags());
             else if(ovx_message_type.equals("OVXFlowStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest(xid, ((OFFlowStatsRequest)message).getFlags(), ((OFFlowStatsRequest)message).getMatch(), ((OFFlowStatsRequest)message).getTableId(), ((OFFlowStatsRequest)message).getOutPort());
             else if(ovx_message_type.equals("OVXPortStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsRequest(xid, ((OFPortStatsRequest)message).getFlags(), ((OFPortStatsRequest)message).getPortNo());
             else if(ovx_message_type.equals("OVXQueueStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsRequest(xid,((OFQueueStatsRequest)message).getFlags(),((OFQueueStatsRequest)message).getPortNo(),((OFQueueStatsRequest)message).getQueueId());
             else if(ovx_message_type.equals("OVXTableStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsRequest(xid, ((OFTableStatsRequest)message).getFlags());
             else if(ovx_message_type.equals("OVXBadActionErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadActionErrorMsg(xid,((OFBadActionErrorMsg)message).getCode(),((OFBadActionErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXBadRequestErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(xid, ((OFBadRequestErrorMsg)message).getCode(), ((OFBadRequestErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXFlowModFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModFailedErrorMsg(xid,((OFFlowModFailedErrorMsg)message).getCode(), ((OFFlowModFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXHelloFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHelloFailedErrorMsg(xid,((OFHelloFailedErrorMsg)message).getCode(), ((OFHelloFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXPortModFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortModFailedErrorMsg(xid,((OFPortModFailedErrorMsg)message).getCode(), ((OFPortModFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXQueueOpFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueOpFailedErrorMsg(xid,((OFQueueOpFailedErrorMsg)message).getCode(), ((OFQueueOpFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXBarrierReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierReply(xid);
             else if(ovx_message_type.equals("OVXBarrierRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierRequest(xid);
             else if(ovx_message_type.equals("OVXEchoReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoReply(xid, ((OFEchoReply)message).getData());
             else if(ovx_message_type.equals("OVXEchoRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoRequest(xid, ((OFEchoRequest)message).getData());
             else if(ovx_message_type.equals("OVXFeaturesReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesReply(xid, ((OFFeaturesReply)message).getDatapathId(),((OFFeaturesReply)message).getNBuffers(), ((OFFeaturesReply)message).getNTables(), ((OFFeaturesReply)message).getCapabilities(),  ((OFFeaturesReply)message).getActions(), ((OFFeaturesReply)message).getPorts());
             else if(ovx_message_type.equals("OVXFeaturesRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesRequest(xid);
             else if(ovx_message_type.equals("OVXFlowAddVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(xid, ((OFFlowAdd)message).getMatch(),((OFFlowAdd)message).getCookie(), ((OFFlowAdd)message).getIdleTimeout(), ((OFFlowAdd)message).getHardTimeout(), ((OFFlowAdd)message).getPriority(), ((OFFlowAdd)message).getBufferId(), ((OFFlowAdd)message).getOutPort(), ((OFFlowAdd)message).getFlags(), ((OFFlowAdd)message).getActions());
             else if(ovx_message_type.equals("OVXFlowDeleteVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(xid, ((OFFlowDelete)message).getMatch(),((OFFlowDelete)message).getCookie(), ((OFFlowDelete)message).getIdleTimeout(), ((OFFlowDelete)message).getHardTimeout(), ((OFFlowDelete)message).getPriority(), ((OFFlowDelete)message).getBufferId(), ((OFFlowDelete)message).getOutPort(), ((OFFlowDelete)message).getFlags(), ((OFFlowDelete)message).getActions());
             else if(ovx_message_type.equals("OVXFlowDeleteStrictVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(xid, ((OFFlowDeleteStrict)message).getMatch(),((OFFlowDeleteStrict)message).getCookie(), ((OFFlowDeleteStrict)message).getIdleTimeout(), ((OFFlowDeleteStrict)message).getHardTimeout(), ((OFFlowDeleteStrict)message).getPriority(), ((OFFlowDeleteStrict)message).getBufferId(), ((OFFlowDeleteStrict)message).getOutPort(), ((OFFlowDeleteStrict)message).getFlags(), ((OFFlowDeleteStrict)message).getActions());
             else if(ovx_message_type.equals("OVXFlowModifyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModify(xid, ((OFFlowModify)message).getMatch(),((OFFlowModify)message).getCookie(), ((OFFlowModify)message).getIdleTimeout(), ((OFFlowModify)message).getHardTimeout(), ((OFFlowModify)message).getPriority(), ((OFFlowModify)message).getBufferId(), ((OFFlowModify)message).getOutPort(), ((OFFlowModify)message).getFlags(), ((OFFlowModify)message).getActions());
             else if(ovx_message_type.equals("OVXFlowModifyStrictVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(xid, ((OFFlowModifyStrict)message).getMatch(),((OFFlowModifyStrict)message).getCookie(), ((OFFlowModifyStrict)message).getIdleTimeout(), ((OFFlowModifyStrict)message).getHardTimeout(), ((OFFlowModifyStrict)message).getPriority(), ((OFFlowModifyStrict)message).getBufferId(), ((OFFlowModifyStrict)message).getOutPort(), ((OFFlowModifyStrict)message).getFlags(), ((OFFlowModifyStrict)message).getActions());
             else if(ovx_message_type.equals("OVXFlowRemovedVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowRemoved(xid,((OFFlowRemoved)message).getMatch(), ((OFFlowRemoved)message).getCookie(), ((OFFlowRemoved)message).getPriority(), ((OFFlowRemoved)message).getReason(), ((OFFlowRemoved)message).getDurationSec(), ((OFFlowRemoved)message).getDurationNsec(), ((OFFlowRemoved)message).getIdleTimeout(), ((OFFlowRemoved)message).getPacketCount(), ((OFFlowRemoved)message).getByteCount());
             else if(ovx_message_type.equals("OVXGetConfigReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigReply(xid,((OFGetConfigReply)message).getFlags(), ((OFGetConfigReply)message).getMissSendLen());
             else if(ovx_message_type.equals("OVXGetConfigRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigRequest(xid);
             else if(ovx_message_type.equals("OVXHelloVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(xid);
             else if(ovx_message_type.equals("OVXPacketInVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketIn(xid,((OFPacketIn)message).getBufferId(), ((OFPacketIn)message).getTotalLen(),((OFPacketIn)message).getInPort(), ((OFPacketIn)message).getReason(), ((OFPacketIn)message).getData());
             else if(ovx_message_type.equals("OVXPacketOutVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketOut(xid,((OFPacketOut)message).getBufferId(),((OFPacketOut)message).getInPort(),((OFPacketOut)message).getActions(),((OFPacketOut)message).getData());
             else if(ovx_message_type.equals("OVXPortModVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortMod(xid,((OFPortMod)message).getPortNo(),((OFPortMod)message).getHwAddr(), ((OFPortMod)message).getConfig(), ((OFPortMod)message).getMask(), ((OFPortMod)message).getAdvertise());
             else if(ovx_message_type.equals("OVXPortStatusVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatus(xid,((OFPortStatus)message).getReason(), ((OFPortStatus)message).getDesc());
             else if(ovx_message_type.equals("OVXQueueGetConfigReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigReply(xid, ((OFQueueGetConfigReply)message).getPort(), ((OFQueueGetConfigReply)message).getQueues());
             else if(ovx_message_type.equals("OVXQueueGetConfigRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigRequest(xid,((OFQueueGetConfigRequest)message).getPort());
             else if(ovx_message_type.equals("OVXSetConfigVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXSetConfig(xid, ((OFSetConfig)message).getFlags(), ((OFSetConfig)message).getMissSendLen());
             else if(ovx_message_type.equals("OVXNiciraControllerRoleReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleReply(xid,((OFNiciraControllerRoleReply)message).getRole());
             else if(ovx_message_type.equals("OVXNiciraControllerRoleRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleRequest(xid,((OFNiciraControllerRoleRequest)message).getRole());

             /* OpenFlow 1.3 Support */
             if(ovx_message_type.equals("OVXHelloVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
             else if(ovx_message_type.equals("OVXAggregateStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsReply(((OFAggregateStatsReply)message).getXid(),((OFAggregateStatsReply)message).getFlags(),((OFAggregateStatsReply)message).getPacketCount(), ((OFAggregateStatsReply)message).getByteCount(), ((OFAggregateStatsReply)message).getFlowCount());
             else if(ovx_message_type.equals("OVXPortDescStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortDescStatsReply(((OFPortDescStatsReply)message).getXid(),((OFPortDescStatsReply)message).getFlags(),((OFPortDescStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXRoleReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXRoleReply(((OFRoleReply)message).getXid(),((OFRoleReply)message).getRole(),((OFRoleReply)message).getGenerationId());
             else if(ovx_message_type.equals("OVXDescStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsReply(((OFDescStatsReply)message).getXid(),((OFDescStatsReply)message).getFlags(),((OFDescStatsReply)message).getMfrDesc(),((OFDescStatsReply)message).getHwDesc(),((OFDescStatsReply)message).getSwDesc(), ((OFDescStatsReply)message).getSerialNum(),((OFDescStatsReply)message).getDpDesc());
             else if(ovx_message_type.equals("OVXFlowStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(((OFFlowStatsReply)message).getXid(),((OFFlowStatsReply)message).getFlags(), ((OFFlowStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXPortStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsReply(((OFPortStatsReply)message).getXid(),((OFPortStatsReply)message).getFlags() , ((OFPortStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXQueueStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsReply(((OFQueueStatsReply)message).getXid(), ((OFQueueStatsReply)message).getFlags(), ((OFQueueStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXTableStatsReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsReply(((OFTableStatsReply)message).getXid(),((OFTableStatsReply)message).getFlags() , ((OFTableStatsReply)message).getEntries());
             else if(ovx_message_type.equals("OVXAggregateStatsRequestVer13"))
                 ovxmessage= OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsRequest(((OFAggregateStatsRequest)message).getXid(), ((OFAggregateStatsRequest)message).getFlags(), ((OFAggregateStatsRequest)message).getTableId(), ((OFAggregateStatsRequest)message).getOutPort(), ((OFAggregateStatsRequest)message).getOutGroup(), ((OFAggregateStatsRequest)message).getCookie(), ((OFAggregateStatsRequest)message).getCookieMask(), ((OFAggregateStatsRequest)message).getMatch());
             else if(ovx_message_type.equals("OVXPortDescStatsRequestVer13"))
                 ovxmessage = OVXFactoryInst.myOVXFactory.buildOVXPortDescStatsRequest(((OFPortDescStatsRequest) message).getXid(), ((OFPortDescStatsRequest) message).getFlags());
             else if(ovx_message_type.equals("OVXRoleRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXRoleRequest(((OFRoleRequest)message).getXid(),((OFRoleRequest)message).getRole(),((OFRoleRequest)message).getGenerationId());
             else if(ovx_message_type.equals("OVXDescStatsRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsRequest(((OFDescStatsRequest)message).getXid(), ((OFDescStatsRequest)message).getFlags());
             else if(ovx_message_type.equals("OVXFlowStatsRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest(((OFFlowStatsRequest)message).getXid(), ((OFFlowStatsRequest)message).getFlags(), ((OFFlowStatsRequest)message).getTableId(), ((OFFlowStatsRequest)message).getOutPort(), ((OFFlowStatsRequest)message).getOutGroup(), ((OFFlowStatsRequest)message).getCookie(), ((OFFlowStatsRequest)message).getCookieMask(), ((OFFlowStatsRequest)message).getMatch());
             else if(ovx_message_type.equals("OVXPortStatsRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsRequest(((OFPortStatsRequest)message).getXid(), ((OFPortStatsRequest)message).getFlags(), ((OFPortStatsRequest)message).getPortNo());
             else if(ovx_message_type.equals("OVXQueueStatsRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsRequest(((OFQueueStatsRequest)message).getXid(),((OFQueueStatsRequest)message).getFlags(),((OFQueueStatsRequest)message).getPortNo(),((OFQueueStatsRequest)message).getQueueId());
             else if(ovx_message_type.equals("OVXTableStatsRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsRequest(((OFTableStatsRequest)message).getXid(), ((OFTableStatsRequest)message).getFlags());
             else if(ovx_message_type.equals("OVXBadActionErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadActionErrorMsg(((OFBadActionErrorMsg)message).getXid(),((OFBadActionErrorMsg)message).getCode(),((OFBadActionErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXBadRequestErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(((OFBadRequestErrorMsg)message).getXid(), ((OFBadRequestErrorMsg)message).getCode(), ((OFBadRequestErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXFlowModFailedErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModFailedErrorMsg(((OFFlowModFailedErrorMsg)message).getXid(),((OFFlowModFailedErrorMsg)message).getCode(), ((OFFlowModFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXHelloFailedErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHelloFailedErrorMsg(((OFHelloFailedErrorMsg)message).getXid(),((OFHelloFailedErrorMsg)message).getCode(), ((OFHelloFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXPortModFailedErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortModFailedErrorMsg(((OFPortModFailedErrorMsg)message).getXid(),((OFPortModFailedErrorMsg)message).getCode(), ((OFPortModFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXQueueOpFailedErrorMsgVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueOpFailedErrorMsg(((OFQueueOpFailedErrorMsg)message).getXid(),((OFQueueOpFailedErrorMsg)message).getCode(), ((OFQueueOpFailedErrorMsg)message).getData());
             else if(ovx_message_type.equals("OVXBarrierReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierReply(((OFBarrierReply)message).getXid());
             else if(ovx_message_type.equals("OVXBarrierRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierRequest(((OFBarrierRequest)message).getXid());
             else if(ovx_message_type.equals("OVXEchoReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoReply(((OFEchoReply)message).getXid(), ((OFEchoReply)message).getData());
             else if(ovx_message_type.equals("OVXEchoRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoRequest(((OFEchoRequest)message).getXid(), ((OFEchoRequest)message).getData());
             else if(ovx_message_type.equals("OVXFeaturesReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesReply(((OFFeaturesReply)message).getXid(), ((OFFeaturesReply)message).getDatapathId(),((OFFeaturesReply)message).getNBuffers(), ((OFFeaturesReply)message).getNTables(),((OFFeaturesReply)message).getAuxiliaryId(), ((OFFeaturesReply)message).getCapabilities(), ((OFFeaturesReply)message).getReserved());
             else if(ovx_message_type.equals("OVXFeaturesRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesRequest(((OFFeaturesRequest)message).getXid());
             else if(ovx_message_type.equals("OVXFlowAddVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(((OFFlowAdd)message).getXid(), ((OFFlowAdd)message).getCookie(), ((OFFlowAdd)message).getCookieMask(), ((OFFlowAdd)message).getTableId(), ((OFFlowAdd)message).getIdleTimeout(), ((OFFlowAdd)message).getHardTimeout(), ((OFFlowAdd)message).getPriority(), ((OFFlowAdd)message).getBufferId(), ((OFFlowAdd)message).getOutPort(), ((OFFlowAdd)message).getOutGroup(), ((OFFlowAdd)message).getFlags(), ((OFFlowAdd)message).getMatch(), ((OFFlowAdd)message).getInstructions());
             else if(ovx_message_type.equals("OVXFlowDeleteVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(((OFFlowDelete)message).getXid(), ((OFFlowDelete)message).getCookie(), ((OFFlowDelete)message).getCookieMask(), ((OFFlowDelete)message).getTableId(), ((OFFlowDelete)message).getIdleTimeout(), ((OFFlowDelete)message).getHardTimeout(), ((OFFlowDelete)message).getPriority(), ((OFFlowDelete)message).getBufferId(), ((OFFlowDelete)message).getOutPort(), ((OFFlowDelete)message).getOutGroup(), ((OFFlowDelete)message).getFlags(), ((OFFlowDelete)message).getMatch(), ((OFFlowDelete)message).getInstructions());
             else if(ovx_message_type.equals("OVXFlowDeleteStrictVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(((OFFlowDeleteStrict)message).getXid(), ((OFFlowDeleteStrict)message).getCookie(), ((OFFlowDeleteStrict)message).getCookieMask(), ((OFFlowDeleteStrict)message).getTableId(), ((OFFlowDeleteStrict)message).getIdleTimeout(), ((OFFlowDeleteStrict)message).getHardTimeout(), ((OFFlowDeleteStrict)message).getPriority(), ((OFFlowDeleteStrict)message).getBufferId(), ((OFFlowDeleteStrict)message).getOutPort(), ((OFFlowDeleteStrict)message).getOutGroup(), ((OFFlowDeleteStrict)message).getFlags(), ((OFFlowDeleteStrict)message).getMatch(), ((OFFlowDeleteStrict)message).getInstructions());
             else if(ovx_message_type.equals("OVXFlowModifyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModify(((OFFlowModify)message).getXid(), ((OFFlowModify)message).getCookie(), ((OFFlowModify)message).getCookieMask(), ((OFFlowModify)message).getTableId(), ((OFFlowModify)message).getIdleTimeout(), ((OFFlowModify)message).getHardTimeout(), ((OFFlowModify)message).getPriority(), ((OFFlowModify)message).getBufferId(), ((OFFlowModify)message).getOutPort(), ((OFFlowModify)message).getOutGroup(), ((OFFlowModify)message).getFlags(), ((OFFlowModify)message).getMatch(), ((OFFlowModify)message).getInstructions());
             else if(ovx_message_type.equals("OVXFlowModifyStrictVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(((OFFlowModifyStrict)message).getXid(), ((OFFlowModifyStrict)message).getCookie(), ((OFFlowModifyStrict)message).getCookieMask(), ((OFFlowModifyStrict)message).getTableId(), ((OFFlowModifyStrict)message).getIdleTimeout(), ((OFFlowModifyStrict)message).getHardTimeout(), ((OFFlowModifyStrict)message).getPriority(), ((OFFlowModifyStrict)message).getBufferId(), ((OFFlowModifyStrict)message).getOutPort(), ((OFFlowModifyStrict)message).getOutGroup(), ((OFFlowModifyStrict)message).getFlags(), ((OFFlowModifyStrict)message).getMatch(), ((OFFlowModifyStrict)message).getInstructions());
             else if(ovx_message_type.equals("OVXFlowRemovedVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowRemoved(((OFFlowRemoved)message).getXid(), ((OFFlowRemoved)message).getCookie(), ((OFFlowRemoved)message).getPriority(), ((OFFlowRemoved)message).getReason(), ((OFFlowRemoved)message).getTableId(), ((OFFlowRemoved)message).getDurationSec(), ((OFFlowRemoved)message).getDurationNsec(), ((OFFlowRemoved)message).getIdleTimeout(), ((OFFlowRemoved)message).getHardTimeout(),((OFFlowRemoved)message).getPacketCount(), ((OFFlowRemoved)message).getByteCount(), ((OFFlowRemoved)message).getMatch());
             else if(ovx_message_type.equals("OVXGetConfigReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigReply(((OFGetConfigReply)message).getXid(),((OFGetConfigReply)message).getFlags(), ((OFGetConfigReply)message).getMissSendLen());
             else if(ovx_message_type.equals("OVXGetConfigRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigRequest(((OFGetConfigRequest)message).getXid());
             else if(ovx_message_type.equals("OVXHelloVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
             else if(ovx_message_type.equals("OVXPacketInVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketIn(((OFPacketIn)message).getXid(),((OFPacketIn)message).getBufferId(), ((OFPacketIn)message).getTotalLen(),((OFPacketIn)message).getReason(), ((OFPacketIn)message).getTableId(), ((OFPacketIn)message).getCookie(), ((OFPacketIn)message).getMatch(), ((OFPacketIn)message).getData());
             else if(ovx_message_type.equals("OVXPacketOutVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketOut(((OFPacketOut)message).getXid(),((OFPacketOut)message).getBufferId(),((OFPacketOut)message).getInPort(),((OFPacketOut)message).getActions(),((OFPacketOut)message).getData());
             else if(ovx_message_type.equals("OVXPortModVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortMod(((OFPortMod)message).getXid(),((OFPortMod)message).getPortNo(),((OFPortMod)message).getHwAddr(), ((OFPortMod)message).getConfig(), ((OFPortMod)message).getMask(), ((OFPortMod)message).getAdvertise());
             else if(ovx_message_type.equals("OVXPortStatusVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatus(((OFPortStatus)message).getXid(),((OFPortStatus)message).getReason(), ((OFPortStatus)message).getDesc());
             else if(ovx_message_type.equals("OVXQueueGetConfigReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigReply(((OFQueueGetConfigReply)message).getXid(), ((OFQueueGetConfigReply)message).getPort(), ((OFQueueGetConfigReply)message).getQueues());
             else if(ovx_message_type.equals("OVXQueueGetConfigRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigRequest(((OFQueueGetConfigRequest)message).getXid(),((OFQueueGetConfigRequest)message).getPort());
             else if(ovx_message_type.equals("OVXSetConfigVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXSetConfig(((OFSetConfig)message).getXid(), ((OFSetConfig)message).getFlags(), ((OFSetConfig)message).getMissSendLen());
             else if(ovx_message_type.equals("OVXNiciraControllerRoleReplyVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleReply(((OFNiciraControllerRoleReply)message).getXid(),((OFNiciraControllerRoleReply)message).getRole());
             else if(ovx_message_type.equals("OVXNiciraControllerRoleRequestVer13"))
                 ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleRequest(((OFNiciraControllerRoleRequest)message).getXid(),((OFNiciraControllerRoleRequest)message).getRole());
             else if(ovx_message_type.equals("OVXTableFeaturesStatsRequestVer13"))
                 ovxmessage = OVXFactoryInst.myOVXFactory.buildOVXTableFeaturesStatsRequest(((OFTableFeaturesStatsRequest)message).getXid(),((OFTableFeaturesStatsRequest) message).getFlags(), ((OFTableFeaturesStatsRequest) message).getEntries());
             else if(ovx_message_type.equals("OVXableFeaturesStatsReplyVer13"))
                 ovxmessage = OVXFactoryInst.myOVXFactory.buildOVXTableFeaturesStatsReply(((OFTableFeaturesStatsReply)message).getXid(),((OFTableFeaturesStatsReply) message).getFlags(), ((OFTableFeaturesStatsReply) message).getEntries());

             System.out.println("Ctrl : "+ ovx_message_type + " convert - "+ ovxmessage);

         } else {
             System.out.println("msg is null");
         }
         return ovxmessage;
    }
    
    
}

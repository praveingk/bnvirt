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
package net.onrc.openvirtex.elements.port;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.db.DBManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortReason;
import org.projectfloodlight.openflow.protocol.OFPortConfig;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortState;
import org.projectfloodlight.openflow.protocol.ver10.OFPortFeaturesSerializerVer10;
import org.projectfloodlight.openflow.types.OFPort;

import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.routing.SwitchRoute;
import net.onrc.openvirtex.util.MACAddress;
import net.onrc.openvirtex.messages.OVXPortStatus;
import net.onrc.openvirtex.elements.Persistable;

public class OVXPort extends Port<OVXSwitch, OVXLink> implements Persistable {

    private static Logger log = LogManager.getLogger(OVXPort.class.getName());

    private final Integer tenantId;
    private final PhysicalPort physicalPort;
    private boolean isActive;
    static OFPortConfig[] port_config_array=new OFPortConfig[]{};
    static Set<OFPortConfig> port_config_set=new HashSet<OFPortConfig>(Arrays.asList(port_config_array));
    static OFPortState[] port_state_array=new OFPortState[]{OFPortState.LINK_DOWN};
    static Set<OFPortState> port_state_set=new HashSet<OFPortState>(Arrays.asList(port_state_array));
    
    public OVXPort(final int tenantId, final PhysicalPort port,
            final Boolean isEdge, final short portNumber)
                    throws IndexOutOfBoundException, SwitchMappingException {
    	
    	super(OFPort.of(portNumber),
        		port.getHwAddr(), 
        		"ovxport-" + portNumber, 
        		port_config_set, 
        		port_state_set, 
        		OFPortFeaturesSerializerVer10.ofWireValue(new PortFeatures().setCurrentOVXPortFeatures().getOVXFeatures()),
        		OFPortFeaturesSerializerVer10.ofWireValue(new PortFeatures().setAdvertisedOVXPortFeatures().getOVXFeatures()),
        		OFPortFeaturesSerializerVer10.ofWireValue(new PortFeatures().setSupportedOVXPortFeatures().getOVXFeatures()),
        		OFPortFeaturesSerializerVer10.ofWireValue(new PortFeatures().setPeerOVXPortFeatures().getOVXFeatures()),
        		isEdge,
        		OVXMap.getInstance().getVirtualSwitch(port.getParentSwitch(), tenantId));
       
        this.tenantId = tenantId;
        this.physicalPort = port;
        this.isActive = false;
        /* commented @N
         * try {
            
        
        } catch (SwitchMappingException e) {
            // something pretty wrong if we get here. Not 100% on how to handle
            // this
            throw new RuntimeException("Unexpected state in OVXMap: "
                    + e.getMessage());
        } */

    }

    public OVXPort(final int tenantId, final PhysicalPort port,
            final boolean isEdge) throws IndexOutOfBoundException, SwitchMappingException {
        this(tenantId, port, isEdge, OVXMap.getInstance().getVirtualSwitch(port.getParentSwitch(), tenantId).getNextPortNumber());
      
    }

    public Integer getTenantId() {
        return this.tenantId;
    }

    public PhysicalPort getPhysicalPort() {
        return this.physicalPort;
    }

    public Short getPhysicalPortNumber() {
        return this.physicalPort.getPortNo().getShortPortNumber();
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isLink() {
        return !this.isEdge;
    }

    public void sendStatusMsg(OFPortReason reason) {
        OFPortStatus status =OVXFactoryInst.myFactory.buildPortStatus()
        		.setDesc(this)
        		.setReason(reason)
        		.build();
        
       
        this.parentSwitch.sendMsg(status, this.parentSwitch);
    }

    /**
     * Registers a port in the virtual parent switch and in the physical port.
     * @throws SwitchMappingException 
     */
    public void register() throws SwitchMappingException {
        this.parentSwitch.addPort(this);
        this.physicalPort.setOVXPort(this);
        if (this.parentSwitch.isActive()) {
            sendStatusMsg(OFPortReason.ADD);
            if (OVXFactoryInst.ofversion == 10) {
                this.parentSwitch.generateFeaturesReply();
            } else if (OVXFactoryInst.ofversion == 13) {
                this.parentSwitch.generateFeaturesReplyV3();
            }
        }
        DBManager.getInstance().save(this);
    }

    /**
     * Modifies the fields of a OVXPortStatus message so that it is consistent
     * with the configs of the corresponding OVXPort.
     *
     * @param portstat the virtual port status
     */
    public void virtualizePortStat(OVXPortStatus portstat) {
        OFPortDesc desc = portstat.getDesc();
        desc.createBuilder().setPortNo(this.getPortNo())
        .setHwAddr(this.getHwAddr())
        .setCurr(this.getCurr())
        .setAdvertised(this.getAdvertised())
        .setSupported(this.getSupported())
        .build();
        portstat.createBuilder()
        .setDesc(desc)
        .build();
        
      
    }

    /**
     * Changes the attribute of this port according to a MODIFY PortStatus.
     *
     * @param portstat the virtual port status
     */
    public OFPortDesc applyPortStatus(OVXPortStatus portstat) {
        if (portstat.getReason() != OFPortReason.MODIFY) {
            return null;
        }
        OFPortDesc psport = portstat.getDesc();
        OFPortDesc edited_port=this.createBuilder().setConfig(psport.getConfig())
        .setState(psport.getState())
        .setPeer(psport.getPeer())
        .build();
        return edited_port;
    }

    public void boot() {
        if (this.isActive) {
            return;
        }
        this.isActive = true;
        OFPortState[] port_state_array=new OFPortState[]{};
        Set<OFPortState> port_state_set=new HashSet<OFPortState>(Arrays.asList(port_state_array)); 
        this.state = port_state_set;
        if (OVXFactoryInst.ofversion == 10) {
            this.parentSwitch.generateFeaturesReply();
        } else if (OVXFactoryInst.ofversion == 13) {
            this.parentSwitch.generateFeaturesReplyV3();
        }
        if (this.parentSwitch.isActive()) {
            sendStatusMsg(OFPortReason.MODIFY);
        }
        if (this.isLink()) {
            this.getLink().getOutLink().getDstPort().boot();
        }
    }

    public void tearDown() {
        if (!this.isActive) {
            return;
        }
        this.isActive = false;
        OFPortState[] port_state_array=new OFPortState[]{OFPortState.LINK_DOWN};
        Set<OFPortState> port_state_set=new HashSet<OFPortState>(Arrays.asList(port_state_array)); 
        this.state = port_state_set;
        if (OVXFactoryInst.ofversion == 10) {
            this.parentSwitch.generateFeaturesReply();
        } else if (OVXFactoryInst.ofversion == 13) {
            this.parentSwitch.generateFeaturesReplyV3();
        }        if (this.parentSwitch.isActive()) {
            sendStatusMsg(OFPortReason.MODIFY);
        }
        if (this.isLink()) {
            this.getLink().getOutLink().getDstPort().tearDown();
        }

        cleanUpFlowMods();
    }

    public void unregister() {
        DBManager.getInstance().remove(this);
        OVXNetwork virtualNetwork = null;
        try {
            virtualNetwork = this.parentSwitch.getMap().getVirtualNetwork(
                    this.tenantId);
        } catch (NetworkMappingException e) {
            log.error(
                    "Error retrieving the network with id {}. Unregister for OVXPort {}/{} not fully done!",
                    this.getTenantId(), this.getParentSwitch().getSwitchName(),
                    this.getPortNo());
            return;
        }
        if (this.parentSwitch.isActive()) {
            sendStatusMsg(OFPortReason.DELETE);
        }
        if (this.isEdge && this.isActive) {
            Host host = virtualNetwork.getHost(this);
            host.unregister();
        } else if (!this.isEdge) {
            this.getLink().egressLink.unregister();
            this.getLink().ingressLink.unregister();
        }
        this.unMap();
        if (OVXFactoryInst.ofversion == 10) {
            this.parentSwitch.generateFeaturesReply();
        } else if (OVXFactoryInst.ofversion == 13) {
            this.parentSwitch.generateFeaturesReplyV3();
        }
        cleanUpFlowMods();
    }

    @Override
    public Map<String, Object> getDBIndex() {
        Map<String, Object> index = new HashMap<String, Object>();
        index.put(TenantHandler.TENANT, this.tenantId);
        return index;
    }

    @Override
    public String getDBKey() {
        return Port.DB_KEY;
    }

    @Override
    public String getDBName() {
        return DBManager.DB_VNET;
    }

    @Override
    public Map<String, Object> getDBObject() {
        Map<String, Object> dbObject = new HashMap<String, Object>();
        dbObject.putAll(this.getPhysicalPort().getDBObject());
        dbObject.put(TenantHandler.VPORT, this.getPortNo());
        return dbObject;
    }

    private void cleanUpFlowMods() {
        log.info("Cleaning up flowmods for sw {} port {}", this
                .getPhysicalPort().getParentSwitch().getSwitchName(),
                this.getPhysicalPortNumber());
        this.getPhysicalPort().parentSwitch.cleanUpTenant(this.tenantId,
                this.getPhysicalPortNumber());
    }

    public boolean equals(final OVXPort port) {
        return this.getPortNo() == port.getPortNo()
                && this.parentSwitch.getSwitchId() == port.getParentSwitch()
                .getSwitchId();
    }

    /**
     * Undoes mapping for this port from the OVXSwitch and PhysicalPort.
     */
    public void unMap() {
        this.parentSwitch.removePort(this.getPortNo().getShortPortNumber());
        this.physicalPort.removeOVXPort(this);
    }

    /**
     * Removes a host from this port, if it's an edge.
     *
     * @throws NetworkMappingException
     */
    public void unMapHost() throws NetworkMappingException {
        if (this.isEdge) {
            OVXNetwork virtualNetwork = this.parentSwitch.getMap()
                    .getVirtualNetwork(this.tenantId);
            Host host = virtualNetwork.getHost(this);
            /*
             * need this check since a port can be created but not have anything
             * attached to it
             */
            if (host != null) {
                host.unregister();
            }
        }
    }

    /**
     * Deletes this port after removing any links mapped to this port.
     *
     * TODO see if this can be consolidated with unregister(), because it shares
     * a lot in common
     *
     * @param stat
     *            PortStatus triggering port deletion
     * @throws NetworkMappingException
     * @throws LinkMappingException
     */
    public void handlePortDelete(OVXPortStatus stat)
            throws NetworkMappingException {
        log.debug("deleting port {}", this.getPortNo());
        handlePortDisable(stat);
        this.unregister();
    }

    /**
     * Checks if this port has associated OVXLink(s) and/or SwitchRoute(s) and
     * attempts to neatly disable them. This port and its neighbor are NOT
     * deleted. Since this port is an end point, OVXLink/SwitchRoute, there is
     * no real backup to recover to in this case, so we don't try.
     *
     * @param stat
     *            PortStatus triggering link down
     * @throws NetworkMappingException
     */
    public void handlePortDisable(OVXPortStatus stat)
            throws NetworkMappingException {
        handleLinkDisable(stat);
        handleRouteDisable(stat);
        this.tearDown();
        log.info("Sending " + stat.toString() + " as OVXSwitch "
                + this.parentSwitch.getSwitchId());
    }

    /**
     * Disables a link for LINK_DOWN or DELETE PortStats. Mapping s for the
     * OVXLink are removed only if the provided PortStat is of reason DELETE.
     *
     * @param stat the port status
     * @throws NetworkMappingException
     */
    public void handleLinkDisable(OVXPortStatus stat)
            throws NetworkMappingException {
        OVXNetwork virtualNetwork = this.parentSwitch.getMap()
                .getVirtualNetwork(this.tenantId);
        if (virtualNetwork.getHost(this) == null && this.portLink != null &&
                this.portLink.exists()) {
            OVXPort dst = this.portLink.egressLink.getDstPort();
            /* unmap vLinks and this port if DELETE */
            if (stat.getReason().equals(OFPortReason.DELETE)) {
                this.portLink.egressLink.unregister();
                this.portLink.ingressLink.unregister();
            }
            /*
             * set this and destPort as edge, and send up Modify PortStat for
             * dest port
             */
            dst.tearDown();
        }

    }

    /**
     * Removes SwitchRoutes from a BVS's routing table if the end points of the
     * route are deleted.
     *
     * @param stat
     */
    public void handleRouteDisable(OVXPortStatus stat) {
        if ((this.parentSwitch instanceof OVXBigSwitch)
                && (stat.getReason().equals(OFPortReason.DELETE))) {
            Map<OVXPort, SwitchRoute> routes = ((OVXBigSwitch) this.parentSwitch)
                    .getRouteMap().get(this);
            if (routes != null) {
                Set<SwitchRoute> rtset = Collections
                        .unmodifiableSet((Set<SwitchRoute>) routes.values());
                for (SwitchRoute route : rtset) {
                    ((OVXBigSwitch) this.parentSwitch).unregisterRoute(route
                            .getRouteId());
                }
            }
            // TODO send flowRemoved's
        }
    }

    /**
     * Brings a disabled port and its links (by association up). Currently it's
     * only the matter of setting the endpoints to nonEdge if they used to be
     * part of a link.
     *
     * @param stat
     *            PortStatus indicating link up
     * @throws NetworkMappingException
     */
    public void handlePortEnable(OVXPortStatus stat)
            throws NetworkMappingException {
        log.debug("enabling port {}", this.getPortNo());
        OVXNetwork virtualNetwork = this.parentSwitch.getMap()
                .getVirtualNetwork(this.tenantId);
        Host h = virtualNetwork.getHost(this);
        this.boot();
        if (h != null) {
            h.getPort().boot();
        } else if (this.portLink != null && this.portLink.exists()) {
            OVXPort dst = this.portLink.egressLink.getDstPort();
            dst.boot();
            dst.isEdge = false;
            this.isEdge = false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((physicalPort == null) ? 0 : physicalPort.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OVXPort)) {
            return false;
        }
        OVXPort other = (OVXPort) obj;
        if (physicalPort == null) {
            if (other.physicalPort != null) {
                return false;
            }
        } else if (!physicalPort.equals(other.physicalPort)) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        int linkId = 0;
        if (isLink()) {
            linkId = this.getLink().getOutLink().getLinkId();
        }
        return "PORT:\n- portNumber: " + this.getPortNo() + "\n- parentSwitch: "
        + this.getParentSwitch().getSwitchName()
        + "\n- virtualNetwork: " + this.getTenantId()
        + "\n- hardwareAddress: "
        + this.getHwAddr().toString()
        + "\n- config: " + this.getConfig() + "\n- state: " + this.getState()
        + "\n- currentFeatures: " + this.getCurr()
        + "\n- advertisedFeatures: " + this.getAdvertised()
        + "\n- supportedFeatures: " + this.getSupported()
        + "\n- peerFeatures: " + this.getPeer() + "\n- isEdge: "
        + this.isEdge + "\n- isActive: " + this.isActive
        + "\n- linkId: " + linkId + "\n- physicalPortNumber: "
        + this.getPhysicalPortNumber() + "\n- physicalSwitchName: "
        + this.getPhysicalPort().getParentSwitch().getSwitchName();
    }
}

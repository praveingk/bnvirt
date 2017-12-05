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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.Persistable;
import net.onrc.openvirtex.elements.datapath.DPIDandPort;
import net.onrc.openvirtex.elements.datapath.Switch;
import net.onrc.openvirtex.elements.link.Link;

import java.util.Arrays;

import net.onrc.openvirtex.util.MACAddress;

import org.projectfloodlight.openflow.protocol.ver10.OFPortConfigSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFPortDescVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFPortFeaturesSerializerVer10;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.protocol.OFPortConfig;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortFeatures;
import org.projectfloodlight.openflow.protocol.OFPortState;

/**
 * The Class Port.
 *
 * @param <T1>
 *            The Generic Switch type
 * @param <T2>
 *            The Generic Link type
 */

@SuppressWarnings("rawtypes")

public class Port<T1 extends Switch, T2 extends Link> extends OFPortDescVer10
        implements Persistable {

    /**
     * Database keyword for ports.
     */
    public static final String DB_KEY = "ports";

    protected MACAddress mac;
    protected Boolean isEdge;
    protected T1 parentSwitch;
    protected LinkPair<T2> portLink;

    // TODO: duplexing/speed on port/link???

    /**
     * Instantiates a new port.
     * 
     * @param ofPort the OpenFlow physical port
     */
    protected Port(OFPort portNo, MacAddress hwAddr, String name, Set<OFPortConfig> config, Set<OFPortState> state, Set<OFPortFeatures> curr, Set<OFPortFeatures> advertised, Set<OFPortFeatures> supported, Set<OFPortFeatures> peer,Boolean isEdge,T1 parentSwitch) {
        super(portNo, hwAddr, name, config, state, curr, advertised, supported, peer);
       /* Commented out due to redundancy @N
        if (this.getHwAddr()==null) {
        	this.createBuilder()
        	.setHwAddr(MacAddress.of(new byte[] {(byte) 0xDE, (byte) 0xAD,
                    (byte) 0xBE, (byte) 0xEF, (byte) 0xCA, (byte) 0xFE}))
                    .build();
           
        }*/
        this.mac = new MACAddress(this.getHwAddr().getBytes());
        this.isEdge = isEdge;
        this.parentSwitch = parentSwitch;
        this.portLink = null;
    }
    protected Port(OFPortDesc port)
    {
    	super(port.getPortNo(),port.getHwAddr(),port.getName(),port.getConfig(),port.getState(),
    			port.getCurr(),port.getAdvertised(),port.getSupported(),port.getPeer());
    
    }
    
    public void setHardwareAddress(final byte[] hardwareAddress) {
    	this.createBuilder()
    	.setHwAddr(MacAddress.of(hardwareAddress))
                .build();
        // no way to update MACAddress instances
        this.mac = new MACAddress(hardwareAddress);
    }

    /**
     * Gets the checks if is edge.
     *
     * @return the checks if is edge
     */
    public Boolean isEdge() {
        return this.isEdge;
    }

    /**
     * Sets the checks if is edge.
     *
     * @param isEdge
     *            the new checks if is edge
     */
    public void setEdge(final Boolean isEdge) {
        this.isEdge = isEdge;
    }

    public T1 getParentSwitch() {
        return this.parentSwitch;
    }

    /**
     * Set the link connected to this port.
     *
     * @param link
     */
    public void setInLink(T2 link) {
        if (this.portLink == null) {
            this.portLink = new LinkPair<T2>();
        }
        this.portLink.setInLink(link);
    }

    /**
     * Set the link connected to this port.
     *
     * @param link
     */
    public void setOutLink(T2 link) {
        if (this.portLink == null) {
            this.portLink = new LinkPair<T2>();
        }
        this.portLink.setOutLink(link);
    }

    /**
     * @return The physical link connected to this port
     */
    public LinkPair<T2> getLink() {
        return this.portLink;
    }

    /**
     *
     * @return the highest nominal throughput currently exposed by the port
     */
    public Integer getCurrentThroughput() {
        PortFeatures feature = new PortFeatures(this.getCurr());
        return feature.getHighestThroughput();
    }

    @Override
    public String toString() {
        return "PORT:\n- portNumber: " + this.getPortNo() + "\n- parentSwitch: "
                + this.getParentSwitch().getSwitchName()
                + "\n- hardwareAddress: "
                + MACAddress.valueOf(this.getHwAddr().getBytes()).toString()
                + "\n- config: " + this.getConfig() + "\n- state: " + this.getState()
                + "\n- currentFeatures: " + this.getCurr()
                + "\n- advertisedFeatures: " + this.getAdvertised()
                + "\n- supportedFeatures: " + this.getSupported()
                + "\n- peerFeatures: " + this.getPeer() + "\n- isEdge: "
                + this.isEdge;
    }

    /*
     * should transient features of the Port be taken into account by
     * hashCode()? They prevent ports from being fetched from maps once their
     * status changes.
     */
    @Override
    public int hashCode() {
    	final int prime = 307;
        int result = 1;
        result = prime * result + OFPortFeaturesSerializerVer10.toWireValue(this.getAdvertised());
        result = prime * result + OFPortConfigSerializerVer10.toWireValue(this.getConfig());
        result = prime * result + Arrays.hashCode(this.getHwAddr().getBytes());
        result = prime * result
                + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + this.getPortNo().getShortPortNumber();
        result = prime * result + this.parentSwitch.hashCode();
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
        if (!(obj instanceof Port)) {
            return false;
        }
        Port other = (Port) obj;
        if (parentSwitch == null) {
            if (other.parentSwitch != null) {
                return false;
            }
        } else if (!parentSwitch.equals(other.parentSwitch)) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getDBIndex() {
        return null;
    }

    @Override
    public String getDBKey() {
        return null;
    }

    @Override
    public String getDBName() {
        return null;
    }

    @Override
    public Map<String, Object> getDBObject() {
        Map<String, Object> dbObject = new HashMap<String, Object>();
        dbObject.put(TenantHandler.DPID, this.parentSwitch.getSwitchId());
        dbObject.put(TenantHandler.PORT, this.getPortNo());
        return dbObject;
    }

    public DPIDandPort toDPIDandPort() {
        return new DPIDandPort(this.parentSwitch.getSwitchId(), this.getPortNo());
    }
}

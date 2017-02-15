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
package net.onrc.openvirtex.messages;

import java.util.Arrays;
import java.util.LinkedList;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkField;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.util.MACAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.util.U16;

public class OVXPacketIn extends OFPacketIn implements Virtualizable {

    private final Logger log = LogManager
            .getLogger(OVXPacketIn.class.getName());
    private PhysicalPort port = null;
    private OVXPort ovxPort = null;
    private Integer tenantId = null;

    @Override
    public void virtualize(final PhysicalSwitch sw) {

        OVXSwitch vSwitch = OVXMessageUtil.untranslateXid(this, sw);
        /*
         * Fetching port from the physical switch
         */

        short inport = this.getInPort();

        port = sw.getPort(inport);
        Mappable map = sw.getMap();

        final OFMatch match = new OFMatch();
        match.loadFromPacket(this.getPacketData(), inport);
        /*
         * Check whether this packet arrived on an edge port.
         *
         * if it did we do not need to rewrite anything, but just find which
         * controller this should be send to.
         */
        if (this.port.isEdge()) {
            this.tenantId = this.fetchTenantId(match, map, true);
            //System.out.println("Pravein : This port is Edge.");
            if (this.tenantId == null) {
                this.log.warn(
                        "PacketIn {} does not belong to any virtual network; "
                                + "dropping and installing a temporary drop rule",
                        this);
                this.installDropRule(sw, match);
                return;
            }
            System.out.println("PacketIn from "+inport + " sw = "+ sw.getSwitchName());
            /*
             * Checks on vSwitch and the virtual port done in swndPkt.
             */
            //System.out.println("Pravein : Got a Packet in from a port of valid tenant!");
            vSwitch = this.fetchOVXSwitch(sw, port.getPortNumber(), vSwitch, map);
            //System.out.println("Pravien :Switch mapping :"+Long.toHexString(sw.getSwitchId())+":"+port.getPortNumber() +" Maps to "+ Long.toHexString(vSwitch.getSwitchId()));
            this.ovxPort = this.port.getOVXPort(this.tenantId, 0);
            if (this.ovxPort == null){
                System.out.println("Pravein : OVX port is null!!");
            }
            this.sendPkt(vSwitch, match, sw);
            this.learnHostIP(match, map);
            this.learnAddresses(match, map);
            this.log.debug("Edge PacketIn {} sent to virtual network {}", this,
                    this.tenantId);
            return;
        }

        /*
         * Below handles packets traveling in the core.
         *
         *
         * The idea here si to rewrite the packets such that the controller is
         * able to recognize them.
         *
         * For IPv4 packets and ARP packets this means rewriting the IP fields
         * and possibly the mac address fields if these packets are at the
         * egress point of a virtual link.
         */
        //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ NOT Edge port...");
        //System.out.println("Match : "+ match.toString());
        if (match.getDataLayerType() == Ethernet.TYPE_IPV4
                || match.getDataLayerType() == Ethernet.TYPE_ARP) {
            //System.out.println("Type is IPV4/ARP");
            //System.out.println("1. tenantid = "+ this.tenantId);

            PhysicalIPAddress srcIP = new PhysicalIPAddress(
                    match.getNetworkSource());
            PhysicalIPAddress dstIP = new PhysicalIPAddress(
                    match.getNetworkDestination());

            Ethernet eth = new Ethernet();
            eth.deserialize(this.getPacketData(), 0,
                    this.getPacketData().length);

            OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(),
                    eth.getDestinationMAC());
            //System.out.println("LUtils : "+lUtils.toString());
            //System.out.println("2. tenantid = "+ this.tenantId);

            // rewrite the OFMatch with the values of the link
            if (lUtils.isValid()) {
                OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(),
                        lUtils.getLinkId());
                //System.out.println("Tenant id : "+ lUtils.getTenantId());
                this.tenantId = lUtils.getTenantId();
                if (srcPort == null) {
                    this.log.error(
                            "Virtual Src Port Unknown: {}, port {} with this match {}; dropping packet",
                            sw.getName(), match.getInputPort(), match);
                    return;
                }
                this.setInPort(srcPort.getPortNumber());
                OVXLink link;
                try {
                    OVXPort dstPort = map.getVirtualNetwork(
                            lUtils.getTenantId()).getNeighborPort(srcPort);
                    //System.out.println("Pravein: Packet IN from "+ Long.toHexString(sw.getSwitchId())
                    //       +",port:"+ srcPort.getPortNumber());
                    //System.out.println("3. tenantid = "+ this.tenantId);

                    link = map.getVirtualSwitch(sw,(int) srcPort.getPortNumber(), lUtils.getTenantId())
                            .getMap().getVirtualNetwork(lUtils.getTenantId())
                            .getLink(dstPort, srcPort);
                } catch (SwitchMappingException | NetworkMappingException e) {
                    return; // same as (link == null)
                }
                //System.out.println("4. tenantid = "+ this.tenantId);

                this.ovxPort = this.port.getOVXPort(lUtils.getTenantId(),
                        link.getLinkId());
                OVXLinkField linkField = OpenVirteXController.getInstance()
                        .getOvxLinkField();
                // TODO: Need to check that the values in linkId and flowId
                // don't exceed their space
                if (linkField == OVXLinkField.MAC_ADDRESS) {
                    try {
                        LinkedList<MACAddress> macList = sw.getMap()
                                .getVirtualNetwork(this.ovxPort.getTenantId())
                                .getFlowManager()
                                .getFlowValues(lUtils.getFlowId());
                        eth.setSourceMACAddress(macList.get(0).toBytes())
                                .setDestinationMACAddress(
                                        macList.get(1).toBytes());
                        match.setDataLayerSource(eth.getSourceMACAddress())
                                .setDataLayerDestination(
                                        eth.getDestinationMACAddress());
                        //System.out.println("5. tenantid = "+ this.tenantId);

                    } catch (NetworkMappingException e) {
                        log.warn(e);
                    }
                } else if (linkField == OVXLinkField.VLAN) {
                    // TODO
                    log.warn("VLAN virtual links not yet implemented.");
                    return;
                }

            }

            if (match.getDataLayerType() == Ethernet.TYPE_ARP) {
                // ARP packet
                final ARP arp = (ARP) eth.getPayload();
                this.tenantId = this.fetchTenantId(match, map, true);
                //System.out.println("6. tenantid = "+ this.tenantId);

                try {
                    //Pravein : Ignore Translation
                    /*if (map.hasVirtualIP(srcIP)) {
                        arp.setSenderProtocolAddress(map.getVirtualIP(srcIP)
                                .getIp());
                    }
                    if (map.hasVirtualIP(dstIP)) {
                        arp.setTargetProtocolAddress(map.getVirtualIP(dstIP)
                                .getIp());
                    }*/
                } catch (Exception e) {
                    log.warn("Inconsistency in OVXMap? : {}", e);
                }
            } else if (match.getDataLayerType() == Ethernet.TYPE_IPV4) {
                try {
                    final IPv4 ip = (IPv4) eth.getPayload();
                    System.out.println("Match : "+ match.toString());
                    // TODO: Incorporate below into fetchTenantId
                    if (this.tenantId == null) {
                        this.tenantId = dstIP.getTenantId();
                    }
                } catch (Exception e) {
                    log.warn("Could not rewrite IP fields : {}", e);
                }
            } else {
                this.log.info("{} handling not yet implemented; dropping",
                        match.getDataLayerType());
                this.installDropRule(sw, match);
                return;
            }
            this.setPacketData(eth.serialize());

            //System.out.println("Towards the end of packet in tenantID = "+ this.tenantId);
            vSwitch = this.fetchOVXSwitch(sw, port.getPortNumber() ,vSwitch, map);
            //System.out.println("Packet "+ this.toString() + " of vSwitch "+ Long.toHexString(vSwitch.getSwitchId()) + " send to "+this.tenantId);
            this.sendPkt(vSwitch, match, sw);
            this.log.debug("IPv4 PacketIn {} sent to virtual network {}", this,
                    this.tenantId);
            return;
        }

        this.tenantId = this.fetchTenantId(match, map, true);
        if (this.tenantId == null) {
            this.log.warn(
                    "PacketIn {} does not belong to any virtual network; "
                            + "dropping and installing a temporary drop rule",
                    this);
            this.installDropRule(sw, match);
            return;
        }
        vSwitch = this.fetchOVXSwitch(sw, port.getPortNumber(),vSwitch, map);
        this.sendPkt(vSwitch, match, sw);
        this.log.debug("Layer2 PacketIn {} sent to virtual network {}", this,
                this.tenantId);
    }

    private void learnHostIP(OFMatch match, Mappable map) {
        if (!match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
            try {
                OVXNetwork vnet = map.getVirtualNetwork(tenantId);
                Host host = vnet.getHost(ovxPort);
                if (host != null) {
                    host.setIPAddress(match.getNetworkSource());
                } else {
                    log.warn("Host not found on virtual port {}", ovxPort);
                }
            } catch (NetworkMappingException e) {
                log.warn("Failed to lookup virtual network {}", this.tenantId);
            } catch (NullPointerException npe) {
                log.warn("No host attached at {} port {}", this.ovxPort
                        .getParentSwitch().getSwitchName(), this.ovxPort
                        .getPhysicalPortNumber());
            }
        }

    }

    private void sendPkt(final OVXSwitch vSwitch, final OFMatch match,
            final PhysicalSwitch sw) {
        if (vSwitch == null || !vSwitch.isActive()) {
            this.log.warn(
                    "Controller for virtual network {} has not yet connected "
                            + "or is down", this.tenantId);
            this.installDropRule(sw, match);
            return;
        }
        this.setBufferId(vSwitch.addToBufferMap(this));
        if (this.port != null && this.ovxPort != null
                && this.ovxPort.isActive()) {
            this.setInPort(this.ovxPort.getPortNumber());
            if ((this.packetData != null)
                    && (vSwitch.getMissSendLen() != OVXSetConfig.MSL_FULL)) {
                this.packetData = Arrays.copyOf(this.packetData,
                        U16.f(vSwitch.getMissSendLen()));
                this.setLengthU(OFPacketIn.MINIMUM_LENGTH
                        + this.packetData.length);
            }
            //System.out.println("Pravein : Sending msg to Switch.. port= "+ this.port.getPortNumber() + " phys ovx = "+ this.ovxPort.getPhysicalPortNumber());
            vSwitch.sendMsg(this, sw);
        } else if (this.port == null) {
            log.error("The port {} doesn't belong to the physical switch {}",
                    this.getInPort(), sw.getName());
        } else if (this.ovxPort == null || !this.ovxPort.isActive()) {
            log.error(
                    "Virtual port associated to physical port {} in physical switch {} for "
                            + "virtual network {} is not defined or inactive",
                    this.getInPort(), sw.getName(), this.tenantId);
        }
    }

    private void learnAddresses(final OFMatch match, final Mappable map) {
        if (match.getDataLayerType() == Ethernet.TYPE_IPV4
                || match.getDataLayerType() == Ethernet.TYPE_ARP) {
            if (!match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
                IPMapper.getPhysicalIp(this.tenantId, match.getNetworkSource());
            }
            if (!match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
                IPMapper.getPhysicalIp(this.tenantId,
                        match.getNetworkDestination());
            }
        }
    }

    private void installDropRule(final PhysicalSwitch sw, final OFMatch match) {
        final OVXFlowMod fm = new OVXFlowMod();
        fm.setMatch(match);
        fm.setBufferId(this.getBufferId());
        fm.setHardTimeout((short) 1);
        sw.sendMsg(fm, sw);
    }

    private Integer fetchTenantId(final OFMatch match, final Mappable map,
            final boolean useMAC) {
        MACAddress mac = MACAddress.valueOf(match.getDataLayerSource());
        //System.out.println("Pravein: Fetching TenantID");
        //System.out.println("OFMatch : "+ match.toString());
        if (useMAC && map.hasMAC(mac)) {
            try {
                return map.getMAC(mac);
            } catch (AddressMappingException e) {
                log.warn("Tried to return non-mapped MAC address : {}", e);
            }
        }
        //System.out.println("Warning!! Mac : Dint find it in the map");
        return null;
    }

    private OVXSwitch fetchOVXSwitch(PhysicalSwitch psw, short port, OVXSwitch vswitch,
            Mappable map) {
        if (vswitch == null) {
            try {
                System.out.println("Fetching virtual switch  for tenantID "+ this.tenantId);
                vswitch = map.getVirtualSwitch(psw, (int) port, this.tenantId);
            } catch (SwitchMappingException e) {
                log.warn("Cannot fetch non-mapped OVXSwitch: {}", e);
            }
        }
        return vswitch;
    }

    public OVXPacketIn(final OVXPacketIn pktIn) {
        this.bufferId = pktIn.bufferId;
        this.inPort = pktIn.inPort;
        this.length = pktIn.length;
        this.packetData = pktIn.packetData;
        this.reason = pktIn.reason;
        this.totalLength = pktIn.totalLength;
        this.type = pktIn.type;
        this.version = pktIn.version;
        this.xid = pktIn.xid;
    }

    public OVXPacketIn() {
        super();
    }

    public OVXPacketIn(final byte[] data, final short portNumber) {
        this();
        this.setInPort(portNumber);
        this.setBufferId(OFPacketOut.BUFFER_ID_NONE);
        this.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        this.setPacketData(data);
        this.setTotalLength((short) (OFPacketIn.MINIMUM_LENGTH + this
                .getPacketData().length));
        this.setLengthU(OFPacketIn.MINIMUM_LENGTH + this.getPacketData().length);
    }

}

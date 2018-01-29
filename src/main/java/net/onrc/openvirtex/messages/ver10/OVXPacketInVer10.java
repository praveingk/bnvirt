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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
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
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.U8;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFFlowWildcards;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.types.U16;

import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.messages.OVXSetConfig;

import org.projectfloodlight.openflow.protocol.OFPacketInReason;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowWildcardsSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFPacketInVer10;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanPcp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class OVXPacketInVer10 extends OFPacketInVer10 implements OVXPacketIn {

   
    public OVXPacketInVer10(long xid, OFBufferId bufferId, int totalLen,
			OFPort inPort, OFPacketInReason reason, byte[] data) {
		super(xid, bufferId, totalLen, inPort, reason, data);
		// TODO Auto-generated constructor stub
	}
    public OVXPacketInVer10(final OVXPacketIn pktIn) {
    	this(pktIn.getXid(),pktIn.getBufferId(),pktIn.getTotalLen(),pktIn.getInPort(),pktIn.getReason(),pktIn.getData());
        
    }

    public OVXPacketInVer10() {
        this(0,null,0,null,null,null);
    }

    public OVXPacketInVer10(final byte[] data, final short portNumber) {
        this(DEFAULT_XID,OFBufferId.NO_BUFFER,(short) (OFPacketInVer10.MINIMUM_LENGTH + data.length),OFPort.ofShort(portNumber),OFPacketInReason.NO_MATCH,data);
       
    }

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

        short inport = this.getInPort().getShortPortNumber();
        port = sw.getPort(inport);
        Mappable map = sw.getMap();

        Match match =  this.loadFromPacket(this.getData(), inport);
        /*
         * Check whether this packet arrived on an edge port.
         *
         * if it did we do not need to rewrite anything, but just find which
         * controller this should be send to.
         */
        if (this.port.isEdge()) {
            this.tenantId = this.fetchTenantId(match, map, true);
            if (this.tenantId == null) {
                this.log.warn(
                        "PacketIn {} does not belong to any virtual network; "
                                + "dropping and installing a temporary drop rule",
                        this);
                this.installDropRule(sw, match);
                return;
            }

            /*
             * Checks on vSwitch and the virtual port done in swndPkt.
             */
            vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);
            this.ovxPort = this.port.getOVXPort(this.tenantId, 0);
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

        if (((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_IPV4
                || ((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_ARP) {
            PhysicalIPAddress srcIP = new PhysicalIPAddress(
                    ((OFMatchV1)match).getIpv4Src().getInt());
            PhysicalIPAddress dstIP = new PhysicalIPAddress(
                    ((OFMatchV1)match).getIpv4Dst().getInt());

            Ethernet eth = new Ethernet();
            eth.deserialize(this.getData(), 0,
                    this.getData().length);

            OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(),
                    eth.getDestinationMAC());
            // rewrite the Match with the values of the link
            if (lUtils.isValid()) {
                OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(),
                        lUtils.getLinkId());
                if (srcPort == null) {
                    this.log.error(
                            "Virtual Src Port Unknown: {}, port {} with this match {}; dropping packet",
                            sw.getName(), ((OFMatchV1)match).getInPort(), match);
                    return;
                }
                this.inPort=srcPort.getPortNo();
                OVXLink link;
                try {
                    OVXPort dstPort = map.getVirtualNetwork(
                            lUtils.getTenantId()).getNeighborPort(srcPort);
                    link = map.getVirtualSwitch(sw, lUtils.getTenantId())
                            .getMap().getVirtualNetwork(lUtils.getTenantId())
                            .getLink(dstPort, srcPort);
                } catch (SwitchMappingException | NetworkMappingException e) {
                    return; // same as (link == null)
                }
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
                        match=((OFMatchV1)match).createBuilder()
                        		.setEthSrc(MacAddress.of(eth.getSourceMACAddress()))
                                .setEthDst(MacAddress.of(eth.getDestinationMACAddress()))
                                .build();
                    } catch (NetworkMappingException e) {
                        log.warn(e);
                    }
                } else if (linkField == OVXLinkField.VLAN) {
                    // TODO
                    log.warn("VLAN virtual links not yet implemented.");
                    return;
                }

            }

            if (((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_ARP) {
                // ARP packet
                final ARP arp = (ARP) eth.getPayload();
                this.tenantId = this.fetchTenantId(match, map, true);
                try {
                    if (map.hasVirtualIP(srcIP)) {
                        arp.setSenderProtocolAddress(map.getVirtualIP(srcIP)
                                .getIp());
                    }
                    if (map.hasVirtualIP(dstIP)) {
                        arp.setTargetProtocolAddress(map.getVirtualIP(dstIP)
                                .getIp());
                    }
                } catch (AddressMappingException e) {
                    log.warn("Inconsistency in OVXMap? : {}", e);
                }
            } else if (((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_IPV4) {
                try {
                    final IPv4 ip = (IPv4) eth.getPayload();
                    ip.setDestinationAddress(map.getVirtualIP(dstIP).getIp());
                    ip.setSourceAddress(map.getVirtualIP(srcIP).getIp());
                    // TODO: Incorporate below into fetchTenantId
                    if (this.tenantId == null) {
                        this.tenantId = dstIP.getTenantId();
                    }
                } catch (AddressMappingException e) {
                    log.warn("Could not rewrite IP fields : {}", e);
                }
            } else {
                this.log.info("{} handling not yet implemented; dropping",
                        ((OFMatchV1)match).getEthType());
                this.installDropRule(sw, match);
                return;
            }
            this.data=eth.serialize();

            vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);

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
        vSwitch = this.fetchOVXSwitch(sw, vSwitch, map);
        this.sendPkt(vSwitch, match, sw);
        this.log.debug("Layer2 PacketIn {} sent to virtual network {}", this,
                this.tenantId);
    }

    private void learnHostIP(Match match, Mappable map) {
        if (!((OFMatchV1)match).isFullyWildcarded(MatchField.IPV4_SRC)) {
            try {
                OVXNetwork vnet = map.getVirtualNetwork(tenantId);
                Host host = vnet.getHost(ovxPort);
                if (host != null) {
                    host.setIPAddress(((OFMatchV1)match).getIpv4Src().getInt());
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

    private void sendPkt(final OVXSwitch vSwitch, final Match match,
            final PhysicalSwitch sw) {
        if (vSwitch == null || !vSwitch.isActive()) {
            this.log.warn(
                    "Controller for virtual network {} has not yet connected "
                            + "or is down", this.tenantId);
            this.installDropRule(sw, match);
            return;
        }
        this.bufferId=OFBufferId.of(vSwitch.addToBufferMap(this));
        if (this.port != null && this.ovxPort != null
                && this.ovxPort.isActive()) {
            this.inPort=this.ovxPort.getPortNo();
            if ((this.getData() != null)
                    && (vSwitch.getMissSendLen() != OVXSetConfigVer10.MSL_FULL)) {
                this.data = Arrays.copyOf(this.getData(),
                        U16.f(vSwitch.getMissSendLen()));
                
            }
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

    private void learnAddresses(final Match match, final Mappable map) {
        if (((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_IPV4
                || ((OFMatchV1)match).getEthType().getValue() == Ethernet.TYPE_ARP) {
            if (!((OFMatchV1)match).isFullyWildcarded(MatchField.IPV4_SRC)) {
                IPMapper.getPhysicalIp(this.tenantId, ((OFMatchV1)match).getIpv4Src().getInt());
            }
            if (!((OFMatchV1)match).isFullyWildcarded(MatchField.IPV4_DST)) {
                IPMapper.getPhysicalIp(this.tenantId,
                        ((OFMatchV1)match).getIpv4Dst().getInt());
            }
        }
    }

    private void installDropRule(final PhysicalSwitch sw, final Match match) {
    	final U64 DEFAULT_COOKIE = U64.ZERO;
    	final OFPort DEFAULT_OUT_PORT = OFPort.ANY;
    	 final Set<OFFlowModFlags> DEFAULT_FLAGS = ImmutableSet.<OFFlowModFlags>of();
    	 final List<OFAction> DEFAULT_ACTIONS = ImmutableList.<OFAction>of();
    	 final OVXFlowMod fm =OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(0, match, DEFAULT_COOKIE, 0, (short) 1, 0, this.getBufferId(), DEFAULT_OUT_PORT, DEFAULT_FLAGS, DEFAULT_ACTIONS);		
        sw.sendMsg(fm, sw);
    }

    private Integer fetchTenantId(final Match match, final Mappable map,
            final boolean useMAC) {
        MACAddress mac = MACAddress.valueOf(((OFMatchV1)match).getEthSrc().toString());
        if (useMAC && map.hasMAC(mac)) {
            try {
                return map.getMAC(mac);
            } catch (AddressMappingException e) {
                log.warn("Tried to return non-mapped MAC address : {}", e);
            }
        }
        return null;
    }

    private OVXSwitch fetchOVXSwitch(PhysicalSwitch psw, OVXSwitch vswitch,
            Mappable map) {
        if (vswitch == null) {
            try {
                vswitch = map.getVirtualSwitch(psw, this.tenantId);
            } catch (SwitchMappingException e) {
                log.warn("Cannot fetch non-mapped OVXSwitch: {}", e);
            }
        }
        return vswitch;
    }

    /**
     * Initializes this Match structure with the corresponding data from the
     * specified packet. Must specify the input port, to ensure that
     * this.in_port is set correctly. Specify OFPort.NONE or OFPort.ANY if input
     * port not applicable or available
     *
     * @param packetData
     *            The packet's data
     * @param inputPort
     *            the port the packet arrived on
     */
    public Match loadFromPacket(final byte[] packetData, final short inputPort) {
        short scratch;
        int transportOffset = 34;
        final ByteBuffer packetDataBB = ByteBuffer.wrap(packetData);
        final int limit = packetDataBB.limit();

        
        Set<OFFlowWildcards> wildcards=new HashSet<OFFlowWildcards>();
        if (OFPort.ofShort(inputPort) == OFPort.ALL) {
        	wildcards.add(OFFlowWildcards.IN_PORT);
        }

        assert limit >= 14;
        // dl dst
       
        byte[] ethDst = new byte[6];
        packetDataBB.get(ethDst);
        // dl src
        byte[] ethSrc = new byte[6];
        packetDataBB.get(ethSrc);
        // dl type
        
        short ethType=packetDataBB.getShort();
        
        short vlanVid=0;
        byte vlanPcp=0;
        

        if (ethType != (short) 0x8100) { // need cast to avoid
                                                         // signed
            // bug
        	
            vlanVid=(short) 0xffff;
            vlanPcp=(byte) 0;
        } else {
            // has vlan tag
            scratch = packetDataBB.getShort();
            vlanVid=(short) (0xfff & scratch);
            vlanPcp=(byte) ((0xe000 & scratch) >> 13);
            ethType = packetDataBB.getShort();
        }
        byte ipDscp=0;
        byte ipProtocol=0;
        int ipv4Src=0;
        int ipv4Dst=0;

       
        
        switch (ethType) {
        case 0x0800:
            // ipv4
            // check packet length
            scratch = packetDataBB.get();
            scratch = (short) (0xf & scratch);
            transportOffset = packetDataBB.position() - 1 + scratch * 4;
            // nw tos (dscp)
            scratch = packetDataBB.get();
            ipDscp=(byte) ((0xfc & scratch) >> 2);
            // nw protocol
            packetDataBB.position(packetDataBB.position() + 7);
            ipProtocol = packetDataBB.get();
            // nw src
            packetDataBB.position(packetDataBB.position() + 2);
            ipv4Src = packetDataBB.getInt();
            // nw dst
            ipv4Dst = packetDataBB.getInt();
            packetDataBB.position(transportOffset);
            break;
        case 0x0806:
            // arp
            final int arpPos = packetDataBB.position();
            // opcode
            scratch = packetDataBB.getShort(arpPos + 6);
            ipDscp=(byte) (0xff & scratch);

            scratch = packetDataBB.getShort(arpPos + 2);
            // if ipv4 and addr len is 4
            if (scratch == 0x800 && packetDataBB.get(arpPos + 5) == 4) {
                // nw src
                ipv4Src = packetDataBB.getInt(arpPos + 14);
                // nw dst
                ipv4Dst = packetDataBB.getInt(arpPos + 24);
            } else {
                ipv4Src=0;
                ipv4Dst=0;
            }
            break;
        default:
            // Not ARP or IP. Wildcard NW_DST and NW_SRC
        	wildcards.add(OFFlowWildcards.NW_DST_ALL);
        	wildcards.add(OFFlowWildcards.NW_SRC_ALL);
        	wildcards.add(OFFlowWildcards.NW_PROTO);
        	wildcards.add(OFFlowWildcards.NW_TOS);
            ipDscp=(byte)0;
        	ipProtocol=(byte)0;
        	ipv4Src=0;
        	ipv4Dst=0;
            break;
        }
        
        short tcpSrc=0;
        short tcpDst=0;
        
        
        		
        switch (ipProtocol) {
        case 0x01:
            // icmp
            // type
            tcpSrc = U8.f(packetDataBB.get());
            // code
            tcpDst = U8.f(packetDataBB.get());
            break;
        case 0x06:
            // tcp
            // tcp src
            tcpSrc = packetDataBB.getShort();
            // tcp dest
            tcpDst = packetDataBB.getShort();
            break;
        case 0x11:
            // udp
            // udp src
            tcpSrc = packetDataBB.getShort();
            // udp dest
            tcpDst = packetDataBB.getShort();
            break;
        default:
            // Unknown network proto.
        	wildcards.add(OFFlowWildcards.TP_DST);
        	wildcards.add(OFFlowWildcards.TP_SRC);
        	
        	tcpDst = (short)0;
        	tcpSrc = (short)0;
        	break;
        }
		Match match_from_packet=OVXFactoryInst.myFactory.buildMatchV1()
        .setWildcards(OFFlowWildcardsSerializerVer10.toWireValue(wildcards))
        .setInPort(OFPort.ofShort(inputPort))
        .setEthDst(MacAddress.of(ethDst))
        .setEthSrc(MacAddress.of(ethSrc))
        .setEthType(EthType.of(ethType))
        .setVlanVid(OFVlanVidMatch.ofRawVid(vlanVid))
        .setVlanPcp(VlanPcp.of(vlanPcp))
        .setIpDscp(IpDscp.of(ipDscp))
        .setIpProto(IpProtocol.of(ipProtocol))
        .setIpv4Src(IPv4Address.of(ipv4Src))
        .setIpv4Dst(IPv4Address.of(ipv4Dst))
        .setTcpSrc(TransportPort.of(tcpSrc))
        .setTcpDst(TransportPort.of(tcpDst))
        .build();
        return match_from_packet;
    }
   

}

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.protocol.OVXMatchV3;
import net.onrc.openvirtex.util.MACAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.ver13.*;
import org.projectfloodlight.openflow.types.*;

import java.nio.ByteBuffer;
import java.util.*;

public class OVXPacketInVer13 extends OFPacketInVer13 implements OVXPacketIn {

   
    public OVXPacketInVer13(
            long xid,
            OFBufferId bufferId,
            int totalLen,
            OFPacketInReason reason,
            TableId tableId,
            U64 cookie,
            Match match,
            byte[] data) {
		super(xid, bufferId, totalLen, reason, tableId, cookie, match, data);
		// TODO Auto-generated constructor stub
	}
    public OVXPacketInVer13(final OVXPacketIn pktIn) {
    	this(pktIn.getXid(),pktIn.getBufferId(),pktIn.getTotalLen(),pktIn.getReason(), pktIn.getTableId(), pktIn.getCookie(), pktIn.getMatch(), pktIn.getData());
        
    }

    public OVXPacketInVer13() {
        this(0,null,0,null,null,null,null,null);
    }

    public OVXPacketInVer13(final byte[] data, final short portNumber) {
        this(DEFAULT_XID,OFBufferId.NO_BUFFER,
                (int) (OFPacketInVer13.MINIMUM_LENGTH + data.length), OFPacketInReason.NO_MATCH, TableId.ALL,
                U64.ZERO, new OVXMatchV3(OFOxmList.of(new OFOxmInPortVer13(OFPort.of(portNumber))), 0, null),data);
       
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

        short inport = this.getMatch().get(MatchField.IN_PORT).getShortPortNumber();
        port = sw.getPort(inport);
        Mappable map = sw.getMap();

        System.out.println("Pravein: Packet In from "+ inport);

        Match match =  this.loadFromPacket(this.getData(), inport);

        System.out.println("Match : "+ match.toString());
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

            System.out.println("The packet belongs to "+ this.tenantId);
            /*
             * Checks on vSwitch and the virtual port done in swndPkt.
             */
            vSwitch = this.fetchOVXSwitch(sw, port.getPortNo().getShortPortNumber(), vSwitch, map);
            //System.out.println("Pravien :Switch mapping :"+Long.toHexString(sw.getSwitchId())+":"+port.getPortNumber() +" Maps to "+ Long.toHexString(vSwitch.getSwitchId()));
            this.ovxPort = this.port.getOVXPort(this.tenantId, 0);
            this.sendPkt(vSwitch, match, sw);
            this.learnHostIP(match, map);
            this.learnAddresses(match, map);
            System.out.println("Packet sent to virt Network");
            this.log.warn("Edge PacketIn {} sent to virtual network {}", this,
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

        if (((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_IPV4
                || ((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_ARP) {

            PhysicalIPAddress srcIP = null;
            PhysicalIPAddress dstIP = null;
            if (match.get(MatchField.IPV4_SRC) != null) {
                srcIP = new PhysicalIPAddress(
                        ((OFMatchV3)match).get(MatchField.IPV4_SRC).getInt());
            }
            if (match.get(MatchField.IPV4_DST) != null) {
                dstIP = new PhysicalIPAddress(
                        ((OFMatchV3)match).get(MatchField.IPV4_DST).getInt());
            }



            Ethernet eth = new Ethernet();
            eth.deserialize(this.getData(), 0,
                    this.getData().length);

            OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(),
                    eth.getDestinationMAC());
            // rewrite the Match with the values of the link
            if (lUtils.isValid()) {
                OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(),
                        lUtils.getLinkId());
                this.tenantId = lUtils.getTenantId();
                if (srcPort == null) {
                    this.log.error(
                            "Virtual Src Port Unknown: {}, port {} with this match {}; dropping packet",
                            sw.getName(), ((OFMatchV3)match).get(MatchField.IN_PORT), match);
                    return;
                }
                //this.match = (OVXMatchV3)this.match.createBuilder().setExact(MatchField.IN_PORT, this.ovxPort.getPortNo());
                OVXLink link;
                try {
                    OVXPort dstPort = map.getVirtualNetwork(
                            lUtils.getTenantId()).getNeighborPort(srcPort);
                    link = map.getVirtualSwitch(sw,(int) srcPort.getPortNo().getPortNumber(), lUtils.getTenantId())
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
//                        match=((OFMatchV3)match).createBuilder()
//                        		.setExact(MatchField.ETH_SRC, MacAddress.of(eth.getSourceMACAddress()))
//                                .setExact(MatchField.ETH_DST, MacAddress.of(eth.getDestinationMACAddress()))
//                                .build();
                        OFOxmList myList = ((OFMatchV3)match).getOxmList();
                        OFOxmEthSrc oxmEthSrc = new OFOxmEthSrcVer13(MacAddress.of(eth.getSourceMACAddress()));
                        OFOxmEthDst oxmEthDst = new OFOxmEthDstVer13(MacAddress.of(eth.getDestinationMACAddress()));
                        Map<MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
                        for (OFOxm<?> ofOxm : myList) {
                            if (ofOxm instanceof OFOxmIpv4Src) {
                                oxmMap.put(MatchFields.ETH_SRC, oxmEthSrc);
                                continue;
                            }
                            if (ofOxm instanceof OFOxmIpv4Dst) {
                                oxmMap.put(MatchFields.ETH_DST, oxmEthDst);
                                continue;
                            }
                            oxmMap.put(ofOxm.getMatchField().id, ofOxm);
                        }
                        OFOxmList oxmList = new OFOxmList(oxmMap);
                        match = OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();

                    } catch (NetworkMappingException e) {
                        log.warn(e);
                    }
                } else if (linkField == OVXLinkField.VLAN) {
                    // TODO
                    log.warn("VLAN virtual links not yet implemented.");
                    return;
                }

            }
            
            if (((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_ARP) {
                // ARP packet
                final ARP arp = (ARP) eth.getPayload();
                this.tenantId = this.fetchTenantId(match, map, true);
                try {
                    if (srcIP != null) {
                        if (map.hasVirtualIP(srcIP)) {
                            arp.setSenderProtocolAddress(map.getVirtualIP(srcIP)
                                    .getIp());
                        }
                    }
                    if (dstIP != null) {
                        if (map.hasVirtualIP(dstIP)) {
                            arp.setTargetProtocolAddress(map.getVirtualIP(dstIP)
                                    .getIp());
                        }
                    }
                } catch (AddressMappingException e) {
                    log.warn("Inconsistency in OVXMap? : {}", e);
                }
            } else if (((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_IPV4) {
                try {
                    final IPv4 ip = (IPv4) eth.getPayload();
                    if (srcIP != null && dstIP != null) {
                        ip.setDestinationAddress(map.getVirtualIP(dstIP).getIp());
                        ip.setSourceAddress(map.getVirtualIP(srcIP).getIp());
                    }
                    // TODO: Incorporate below into fetchTenantId
                    if (this.tenantId == null) {
                        this.tenantId = dstIP.getTenantId();
                    }
                } catch (AddressMappingException e) {
                    log.warn("Could not rewrite IP fields : {}", e);
                }
            } else {
                this.log.info("{} handling not yet implemented; dropping",
                        ((OFMatchV3)match).get(MatchField.ETH_TYPE));
                this.installDropRule(sw, match);
                return;
            }
            this.data=eth.serialize();

            vSwitch = this.fetchOVXSwitch(sw, port.getPortNo().getShortPortNumber(), vSwitch, map);

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
        vSwitch = this.fetchOVXSwitch(sw, port.getPortNo().getShortPortNumber(),vSwitch, map);
        this.sendPkt(vSwitch, match, sw);
        this.log.debug("Layer2 PacketIn {} sent to virtual network {}", this,
                this.tenantId);
    }

    private void learnHostIP(Match match, Mappable map) {
        if (((OFMatchV3)match).get(MatchField.IPV4_SRC) != null) {
            try {
                OVXNetwork vnet = map.getVirtualNetwork(tenantId);
                Host host = vnet.getHost(ovxPort);
                if (host != null) {
                    host.setIPAddress(((OFMatchV3) match).get(MatchField.IPV4_SRC).getInt());
                    System.out.println("Recorded " + host.getIp().toString());

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

    private OFMatchV3 addtoMatch(OFMatchV3 myMatch, OFPort port) {
        OFMatchV3 newMatch;
        OFOxmList myList = myMatch.getOxmList();
        OFOxmInPort oxmInPort = new OFOxmInPortVer13(port);
        Map <MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
        oxmMap.put(MatchFields.IN_PORT, oxmInPort);
        for (OFOxm<?> ofOxm : myList) {
            if (ofOxm instanceof OFOxmInPort) continue;
            System.out.println(ofOxm.getMatchField().id.toString());
            oxmMap.put(ofOxm.getMatchField().id, ofOxm);
        }

        OFOxmList oxmList = new OFOxmList(oxmMap);
        System.out.println(myList.toString());

        System.out.println(oxmList.toString());
        newMatch = OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();
        return newMatch;

    }
    private void sendPkt(final OVXSwitch vSwitch, final Match match,
            final PhysicalSwitch sw) {
        System.out.println("sending packet");
        if (vSwitch == null || !vSwitch.isActive()) {
            System.out.println("Controller not connected");
            this.log.warn(
                    "Controller for virtual network {} has not yet connected "
                            + "or is down", this.tenantId);
            this.installDropRule(sw, match);
            return;
        }
        this.match = addtoMatch((OFMatchV3)match, this.ovxPort.getPortNo());
        System.out.println("Match :"+ this.match.toString());
        this.bufferId=OFBufferId.of(vSwitch.addToBufferMap(this));
        if (this.port != null && this.ovxPort != null
                && this.ovxPort.isActive()) {
            System.out.println("ovx port is active");
            System.out.println(this.match.toString());
            if ((this.getData() != null)
                    && (vSwitch.getMissSendLen() != OVXSetConfigVer13.MSL_FULL)) {
                System.out.println("Data is not null");
                this.data = Arrays.copyOf(this.getData(),
                        U16.f(vSwitch.getMissSendLen()));
                System.out.println(this.data.toString());
                
            }
            System.out.println("Pravein : Sending msg to Switch.. port= "+ this.port.getPortNo() + " phys ovx = "+ this.ovxPort.getPortNo());
            vSwitch.sendMsg(this, sw);
        } else if (this.port == null) {
            log.error("The port {} doesn't belong to the physical switch {}",
                    this.ovxPort, sw.getName());
        } else if (this.ovxPort == null || !this.ovxPort.isActive()) {
            log.error(
                    "Virtual port associated to physical port {} in physical switch {} for "
                            + "virtual network {} is not defined or inactive",
                    this.port, sw.getName(), this.tenantId);
        }
    }

    private void learnAddresses(final Match match, final Mappable map) {
        System.out.println("learn address : "+ match.toString());
        if (((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_IPV4
                || ((OFMatchV3)match).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_ARP) {
            if (((OFMatchV3)match).get(MatchField.IPV4_SRC) != null ) {
                IPMapper.getPhysicalIp(this.tenantId, ((OFMatchV3)match).get(MatchField.IPV4_SRC).getInt());
            }
            if (((OFMatchV3)match).get(MatchField.IPV4_SRC) != null ) {
                IPMapper.getPhysicalIp(this.tenantId,
                        ((OFMatchV3)match).get(MatchField.IPV4_DST).getInt());
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
        MACAddress mac = MACAddress.valueOf(((OFMatchV3)match).get(MatchField.ETH_SRC).toString());
        if (useMAC && map.hasMAC(mac)) {
            try {
                return map.getMAC(mac);
            } catch (AddressMappingException e) {
                log.warn("Tried to return non-mapped MAC address : {}", e);
            }
        }
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

        HashMap<MatchField, OFOxm<?>> MatchMap = new HashMap<>();
        Set<OFFlowWildcards> wildcards=new HashSet<OFFlowWildcards>();
        if (OFPort.ofShort(inputPort) == OFPort.ALL) {
        	wildcards.add(OFFlowWildcards.IN_PORT);
        }

        System.out.println("Pravein : Printing packet Bytes");
        for (byte packetByte: packetData) {
            System.out.print (String.format("%02X ", packetByte));
        }
        System.out.println();
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
        

        if (ethType != (short) 0x8100) {
            /* Untagged value in OF 1.3 */
            vlanVid= 0x0000;
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
            System.out.println("Got an IP Packet");
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
            System.out.println("Got an ARP Packet");

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
        Map <MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
        OFOxmInPort oxmInPort = new OFOxmInPortVer13(OFPort.ofShort(inputPort));
        OFOxmEthSrc oxmEthSrc = new OFOxmEthSrcVer13(MacAddress.of(ethSrc));
        OFOxmEthDst oxmEthDst = new OFOxmEthDstVer13(MacAddress.of(ethDst));
        OFOxmEthType oxmEthType = new OFOxmEthTypeVer13(EthType.of(ethType));
        OFOxmVlanVid oxmVlanVid = new OFOxmVlanVidVer13(OFVlanVidMatch.ofRawVid(vlanVid));
        OFOxmVlanPcp oxmVlanPcp = new OFOxmVlanPcpVer13(VlanPcp.FULL_MASK.of(vlanPcp));
        OFOxmIpDscp oxmIpDscp = new OFOxmIpDscpVer13(IpDscp.of(ipDscp));

        oxmMap.put(MatchFields.IN_PORT, oxmInPort);
        oxmMap.put(MatchFields.ETH_SRC, oxmEthSrc);
        oxmMap.put(MatchFields.ETH_DST, oxmEthDst);
        oxmMap.put(MatchFields.ETH_TYPE, oxmEthType);
        oxmMap.put(MatchFields.VLAN_VID, oxmVlanVid);
        oxmMap.put(MatchFields.VLAN_PCP, oxmVlanPcp);
        oxmMap.put(MatchFields.IP_DSCP, oxmIpDscp);

        if (ipProtocol != 0) {
            OFOxmIpProto oxmIpProto = new OFOxmIpProtoVer13(IpProtocol.of(ipProtocol));
            oxmMap.put(MatchFields.IP_PROTO, oxmIpProto);
        }
        if (ipv4Src != 0) {
            OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(ipv4Src));
            oxmMap.put(MatchFields.IPV4_SRC, oxmIpv4Src);
        }
        if (ipv4Dst != 0) {
            OFOxmIpv4Dst oxmIpv4Dst = new OFOxmIpv4DstVer13(IPv4Address.of(ipv4Dst));
            oxmMap.put(MatchFields.IPV4_DST, oxmIpv4Dst);
        }
        if (tcpSrc != 0) {
            OFOxmTcpSrc oxmTcpSrc = new OFOxmTcpSrcVer13(TransportPort.of(tcpSrc));
            oxmMap.put(MatchFields.TCP_SRC, oxmTcpSrc);
        }
        if (tcpDst != 0) {
            OFOxmTcpDst oxmTcpDst = new OFOxmTcpDstVer13(TransportPort.of(tcpDst));
            oxmMap.put(MatchFields.TCP_DST, oxmTcpDst);
        }
        OFOxmList oxmList = new OFOxmList(oxmMap);

        Match matchFromPacket =OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();
        return matchFromPacket;
    }
   

}

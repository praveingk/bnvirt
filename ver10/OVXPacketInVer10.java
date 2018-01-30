package net.onrc.openvirtex.messages.ver10;



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
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXSetConfig;
import net.onrc.openvirtex.messages.Virtualizable;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.util.MACAddress;


import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.util.U16;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFMessageWriter;
import org.projectfloodlight.openflow.protocol.OVXPacketIn;

import org.projectfloodlight.openflow.protocol.OFPacketInReason;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U32;
import org.projectfloodlight.openflow.util.ChannelUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import org.projectfloodlight.openflow.protocol.ver10.OFPacketInVer10;

public class OVXPacketInVer10 extends OFPacketInVer10 implements OVXPacketIn{

	private static final Logger logger = LoggerFactory.getLogger(OVXPacketInVer10.class);
	// version: 1.0
	
	
    private final static long DEFAULT_XID = 0x0L;
    private final static OFBufferId DEFAULT_BUFFER_ID = OFBufferId.NO_BUFFER;
    private final static int DEFAULT_TOTAL_LEN = 0x0;
    private final static OFPort DEFAULT_IN_PORT = OFPort.ANY;
    private final static byte[] DEFAULT_DATA = new byte[0];
    
    // OVX message fields
    private final PhysicalPort port;
    private final OVXPort ovxPort;
    private final Integer tenantId;
    
    OVXPacketInVer10(long xid, OFBufferId bufferId, int totalLen, OFPort inPort,
			OFPacketInReason reason, byte[] data,PhysicalPort port,OVXPort ovxPort,Integer tenantId) {
		super(xid, bufferId, totalLen, inPort, reason, data);
		this.port=port;
		this.ovxPort=ovxPort;
		this.tenantId=tenantId;
	}
    
    @Override
    public PhysicalPort getport() {
        return port;
    }

    @Override
    public OVXPort getovxPort() {
        return ovxPort;
    }

    @Override
    public Integer gettenantId() {
        return tenantId;
    }

	public static class Builder extends OFPacketInVer10.Builder implements OVXPacketIn.Builder,Virtualizable{ 
		
	    private PhysicalPort port = null;
	    private OVXPort ovxPort = null;
	    private Integer tenantId = null;
	    private OVXPacketInVer10 pktIn =null;
	    private byte[] data =null;
	    private OFPort inport =null;
		public Builder(final OVXPacketInVer10 pktIn) {this.pktIn=pktIn;}
		public Builder(final byte[] data, final OFPort portNumber) {this.data= data;this.inport=portNumber;}
		public Builder(){}
		
		@Override
	    public PhysicalPort getport() {
	        return port;
	    }

	    @Override
	    public OVXPort getovxPort() {
	        return ovxPort;
	    }

	    @Override
	    public Integer gettenantId() {
	        return tenantId;
	    }
		
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

	        if (match.getDataLayerType() == Ethernet.TYPE_IPV4
	                || match.getDataLayerType() == Ethernet.TYPE_ARP) {
	            PhysicalIPAddress srcIP = new PhysicalIPAddress(
	                    match.getNetworkSource());
	            PhysicalIPAddress dstIP = new PhysicalIPAddress(
	                    match.getNetworkDestination());

	            Ethernet eth = new Ethernet();
	            eth.deserialize(this.getPacketData(), 0,
	                    this.getPacketData().length);

	            OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(),
	                    eth.getDestinationMAC());
	            // rewrite the OFMatch with the values of the link
	            if (lUtils.isValid()) {
	                OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(),
	                        lUtils.getLinkId());
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
	                        match.setDataLayerSource(eth.getSourceMACAddress())
	                                .setDataLayerDestination(
	                                        eth.getDestinationMACAddress());
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
	            } else if (match.getDataLayerType() == Ethernet.TYPE_IPV4) {
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
	                        match.getDataLayerType());
	                this.installDropRule(sw, match);
	                return;
	            }
	            this.setPacketData(eth.serialize());

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

		public OVXPacketInVer10 build() { 
			long xid = pktIn !=null ? pktIn.getXid(): DEFAULT_XID;
			OFBufferId bufferId= pktIn !=null ? pktIn.getBufferId() : DEFAULT_BUFFER_ID;
			 if(bufferId == null)
	                throw new NullPointerException("Property bufferId must not be null");
			int totalLen = pktIn !=null ? pktIn.getTotalLen() : DEFAULT_TOTAL_LEN;
			OFPort inport = pktIn != null ? pktIn.getInPort():(this.inport !=null ? this.inport: DEFAULT_IN_PORT);
			 if(inport == null)
	                throw new NullPointerException("Property inPort must not be null");
	         if(this.getReason() == null)
	                throw new NullPointerException("Property reason must not be null");
			byte[] data = pktIn !=null ? pktIn.getData():(this.data != null ? this.data: DEFAULT_DATA );
			return new OVXPacketInVer10(xid, bufferId, totalLen,
				inport,this.getReason(),data,port,ovxPort,tenantId);
			
			}

		}
	
	
	
	
	
}
	


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
import java.util.*;

import net.onrc.openvirtex.api.Global.GlobalConfig;
import net.onrc.openvirtex.api.Global.TAG;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.Mapper.TenantMapperTos;
import net.onrc.openvirtex.elements.Mapper.TenantMapperVlan;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerDestinationVer10;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerSourceVer10;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFBadRequestCode;
import org.projectfloodlight.openflow.protocol.OFFlowWildcards;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.*;

import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFErrorCauseData;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.types.U8;
import org.projectfloodlight.openflow.types.VlanPcp;

import net.onrc.openvirtex.messages.OVXErrorMsg;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.messages.OVXPacketOut;

import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowWildcardsSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFPacketOutVer10;
import org.projectfloodlight.openflow.types.OFBufferId;

public class OVXPacketOutVer10 extends OFPacketOutVer10 implements OVXPacketOut {

   


    public OVXPacketOutVer10(
			long xid,
			OFBufferId bufferId,
			OFPort inPort,
			List<OFAction> actions,
			byte[] data) {
		super(xid, bufferId, inPort, actions, data);
		// TODO Auto-generated constructor stub
	}
    

    public OVXPacketOutVer10(final OVXPacketOut pktOut) {
        super(pktOut.getXid(),pktOut.getBufferId(),pktOut.getInPort(),pktOut.getActions(),pktOut.getData());
    	    }

    public OVXPacketOutVer10() {
        super(0,null,null,null,null);
    }

    public OVXPacketOutVer10(final byte[] pktData, final OFPort inPort,
            final OFPort outPort) {
    	super(DEFAULT_XID,
    			OFBufferId.NO_BUFFER,
    			inPort, 
    			(ArrayList<OFAction>)Arrays.asList(
    					   new OFAction[]
    					   {OVXFactoryInst.myFactory.actions()
    						.buildOutput().setPort(outPort).build()}
    					   ),
    					pktData);
    	
    	
    	
       
    }

	private final Logger log = LogManager.getLogger(OVXPacketOut.class
            .getName());
    private Match match = null;
    private final List<OFAction> approvedActions = new LinkedList<OFAction>();

    @Override
    public void devirtualize(final OVXSwitch sw) {
     short OFPP_MAX_SHORT = (short) 0xFF00;
       
        final OVXPort inport = sw.getPort(this.getInPort().getShortPortNumber());
        OVXMatch ovxMatch = null;

        if (this.getBufferId() == OFBufferId.NO_BUFFER) {
            if (this.getData().length <= 14) {
                this.log.error("PacketOut has no buffer or data {}; dropping",
                        this);
                ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
            	this.writeTo(buf);
            	byte[] byte_msg=buf.array();
            	OFErrorCauseData offendingMsg=OFErrorCauseData.of(byte_msg, OFVersion.OF_10);
            	
                final OVXErrorMsg err =(OVXErrorMsg) OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(this.getXid(),OFBadRequestCode.BAD_LEN,offendingMsg);
                		
                sw.sendMsg(err, sw);
                return;
            }
            this.match = this.loadFromPacket(this.getData(),
                    this.inPort.getShortPortNumber());
            ovxMatch = OVXFactoryInst.myOVXFactory.buildOVXMatchV1(match);
            ovxMatch.setPktData(this.getData());
        } else {
            final OVXPacketIn cause = sw.getFromBufferMap(this.bufferId.getInt());
            if (cause == null) {
                this.log.error(
                        "Unknown buffer id {} for virtual switch {}; dropping",
                        this.bufferId, sw);
                return;
            }

            this.match = this.loadFromPacket(cause.getData(),
                    this.inPort.getShortPortNumber());
            this.bufferId=cause.getBufferId();
            ovxMatch = OVXFactoryInst.myOVXFactory.buildOVXMatchV1(match);
            ovxMatch.setPktData(cause.getData());
            if (cause.getBufferId() == OFBufferId.NO_BUFFER) {
                this.data=cause.getData();
                }
        }

        for (OFAction act : this.getActions()) {
            try {
            	if(act.getType().equals(OFActionType.SET_NW_DST))
                	act=OVXFactoryInst.myOVXFactory.buildOVXActionDataLayerDestination(((OFActionSetDlDst)act).getDlAddr());
                	else if(act.getType().equals(OFActionType.SET_NW_SRC))
                    act=OVXFactoryInst.myOVXFactory.buildOVXActionDataLayerSource(((OFActionSetDlSrc)act).getDlAddr());
                	else if(act.getType().equals(OFActionType.ENQUEUE))
                    	act=OVXFactoryInst.myOVXFactory.buildOVXActionEnqueue(((OFActionEnqueue)act).getPort(),((OFActionEnqueue)act).getQueueId());
                    	else if(act.getType().equals(OFActionType.SET_NW_DST))
                        act=OVXFactoryInst.myOVXFactory.buildOVXActionNetworkLayerDestination(((OFActionSetNwDst)act).getNwAddr());
                    	else if(act.getType().equals(OFActionType.SET_NW_SRC))
                        	act=OVXFactoryInst.myOVXFactory.buildOVXActionNetworkLayerSource(((OFActionSetNwSrc)act).getNwAddr());
                        	else if(act.getType().equals(OFActionType.SET_NW_TOS))
                            act=OVXFactoryInst.myOVXFactory.buildOVXActionNetworkTypeOfService(((OFActionSetNwTos)act).getNwTos());
                        	else if(act.getType().equals(OFActionType.OUTPUT))
                            	act=OVXFactoryInst.myOVXFactory.buildOVXActionOutput(((OFActionOutput)act).getPort(),((OFActionOutput)act).getMaxLen());
                            	else if(act.getType().equals(OFActionType.STRIP_VLAN))
                                act=OVXFactoryInst.myOVXFactory.buildOVXActionStripVirtualLan();
                            	else if(act.getType().equals(OFActionType.SET_TP_DST))
                                	act=OVXFactoryInst.myOVXFactory.buildOVXActionTransportLayerDestination(((OFActionSetTpDst)act).getTpPort());
                                	else if(act.getType().equals(OFActionType.SET_TP_SRC))
                                    act=OVXFactoryInst.myOVXFactory.buildOVXActionTransportLayerSource(((OFActionSetTpSrc)act).getTpPort());
                                	else if(act.getType().equals(OFActionType.SET_VLAN_VID))
                                		act=OVXFactoryInst.myOVXFactory.buildOVXActionVirtualLanIdentifier(((OFActionSetVlanVid)act).getVlanVid());
                                		else if(act.getType().equals(OFActionType.SET_VLAN_PCP))
                                		act=OVXFactoryInst.myOVXFactory.buildOVXActionVirtualLanPriorityCodePoint(((OFActionSetVlanPcp)act).getVlanPcp());
            	 
            	
            	((VirtualizableAction) act).virtualize(sw,
                        this.approvedActions, ovxMatch);

            } catch (final ActionVirtualizationDenied e) {
                this.log.warn("Action {} could not be virtualized; error: {}",
                        act, e.getMessage());
                
                ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
            	this.writeTo(buf);
            	byte[] byte_msg=buf.array();
            	OFErrorCauseData offendingMsg=OFErrorCauseData.of(byte_msg, OFVersion.OF_10);
            	
                final OVXErrorMsg err =(OVXErrorMsg) OVXFactoryInst.myOVXFactory.buildOVXBadActionErrorMsg(this.getXid(),e.getErrorCode(),offendingMsg);
                
                
                sw.sendMsg(err, sw);
                return;
            } catch (final DroppedMessageException e) {
                this.log.debug("Dropping packetOut {}", this);
                return;
            }
        }

        if (U16.f(this.getInPort().getShortPortNumber()) < U16.f(OFPP_MAX_SHORT)) {
            this.inPort=OFPort.ofShort(inport.getPhysicalPortNumber());
        }
        
        if (GlobalConfig.bnvTagType == TAG.TOS) {
            TenantMapperTos.prependRewriteActions(sw.getTenantId(), (OFMatchV1) match, approvedActions);
        } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
            TenantMapperVlan.prependRewriteActions(sw.getTenantId(), (OFMatchV1) match, approvedActions);
        } else if (GlobalConfig.bnvTagType == TAG.IP) {
			this.prependRewriteActions(sw);
		} else if (GlobalConfig.bnvTagType == TAG.NOTAG){
            /* Do Nothing */
        }
        this.actions=this.approvedActions;
       

        // TODO: Beacon sometimes send msg with inPort == controller, check with
        // Ayaka if it's ok
        
        
        if (U16.f(this.getInPort().getShortPortNumber()) < U16.f(OFPP_MAX_SHORT)) {
            OVXMessageUtil.translateXid(this, inport);
        }
        this.log.debug("Sending packet-out to sw {}: {}", sw.getName(), this);
        sw.sendSouth(this, inport);
    }

    private void prependRewriteActions(final OVXSwitch sw) {
    	
    	 final List<OFAction> actions = new LinkedList<OFAction>();
         if (!match.isFullyWildcarded(MatchField.IPV4_SRC)) {
             final OVXActionNetworkLayerSourceVer10 srcAct = new OVXActionNetworkLayerSourceVer10(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                     ((OFMatchV1)match).getIpv4Src().getInt())));
             this.approvedActions.add(0, srcAct);
         }
         if (!match.isFullyWildcarded(MatchField.IPV4_DST)) {
             final OVXActionNetworkLayerDestinationVer10 dstAct = new OVXActionNetworkLayerDestinationVer10(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                     ((OFMatchV1)match).getIpv4Dst().getInt())));
             this.approvedActions.add(0, dstAct);
         }
     
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

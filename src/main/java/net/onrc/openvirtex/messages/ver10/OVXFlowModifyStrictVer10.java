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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.UnknownActionException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerDestinationVer10;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerSourceVer10;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.OVXUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwTos;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanPcp;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanVid;

import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXFlowModify;
import net.onrc.openvirtex.messages.OVXFlowModifyStrict;
import net.onrc.openvirtex.messages.OVXMessageUtil;

import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowModFlagsSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowModifyStrictVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFMatchV1Ver10;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

public class OVXFlowModifyStrictVer10 extends OFFlowModifyStrictVer10 implements OVXFlowModifyStrict {

   

    protected OVXFlowModifyStrictVer10(
			long xid,
			Match match,
			U64 cookie,
			int idleTimeout,
			int hardTimeout,
			int priority,
			OFBufferId bufferId,
			OFPort outPort,
			Set<OFFlowModFlags> flags,
			List<org.projectfloodlight.openflow.protocol.action.OFAction> actions) {
		super(xid, match, cookie, idleTimeout, hardTimeout, priority, bufferId,
				outPort, flags, actions);
		// TODO Auto-generated constructor stub
	}
    private final Logger log = LogManager.getLogger(OVXFlowModifyStrictVer10.class.getName());

    private OVXSwitch sw = null;
    private final List<OFAction> approvedActions = new LinkedList<OFAction>();

    private long ovxCookie = -1;

    @Override
    public void devirtualize(final OVXSwitch sw) {
        /* Drop LLDP-matching messages sent by some applications */
        if (((OFMatchV1)this.match).getEthType().getValue() == Ethernet.TYPE_LLDP) {
            return;
        }

        this.sw = sw;
        FlowTable ft = this.sw.getFlowTable();

        int bufferId = OFBufferId.NO_BUFFER.getInt();
        if (sw.getFromBufferMap(this.bufferId.getInt()) != null) {
            bufferId = sw.getFromBufferMap(this.bufferId.getInt()).getBufferId().getInt();
        }
        final short inport = ((OFMatchV1)this.getMatch()).getInPort().getShortPortNumber();

        /* let flow table process FlowMod, generate cookie as needed */
        boolean pflag = ft.handleFlowMods(this.clone());

        /* used by OFAction virtualization */
        OVXMatch ovxMatch = new OVXMatch((OFMatchV1)this.match);
        ovxCookie = ((OVXFlowTable) ft).getCookie(this, false);
        ovxMatch.setCookie(ovxCookie);
        this.cookie=U64.of(ovxMatch.getCookie());

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
                ft.deleteFlowMod(ovxCookie);
                sw.sendMsg(OVXMessageUtil.makeError(e.getErrorCode(), this), sw);
                return;
            } catch (final DroppedMessageException e) {
                this.log.warn("Dropping flowmod {} {}", this, e);
                ft.deleteFlowMod(ovxCookie);
                // TODO perhaps send error message to controller
                return;
            }
        }

        final OVXPort ovxInPort = sw.getPort(inport);
        this.bufferId=OFBufferId.of(bufferId);

        if (ovxInPort == null) {
            if (this.getMatch().isFullyWildcarded(MatchField.IN_PORT)) {
                /* expand match to all ports */
                for (OVXPort iport : sw.getPorts().values()) {
                    int wcard = ((OFMatchV1)this.match).getWildcards()
                            & (~OFMatchV1Ver10.OFPFW_IN_PORT);
                    
                    this.match=((OFMatchV1)this.match).createBuilder().setWildcards(wcard).build();
                    prepAndSendSouth(iport, pflag);
                }
            } else {
                this.log.error(
                        "Unknown virtual port id {}; dropping flowmod {}",
                        inport, this);
                sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                        OFFlowModFailedCode.EPERM, this), sw);
                return;
            }
        } else {
            prepAndSendSouth(ovxInPort, pflag);
        }
    }

    private void prepAndSendSouth(OVXPort inPort, boolean pflag) {
        if (!inPort.isActive()) {
            log.warn("Virtual network {}: port {} on switch {} is down.",
                    sw.getTenantId(), inPort.getPortNo(),
                    sw.getSwitchName());
            return;
        }
        this.match=((OFMatchV1)this.getMatch()).createBuilder().setInPort(OFPort.ofShort(inPort.getPhysicalPortNumber())).build();
        OVXMessageUtil.translateXid(this, inPort);
        try {
            if (inPort.isEdge()) {
                this.prependRewriteActions();
            } else {
                IPMapper.rewriteMatch(sw.getTenantId(), this.match);
                // TODO: Verify why we have two send points... and if this is
                // the right place for the match rewriting
                if (inPort != null
                        && inPort.isLink()
                        && (!this.getMatch().isFullyWildcarded(MatchField.ETH_DST) || !this.getMatch().isFullyWildcarded(MatchField.ETH_SRC))) {
                    // rewrite the OFMatch with the values of the link
                    OVXPort dstPort = sw.getMap()
                            .getVirtualNetwork(sw.getTenantId())
                            .getNeighborPort(inPort);
                    OVXLink link = sw.getMap()
                            .getVirtualNetwork(sw.getTenantId())
                            .getLink(dstPort, inPort);
                    if (inPort != null && link != null) {
                        try {
                            Integer flowId = sw
                                    .getMap()
                                    .getVirtualNetwork(sw.getTenantId())
                                    .getFlowManager()
                                    .getFlowId(((OFMatchV1)this.match).getEthSrc().getBytes(),
                                            ((OFMatchV1)this.match).getEthDst().getBytes());
                            OVXLinkUtils lUtils = new OVXLinkUtils(
                                    sw.getTenantId(), link.getLinkId(), flowId);
                            lUtils.rewriteMatch(this.getMatch());
                        } catch (IndexOutOfBoundException e) {
                            log.error(
                                    "Too many host to generate the flow pairs in this virtual network {}. "
                                            + "Dropping flow-mod {} ",
                                            sw.getTenantId(), this);
                            throw new DroppedMessageException();
                        } 
                    }
                }
            }
        } catch (NetworkMappingException e) {
            log.warn(
                    "OVXFlowMod. Error retrieving the network with id {} for flowMod {}. Dropping packet...",
                    this.sw.getTenantId(), this);
        } catch (DroppedMessageException e) {
            log.warn(
                    "OVXFlowMod. Error retrieving flowId in network with id {} for flowMod {}. Dropping packet...",
                    this.sw.getTenantId(), this);
        }
        this.actions=this.approvedActions;
        if (pflag) {
            this.flags.add(OFFlowModFlags.SEND_FLOW_REM);
            sw.sendSouth(this, inPort);
        }
    }

   

    private void prependRewriteActions() {
        if (!this.getMatch().isFullyWildcarded(MatchField.IPV4_SRC)) {
            final OVXActionNetworkLayerSource srcAct =OVXFactoryInst.myOVXFactory.buildOVXActionNetworkLayerSource
            		(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                    ((OFMatchV1)this.match).getIpv4Src().getInt())));
            
            this.approvedActions.add(0, srcAct);
        }

        if (!this.getMatch().isFullyWildcarded(MatchField.IPV4_DST)) {
            final OVXActionNetworkLayerDestination dstAct =OVXFactoryInst.myOVXFactory.buildOVXActionNetworkLayerDestination
            		(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                            ((OFMatchV1)this.match).getIpv4Dst().getInt())));
            this.approvedActions.add(0, dstAct);
        }
    }

    /**
     * @param flagbit
     *            The OFFlowMod flag
     * @return true if the flag is set
     */
    public boolean hasFlag(short flagbit) {
        return (this.flags.contains(OFFlowModFlagsSerializerVer10.ofWireValue(flagbit)));
    }

    public OVXFlowMod clone() {
        OVXFlowMod flowMod = null;
        flowMod = OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(this.getXid(),this.getMatch(), this.getCookie(), this.getIdleTimeout(), this.getHardTimeout(),this.getPriority(), this.getBufferId(), this.getOutPort(), this.getFlags(), this.getActions());
        return flowMod;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (this.match != null) {
            map.put("match", OVXFactoryInst.myOVXFactory.buildOVXMatchV1(match).toMap());
        }
        LinkedList<Map<String, Object>> actions = new LinkedList<Map<String, Object>>();
        for (OFAction act : this.actions) {
            try {
                actions.add(OVXUtil.actionToMap(act));
            } catch (UnknownActionException e) {
                log.warn("Ignoring action {} because {}", act, e.getMessage());
            }
        }
        map.put("actionsList", actions);
        map.put("priority", String.valueOf(this.priority));
        return map;
    }

    public void setVirtualCookie() {
        long tmp = this.ovxCookie;
        this.ovxCookie = this.cookie.getValue();
        this.cookie = U64.of(tmp);
    }
	
}

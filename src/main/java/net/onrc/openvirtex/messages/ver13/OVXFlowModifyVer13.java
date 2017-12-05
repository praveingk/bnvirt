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

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.*;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXFlowModify;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.actions.*;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.protocol.OVXMatchV3;
import net.onrc.openvirtex.util.OVXUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmInPort;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Dst;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Src;
import org.projectfloodlight.openflow.protocol.ver13.*;
import org.projectfloodlight.openflow.types.*;

import java.util.*;

public class OVXFlowModifyVer13 extends OFFlowModifyVer13 implements OVXFlowModify {

   

    protected OVXFlowModifyVer13(
            long xid,
            U64 cookie,
            U64 cookieMask,
            TableId tableId,
            int idleTimeout,
            int hardTimeout,
            int priority,
            OFBufferId bufferId,
            OFPort outPort,
            OFGroup outGroup,
            Set<OFFlowModFlags> flags,
            Match match,
            List<OFInstruction> instructions) {
        super(xid, cookie, cookieMask, tableId, idleTimeout, hardTimeout, priority, bufferId, outPort, outGroup, flags, match, instructions);

		// TODO Auto-generated constructor stub
	}
    private final Logger log = LogManager.getLogger(OVXFlowModifyVer13.class.getName());

    private OVXSwitch sw = null;
    private final List<OFAction> approvedActions = new LinkedList<OFAction>();

    private long ovxCookie = -1;

    @Override
    public void devirtualize(final OVXSwitch sw) {
        /* Drop LLDP-matching messages sent by some applications */
        if (((OFMatchV3)this.getMatch()).get(MatchField.ETH_TYPE) != null) {
            if (((OFMatchV3)this.getMatch()).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_LLDP) {
                return;
            }
        }
        this.sw = sw;
        FlowTable ft = this.sw.getFlowTable();

        int bufferId = OFBufferId.NO_BUFFER.getInt();
        if (sw.getFromBufferMap(this.bufferId.getInt()) != null) {
            bufferId = sw.getFromBufferMap(this.bufferId.getInt()).getBufferId().getInt();
        }
        short inport = -1;
        if (((OFMatchV3)this.getMatch()).get(MatchField.IN_PORT) != null) {
            inport = ((OFMatchV3) this.getMatch()).get(MatchField.IN_PORT).getShortPortNumber();
        }

        System.out.println("=====================FLOWMOD Modify=========================");
        System.out.println(sw.getSwitchName()+"-> Match:"+ this.getMatch().toString()+", Action:"+this.getActions() );

        /* let flow table process FlowMod, generate cookie as needed */
        boolean pflag = ft.handleFlowMods(this.clone());

        /* used by OFAction virtualization */
        OVXMatchV3 ovxMatch = new OVXMatchV3((OFMatchV3)this.getMatch());
        ovxCookie = ((OVXFlowTable) ft).getCookie(this, false);
        ovxMatch.setCookie(ovxCookie);
        this.cookie = U64.of(ovxMatch.getCookie());

        for (OFAction act: this.getActions()) {
            try {
                if (act.getType().equals(OFActionType.SET_FIELD)) {
                    OFOxm<?> oxmField = ((OFActionSetField)act).getField();
                    act  = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmField);
                } else if (act.getType().equals(OFActionType.OUTPUT)) {
                    act=OVXFactoryInst.myOVXFactory.buildOVXActionOutputV3(((OFActionOutput)act).getPort(),((OFActionOutput)act).getMaxLen());
                } else if (act.getType().equals(OFActionType.SET_QUEUE)) {
                    act=OVXFactoryInst.myOVXFactory.buildOVXActionSetQueue(((OFActionSetQueue)act).getQueueId());
                } else if (act.getType().equals(OFActionType.PUSH_VLAN)) {
                    act=OVXFactoryInst.myOVXFactory.buildOVXActionPushVlan(((OFActionPushVlan)act).getEthertype());
                } else if (act.getType().equals(OFActionType.POP_VLAN)) {
                    act=OVXFactoryInst.myOVXFactory.buildOVXActionPopVlan();
                }

                ((VirtualizableActionV3) act).virtualize(sw,
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
                    this.match = addtoMatch((OFMatchV3)this.match, iport.getPortNo());
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

    private OFMatchV3 addtoMatch(OFMatchV3 myMatch, OFPort port) {
        OFMatchV3 newMatch;
        OFOxmList myList = myMatch.getOxmList();
        OFOxmInPort oxmInPort = new OFOxmInPortVer13(port);
        Map <MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
        for (OFOxm<?> ofOxm : myList) {
            oxmMap.put(ofOxm.getMatchField().id, ofOxm);
        }
        oxmMap.put(MatchFields.IN_PORT, oxmInPort);
        OFOxmList oxmList = new OFOxmList(oxmMap);

        newMatch = OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();
        return newMatch;

    }

    private void prepAndSendSouth(OVXPort inPort, boolean pflag) {
        if (!inPort.isActive()) {
            log.warn("Virtual network {}: port {} on switch {} is down.",
                    sw.getTenantId(), inPort.getPortNo(),
                    sw.getSwitchName());
            return;
        }
        this.match = addtoMatch((OFMatchV3)this.match,  OFPort.ofShort(inPort.getPhysicalPortNumber()));
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
                                    .getFlowId(((OFMatchV3)this.match).get(MatchField.ETH_SRC).getBytes(),
                                            ((OFMatchV3)this.match).get(MatchField.ETH_DST).getBytes());
                            OVXLinkUtils lUtils = new OVXLinkUtils(
                                    sw.getTenantId(), link.getLinkId(), flowId);
                            this.match = lUtils.rewriteMatch(this.getMatch());
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
        this.setActions(this.approvedActions);
        if (pflag) {
            this.flags.add(OFFlowModFlags.SEND_FLOW_REM);
            sw.sendSouth(this, inPort);
        }
    }


    private void prependRewriteActions() {
        if (!this.getMatch().isFullyWildcarded(MatchField.IPV4_SRC)) {
            OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                    ((OFMatchV3)this.match).get(MatchField.IPV4_SRC).getInt())));
            final OVXActionSetField srcAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Src);
            this.approvedActions.add(0, srcAct);
        }

        if (!this.getMatch().isFullyWildcarded(MatchField.IPV4_DST)) {
            OFOxmIpv4Dst oxmIpv4Dst = new OFOxmIpv4DstVer13(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                    ((OFMatchV3)this.match).get(MatchField.IPV4_DST).getInt())));
            final OVXActionSetField dstAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Dst);
            this.approvedActions.add(0, dstAct);
        }
    }

    /**
     * @param flagbit
     *            The OFFlowMod flag
     * @return true if the flag is set
     */
    public boolean hasFlag(short flagbit) {
        return (this.flags.contains(OFFlowModFlagsSerializerVer13.ofWireValue(flagbit)));
    }

    public OVXFlowMod clone() {
        OVXFlowMod flowMod = null;
        flowMod = OVXFactoryInst.myOVXFactory.buildOVXFlowModify(this.getXid(),this.getMatch(), this.getCookie(), this.getIdleTimeout(), this.getHardTimeout(),this.getPriority(), this.getBufferId(), this.getOutPort(), this.getFlags(), this.getActions());
        return flowMod;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("Match", this.getMatch());
        map.put("instructions", this.getInstructions());
        map.put("priority", String.valueOf(this.getPriority()));
        return map;
    }

    public void setVirtualCookie() {
        long tmp = this.ovxCookie;
        this.ovxCookie = this.cookie.getValue();
        this.cookie = U64.of(tmp);
    }

}

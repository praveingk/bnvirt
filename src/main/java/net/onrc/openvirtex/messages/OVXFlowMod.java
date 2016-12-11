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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.elements.Mapper.TenantMapper;
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
import net.onrc.openvirtex.messages.actions.OVXActionVirtualLanIdentifier;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.util.OVXUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFError.OFFlowModFailedCode;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.Wildcards;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;

public class OVXFlowMod extends OFFlowMod implements Devirtualizable {


    private final Logger log = LogManager.getLogger(OVXFlowMod.class.getName());

    private OVXSwitch sw = null;
    private List<OFAction> approvedActions = new LinkedList<OFAction>();
    private long ovxCookie = -1;

    @Override
    public void devirtualize(final OVXSwitch sw) {
        /* Drop LLDP-matching messages sent by some applications */
        if (this.match.getDataLayerType() == Ethernet.TYPE_LLDP) {
            return;
        }

        this.sw = sw;
        FlowTable ft = this.sw.getFlowTable();

        int bufferId = OVXPacketOut.BUFFER_ID_NONE;
        if (sw.getFromBufferMap(this.bufferId) != null) {
            bufferId = sw.getFromBufferMap(this.bufferId).getBufferId();
        }
        final short inport = this.getMatch().getInputPort();

        /* Below logic, are some throttling to make sure flowmod contains some basic Match criteria
         * We basically want to avoid situations where flowmod with "no match criteria" or "just an in_port" may arrive
         *
         * In such conditions, its best to deny the addition of the flow.
         */
        if (isMatchViolates()) {
            System.out.println("FlowMod Violates Isolation!!");
            
            return;
        }


        /* Below Logic surfaces when a flowadd appears without an in_port.
         * In such cases, we enumerate the available ports on the ovxswitch, and
         * create a flow with each in_port.
         *
         * We need to have in_port to keep track of all mac translation, and to maintain isolation.
         */
        OVXPort ovxInPort = sw.getPort(inport);
        OFMatch origMatch = this.match.clone();
        List<OFAction> origActions = new LinkedList<>();
        for (final OFAction act : this.getActions()) {
            origActions.add(act);
        }
        if (ovxInPort == null) {
            System.out.println("OVXFlowMod : No inport Specificied..Enumerating all..");
            for (short iport : sw.getPorts().keySet()) {
                System.out.println("Initiating Devirtualization with inport : "+iport);
                OVXFlowMod myNewFlow = this.clone();
                OFMatch myMatch  = origMatch.clone();
                myMatch.setInputPort(iport);
                int wcard = myMatch.getWildcards()
                        & (~OFMatch.OFPFW_IN_PORT);
                myMatch.setWildcards(wcard);
                myNewFlow.setMatch(myMatch);
                myNewFlow.setActions(origActions);
                approvedActions = new LinkedList<OFAction>();
                myNewFlow.devirtualize(sw);
                System.out.println("----------------------------------");
            }
            return;
        }

        //System.out.println("Devirtualizing inport = "+ inport);
        /* let flow table process FlowMod, generate cookie as needed */
        boolean pflag = ft.handleFlowMods(this.clone());

        /* used by OFAction virtualization */
        OVXMatch ovxMatch = new OVXMatch(this.match);
        ovxCookie = ((OVXFlowTable) ft).getCookie(this, false);
        ovxMatch.setCookie(ovxCookie);
        this.setCookie(ovxMatch.getCookie());
        System.out.println("Match : "+ovxMatch.toString() +" inport = "+ovxMatch.getInputPort());
        for (final OFAction act : this.getActions()) {
            try {

                ((VirtualizableAction) act).virtualize(sw,
                        this.approvedActions, ovxMatch);
                    //System.out.println("New Match : "+ ovxMatch.toString() + " cookie = "+ovxCookie);
                    //System.out.println("Finished virt.. action : "+this.approvedActions.toString());

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

        this.setBufferId(bufferId);

        if (ovxInPort == null) {
            System.out.println("Pravein: ovxinport is NULL!!, Enumerating all inports..");
            if (this.match.getWildcardObj().isWildcarded(Flag.IN_PORT)) {
                /* expand match to all ports */
                for (OVXPort iport : sw.getPorts().values()) {
                    int wcard = this.match.getWildcards()
                            & (~OFMatch.OFPFW_IN_PORT);
                    this.match.setWildcards(wcard);
                    prepAndSendSouth(iport, pflag);
                }
            } else {
                this.log.error(
                        "Unknown virtual port id {}; dropping flowmod {}",
                        inport, this);
                sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                        OFFlowModFailedCode.OFPFMFC_EPERM, this), sw);
                return;
            }
        } else {
            prepAndSendSouth(ovxInPort, pflag);
        }
    }

    private boolean isMatchViolates() {
        int wildcards = match.getWildcards();
        System.out.println("Wildcard match : "+ Integer.toBinaryString(wildcards));
        short vlan = 1;
        if ((wildcards & OFMatch.OFPFW_IN_PORT) == 1) {
            System.out.println("No Inport..");
        }
        if (!match.getWildcardObj().isWildcarded(Wildcards.Flag.DL_VLAN)) {
            vlan  = match.getDataLayerVirtualLan();
        }
        if (vlan > 2000) {
            System.out.println("Use of the VLAN prohibited.");
            return true;
        }
//        if ((wildcards & OFMatch.OFPFW_DL_SRC) == OFMatch.OFPFW_DL_SRC && (wildcards & OFMatch.OFPFW_DL_DST) == OFMatch.OFPFW_DL_DST) {
//            System.out.println("No Mac src/dest..");
//            if ((wildcards & OFMatch.OFPFW_NW_SRC_ALL) == OFMatch.OFPFW_NW_SRC_ALL && (wildcards & OFMatch.OFPFW_NW_DST_ALL) == OFMatch.OFPFW_NW_DST_ALL) {
//                System.out.println("No ipsrc/dest too.. This can't be good..");
//                return true;
//            }
//        }
        return false;
    }

    private void prepAndSendSouth(OVXPort inPort, boolean pflag) {
        if (!inPort.isActive()) {
            log.warn("Virtual network {}: port {} on switch {} is down.",
                    sw.getTenantId(), inPort.getPortNumber(),
                    sw.getSwitchName());
            return;
        }
        this.getMatch().setInputPort(inPort.getPhysicalPortNumber());
        OVXMessageUtil.translateXid(this, inPort);
        try {
            if (inPort.isEdge()) {
                this.prependRewriteActions();
            } else {
                TenantMapper.rewriteMatch(sw.getTenantId(), this.match);
                System.out.println(" New Match after rewriting VLAN  : "+ this.match.toString());
                // TODO: Verify why we have two send points... and if this is
                // the right place for the match rewriting
                if (inPort != null
                        && inPort.isLink()
                        && (!this.match.getWildcardObj().isWildcarded(
                                Flag.DL_DST) || !this.match.getWildcardObj()
                                .isWildcarded(Flag.DL_SRC))) {
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
                                    .getFlowId(this.match.getDataLayerSource(),
                                            this.match.getDataLayerDestination());
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
        this.computeLength();
        if (pflag) {
            this.flags |= OFFlowMod.OFPFF_SEND_FLOW_REM;
            sw.sendSouth(this, inPort);
        }
    }

    private void computeLength() {
        this.setActions(this.approvedActions);
        this.setLengthU(OVXFlowMod.MINIMUM_LENGTH);
        for (final OFAction act : this.approvedActions) {
            this.setLengthU(this.getLengthU() + act.getLengthU());
        }
    }

    private void prependRewriteActions() {

        final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifier();
        boolean match_vlan = false;
        short vlan = 1;
        int index = -1;
        int stripindex = -1;
        if (!match.getWildcardObj().isWildcarded(Wildcards.Flag.DL_VLAN)) {
            match_vlan = true;
        }
        System.out.println("Inside PrependRewriteActions .. Approved Actions so far :"+ approvedActions.toString());
        for (int i=0;i<this.approvedActions.size();i++) {
            OFAction action = approvedActions.get(i);
            if (action.getType() == OFActionType.SET_VLAN_ID) {
                OVXActionVirtualLanIdentifier existingVlan = (OVXActionVirtualLanIdentifier) action;
                vlan = existingVlan.getVirtualLanIdentifier();
                index = i;
                System.out.println("Found Vlan action at index "+ i+ " with vlan tag "+ vlan);
                break;
            }
            if (action.getType() == OFActionType.STRIP_VLAN) {
                vlan = 1;
                stripindex = i;
                System.out.println("Found Strip Vlan at index "+ i);
            }
        }
        if (index > -1) {
            approvedActions.remove(index);
        }
        if (stripindex > -1) {
            approvedActions.remove(stripindex);
        }
        ovlan.setVirtualLanIdentifier(TenantMapper.getPhysicalVlan(sw.getTenantId(), vlan));

        this.approvedActions.add(0,ovlan);

        System.out.println("Pravein: Rewriting Action.. to set vlan ID "+ vlan+" to "+ TenantMapper.getPhysicalVlan(sw.getTenantId(), vlan));

//
//        if (!this.match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
//            final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource();
//            srcAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(),
//                    this.match.getNetworkSource()));
//            this.approvedActions.add(0, srcAct);
//        }
//
//        if (!this.match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
//            final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination();
//            dstAct.setNetworkAddress(IPMapper.getPhysicalIp(sw.getTenantId(),
//                    this.match.getNetworkDestination()));
//            this.approvedActions.add(0, dstAct);
//        }
    }

    /**
     * @param flagbit
     *            The OFFlowMod flag
     * @return true if the flag is set
     */
    public boolean hasFlag(short flagbit) {
        return (this.flags & flagbit) == flagbit;
    }

    public OVXFlowMod clone() {
        OVXFlowMod flowMod = null;
        try {
            flowMod = (OVXFlowMod) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Error cloning flowMod: {}", this);
        }
        return flowMod;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (this.match != null) {
            map.put("match", new OVXMatch(match).toMap());
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
        this.ovxCookie = this.cookie;
        this.cookie = tmp;
    }


}

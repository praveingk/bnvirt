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
package net.onrc.openvirtex.messages.actions.ver13;

import net.onrc.openvirtex.api.Global.GlobalConfig;
import net.onrc.openvirtex.api.Global.TAG;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.Mapper.TenantMapperVlan;
import net.onrc.openvirtex.elements.Mapper.TenantMapperTos;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.*;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.actions.OVXActionOutputV3;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.protocol.OVXMatchV3;
import net.onrc.openvirtex.routing.SwitchRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmEthDst;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmEthSrc;
import org.projectfloodlight.openflow.protocol.ver13.OFActionOutputVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFActionSetFieldVer13;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.types.U64;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OVXActionOutputVer13 extends OFActionOutputVer13 implements
OVXActionOutputV3 {
    public OVXActionOutputVer13(OFPort port, int maxLen) {
		super(port, maxLen);
		// TODO Auto-generated constructor stub
	}

	Logger log = LogManager.getLogger(OVXActionOutputVer13.class.getName());

    @Override
    public void virtualize(final OVXSwitch sw,
            final List<OFAction> approvedActions, final OVXMatchV3 match)
                    throws ActionVirtualizationDenied, DroppedMessageException {
        final OVXPort inPort = sw.getPort(match.get(MatchField.IN_PORT).getShortPortNumber());

        // TODO: handle TABLE output port here

        final LinkedList<OVXPort> outPortList = this.fillPortList(
                match.get(MatchField.IN_PORT).getShortPortNumber(), this.getPort().getShortPortNumber(), sw);
        final OVXNetwork vnet;
        try {
            vnet = sw.getMap().getVirtualNetwork(sw.getTenantId());
        } catch (NetworkMappingException e) {
            log.warn("{}: skipping processing of OFAction", e);
            System.out.println("Networking mapping error!!!-------------------------------------------");
            return;
        }

        System.out.println("Inside OVXActionOutput ");
        if (match.isFlowMod()) {
            /*
             * FlowMod management Iterate through the output port list. Two main
             * scenarios: - OVXSwitch is BigSwitch and inPort & outPort belongs
             * to different physical switches - Other cases, e.g. SingleSwitch
             * and BigSwitch with inPort & outPort belonging to the same
             * physical switch
             */
            // Retrieve the flowMod from the virtual flow map
            OVXFlowMod fm;
            try {
                fm = sw.getFlowMod(match.getCookie());
            } catch (MappingException e) {
                log.warn("FlowMod not found in our FlowTable");
                return;
            }
            if (fm.getCommand().equals(OFFlowModCommand.ADD))
                fm= OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(fm.getXid(), U64.of(match.getCookie()), U64.ZERO, fm.getTableId(), fm.getIdleTimeout(), fm.getHardTimeout(), fm.getPriority(), fm.getBufferId(), fm.getOutPort(), fm.getOutGroup(), fm.getFlags(), fm.getMatch(), fm.getInstructions());
            else if (fm.getCommand().equals(OFFlowModCommand.DELETE))
                fm=OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(fm.getXid(), U64.of(match.getCookie()), U64.ZERO, fm.getTableId(), fm.getIdleTimeout(), fm.getHardTimeout(), fm.getPriority(), fm.getBufferId(), fm.getOutPort(), fm.getOutGroup(), fm.getFlags(), fm.getMatch(), fm.getInstructions());
            else if (fm.getCommand().equals(OFFlowModCommand.DELETE_STRICT))
                fm=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(fm.getXid(), U64.of(match.getCookie()), U64.ZERO, fm.getTableId(), fm.getIdleTimeout(), fm.getHardTimeout(), fm.getPriority(), fm.getBufferId(), fm.getOutPort(), fm.getOutGroup(), fm.getFlags(), fm.getMatch(), fm.getInstructions());
            else if (fm.getCommand().equals(OFFlowModCommand.MODIFY))
                fm=OVXFactoryInst.myOVXFactory.buildOVXFlowModify(fm.getXid(), U64.of(match.getCookie()), U64.ZERO, fm.getTableId(), fm.getIdleTimeout(), fm.getHardTimeout(), fm.getPriority(), fm.getBufferId(), fm.getOutPort(), fm.getOutGroup(), fm.getFlags(), fm.getMatch(), fm.getInstructions());
            else if (fm.getCommand().equals(OFFlowModCommand.MODIFY_STRICT))
                fm=OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(fm.getXid(), U64.of(match.getCookie()), U64.ZERO, fm.getTableId(), fm.getIdleTimeout(), fm.getHardTimeout(), fm.getPriority(), fm.getBufferId(), fm.getOutPort(), fm.getOutGroup(), fm.getFlags(), fm.getMatch(), fm.getInstructions());


            // TODO: Check if the FM has been retrieved

            for (final OVXPort outPort : outPortList) {
                Integer linkId = 0;
                Integer flowId = 0;
                /*
                 * OVXSwitch is BigSwitch and inPort & outPort belongs to
                 * different physical switches
                 */
                if (sw instanceof OVXBigSwitch
                        && inPort.getPhysicalPort().getParentSwitch() != outPort
                        .getPhysicalPort().getParentSwitch()) {
                    // Retrieve the route between the two OVXPorts
                    final OVXBigSwitch bigSwitch = (OVXBigSwitch) outPort
                            .getParentSwitch();
                    final SwitchRoute route = bigSwitch.getRoute(inPort,
                            outPort);
                    if (route == null) {
                        this.log.error(
                                "Cannot retrieve the bigswitch internal route between ports {} {}, dropping message",
                                inPort, outPort);
                        throw new DroppedMessageException(
                                "No such internal route");
                    }

                    // If the inPort belongs to an OVXLink, add rewrite actions
                    // to unset the packet link fields
                    if (inPort.isLink()) {
                        final OVXPort dstPort = vnet.getNeighborPort(inPort);
                        final OVXLink link = inPort.getLink().getOutLink();
                        if (link != null
                                && (!match.isFullyWildcarded(MatchField.ETH_DST) || !match.isFullyWildcarded(MatchField.ETH_SRC))) {
                            try {
                                flowId = vnet.getFlowManager().getFlowId(
                                        match.get(MatchField.ETH_SRC).getBytes(),
                                        match.get(MatchField.ETH_DST).getBytes());
                                OVXLinkUtils lUtils = new OVXLinkUtils(
                                        sw.getTenantId(), link.getLinkId(), flowId);
                                approvedActions.addAll(lUtils.unsetLinkFields(false, false));
                            } catch (IndexOutOfBoundException e) {
                                log.error(
                                        "Too many host to generate the flow pairs in this virtual network {}. "
                                                + "Dropping flow-mod {} ",
                                                sw.getTenantId(), fm);
                                throw new DroppedMessageException();
                            }
                        } else {
                            this.log.error(
                                    "Cannot retrieve the virtual link between ports {} {}, dropping message",
                                    dstPort, inPort);
                            return;
                        }
                    }


                    route.generateRouteFMs(fm.clone());
                    final short OFPP_IN_PORT_SHORT = (short) 0xFFf8;

                    // add the output action with the physical outPort (srcPort
                    // of the route)
                    if (inPort.getPhysicalPortNumber() != route
                            .getPathSrcPort().getPortNo().getShortPortNumber()) {
                        approvedActions.add(OVXFactoryInst.myFactory.actions().
                        		output(route.getPathSrcPort().getPortNo(), 0));
                    } else {
                        approvedActions.add(OVXFactoryInst.myFactory.actions().
                        		output(OFPort.IN_PORT, 0));
                    }
                } else {
                    /*
                     * SingleSwitch and BigSwitch with inPort & outPort
                     * belonging to the same physical switch
                     */
                    if (inPort.isEdge()) {
                        if (outPort.isEdge()) {
                            // TODO: this is logically incorrect, i have to do
                            // this because we always add the rewriting actions
                            // in the flowMod. Change it.
                            if (GlobalConfig.bnvTagType == TAG.TOS) {
                                TenantMapperTos.prependUnRewriteActions((OFMatchV3) fm.getMatch(), approvedActions);
                            } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                                TenantMapperVlan.prependUnRewriteActions((OFMatchV3) fm.getMatch(), approvedActions);
                            }  else if (GlobalConfig.bnvTagType == TAG.IP) {
                            	approvedActions.addAll(IPMapper.prependUnRewriteActions(match));
                            } else if (GlobalConfig.bnvTagType == TAG.NOTAG){
                                /* Do Nothing */
                            }
                        } else {
                            /*
                             * If inPort is edge and outPort is link:
                             * - retrieve link
                             * - generate the link's FMs
                             * - add actions to current FM to write packet fields
                             * related to the link
                             */
                            System.out.println("outPort is link");
                            final OVXLink link = outPort.getLink().getOutLink();
                            linkId = link.getLinkId();
                            try {
                                flowId = vnet.getFlowManager().getFlowId(
                                        match.get(MatchField.ETH_SRC).getBytes(),
                                        match.get(MatchField.ETH_DST).getBytes());
                                link.generateLinkFMsV3(fm.clone(), flowId);
                                approvedActions.addAll(new OVXLinkUtils(sw
                                        .getTenantId(), linkId, flowId)
                                .setLinkFields());
                            } catch (IndexOutOfBoundException e) {
                                log.error(
                                        "Too many host to generate the flow pairs in this virtual network {}. "
                                                + "Dropping flow-mod {} ",
                                                sw.getTenantId(), fm);
                                throw new DroppedMessageException();
                            }
                        }
                    } else {
                        if (outPort.isEdge()) {
                            /*
                             * If inPort belongs to a link and outPort is edge:
                             * - retrieve link
                             * - add actions to current FM to restore original IPs
                             * - add actions to current FM to restore packet fields
                             * related to the link
                             */
                            System.out.println("outport is edge");

                            if (GlobalConfig.bnvTagType == TAG.TOS) {
                                TenantMapperTos.prependUnRewriteActions(match, approvedActions);
                            } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                                TenantMapperVlan.prependUnRewriteActions(match, approvedActions);
                            }  else if (GlobalConfig.bnvTagType == TAG.IP) {
                                 approvedActions.addAll(IPMapper.prependUnRewriteActions(match));
                            } else if (GlobalConfig.bnvTagType == TAG.NOTAG){
                                /* Do Nothing */
                            }
                            // rewrite the OFMatch with the values of the link
                            final OVXPort dstPort = vnet
                                    .getNeighborPort(inPort);
                            final OVXLink link = dstPort.getLink().getOutLink();
                            if (link != null) {
                                try {
                                    flowId = vnet.getFlowManager().getFlowId(
                                            match.get(MatchField.ETH_SRC).getBytes(),
                                            match.get(MatchField.ETH_DST).getBytes());
                                    OVXLinkUtils lUtils = new OVXLinkUtils(
                                            sw.getTenantId(), link.getLinkId(),
                                            flowId);
                                    // Don't rewrite src or dst MAC if the action already exists
                                    boolean skipSrcMac = false;
                                    boolean skipDstMac = false;
                                    for (final OFAction act : approvedActions) {
                                        if (act instanceof OFActionSetFieldVer13) {
                                            if (((OFActionSetFieldVer13)act).getField() instanceof OFOxmEthSrc) {
                                                skipSrcMac = true;
                                            }
                                            if (((OFActionSetFieldVer13)act).getField() instanceof OFOxmEthDst) {
                                                skipDstMac = true;
                                            }
                                        }
                                    }
                                    approvedActions.addAll(lUtils.unsetLinkFields(skipSrcMac, skipDstMac));
                                } catch (IndexOutOfBoundException e) {
                                    log.error(
                                            "Too many host to generate the flow pairs in this virtual network {}. "
                                                    + "Dropping flow-mod {} ",
                                                    sw.getTenantId(), fm);
                                    throw new DroppedMessageException();
                                }
                            } else {
                                // TODO: substitute all the return with
                                // exceptions
                                this.log.error(
                                        "Cannot retrieve the virtual link between ports {} {}, dropping message",
                                        dstPort, inPort);
                                return;
                            }
                        } else {
                            System.out.println("Nothng else");
                            final OVXLink link = outPort.getLink().getOutLink();
                            linkId = link.getLinkId();
                            try {
                                flowId = vnet.getFlowManager().getFlowId(
                                        match.get(MatchField.ETH_SRC).getBytes(),
                                        match.get(MatchField.ETH_DST).getBytes());
                                link.generateLinkFMs(fm.clone(), flowId);
                                approvedActions.addAll(new OVXLinkUtils(sw
                                        .getTenantId(), linkId, flowId)
                                .setLinkFields());
                            } catch (IndexOutOfBoundException e) {
                                log.error(
                                        "Too many host to generate the flow pairs in this virtual network {}. "
                                                + "Dropping flow-mod {} ",
                                                sw.getTenantId(), fm);
                                throw new DroppedMessageException();
                            }
                        }
                    }
                    if (inPort.getPhysicalPortNumber() != outPort
                            .getPhysicalPortNumber()) {
                        approvedActions.add(OVXFactoryInst.myFactory.actions().
                        		output(OFPort.ofShort(outPort
                                        .getPhysicalPortNumber()),0));
                        		
                        		
                    } else {
                        approvedActions.add(OVXFactoryInst.myFactory.actions().
                        		output(OFPort.IN_PORT,0));
                               
                    }
                }
                // TODO: Check if I need to do the unrewrite here for the single
                // switch
            }
        } else if (match.isPacketOut()) {
            /*
             * PacketOut management. Iterate through the output port list.
             *
             * Three possible scenarios:
             * (1) outPort belongs to a link: send a packetIn coming from the
             * virtual link end point to the controller
             * (2) outPort is an edge port: two different sub-cases:
             * (2a) inPort & outPort belong to the same physical switch: rewrite outPort
             * (2b) inPort & outPort belong to different switches (bigSwitch):
             * send a packetOut to the physical port @ the end of the BS route.
             */

            // TODO check how to delete the packetOut and if it's required
            boolean throwException = true;
            System.out.println("Its a packetout");
            for (final OVXPort outPort : outPortList) {
                /**
                 * If the outPort belongs to a virtual link, generate a packetIn
                 * coming from the end point of the link to the controller.
                 */
                if (outPort.isLink()) {
                    final OVXPort dstPort = outPort.getLink().getOutLink()
                            .getDstPort();
                    dstPort.getParentSwitch().sendMsg(OVXFactoryInst.myOVXFactory.buildOVXPacketIn(match.getPktData(), dstPort.getPortNo().getShortPortNumber()), sw);
                    /**
                     * Pravein : Unset the throwException
                     * Basically, this was not set to false in the original OVX code, not sure why.
                     * However logically it seems wrong to unset it. Also, connectivity is lost.
                     *
                     * Hence, we set it to false.
                     */

                    throwException = false;
                    this.log.debug(
                            "Generate a packetIn from OVX Port {}/{}, physicalPort {}/{}",
                            dstPort.getParentSwitch().getSwitchName(),
                            dstPort.getPortNo(), dstPort.getPhysicalPort()
                            .getParentSwitch().getSwitchName(),
                            dstPort.getPhysicalPortNumber());
                } else if (sw instanceof OVXBigSwitch) {
                    /**
                     * Big-switch management. Generate a packetOut to the
                     * physical outPort
                     */
                    // Only generate pkt_out if a route is configured between in
                    // and output port.
                    // If parent switches are identical, no route will be configured
                    // although we do want to output the pkt_out.
                    if ((inPort == null)
                            || (inPort.getParentSwitch() == outPort.getParentSwitch())
                            || (((OVXBigSwitch) sw).getRoute(inPort, outPort) != null)) {
                        final PhysicalPort dstPort = outPort.getPhysicalPort();
                        dstPort.getParentSwitch().sendMsg(OVXFactoryInst.myOVXFactory
                        		.buildOVXPacketOut(match.getPktData(),OFPort.ANY,
                                        dstPort.getPortNo()), sw);
                        this.log.debug("PacketOut for a bigSwitch port, "
                                + "generate a packet from Physical Port {}/{}",
                                dstPort.getParentSwitch().getSwitchName(),
                                dstPort.getPortNo());
                    }
                } else {
                    /**
                     * Else (e.g. the outPort is an edgePort in a single switch)
                     * modify the packet and send to the physical switch.
                     */
                    throwException = false;

                    if (GlobalConfig.bnvTagType == TAG.TOS) {
                        TenantMapperTos.prependUnRewriteActions(match, approvedActions);
                    } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                        TenantMapperVlan.prependUnRewriteActions(match, approvedActions);
                    } else if (GlobalConfig.bnvTagType == TAG.IP) {
                    	approvedActions.addAll(IPMapper.prependUnRewriteActions(match));
                    } else if (GlobalConfig.bnvTagType == TAG.NOTAG){
                        /* Do Nothing */
                    }
                    approvedActions.add(OVXFactoryInst.myFactory.actions()
                    		.output(OFPort.ofShort(outPort.getPhysicalPortNumber()), 0)
                    		);
                    this.log.debug(
                            "Physical ports are on the same physical switch, rewrite only outPort to {}",
                            outPort.getPhysicalPortNumber());
                }
            }
            if (throwException) {
                throw new DroppedMessageException();
            }
        }

    }

    private LinkedList<OVXPort> fillPortList(final Short inPort,
            final Short outPort, final OVXSwitch sw)
                    throws DroppedMessageException {
;
        final short OFPP_ALL_SHORT = (short) 0xFFfc;
        final short OFPP_FLOOD_SHORT = (short) 0xFFfb;
        final short OFPP_MAX_SHORT = (short) 0xFF00;

        System.out.println(Long.toHexString(U16.f(outPort)));
        final LinkedList<OVXPort> outPortList = new LinkedList<OVXPort>();
        if (U16.f(outPort) < U16.f(OFPP_MAX_SHORT)) {
            if (sw.getPort(outPort) != null && sw.getPort(outPort).isActive()) {
                outPortList.add(sw.getPort(outPort));
            }
        } else if (U16.f(outPort) == U16.f(OFPP_FLOOD_SHORT)) {
            final Map<Short, OVXPort> ports = sw.getPorts();
            for (final OVXPort port : ports.values()) {
                if (port.getPortNo().getShortPortNumber() != inPort && port.isActive()) {
                    outPortList.add(port);
                }
            }
        } else if (U16.f(outPort) == U16.f(OFPP_ALL_SHORT)) {
            final Map<Short, OVXPort> ports = sw.getPorts();
            for (final OVXPort port : ports.values()) {
                if (port.isActive()) {
                    outPortList.add(port);
                }
            }
        } else {
            log.warn(
                    "Output port from controller currently not supported. Short = {}, Exadecimal = 0x{}",
                    U16.f(outPort),
                    Integer.toHexString(U16.f(outPort) & 0xffff));
        }

        if (outPortList.size() < 1) {
            throw new DroppedMessageException(
                    "No output ports defined; dropping");
        }
        return outPortList;
    }

}

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

import com.google.common.hash.PrimitiveSink;
import net.onrc.openvirtex.api.Global.GlobalConfig;
import net.onrc.openvirtex.api.Global.TAG;


import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.Mapper.MeterMap;
import net.onrc.openvirtex.elements.Mapper.TenantMapperTos;
import net.onrc.openvirtex.elements.Mapper.TenantMapperVlan;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.*;
import net.onrc.openvirtex.messages.OVXFlowAdd;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXMeterMod;
import net.onrc.openvirtex.messages.actions.*;
import net.onrc.openvirtex.messages.actions.ver13.OVXActionSetFieldVer13;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.protocol.OVXMatch;
import net.onrc.openvirtex.protocol.OVXMatchV3;
import net.onrc.openvirtex.util.OVXUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmInPort;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Dst;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Src;
import org.projectfloodlight.openflow.protocol.ver13.*;
import org.projectfloodlight.openflow.types.*;

import java.util.*;

public class OVXFlowAddVer13 extends OFFlowAddVer13 implements OVXFlowAdd {

   

    protected OVXFlowAddVer13(
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
 	}

	private final Logger log = LogManager.getLogger(OVXFlowAddVer13.class.getName());

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
        if ((this.getMatch()).get(MatchField.IN_PORT) != null) {
            inport = (this.getMatch()).get(MatchField.IN_PORT).getShortPortNumber();
        }
        /* Below logic, are some throttling to make sure flowmod contains some basic Match criteria
         * We basically want to avoid situations where flowmod with "no match criteria" or "just an in_port" may arrive
         *
         * In such conditions, its best to deny the addition of the flow.
         */
        if (GlobalConfig.bnvTagType == TAG.NOTAG) {
            /* If there is no explicit tagging, we need a clear checking mechanism */
            if (isMatchViolates()) {
                System.out.println("FlowMod Violates Isolation!!");
                sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                        OFFlowModFailedCode.OVERLAP, this), sw);
                return;
            }
        }

        System.out.println("=====================FLOWMOD Add=========================");
        /* Below Logic surfaces when a flowadd appears without an in_port.
         * In such cases, we enumerate the available ports on the ovxswitch, and
         * create a flow with each in_port.
         *
         * We need to have in_port to keep track of all mac translation, and to maintain isolation.
         */
        OVXPort ovxInPort = sw.getPort(inport);
        OFMatchV3 origMatch = OVXFactoryInst.myOVXFactory.buildOVXMatchV3(this.match);
        List<OFAction> origActions = new LinkedList<>();
        if (isActionViolates(origActions)) {
            System.out.println("FlowMod Violates Isolation!! Action VLan not supported!");
            sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                    OFFlowModFailedCode.OVERLAP, this), sw);
            return;
        }

        if (ovxInPort == null) {
            System.out.println("OVXFlowMod : No inport Specificied..Enumerating all..");
            for (short iport : sw.getPorts().keySet()) {
                System.out.println("Initiating De-virtualization with inport : "+iport);
                OVXFlowMod myNewFlow = this.clone();
                OFMatchV3 myNewMatch = OVXFactoryInst.myOVXFactory.buildOVXMatchV3(this.match);
                addtoMatch(myNewMatch, sw.getPorts().get(iport).getPortNo());
                myNewFlow = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(this.getXid(), this.getCookie(), this.getCookieMask(), this.getTableId(), this.getIdleTimeout(), this.getHardTimeout(), this.getPriority(), this.getBufferId(), this.getOutPort(), this.getOutGroup(), this.getFlags(), myNewMatch, this.getInstructions());
                myNewFlow.devirtualize(sw);
                System.out.println("----------------------------------");
            }
            return;
        }
        /* let flow table process FlowMod, generate cookie as needed */
        boolean pflag = ft.handleFlowMods(this.clone());

        System.out.println("Came after handleFlowMod");
        /* used by OFAction virtualization */
        OVXMatchV3 ovxMatch = new OVXMatchV3((OFMatchV3) this.getMatch());
        ovxCookie = ((OVXFlowTable) ft).getCookie(this, false);
        ovxMatch.setCookie(ovxCookie);

        this.cookie=U64.of(ovxMatch.getCookie());

        System.out.println("Going after actions with ovxmatch="+ ovxMatch.toString());
        OVXPort outPort = null;
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
                if (act.getType().equals(OFActionType.OUTPUT)) {
                    short port  = ((OFActionOutputVer13)act).getPort().getShortPortNumber();
                    outPort = sw.getPort(port);
                }
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

        System.out.println("Came here..");

        this.bufferId=OFBufferId.of(bufferId);

        if (ovxInPort == null) {
            if (this.getMatch().get(MatchField.IN_PORT) == null) {
                /* expand match to all ports */
                for (OVXPort iport : sw.getPorts().values()) {
                    //Match.Builder builder = ((OFMatchV3)this.match).createBuilder().setExact(MatchField.IN_PORT, iport.getPortNo());
                    this.match = addtoMatch((OFMatchV3)this.match, iport.getPortNo());
                    prepAndSendSouth(iport, pflag, outPort);
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
            prepAndSendSouth(ovxInPort, pflag, outPort);

            //System.out.println("Done with Flowmod. Lets try adding a meter");
            //createMeter(sw);
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

    private boolean isActionViolates(List<OFAction> origActions) {
        for (int i=0;i< origActions.size();i++) {
//            if (origActions.get(i).getType() == OFActionType.SET_VLAN_VID) {
//                OFActionVirtualLanIdentifier vlanAcion =  (OFActionVirtualLanIdentifier) origActions.get(i);
//                if (vlanAcion.getVirtualLanIdentifier() != 1) {
//                    System.out.println("Vlan of "+vlanAcion.getVirtualLanIdentifier() + " not Allowed!");
//                    return true;
//                }
//            }
            if (origActions.get(i).getType() == OFActionType.STRIP_VLAN) {
                System.out.println("Strip VLAN action not supported!");
                return true;
            }
        }
        return false;
    }
    private boolean isMatchViolates() {
//        int wildcards = match.getWildcards();
//        //System.out.println("Wildcard match : "+ Integer.toBinaryString(wildcards));
//        short vlan = 1;
//        if ((wildcards & OFMatch.OFPFW_IN_PORT) == 1) {
//            System.out.println("No Inport.");
//        }
        /* No Need to prevent Vlan Matches. However, Throttle VLAN Action */
        //System.out.println("Is Vlan Present ? = "+ (wildcards & OFMatch.OFPFW_DL_VLAN) );
        //if ((wildcards & OFMatch.OFPFW_DL_VLAN) != OFMatch.OFPFW_DL_VLAN) {
        //    System.out.println("Match : "+match.toString());
        //   System.out.println("Use of the VLAN prohibited.");
        //    return true;
        //}

//        if ((wildcards & OFMatch.OFPFW_DL_SRC) == OFMatch.OFPFW_DL_SRC && (wildcards & OFMatch.OFPFW_DL_DST) == OFMatch.OFPFW_DL_DST) {
//            System.out.println("No Mac src/dest..");
//            if ((wildcards & OFMatch.OFPFW_NW_SRC_ALL) == OFMatch.OFPFW_NW_SRC_ALL && (wildcards & OFMatch.OFPFW_NW_DST_ALL) == OFMatch.OFPFW_NW_DST_ALL) {
//                System.out.println("No ipsrc/dest too.. This can't be good..");
//                System.out.println(" Rejecting "+ match.toString());
//                return true;
//            }
//        }
        return false;
    }
	
    private void prepAndSendSouth(OVXPort inPort, boolean pflag, OVXPort outport) {
        if (!inPort.isActive()) {
            log.warn("Virtual network {}: port {} on switch {} is down.",
                    sw.getTenantId(), inPort.getPortNo(),
                    sw.getSwitchName());
            return;
        }
        //this.match = this.match.createBuilder().setExact(MatchField.IN_PORT, OFPort.ofShort(inPort.getPhysicalPortNumber())).build();
        //this.match = this.getMatch().createBuilder().set.
        long meterId = MeterMap.meterMapper.get(outport).getMeterId();
        this.match = addtoMatch((OFMatchV3)this.match,  OFPort.ofShort(inPort.getPhysicalPortNumber()));
        System.out.println(this.match.toString());

        OVXMessageUtil.translateXid(this, inPort);
        try {
            if (inPort.isEdge()) {

                if (GlobalConfig.bnvTagType == TAG.TOS) {
                    TenantMapperTos.prependRewriteActions(sw.getTenantId(), (OFMatchV3)match, this.approvedActions);
                } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                    TenantMapperVlan.prependRewriteActions(sw.getTenantId(), (OFMatchV3) match, approvedActions);
                } else if (GlobalConfig.bnvTagType == TAG.IP) {
                    this.prependRewriteActions();
                } else if (GlobalConfig.bnvTagType == TAG.NOTAG){
                    /* Do Nothing */
                }
            } else {
                
                if (GlobalConfig.bnvTagType == TAG.TOS) {
                    TenantMapperTos.rewriteMatch(sw.getTenantId(), (OFMatchV3) this.match);
                } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                    TenantMapperVlan.rewriteMatch(sw.getTenantId(), (OFMatchV3) this.match);
                }  else if (GlobalConfig.bnvTagType == TAG.IP) {
                    this.match = IPMapper.rewriteMatch(sw.getTenantId(), this.match);
                } else if (GlobalConfig.bnvTagType == TAG.NOTAG){
                    /* Do Nothing */
                }
                // TODO: Verify why we have two send points... and if this is
                System.out.println("Done with rewritematch  :" + this.match.toString());
                // the right place for the match rewriting
                if (inPort != null
                        && inPort.isLink()
                        && (this.getMatch().get(MatchField.ETH_DST) != null || this.getMatch().get(MatchField.ETH_SRC) != null)) {
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
                            System.out.println("Going to rewrite in lutils");
                            this.match = lUtils.rewriteMatch(this.getMatch());
                            System.out.println(this.getMatch().toString());
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

        //this.setActions(this.approvedActions);
        this.setActionsWithMeter(meterId, this.approvedActions);
        System.out.println("&&&&&&&"+ Long.toHexString(sw.getSwitchId()) + "&&&&&&&Adding a New FlowMod : "+ this.toString());
        if (pflag) {
        	Set<OFFlowModFlags> flag_new=new HashSet<OFFlowModFlags>(this.flags);
        	flag_new.add(OFFlowModFlags.SEND_FLOW_REM);
            this.flags=flag_new;
            //System.out.println("Actually adding.$$$$");
            sw.sendSouth(this, inPort);
        }
    }

   

    private void prependRewriteActions() {
        if (this.getMatch().get(MatchField.IPV4_SRC) != null) {
            OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(IPMapper.getPhysicalIp(sw.getTenantId(),
                    ((OFMatchV3)this.match).get(MatchField.IPV4_SRC).getInt())));
            final OVXActionSetField srcAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Src);
            this.approvedActions.add(0, srcAct);
        }

        if (this.getMatch().get(MatchField.IPV4_DST) != null) {
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
        return (this.getFlags().contains(OFFlowModFlagsSerializerVer13.ofWireValue(flagbit)));
    }

    public OVXFlowMod clone() {
        OVXFlowMod flowMod = null;
        if (OVXFactoryInst.ofversion == 10) {
            flowMod = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(this.getXid(),this.getMatch(), this.getCookie(), this.getIdleTimeout(), this.getHardTimeout(),this.getPriority(), this.getBufferId(), this.getOutPort(), this.getFlags(), this.getActions());
        } else {
            flowMod = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(this.getXid(), this.getCookie(), this.getCookieMask(), this.getTableId(), this.getIdleTimeout(), this.getHardTimeout(), this.getPriority(), this.getBufferId(), this.getOutPort(), this.getOutGroup(), this.getFlags(), this.getMatch(), this.getInstructions());
        }
        return flowMod;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (this.match != null) {
            map.put("match", OVXFactoryInst.myOVXFactory.buildOVXMatchV3(match).toMap());
        }
        LinkedList<Map<String, Object>> actions = new LinkedList<Map<String, Object>>();

        for (OFAction act : this.getActions()) {
            try {
                actions.add(OVXUtil.actionToMap(act));
            } catch (UnknownActionException e) {
                log.warn("Ignoring action {} because {}", act, e.getMessage());
            }
        }
        map.put("instructionApplyActionsList", actions);
        map.put("priority", String.valueOf(this.getPriority()));
        return map;
    }

    public void setVirtualCookie() {
        long tmp = this.ovxCookie;
        this.ovxCookie = this.cookie.getValue();
        this.cookie = U64.of(tmp);
    }

    public void createMeter(OVXSwitch sw) {
        OVXMeterMod meterMod = null;
        int command = 0x0; //ADD
        int flags = 0x0;
        long meterId = 0x1;
        OFMeterBandDrop ofm = new OFMeterBandDropVer13(1000, 1000);
        List<OFMeterBand> ofmlist = new ArrayList<>();
        ofmlist.add(ofm);
        meterMod = OVXFactoryInst.myOVXFactory.buildOVXMeterMod(this.getXid(), command, flags, meterId, ofmlist);
        System.out.println("Creating Meter");
        meterMod.devirtualize(sw);
    }

}

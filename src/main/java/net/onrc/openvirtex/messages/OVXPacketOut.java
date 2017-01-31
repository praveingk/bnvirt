/*******************************************************************************
 * Copyright 2014 Open Networking Laboratory
 *se Apache License, Version 2.0 (the "License");
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.Mapper.TenantMapper;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.messages.actions.*;
import net.onrc.openvirtex.protocol.OVXMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFError.OFBadRequestCode;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;

public class OVXPacketOut extends OFPacketOut implements Devirtualizable {


    private final Logger log = LogManager.getLogger(OVXPacketOut.class
            .getName());
    private OFMatch match = null;
    private final List<OFAction> approvedActions = new LinkedList<OFAction>();
    private OVXPort inport = null;

    @Override
    public void devirtualize(final OVXSwitch sw) {

        final OVXPort inport = sw.getPort(this.getInPort());
        this.inport = inport;
        OVXMatch ovxMatch = null;

        //System.out.println("Inside OVXPacketOut : inPort(phys)="+ inport.getPhysicalPortNumber());

        if (this.getBufferId() == OVXPacketOut.BUFFER_ID_NONE) {
            if (this.getPacketData().length <= 14) {
                this.log.error("PacketOut has no buffer or data {}; dropping",
                        this);
                sw.sendMsg(OVXMessageUtil.makeErrorMsg(
                        OFBadRequestCode.OFPBRC_BAD_LEN, this), sw);
                return;
            }
            this.match = new OFMatch().loadFromPacket(this.packetData,
                    this.inPort);
            ovxMatch = new OVXMatch(match);
            ovxMatch.setPktData(this.packetData);
        } else {
            final OVXPacketIn cause = sw.getFromBufferMap(this.bufferId);
            if (cause == null) {
                this.log.error(
                        "Unknown buffer id {} for virtual switch {}; dropping",
                        this.bufferId, sw);
                return;
            }

            this.match = new OFMatch().loadFromPacket(cause.getPacketData(),
                    this.inPort);
            this.setBufferId(cause.getBufferId());
            ovxMatch = new OVXMatch(match);
            ovxMatch.setPktData(cause.getPacketData());
            if (cause.getBufferId() == OVXPacketOut.BUFFER_ID_NONE) {
                this.setPacketData(cause.getPacketData());
                this.setLengthU(this.getLengthU() + this.packetData.length);
            }
        }

        //System.out.println("OutputList : "+this.getActions().toString());

        for (final OFAction act : this.getActions()) {
            try {
                //System.out.println("Actions : "+ act.toString());
                if (act.getType().getTypeValue() == 0) {
                    OFActionOutput outAction = (OFActionOutput)act;
                    //System.out.println("Output : "+outAction.getPort());
                }
                ((VirtualizableAction) act).virtualize(sw,
                        this.approvedActions, ovxMatch);
                //System.out.println("Finished virtualization..");
            } catch (final ActionVirtualizationDenied e) {
                this.log.warn("Action {} could not be virtualized; error: {}",
                        act, e.getMessage());
                System.out.println("$$$$$$$$$$$$$$$$$$$ virt denied...");
                sw.sendMsg(OVXMessageUtil.makeError(e.getErrorCode(), this), sw);
                return;
            } catch (final DroppedMessageException e) {
                this.log.debug("####################Dropping packetOut {}", this);
                System.out.println("Dropping Packet.. for sw "+ sw.getName());
                e.printStackTrace();
                return;
            }
        }
        //System.out.println("Got Out of Loop of filling actions for sw : "+ sw.getName());

        if (U16.f(this.getInPort()) < U16.f(OFPort.OFPP_MAX.getValue())) {
            this.setInPort(inport.getPhysicalPortNumber());
        }
        this.prependRewriteActions(sw);
        this.setActions(this.approvedActions);
        this.setActionsLength((short) 0);
        this.setLengthU(OVXPacketOut.MINIMUM_LENGTH + this.packetData.length);
        for (final OFAction act : this.approvedActions) {
            this.setLengthU(this.getLengthU() + act.getLengthU());
            this.setActionsLength((short) (this.getActionsLength() + act
                    .getLength()));
        }

        // TODO: Beacon sometimes send msg with inPort == controller, check with
        // Ayaka if it's ok
        if (U16.f(this.getInPort()) < U16.f(OFPort.OFPP_MAX.getValue())) {
            OVXMessageUtil.translateXid(this, inport);
        }
        this.log.debug("Sending packet-out to sw {}: {}", sw.getName(), this);
        //System.out.println("Sending packet-out to sw {"+sw.getName()+"}+ {"+ this+"}" + "Inport = "+inport.getPhysicalPortNumber());
        sw.sendSouth(this, inport);
        //System.out.println("Finished sendSouth, returning call");
    }

    public void sendPacketsOut(final OVXSwitch sw) {
        if (this.approvedActions.size() == 0) {
            System.out.println("No action.. clear");
            return;
        }
        if (U16.f(this.getInPort()) < U16.f(OFPort.OFPP_MAX.getValue())) {
            this.setInPort(this.inport.getPhysicalPortNumber());
        }
        this.prependRewriteActions(sw);
        this.setActions(this.approvedActions);
        this.setActionsLength((short) 0);
        this.setLengthU(OVXPacketOut.MINIMUM_LENGTH + this.packetData.length);
        for (final OFAction act : this.approvedActions) {
            this.setLengthU(this.getLengthU() + act.getLengthU());
            this.setActionsLength((short) (this.getActionsLength() + act
                    .getLength()));
        }

        // TODO: Beacon sometimes send msg with inPort == controller, check with
        // Ayaka if it's ok
        if (U16.f(this.getInPort()) < U16.f(OFPort.OFPP_MAX.getValue())) {
            OVXMessageUtil.translateXid(this, inport);
        }
        this.log.debug("Sending packet-out to sw {}: {}", sw.getName(), this);
        System.out.println("Sending packet-out to sw {"+sw.getName()+"}+ {"+ this+"}" + "Inport = "+inport.getPhysicalPortNumber());
        sw.sendSouth(this, inport);
    }

    private void prependRewriteActions(final OVXSwitch sw) {
//
//        byte tos  = sw.getTenantId().byteValue();
//        final OVXActionNetworkTypeOfService ovtos = new OVXActionNetworkTypeOfService();
////        if (!match.getWildcardObj().isWildcarded(Wildcards.Flag.DL_VLAN)) {
////            vlan = match.getDataLayerVirtualLan();
////        }
//        System.out.println("Pravein: Rewriting Action.. to set tos to " + sw.getTenantId());
////
//        ovtos.setNetworkTypeOfService(tos);
//
////
//        approvedActions.add(0, ovtos);
//        System.out.println("Actions : "+ approvedActions.toString());
//        final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifier();
//        short vlan = 1;
//        ovlan.setVirtualLanIdentifier(TenantMapper.getPhysicalVlan(sw.getTenantId(), vlan));
//
//        this.approvedActions.add(ovlan);
//        System.out.println("Pravein: Rewriting Action.. to set vlan to "+ TenantMapper.getPhysicalVlan(sw.getTenantId(), vlan));
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

    public OVXPacketOut(final OVXPacketOut pktOut) {
        this.bufferId = pktOut.bufferId;
        this.inPort = pktOut.inPort;
        this.length = pktOut.length;
        this.packetData = pktOut.packetData;
        this.type = pktOut.type;
        this.version = pktOut.version;
        this.xid = pktOut.xid;
        this.actions = pktOut.actions;
        this.actionsLength = pktOut.actionsLength;
    }

    public OVXPacketOut() {
        super();
    }

    public OVXPacketOut(final byte[] pktData, final short inPort,
            final short outPort) {
        this.setInPort(inPort);
        this.setBufferId(OFPacketOut.BUFFER_ID_NONE);
        final OFActionOutput outAction = new OFActionOutput(outPort);
        final ArrayList<OFAction> actions = new ArrayList<OFAction>();
        actions.add(outAction);
        this.setActions(actions);
        this.setActionsLength(outAction.getLength());
        this.setPacketData(pktData);
        this.setLengthU((short) (OFPacketOut.MINIMUM_LENGTH
                + this.getPacketData().length + OFActionOutput.MINIMUM_LENGTH));
    }


}

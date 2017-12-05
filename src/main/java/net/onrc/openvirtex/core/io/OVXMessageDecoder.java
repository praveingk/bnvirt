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
package net.onrc.openvirtex.core.io;

import java.util.List;
import java.util.Set;

import net.onrc.openvirtex.core.OVXFactoryInst;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.errormsg.OFBadActionErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFBadRequestErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFFlowModFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFHelloFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFPortModFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.errormsg.OFQueueOpFailedErrorMsg;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleReplyVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleRequestVer10;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFErrorCauseData;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

/**
 * Decode an openflow message from a netty Channel.
 *
 * @author alshabib
 */
public class OVXMessageDecoder extends FrameDecoder {

    

    @Override
    protected Object decode(final ChannelHandlerContext ctx,
            final Channel channel, final ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            // if the channel is closed, there will be nothing to read.
            return null;
        }

        final OFMessage message = OVXFactoryInst.myFactory.getReader().readFrom(buffer);
       
        OFMessage ovxmessage=null;
        if(message!=null)
        {
            String class_name=message.getClass().getName();
            String of_message_type=class_name.substring(class_name.lastIndexOf(".") + 1);
            System.out.println("Inside OVX decode. "+ of_message_type);
            if(of_message_type.equals("OFHelloVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
            else if(of_message_type.equals("OFAggregateStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsReply(((OFAggregateStatsReply)message).getXid(),((OFAggregateStatsReply)message).getFlags(),((OFAggregateStatsReply)message).getPacketCount(), ((OFAggregateStatsReply)message).getByteCount(), ((OFAggregateStatsReply)message).getFlowCount());
            else if(of_message_type.equals("OFDescStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsReply(((OFDescStatsReply)message).getXid(),((OFDescStatsReply)message).getFlags(),((OFDescStatsReply)message).getMfrDesc(),((OFDescStatsReply)message).getHwDesc(),((OFDescStatsReply)message).getSwDesc(), ((OFDescStatsReply)message).getSerialNum(),((OFDescStatsReply)message).getDpDesc());
            else if(of_message_type.equals("OFFlowStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(((OFFlowStatsReply)message).getXid(),((OFFlowStatsReply)message).getFlags(), ((OFFlowStatsReply)message).getEntries());
            else if(of_message_type.equals("OFPortStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsReply(((OFPortStatsReply)message).getXid(),((OFPortStatsReply)message).getFlags() , ((OFPortStatsReply)message).getEntries());
            else if(of_message_type.equals("OFQueueStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsReply(((OFQueueStatsReply)message).getXid(), ((OFQueueStatsReply)message).getFlags(), ((OFQueueStatsReply)message).getEntries());
            else if(of_message_type.equals("OFTableStatsReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsReply(((OFTableStatsReply)message).getXid(),((OFTableStatsReply)message).getFlags() , ((OFTableStatsReply)message).getEntries());
            else if(of_message_type.equals("OFAggregateStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsRequest(((OFAggregateStatsRequest)message).getXid(),((OFAggregateStatsRequest)message).getFlags(), ((OFAggregateStatsRequest)message).getMatch(),((OFAggregateStatsRequest)message).getTableId(),((OFAggregateStatsRequest)message).getOutPort());
            else if(of_message_type.equals("OFDescStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsRequest(((OFDescStatsRequest)message).getXid(), ((OFDescStatsRequest)message).getFlags());
            else if(of_message_type.equals("OFFlowStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest(((OFFlowStatsRequest)message).getXid(), ((OFFlowStatsRequest)message).getFlags(), ((OFFlowStatsRequest)message).getMatch(), ((OFFlowStatsRequest)message).getTableId(), ((OFFlowStatsRequest)message).getOutPort());
            else if(of_message_type.equals("OFPortStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsRequest(((OFPortStatsRequest)message).getXid(), ((OFPortStatsRequest)message).getFlags(), ((OFPortStatsRequest)message).getPortNo());
            else if(of_message_type.equals("OFQueueStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsRequest(((OFQueueStatsRequest)message).getXid(),((OFQueueStatsRequest)message).getFlags(),((OFQueueStatsRequest)message).getPortNo(),((OFQueueStatsRequest)message).getQueueId());
            else if(of_message_type.equals("OFTableStatsRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsRequest(((OFTableStatsRequest)message).getXid(), ((OFTableStatsRequest)message).getFlags());
            else if(of_message_type.equals("OFBadActionErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadActionErrorMsg(((OFBadActionErrorMsg)message).getXid(),((OFBadActionErrorMsg)message).getCode(),((OFBadActionErrorMsg)message).getData());
            else if(of_message_type.equals("OFBadRequestErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(((OFBadRequestErrorMsg)message).getXid(), ((OFBadRequestErrorMsg)message).getCode(), ((OFBadRequestErrorMsg)message).getData());
            else if(of_message_type.equals("OFFlowModFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModFailedErrorMsg(((OFFlowModFailedErrorMsg)message).getXid(),((OFFlowModFailedErrorMsg)message).getCode(), ((OFFlowModFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFHelloFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHelloFailedErrorMsg(((OFHelloFailedErrorMsg)message).getXid(),((OFHelloFailedErrorMsg)message).getCode(), ((OFHelloFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFPortModFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortModFailedErrorMsg(((OFPortModFailedErrorMsg)message).getXid(),((OFPortModFailedErrorMsg)message).getCode(), ((OFPortModFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFQueueOpFailedErrorMsgVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueOpFailedErrorMsg(((OFQueueOpFailedErrorMsg)message).getXid(),((OFQueueOpFailedErrorMsg)message).getCode(), ((OFQueueOpFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFBarrierReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierReply(((OFBarrierReply)message).getXid());
            else if(of_message_type.equals("OFBarrierRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierRequest(((OFBarrierRequest)message).getXid());
            else if(of_message_type.equals("OFEchoReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoReply(((OFEchoReply)message).getXid(), ((OFEchoReply)message).getData());
            else if(of_message_type.equals("OFEchoRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoRequest(((OFEchoRequest)message).getXid(), ((OFEchoRequest)message).getData());
            else if(of_message_type.equals("OFFeaturesReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesReply(((OFFeaturesReply)message).getXid(), ((OFFeaturesReply)message).getDatapathId(),((OFFeaturesReply)message).getNBuffers(), ((OFFeaturesReply)message).getNTables(), ((OFFeaturesReply)message).getCapabilities(),  ((OFFeaturesReply)message).getActions(), ((OFFeaturesReply)message).getPorts());
            else if(of_message_type.equals("OFFeaturesRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesRequest(((OFFeaturesRequest)message).getXid());
            else if(of_message_type.equals("OFFlowAddVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(((OFFlowAdd)message).getXid(), ((OFFlowAdd)message).getMatch(),((OFFlowAdd)message).getCookie(), ((OFFlowAdd)message).getIdleTimeout(), ((OFFlowAdd)message).getHardTimeout(), ((OFFlowAdd)message).getPriority(), ((OFFlowAdd)message).getBufferId(), ((OFFlowAdd)message).getOutPort(), ((OFFlowAdd)message).getFlags(), ((OFFlowAdd)message).getActions());
            else if(of_message_type.equals("OFFlowDeleteVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(((OFFlowDelete)message).getXid(), ((OFFlowDelete)message).getMatch(),((OFFlowDelete)message).getCookie(), ((OFFlowDelete)message).getIdleTimeout(), ((OFFlowDelete)message).getHardTimeout(), ((OFFlowDelete)message).getPriority(), ((OFFlowDelete)message).getBufferId(), ((OFFlowDelete)message).getOutPort(), ((OFFlowDelete)message).getFlags(), ((OFFlowDelete)message).getActions());
            else if(of_message_type.equals("OFFlowDeleteStrictVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(((OFFlowDeleteStrict)message).getXid(), ((OFFlowDeleteStrict)message).getMatch(),((OFFlowDeleteStrict)message).getCookie(), ((OFFlowDeleteStrict)message).getIdleTimeout(), ((OFFlowDeleteStrict)message).getHardTimeout(), ((OFFlowDeleteStrict)message).getPriority(), ((OFFlowDeleteStrict)message).getBufferId(), ((OFFlowDeleteStrict)message).getOutPort(), ((OFFlowDeleteStrict)message).getFlags(), ((OFFlowDeleteStrict)message).getActions());
            else if(of_message_type.equals("OFFlowModifyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModify(((OFFlowModify)message).getXid(), ((OFFlowModify)message).getMatch(),((OFFlowModify)message).getCookie(), ((OFFlowModify)message).getIdleTimeout(), ((OFFlowModify)message).getHardTimeout(), ((OFFlowModify)message).getPriority(), ((OFFlowModify)message).getBufferId(), ((OFFlowModify)message).getOutPort(), ((OFFlowModify)message).getFlags(), ((OFFlowModify)message).getActions());
            else if(of_message_type.equals("OFFlowModifyStrictVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(((OFFlowModifyStrict)message).getXid(), ((OFFlowModifyStrict)message).getMatch(),((OFFlowModifyStrict)message).getCookie(), ((OFFlowModifyStrict)message).getIdleTimeout(), ((OFFlowModifyStrict)message).getHardTimeout(), ((OFFlowModifyStrict)message).getPriority(), ((OFFlowModifyStrict)message).getBufferId(), ((OFFlowModifyStrict)message).getOutPort(), ((OFFlowModifyStrict)message).getFlags(), ((OFFlowModifyStrict)message).getActions());
            else if(of_message_type.equals("OFFlowRemovedVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowRemoved(((OFFlowRemoved)message).getXid(),((OFFlowRemoved)message).getMatch(), ((OFFlowRemoved)message).getCookie(), ((OFFlowRemoved)message).getPriority(), ((OFFlowRemoved)message).getReason(), ((OFFlowRemoved)message).getDurationSec(), ((OFFlowRemoved)message).getDurationNsec(), ((OFFlowRemoved)message).getIdleTimeout(), ((OFFlowRemoved)message).getPacketCount(), ((OFFlowRemoved)message).getByteCount());
            else if(of_message_type.equals("OFGetConfigReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigReply(((OFGetConfigReply)message).getXid(),((OFGetConfigReply)message).getFlags(), ((OFGetConfigReply)message).getMissSendLen());
            else if(of_message_type.equals("OFGetConfigRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigRequest(((OFGetConfigRequest)message).getXid());
            else if(of_message_type.equals("OFHelloVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
            else if(of_message_type.equals("OFPacketInVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketIn(((OFPacketIn)message).getXid(),((OFPacketIn)message).getBufferId(), ((OFPacketIn)message).getTotalLen(),((OFPacketIn)message).getInPort(), ((OFPacketIn)message).getReason(), ((OFPacketIn)message).getData());
            else if(of_message_type.equals("OFPacketOutVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketOut(((OFPacketOut)message).getXid(),((OFPacketOut)message).getBufferId(),((OFPacketOut)message).getInPort(),((OFPacketOut)message).getActions(),((OFPacketOut)message).getData());
            else if(of_message_type.equals("OFPortModVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortMod(((OFPortMod)message).getXid(),((OFPortMod)message).getPortNo(),((OFPortMod)message).getHwAddr(), ((OFPortMod)message).getConfig(), ((OFPortMod)message).getMask(), ((OFPortMod)message).getAdvertise());
            else if(of_message_type.equals("OFPortStatusVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatus(((OFPortStatus)message).getXid(),((OFPortStatus)message).getReason(), ((OFPortStatus)message).getDesc());
            else if(of_message_type.equals("OFQueueGetConfigReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigReply(((OFQueueGetConfigReply)message).getXid(), ((OFQueueGetConfigReply)message).getPort(), ((OFQueueGetConfigReply)message).getQueues());
            else if(of_message_type.equals("OFQueueGetConfigRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigRequest(((OFQueueGetConfigRequest)message).getXid(),((OFQueueGetConfigRequest)message).getPort());
            else if(of_message_type.equals("OFSetConfigVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXSetConfig(((OFSetConfig)message).getXid(), ((OFSetConfig)message).getFlags(), ((OFSetConfig)message).getMissSendLen());
            else if(of_message_type.equals("OFNiciraControllerRoleReplyVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleReply(((OFNiciraControllerRoleReply)message).getXid(),((OFNiciraControllerRoleReply)message).getRole());
            else if(of_message_type.equals("OFNiciraControllerRoleRequestVer10"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleRequest(((OFNiciraControllerRoleRequest)message).getXid(),((OFNiciraControllerRoleRequest)message).getRole());

            /* OpenFlow 1.3 Support */

            if(of_message_type.equals("OFHelloVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
            else if(of_message_type.equals("OFAggregateStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsReply(((OFAggregateStatsReply)message).getXid(),((OFAggregateStatsReply)message).getFlags(),((OFAggregateStatsReply)message).getPacketCount(), ((OFAggregateStatsReply)message).getByteCount(), ((OFAggregateStatsReply)message).getFlowCount());
            else if(of_message_type.equals("OFPortDescStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortDescStatsReply(((OFPortDescStatsReply)message).getXid(),((OFPortDescStatsReply)message).getFlags(),((OFPortDescStatsReply)message).getEntries());
            else if(of_message_type.equals("OFRoleReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXRoleReply(((OFRoleReply)message).getXid(),((OFRoleReply)message).getRole(),((OFRoleReply)message).getGenerationId());
            else if(of_message_type.equals("OFDescStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsReply(((OFDescStatsReply)message).getXid(),((OFDescStatsReply)message).getFlags(),((OFDescStatsReply)message).getMfrDesc(),((OFDescStatsReply)message).getHwDesc(),((OFDescStatsReply)message).getSwDesc(), ((OFDescStatsReply)message).getSerialNum(),((OFDescStatsReply)message).getDpDesc());
            else if(of_message_type.equals("OFFlowStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsReply(((OFFlowStatsReply)message).getXid(),((OFFlowStatsReply)message).getFlags(), ((OFFlowStatsReply)message).getEntries());
            else if(of_message_type.equals("OFPortStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsReply(((OFPortStatsReply)message).getXid(),((OFPortStatsReply)message).getFlags() , ((OFPortStatsReply)message).getEntries());
            else if(of_message_type.equals("OFQueueStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsReply(((OFQueueStatsReply)message).getXid(), ((OFQueueStatsReply)message).getFlags(), ((OFQueueStatsReply)message).getEntries());
            else if(of_message_type.equals("OFTableStatsReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsReply(((OFTableStatsReply)message).getXid(),((OFTableStatsReply)message).getFlags() , ((OFTableStatsReply)message).getEntries());
            else if(of_message_type.equals("OFAggregateStatsRequestVer13"))
                ovxmessage= OVXFactoryInst.myOVXFactory.buildOVXAggregateStatsRequest(((OFAggregateStatsRequest)message).getXid(), ((OFAggregateStatsRequest)message).getFlags(), ((OFAggregateStatsRequest)message).getTableId(), ((OFAggregateStatsRequest)message).getOutPort(), ((OFAggregateStatsRequest)message).getOutGroup(), ((OFAggregateStatsRequest)message).getCookie(), ((OFAggregateStatsRequest)message).getCookieMask(), ((OFAggregateStatsRequest)message).getMatch());
            else if(of_message_type.equals("OFPortDescStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortDescStatsRequest(((OFPortDescStatsRequest)message).getXid(), ((OFPortDescStatsRequest)message).getFlags());
            else if(of_message_type.equals("OFRoleRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXRoleRequest(((OFRoleRequest)message).getXid(),((OFRoleRequest)message).getRole(),((OFRoleRequest)message).getGenerationId());
            else if(of_message_type.equals("OFDescStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXDescStatsRequest(((OFDescStatsRequest)message).getXid(), ((OFDescStatsRequest)message).getFlags());
            else if(of_message_type.equals("OFFlowStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowStatsRequest(((OFFlowStatsRequest)message).getXid(), ((OFFlowStatsRequest)message).getFlags(), ((OFFlowStatsRequest)message).getTableId(), ((OFFlowStatsRequest)message).getOutPort(), ((OFFlowStatsRequest)message).getOutGroup(), ((OFFlowStatsRequest)message).getCookie(), ((OFFlowStatsRequest)message).getCookieMask(), ((OFFlowStatsRequest)message).getMatch());
            else if(of_message_type.equals("OFPortStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatsRequest(((OFPortStatsRequest)message).getXid(), ((OFPortStatsRequest)message).getFlags(), ((OFPortStatsRequest)message).getPortNo());
            else if(of_message_type.equals("OFQueueStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueStatsRequest(((OFQueueStatsRequest)message).getXid(),((OFQueueStatsRequest)message).getFlags(),((OFQueueStatsRequest)message).getPortNo(),((OFQueueStatsRequest)message).getQueueId());
            else if(of_message_type.equals("OFTableStatsRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXTableStatsRequest(((OFTableStatsRequest)message).getXid(), ((OFTableStatsRequest)message).getFlags());
            else if(of_message_type.equals("OFBadActionErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadActionErrorMsg(((OFBadActionErrorMsg)message).getXid(),((OFBadActionErrorMsg)message).getCode(),((OFBadActionErrorMsg)message).getData());
            else if(of_message_type.equals("OFBadRequestErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBadRequestErrorMsg(((OFBadRequestErrorMsg)message).getXid(), ((OFBadRequestErrorMsg)message).getCode(), ((OFBadRequestErrorMsg)message).getData());
            else if(of_message_type.equals("OFFlowModFailedErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModFailedErrorMsg(((OFFlowModFailedErrorMsg)message).getXid(),((OFFlowModFailedErrorMsg)message).getCode(), ((OFFlowModFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFHelloFailedErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHelloFailedErrorMsg(((OFHelloFailedErrorMsg)message).getXid(),((OFHelloFailedErrorMsg)message).getCode(), ((OFHelloFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFPortModFailedErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortModFailedErrorMsg(((OFPortModFailedErrorMsg)message).getXid(),((OFPortModFailedErrorMsg)message).getCode(), ((OFPortModFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFQueueOpFailedErrorMsgVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueOpFailedErrorMsg(((OFQueueOpFailedErrorMsg)message).getXid(),((OFQueueOpFailedErrorMsg)message).getCode(), ((OFQueueOpFailedErrorMsg)message).getData());
            else if(of_message_type.equals("OFBarrierReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierReply(((OFBarrierReply)message).getXid());
            else if(of_message_type.equals("OFBarrierRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXBarrierRequest(((OFBarrierRequest)message).getXid());
            else if(of_message_type.equals("OFEchoReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoReply(((OFEchoReply)message).getXid(), ((OFEchoReply)message).getData());
            else if(of_message_type.equals("OFEchoRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXEchoRequest(((OFEchoRequest)message).getXid(), ((OFEchoRequest)message).getData());
            else if(of_message_type.equals("OFFeaturesReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesReply(((OFFeaturesReply)message).getXid(), ((OFFeaturesReply)message).getDatapathId(),((OFFeaturesReply)message).getNBuffers(), ((OFFeaturesReply)message).getNTables(),((OFFeaturesReply)message).getAuxiliaryId(), ((OFFeaturesReply)message).getCapabilities(), ((OFFeaturesReply)message).getReserved());
            else if(of_message_type.equals("OFFeaturesRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFeaturesRequest(((OFFeaturesRequest)message).getXid());
            else if(of_message_type.equals("OFFlowAddVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(((OFFlowAdd)message).getXid(), ((OFFlowAdd)message).getCookie(), ((OFFlowAdd)message).getCookieMask(), ((OFFlowAdd)message).getTableId(), ((OFFlowAdd)message).getIdleTimeout(), ((OFFlowAdd)message).getHardTimeout(), ((OFFlowAdd)message).getPriority(), ((OFFlowAdd)message).getBufferId(), ((OFFlowAdd)message).getOutPort(), ((OFFlowAdd)message).getOutGroup(), ((OFFlowAdd)message).getFlags(), ((OFFlowAdd)message).getMatch(), ((OFFlowAdd)message).getInstructions());
            else if(of_message_type.equals("OFFlowDeleteVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(((OFFlowDelete)message).getXid(), ((OFFlowDelete)message).getCookie(), ((OFFlowDelete)message).getCookieMask(), ((OFFlowDelete)message).getTableId(), ((OFFlowDelete)message).getIdleTimeout(), ((OFFlowDelete)message).getHardTimeout(), ((OFFlowDelete)message).getPriority(), ((OFFlowDelete)message).getBufferId(), ((OFFlowDelete)message).getOutPort(), ((OFFlowDelete)message).getOutGroup(), ((OFFlowDelete)message).getFlags(), ((OFFlowDelete)message).getMatch(), ((OFFlowDelete)message).getInstructions());
            else if(of_message_type.equals("OFFlowDeleteStrictVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(((OFFlowDeleteStrict)message).getXid(), ((OFFlowDeleteStrict)message).getCookie(), ((OFFlowDeleteStrict)message).getCookieMask(), ((OFFlowDeleteStrict)message).getTableId(), ((OFFlowDeleteStrict)message).getIdleTimeout(), ((OFFlowDeleteStrict)message).getHardTimeout(), ((OFFlowDeleteStrict)message).getPriority(), ((OFFlowDeleteStrict)message).getBufferId(), ((OFFlowDeleteStrict)message).getOutPort(), ((OFFlowDeleteStrict)message).getOutGroup(), ((OFFlowDeleteStrict)message).getFlags(), ((OFFlowDeleteStrict)message).getMatch(), ((OFFlowDeleteStrict)message).getInstructions());
            else if(of_message_type.equals("OFFlowModifyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModify(((OFFlowModify)message).getXid(), ((OFFlowModify)message).getCookie(), ((OFFlowModify)message).getCookieMask(), ((OFFlowModify)message).getTableId(), ((OFFlowModify)message).getIdleTimeout(), ((OFFlowModify)message).getHardTimeout(), ((OFFlowModify)message).getPriority(), ((OFFlowModify)message).getBufferId(), ((OFFlowModify)message).getOutPort(), ((OFFlowModify)message).getOutGroup(), ((OFFlowModify)message).getFlags(), ((OFFlowModify)message).getMatch(), ((OFFlowModify)message).getInstructions());
            else if(of_message_type.equals("OFFlowModifyStrictVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(((OFFlowModifyStrict)message).getXid(), ((OFFlowModifyStrict)message).getCookie(), ((OFFlowModifyStrict)message).getCookieMask(), ((OFFlowModifyStrict)message).getTableId(), ((OFFlowModifyStrict)message).getIdleTimeout(), ((OFFlowModifyStrict)message).getHardTimeout(), ((OFFlowModifyStrict)message).getPriority(), ((OFFlowModifyStrict)message).getBufferId(), ((OFFlowModifyStrict)message).getOutPort(), ((OFFlowModifyStrict)message).getOutGroup(), ((OFFlowModifyStrict)message).getFlags(), ((OFFlowModifyStrict)message).getMatch(), ((OFFlowModifyStrict)message).getInstructions());
            else if(of_message_type.equals("OFFlowRemovedVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXFlowRemoved(((OFFlowRemoved)message).getXid(), ((OFFlowRemoved)message).getCookie(), ((OFFlowRemoved)message).getPriority(), ((OFFlowRemoved)message).getReason(), ((OFFlowRemoved)message).getTableId(), ((OFFlowRemoved)message).getDurationSec(), ((OFFlowRemoved)message).getDurationNsec(), ((OFFlowRemoved)message).getIdleTimeout(), ((OFFlowRemoved)message).getHardTimeout(),((OFFlowRemoved)message).getPacketCount(), ((OFFlowRemoved)message).getByteCount(), ((OFFlowRemoved)message).getMatch());
            else if(of_message_type.equals("OFGetConfigReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigReply(((OFGetConfigReply)message).getXid(),((OFGetConfigReply)message).getFlags(), ((OFGetConfigReply)message).getMissSendLen());
            else if(of_message_type.equals("OFGetConfigRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXGetConfigRequest(((OFGetConfigRequest)message).getXid());
            else if(of_message_type.equals("OFHelloVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXHello(((OFHello)message).getXid());
            else if(of_message_type.equals("OFPacketInVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketIn(((OFPacketIn)message).getXid(),((OFPacketIn)message).getBufferId(), ((OFPacketIn)message).getTotalLen(),((OFPacketIn)message).getReason(), ((OFPacketIn)message).getTableId(), ((OFPacketIn)message).getCookie(), ((OFPacketIn)message).getMatch(), ((OFPacketIn)message).getData());
            else if(of_message_type.equals("OFPacketOutVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPacketOut(((OFPacketOut)message).getXid(),((OFPacketOut)message).getBufferId(),((OFPacketOut)message).getInPort(),((OFPacketOut)message).getActions(),((OFPacketOut)message).getData());
            else if(of_message_type.equals("OFPortModVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortMod(((OFPortMod)message).getXid(),((OFPortMod)message).getPortNo(),((OFPortMod)message).getHwAddr(), ((OFPortMod)message).getConfig(), ((OFPortMod)message).getMask(), ((OFPortMod)message).getAdvertise());
            else if(of_message_type.equals("OFPortStatusVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXPortStatus(((OFPortStatus)message).getXid(),((OFPortStatus)message).getReason(), ((OFPortStatus)message).getDesc());
            else if(of_message_type.equals("OFQueueGetConfigReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigReply(((OFQueueGetConfigReply)message).getXid(), ((OFQueueGetConfigReply)message).getPort(), ((OFQueueGetConfigReply)message).getQueues());
            else if(of_message_type.equals("OFQueueGetConfigRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXQueueGetConfigRequest(((OFQueueGetConfigRequest)message).getXid(),((OFQueueGetConfigRequest)message).getPort());
            else if(of_message_type.equals("OFSetConfigVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXSetConfig(((OFSetConfig)message).getXid(), ((OFSetConfig)message).getFlags(), ((OFSetConfig)message).getMissSendLen());
            else if(of_message_type.equals("OFNiciraControllerRoleReplyVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleReply(((OFNiciraControllerRoleReply)message).getXid(),((OFNiciraControllerRoleReply)message).getRole());
            else if(of_message_type.equals("OFNiciraControllerRoleRequestVer13"))
                ovxmessage=OVXFactoryInst.myOVXFactory.buildOVXNiciraControllerRoleRequest(((OFNiciraControllerRoleRequest)message).getXid(),((OFNiciraControllerRoleRequest)message).getRole());
            else if(of_message_type.equals("OFTableFeaturesStatsRequestVer13"))
                ovxmessage = OVXFactoryInst.myOVXFactory.buildOVXTableFeaturesStatsRequest(((OFTableFeaturesStatsRequest)message).getXid(),((OFTableFeaturesStatsRequest) message).getFlags(), ((OFTableFeaturesStatsRequest) message).getEntries());
            else if(of_message_type.equals("OFTableFeaturesStatsReplyVer13"))
                ovxmessage = OVXFactoryInst.myOVXFactory.buildOVXTableFeaturesStatsReply(((OFTableFeaturesStatsReply)message).getXid(),((OFTableFeaturesStatsReply) message).getFlags(), ((OFTableFeaturesStatsReply) message).getEntries());

        }
        System.out.println("of   "+message+"   ovx  "+ovxmessage);
        return ovxmessage;
        
        
    }

}

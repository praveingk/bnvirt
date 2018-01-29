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
/**
 * author: alshabib
 *
 * heavily inspired from floodlight.
 *
 */
package net.onrc.openvirtex.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.exceptions.HandshakeTimeoutException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.SwitchStateException;
import net.onrc.openvirtex.messages.OVXFactory;
import net.onrc.openvirtex.messages.OVXSetConfig;
import net.onrc.openvirtex.messages.ver10.OVXSetConfigVer10;
import net.onrc.openvirtex.packet.OVXLLDP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.util.HexString;

public class SwitchChannelHandler extends OFChannelHandler {

    Logger log = LogManager.getLogger(SwitchChannelHandler.class.getName());
    protected ArrayList<OFPortStatus> pendingPortStatusMsg = null;

    /*
     *
     * The enum below implements the connection state machine. Each method in
     * individual enum elements override previous implementations of each
     * message processor. Each state expects some event and passes to the next
     * state.
     */
    enum ChannelState {
        INIT(false) {

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m)
                    throws IOException {
                // no need to actually implement
                // because it won't happen because nothing
                // is connected to us.

            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                this.unhandledMessageReceived(h, m);
            }

        },
        WAIT_HELLO(false) {

            @Override
            void processOFHello(final SwitchChannelHandler h, final OFHello m)
                    throws IOException {
                h.sendHandShakeMessage(OFType.FEATURES_REQUEST);
                h.setState(WAIT_FEATURES_REPLY);
            }

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m) {
                h.log.error("Error waiting for Hello (type:{}, code:{})",
                        m.getErrType(), m.getData());

                h.channel.disconnect();
            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                this.unhandledMessageReceived(h, m);
            }
        },
        WAIT_FEATURES_REPLY(false) {

            @Override
            void processOFFeaturesReply(final SwitchChannelHandler h,
                    final OFFeaturesReply m) throws IOException {
                h.featuresReply = m;
                h.sendHandshakeSetConfig();
                h.setState(WAIT_CONFIG_REPLY);
            }

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m)
                    throws IOException {
                h.log.error(
                        "Error waiting for config reply (type:{}, code:{})",
                        m.getErrType(), m.getData());
                h.channel.disconnect();
            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                this.unhandledMessageReceived(h, m);
            }
        },
        WAIT_CONFIG_REPLY(false) {

            @Override
            void processOFGetConfigReply(final SwitchChannelHandler h,
                    final OFGetConfigReply m) throws IOException {
                if (m.getMissSendLen() != (short) 0xffff) {
                    h.log.error(
                            "Miss send length was not set properly by switch {}",
                            h.featuresReply.getDatapathId());
                }
                if (OVXFactoryInst.ofversion == 10) {
                    h.sendHandshakeDescriptionStatsRequest();
                    h.setState(WAIT_DESCRIPTION_STAT_REPLY);
                } else if (OVXFactoryInst.ofversion == 13) {
                    h.sendHandshakePortDescRequest();
                    h.setState(WAIT_PORT_STAT_REPLY);
                }

            }

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m) {
                if (m.getType() != OFType.BARRIER_REQUEST) {
				    h.log.error(
				            "Error waiting for features (type:{}, code:{})",
				            m.getErrType(), m.getData());
				    if (h.channel.isOpen()) {
				        h.channel.close();
				    }
				} else {
				    h.log.warn(
				            "Barrier Request message not understood by switch {}; "
				                    + "if it's an HP switch you are probably ok.",
				                    HexString.toHexString(h.featuresReply
				                            .getDatapathId().getBytes()));
				}
            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                h.pendingPortStatusMsg.add(m);

            }
        },
        WAIT_PORT_STAT_REPLY(false) {

            @Override
            void processOFStatsReply(final SwitchChannelHandler h,
                                     final OFStatsReply m) throws SwitchMappingException {
                h.portDescStatsReply = (OFPortDescStatsReply)m;
                h.sendHandshakeDescriptionStatsRequest();
                //h.sendHandshakeTableDescStatsRequest();
                h.setState(WAIT_DESCRIPTION_STAT_REPLY);
            }
            @Override
            void processOFErrorMsg(SwitchChannelHandler h, OFErrorMsg m) throws IOException {
                h.log.error(
                        "Error waiting for desc stats reply (type:{}, code:{})",
                        m.getErrType(), m.getData());
                h.channel.disconnect();
            }

            @Override
            void processOFPortStatus(SwitchChannelHandler h, OFPortStatus m) throws IOException {
                h.pendingPortStatusMsg.add(m);

            }
        },
        WAIT_TABLE_STAT_REPLY(false) {

            @Override
            void processOFStatsReply(final SwitchChannelHandler h,
                                     final OFStatsReply m) throws SwitchMappingException {
                h.tableDescStatsReply = (OFTableFeaturesStatsReply)m;
                h.sendHandshakeDescriptionStatsRequest();
                h.setState(WAIT_DESCRIPTION_STAT_REPLY);
            }
            @Override
            void processOFErrorMsg(SwitchChannelHandler h, OFErrorMsg m) throws IOException {
                h.log.error(
                        "Error waiting for desc stats reply (type:{}, code:{})",
                        m.getErrType(), m.getData());
                h.channel.disconnect();
            }

            @Override
            void processOFPortStatus(SwitchChannelHandler h, OFPortStatus m) throws IOException {
                h.pendingPortStatusMsg.add(m);

            }
        },
        WAIT_DESCRIPTION_STAT_REPLY(false) {

            @Override
            void processOFStatsReply(final SwitchChannelHandler h,
                    final OFStatsReply m) throws SwitchMappingException {
                // Read description, if it has been updated
                OFFlowDelete fm = null;
                OFGroupDelete gd = null;
                if (OVXFactoryInst.ofversion == 10) {
                    fm = OVXFactoryInst.myFactory.buildFlowDelete()
                            .setMatch(OVXFactoryInst.myFactory.buildMatchV1().build())
                            .setMatch(OVXFactoryInst.myFactory.buildMatchV1().build())
                            .build();
                    h.channel.write(Collections.singletonList(fm));
                } else if (OVXFactoryInst.ofversion == 13) {
                    h.sendHandshakeRoleRequest();
                    fm = OVXFactoryInst.myFactory.buildFlowDelete()
                            .setMatch(OVXFactoryInst.myFactory.buildMatchV3().build())
                            .setMatch(OVXFactoryInst.myFactory.buildMatchV3().build())
                            .build();
                    h.channel.write(Collections.singletonList(fm));
                    gd = OVXFactoryInst.myFactory.buildGroupDelete().setGroup(OFGroup.ALL).setGroupType(OFGroupType.ALL).setBuckets(ImmutableList.<OFBucket>of()).build();
                    h.channel.write(Collections.singletonList(gd));
                }

          

                h.sw = new PhysicalSwitch(h.featuresReply.getDatapathId().getLong());
                // set switch information
                // set features reply and channel first so we have a DPID and
                // channel info.
                h.sw.setFeaturesReply(h.featuresReply);
                h.sw.setDescriptionStats((OFDescStatsReply)m);
                h.sw.setPortDescStatsReply(h.portDescStatsReply);
                h.sw.setConnected(true);
                h.sw.setChannel(h.channel);
                h.sw.setOfversion(OVXFactoryInst.ofversion);

                for (final OFPortStatus ps : h.pendingPortStatusMsg) {
                    this.handlePortStatusMessage(h, ps);
                }
                h.pendingPortStatusMsg.clear();
                h.sw.boot();
                h.setState(ACTIVE);
            }

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m)
                    throws IOException {
                h.log.error(
                        "Error waiting for desc stats reply (type:{}, code:{})",
                        m.getErrType(), m.getData());
                h.channel.disconnect();

            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                h.pendingPortStatusMsg.add(m);

            }
        },
        ACTIVE(true) {

            @Override
            void processOFMessage(final SwitchChannelHandler h,
                    final OFMessage m) throws IOException {

                System.out.println("Processing OF Message :"+ m.toString());

                switch (m.getType()) {
                case ECHO_REQUEST:
                    this.processOFEchoRequest(h, (OFEchoRequest) m);
                    break;
                case BARRIER_REPLY:
                case ECHO_REPLY:
                    // do nothing but thank the switch
                    break;
                case HELLO:
                    h.sendHandShakeMessage(OFType.FEATURES_REQUEST);
                    break;
                case FEATURES_REPLY:
                    h.featuresReply = (OFFeaturesReply) m;
                    h.sw.setFeaturesReply(h.featuresReply);
                    break;
                case ERROR:
                case FLOW_REMOVED:
                case GET_CONFIG_REPLY:
                case PACKET_IN:
                case PORT_STATUS:
                case QUEUE_GET_CONFIG_REPLY:
                case STATS_REPLY:
                case EXPERIMENTER:
                    try {
						h.sw.handleIO(m, h.channel);
					} catch (SwitchMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                    // The following messages are sent to switches. The controller
                    // should never receive them
                case SET_CONFIG:
                case GET_CONFIG_REQUEST:
                case PACKET_OUT:
                case PORT_MOD:
                case QUEUE_GET_CONFIG_REQUEST:
                case BARRIER_REQUEST:
                case STATS_REQUEST:
                case FEATURES_REQUEST:
                case FLOW_MOD:
                    this.illegalMessageReceived(h, m);
                    break;
                default:
                    break;
                }

            }

            @Override
            void processOFErrorMsg(final SwitchChannelHandler h, final OFErrorMsg m)
                    throws IOException {
                // should never happen

            }

            @Override
            void processOFPortStatus(final SwitchChannelHandler h,
                    final OFPortStatus m) throws IOException {
                // should never happen

            }

        };

        private boolean handshakeComplete = false;

        ChannelState(final boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

        public boolean isHandShakeComplete() {
            return this.handshakeComplete;
        }

        /**
         * Get a string specifying the switch connection, state, and message
         * received. To be used as message for SwitchStateException or log
         * messages
         *
         * @param h
         *            The channel handler (to get switch information_
         * @param m
         *            The OFMessage that has just been received
         * @param details
         *            A string giving more details about the exact nature of the
         *            problem.
         * @return
         */
        // needs to be protected because enum members are actually subclasses
        protected String getSwitchStateMessage(final SwitchChannelHandler h,
                final OFMessage m, final String details) {
            return String.format("Switch: [%s], State: [%s], received: [%s]"
                    + ", details: %s", h.getSwitchInfoString(),
                    this.toString(), m.getType().toString(), details);
        }



        /**
         * We have an OFMessage we didn't expect given the current state and we
         * want to treat this as an error. We currently throw an exception that
         * will terminate the connection However, we could be more forgiving
         *
         * @param h
         *            the channel handler that received the message
         * @param m
         *            the message
         * @throws SwitchStateExeption
         *             we always through the execption
         */
        // needs to be protected because enum members are acutally subclasses
        protected void illegalMessageReceived(final SwitchChannelHandler h,
                final OFMessage m) {
            final String msg = this
                    .getSwitchStateMessage(h, m,
                            "Switch should never send this message in the current state");
            throw new SwitchStateException(msg);

        }

        /**
         * Handles an OFMessage we didn't expect given the current state, by
         * ignoring the message.
         *
         * @param h
         *            the channel handler the received the message
         * @param m
         *            the message
         */
        protected void unhandledMessageReceived(final SwitchChannelHandler h,
                final OFMessage m) {
            h.log.warn(this.getSwitchStateMessage(h, m,
                    "Received unhandled message; moving swiftly along..."));
        }

        /**
         * Handle a port status message.
         *
         * Handle a port status message by updating the port maps in a switch
         * instance and notifying Controller about the change so it can dispatch
         * a switch update.
         *
         * @param h
         *            The OFChannelHhandler that received the message
         * @param m
         *            The PortStatus message we received
         */
        protected void handlePortStatusMessage(final SwitchChannelHandler h,
                final OFPortStatus m) {
            if (h.sw == null) {
                final String msg = this.getSwitchStateMessage(h, m,
                        "State machine error: switch is null. Should never "
                                + "happen");
                throw new SwitchStateException(msg);
            }
            try {
				h.sw.handleIO(m, h.channel);
			} catch (SwitchMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        /**
         * Process an OF message received on the channel and update state
         * accordingly.
         *
         * The main "event" of the state machine. Process the received message,
         * send follow up message if required and update state if required.
         *
         * Switches on the message type and calls more specific event handlers
         * for each individual OF message type. If we receive a message that is
         * supposed to be sent from a controller to a switch we throw a
         * SwitchStateExeption.
         *
         * The more specific handlers can also throw SwitchStateExceptions
         *
         * @param h
         *            The SwitchChannelHandler that received the message
         * @param m
         *            The message we received.
         * @throws SwitchStateException
         * @throws IOException
         * @throws SwitchMappingException 
         */
        void processOFMessage(final SwitchChannelHandler h, final OFMessage m)
                throws IOException, SwitchMappingException {
            switch (m.getType()) {
            case HELLO:
                this.processOFHello(h, (OFHello) m);
                break;
            case BARRIER_REPLY:
                this.processOFBarrierReply(h, (OFBarrierReply) m);
                break;
            case ECHO_REPLY:
                this.processOFEchoReply(h, (OFEchoReply) m);
                break;
            case ECHO_REQUEST:
                this.processOFEchoRequest(h, (OFEchoRequest) m);
                break;
            case ERROR:
                this.processOFErrorMsg(h, (OFErrorMsg) m);
                break;
            case FEATURES_REPLY:
                this.processOFFeaturesReply(h, (OFFeaturesReply) m);
                break;
            case FLOW_REMOVED:
                this.processOFFlowRemoved(h, (OFFlowRemoved) m);
                break;
            case GET_CONFIG_REPLY:
                this.processOFGetConfigReply(h, (OFGetConfigReply) m);
                break;
            case PACKET_IN:
                this.processOFPacketIn(h, (OFPacketIn) m);
                break;
            case PORT_STATUS:
                this.processOFPortStatus(h, (OFPortStatus) m);
                break;
            case QUEUE_GET_CONFIG_REPLY:
                this.processOFQueueGetConfigReply(h, (OFQueueGetConfigReply) m);
                break;
            case STATS_REPLY:
                this.processOFStatsReply(h, (OFStatsReply) m);
                break;
            case EXPERIMENTER:
                this.processOFExperimenter(h, (OFExperimenter) m);
                break;
            case ROLE_REPLY:
                this.procesOFRoleReply(h, (OFRoleReply) m);
                break;
                // The following messages are sent to switches. The controller
                // should never receive them
            case SET_CONFIG:
            case GET_CONFIG_REQUEST:
            case PACKET_OUT:
            case PORT_MOD:
            case QUEUE_GET_CONFIG_REQUEST:
            case BARRIER_REQUEST:
            case STATS_REQUEST:
            case FEATURES_REQUEST:
            case FLOW_MOD:
                this.illegalMessageReceived(h, m);
                break;
            default:
                break;
            }
        }

        void procesOFRoleReply(SwitchChannelHandler h, OFRoleReply m) {
            //Do Nothing
        }

        /**
         * Default implementation for message handlers in any state.
         *
         * Individual states must override these if they want a behavior
         * that differs from the default.
         *
         * In general, these handlers simply ignore the message and do
         * nothing.
         *
         * There are some exceptions though, since some messages really
         * are handled the same way in every state (e.g., ECHO_REQUST) or
         * that are only valid in a single state (e.g., HELLO, GET_CONFIG_REPLY
         *
         * @param h the switch channel handler
         * @param m the OpenFlow hello message
         * @throws IOException
         **/
        void processOFHello(final SwitchChannelHandler h, final OFHello m)
                throws IOException {
            // we only expect hello in the WAIT_HELLO state
            this.illegalMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow barrier reply message.
         * UNIMPLEMENTED
         *
         * @param h the switch channel handler
         * @param m the the barrier reply message
         * @throws IOException TODO
         */
        void processOFBarrierReply(final SwitchChannelHandler h,
                final OFBarrierReply m) throws IOException {
            // Silently ignore.
            return;
        }

        /**
         * Processes OpenFlow echo request message.
         *
         * @param h the switch channel handler
         * @param m the echo request message
         * @throws IOException TODO
         */
        void processOFEchoRequest(final SwitchChannelHandler h,
                final OFEchoRequest m) throws IOException {
            final OFEchoReply reply = OVXFactoryInst.myFactory.buildEchoReply()
            		.setXid(m.getXid())
            		.setData(m.getData())
            		.build();
            h.channel.write(Collections.singletonList(reply));
        }

        /**
         * Processes OpenFlow echo reply.
         *
         * @param h the switch channel handler
         * @param m the echo reply message
         * @throws IOException TODO
         */
        void processOFEchoReply(final SwitchChannelHandler h,
                final OFEchoReply m) throws IOException {
            // Do nothing with EchoReplies !!
        }

        /**
         * Processes OpenFlow error message. We don't have
         * a default implementation for OFErrorMsg, every state
         * must override it.
         *
         * @param h the switch channel handler
         * @param m the error message
         * @throws IOException TODO
         */
        abstract void processOFErrorMsg(SwitchChannelHandler h, OFErrorMsg m)
                throws IOException;

        /**
         * Processes OpenFlow features reply message.
         *
         * @param h the switch channel handler
         * @param m the features reply message
         * @throws IOException TODO
         */
        void processOFFeaturesReply(final SwitchChannelHandler h,
                final OFFeaturesReply m) throws IOException {
            this.unhandledMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow flow removed message.
         *
         * @param h the switch channel handler
         * @param m the flow removed message
         * @throws IOException TODO
         */
        void processOFFlowRemoved(final SwitchChannelHandler h,
                final OFFlowRemoved m) throws IOException {
            this.unhandledMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow config reply message.
         *
         * @param h the switch channel handler
         * @param m the config reply message
         * @throws IOException TODO
         */
        void processOFGetConfigReply(final SwitchChannelHandler h,
                final OFGetConfigReply m) throws IOException {

            this.illegalMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow packet in message.
         *
         * @param h the switch channel handler
         * @param m the packet in message
         * @throws IOException TODO
         */
        void processOFPacketIn(final SwitchChannelHandler h, final OFPacketIn m)
                throws IOException {
            this.unhandledMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow port status message.
         * No default implementation, every state needs to handle it.
         *
         * @param h the switch channel handler
         * @param m the port status message
         * @throws IOException TODO
         */
        abstract void processOFPortStatus(SwitchChannelHandler h, OFPortStatus m)
                throws IOException;

        /**
         * Processes OpenFlow queue config reply message.
         *
         * @param h the switch channel handler
         * @param m the queue config reply message
         * @throws IOException TODO
         */
        void processOFQueueGetConfigReply(final SwitchChannelHandler h,
                final OFQueueGetConfigReply m) throws IOException {
            this.unhandledMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow statistics reply message.
         *
         * @param h the switch channel handler
         * @param m the statistics reply message
         * @throws IOException TODO
         * @throws SwitchMappingException 
         */
        void processOFStatsReply(final SwitchChannelHandler h,
                final OFStatsReply m) throws IOException, SwitchMappingException {
            this.unhandledMessageReceived(h, m);
        }

        /**
         * Processes OpenFlow vendor message.
         *
         * @param h the switch channel handler
         * @param m the vendor message
         * @throws IOException TODO
         */
        void processOFExperimenter(final SwitchChannelHandler h, final OFExperimenter m)
                throws IOException {
            this.unhandledMessageReceived(h, m);
        }
    }

    private ChannelState state;
    private OFFeaturesReply featuresReply;
    private OFPortDescStatsReply portDescStatsReply;
    private OFTableFeaturesStatsReply tableDescStatsReply;

    /*
     * Transaction ids to use during initialization
     */
    private int handshakeTransactionIds = -1;

    public SwitchChannelHandler(final OpenVirteXController ctrl) {
        this.ctrl = ctrl;
        this.state = ChannelState.INIT;
        this.pendingPortStatusMsg = new ArrayList<OFPortStatus>();
    }

    @Override
    public boolean isHandShakeComplete() {
        return this.state.isHandShakeComplete();
    }

    /**
     * Return. a string describing this switch based on the already available
     * information (DPID and/or remote socket).
     *
     * @return
     */
    @Override
    protected String getSwitchInfoString() {
        if (this.sw != null) {
            return this.sw.toString();
        }
        String channelString;
        if (this.channel == null || this.channel.getRemoteAddress() == null) {
            channelString = "?";
        } else {
            channelString = this.channel.getRemoteAddress().toString();
        }
        String dpidString;
        if (this.featuresReply == null) {
            dpidString = "?";
        } else {
            dpidString = HexString.toHexString(this.featuresReply
                    .getDatapathId().getBytes());
        }
        return String.format("DPID -> %s(%s)", dpidString, channelString);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx,
            final ChannelStateEvent e) throws Exception {

        this.channel = e.getChannel();
        this.sendHandShakeMessage(OFType.HELLO);
        this.setState(ChannelState.WAIT_HELLO);
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx,
            final ChannelStateEvent e) throws Exception {

        if (this.sw != null) {
            this.sw.setConnected(false);
            this.sw.unregister();
        }

    }

    /**
     * Send a message to the switch using the handshake transactions ids.
     *
     * @param type the type
     * @throws IOException
     */

    @Override
    protected OFMessage sendHandShakeMessage(final OFType type) throws IOException {
       
        OFMessage m = null;
   	 
        if(type == OFType.HELLO)
        {
        	m=(OFMessage) OVXFactoryInst.myFactory.buildHello().setXid(this.handshakeTransactionIds--).build();
        	this.channel.write(Collections.singletonList(m));
        }
        else if(type == OFType.FEATURES_REQUEST)
        {
        	m=(OFMessage) OVXFactoryInst.myFactory.buildFeaturesRequest().setXid(this.handshakeTransactionIds--).build();
        	this.channel.write(Collections.singletonList(m));
        }
        
        return m;
    }

    /**
     * Sends the configuration requests to tell the switch we want full packets.
     *
     * @throws IOException
     */
    private void sendHandshakeSetConfig() throws IOException {
        final List<OFMessage> msglist = new ArrayList<OFMessage>(3);

        // Ensure we receive the full packet via PacketIn

        final OFSetConfig configSet = OVXFactoryInst.myFactory.buildSetConfig()
        		.setMissSendLen(OVXSetConfigVer10.MSL_FULL)
                .setXid(this.handshakeTransactionIds--)
                .build();
        msglist.add(configSet);

        // Barrier
        final OFBarrierRequest barrier = OVXFactoryInst.myFactory.buildBarrierRequest()
        		.setXid(this.handshakeTransactionIds--)
        		.build();
        msglist.add(barrier);

        // Verify (need barrier?)
        final OFGetConfigRequest configReq = OVXFactoryInst.myFactory.buildGetConfigRequest()
        		.setXid(this.handshakeTransactionIds--)
        		.build();
        msglist.add(configReq);
        this.channel.write(msglist);
    }

    protected void sendHandshakePortDescRequest() {
        final OFPortDescStatsRequest req = OVXFactoryInst.myFactory.buildPortDescStatsRequest()
                .setXid(this.handshakeTransactionIds--)
                .build();

        this.channel.write(Collections.singletonList(req));

    }

    protected void sendHandshakeDescriptionStatsRequest() {
        final OFDescStatsRequest req = OVXFactoryInst.myFactory.buildDescStatsRequest()
        		.setXid(this.handshakeTransactionIds--)
        		.build();

        this.channel.write(Collections.singletonList(req));

    }

    protected void sendHandshakeTableDescStatsRequest() {
        final OFTableFeaturesStatsRequest req = OVXFactoryInst.myFactory.buildTableFeaturesStatsRequest()
                .setXid(this.handshakeTransactionIds--)
                .setFlags(ImmutableSet.<OFStatsRequestFlags>of())
                .setEntries(ImmutableList.<OFTableFeatures>of())
                .build();

        this.channel.write(Collections.singletonList(req));
    }

    protected void sendHandshakeRoleRequest() {
        final OFRoleRequest req = OVXFactoryInst.myFactory.buildRoleRequest()
                .setXid(this.handshakeTransactionIds--)
                .setRole(OFControllerRole.ROLE_MASTER)
                .setGenerationId(U64.ZERO)
                .build();

        this.channel.write(Collections.singletonList(req));

    }
    @Override
    public void channelIdle(final ChannelHandlerContext ctx,
            final IdleStateEvent e) throws Exception {
        final OFMessage m = OVXFactoryInst.myFactory.buildEchoRequest().build();
        e.getChannel().write(Collections.singletonList(m));
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx,
            final MessageEvent e) throws Exception {

        /*
         * Pass all messages to the handlers, except LLDP which we send straight
         * to the topology controller.
         *
         * This should be implemented with a token bucket in order to rate limit
         * the connections a little.
         */
        if (e.getMessage() instanceof OFMessage) {
            //@SuppressWarnings("unchecked")
           // final List<OFMessage> msglist = (List<OFMessage>) e.getMessage();

           // for (final OFMessage ofm : msglist) {
            //System.out.println("***Ging to parse message");
        	OFMessage ofm=(OFMessage) e.getMessage();
            //System.out.println("Inside Message received :"+ofm.toString() );
        	
//                try {

                    switch (ofm.getType()) {
                    case PACKET_IN:
                        /*
                         * Is this packet a packet in? If yes is it an lldp?
                         * then send it to the PhysicalNetwork.
                         */
                        final byte[] data = ((OFPacketIn) ofm).getData();
                        if (OVXLLDP.isLLDP(data)) {
                            if (this.sw != null) {
                                PhysicalNetwork.getInstance().handleLLDP(ofm,
                                        this.sw);
                            } else {
                                this.log.warn("Switch has not connected yet; dropping LLDP for now");
                            }
                            break;
                        }
                    default:
                        // Process all non-packet-ins
                        this.state.processOFMessage(this, ofm);
                        break;
                    }

//                } catch (final Exception ex) {
//                    // We are the last handler in the stream, so run the
//                    // exception through the channel again by passing in
//                    // ctx.getChannel().
//                    Channels.fireExceptionCaught(ctx.getChannel(), ex);
//                }
            

        } else {
            Channels.fireExceptionCaught(this.channel, new AssertionError(
                    "Message received from Channel is not a list"));
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
            final ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ReadTimeoutException) {
            // switch timeout
            this.log.error("Disconnecting switch {} due to read timeout ",
                    this.getSwitchInfoString());

            ctx.getChannel().close();
        } else if (e.getCause() instanceof HandshakeTimeoutException) {
            this.log.error(
                    "Disconnecting switch {} failed to complete handshake ",
                    this.getSwitchInfoString());

            ctx.getChannel().close();
        } else if (e.getCause() instanceof ClosedChannelException) {
            this.log.error(
                    "Channel for sw {} already closed; switch needs to reconnect",
                    this.getSwitchInfoString());

        } else if (e.getCause() instanceof IOException) {
            this.log.error("Disconnecting switch {} due to IO Error.",
                    this.getSwitchInfoString());

            ctx.getChannel().close();
        } else if (e.getCause() instanceof SwitchStateException) {
            this.log.error("Disconnecting switch {} due to switch state error",
                    this.getSwitchInfoString());

            ctx.getChannel().close();
        } else if (e.getCause() instanceof OFParseError) {
            this.log.error(
                    "Disconnecting switch {} due to message parse failure",
                    this.getSwitchInfoString());
            e.getCause().printStackTrace();
            ctx.getChannel().close();
        } else if (e.getCause() instanceof RejectedExecutionException) {
            this.log.error("Could not process message: queue full",
                    e.getCause());

        } else {

            this.log.error(
                    "Error while processing message from switch {} state {}",
                    this.getSwitchInfoString(), this.state, e.getCause());

            ctx.getChannel().close();
            executeCommand("sudo killall java");
            throw new RuntimeException(e.getCause());
        }
        this.log.debug(e.getCause());
    }
    private String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
    /*
     * Set the state for this channel
     */
    private void setState(final ChannelState state) {
        this.state = state;
    }

}

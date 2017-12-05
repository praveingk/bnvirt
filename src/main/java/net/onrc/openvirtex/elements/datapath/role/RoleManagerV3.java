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
package net.onrc.openvirtex.elements.datapath.role;

import net.onrc.openvirtex.exceptions.UnknownRoleException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.OFControllerRole;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.ver10.OFNiciraControllerRoleSerializerVer10;
import org.projectfloodlight.openflow.types.U64;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class RoleManagerV3 {

    private static Logger log = LogManager.getLogger(RoleManagerV3.class
            .getName());
    private HashMap<Channel, OFControllerRole> state;
    private final AtomicReference<HashMap<Channel, OFControllerRole>> currentState;
    private Channel currentMaster;
    private U64 generationId;

    public RoleManagerV3() {
        this.state = new HashMap<Channel, OFControllerRole>();
        this.currentState = new AtomicReference<HashMap<Channel, OFControllerRole>>(state);
        this.generationId = U64.of(0);
    }

    private HashMap<Channel, OFControllerRole> getState() {
        return new HashMap<>(this.currentState.get());
    }

    private void setState() {
        this.currentState.set(this.state);
    }

    public synchronized void addController(Channel chan) {
        if (chan == null) {
            return;
        }
        this.state = getState();
        this.state.put(chan, OFControllerRole.ROLE_EQUAL);
        setState();
    }

    public synchronized void setRole(Channel channel, OFControllerRole role)
            throws IllegalArgumentException, UnknownRoleException {
        if (!this.currentState.get().containsKey(channel)) {
            throw new IllegalArgumentException("Unknown controller "
                    + channel.getRemoteAddress());
        }
        this.state = getState();
        log.info("Setting controller {} to role {}",
                channel.getRemoteAddress(), role);
        if (role == OFControllerRole.ROLE_MASTER) {
            if (channel == currentMaster) {
                this.state.put(channel, OFControllerRole.ROLE_MASTER);
            } else {
                generationId = U64.of(generationId.getValue() + 1);
            }
            this.state.put(currentMaster, OFControllerRole.ROLE_SLAVE);
            this.state.put(channel, OFControllerRole.ROLE_MASTER);
            this.currentMaster = channel;
        } else if (role == OFControllerRole.ROLE_SLAVE) {
            if (channel == currentMaster) {
                this.state.put(channel, OFControllerRole.ROLE_SLAVE);
                currentMaster = null;
            }
            this.state.put(channel, OFControllerRole.ROLE_SLAVE);
        } else if (role == OFControllerRole.ROLE_EQUAL) {
            if (channel == currentMaster) {
                this.state.put(channel, OFControllerRole.ROLE_EQUAL);
                this.currentMaster = null;
            }
            this.state.put(channel, OFControllerRole.ROLE_EQUAL);
        } else {
            throw new UnknownRoleException("Unknown role : " + role);
        }
        System.out.println("Size of role map = "+ this.state.size() + " , "+ this.state.toString());
        setState();

    }

    public boolean canSend(Channel channel, OFMessage m) {
        OFControllerRole r = this.currentState.get().get(channel);
        //System.out.println("canSend? Role="+ r);
        if (r == OFControllerRole.ROLE_MASTER || r == OFControllerRole.ROLE_EQUAL) {
            return true;
        }
        OFType type=m.getType();
        switch (type) {
        case GET_CONFIG_REQUEST:
        case QUEUE_GET_CONFIG_REQUEST:
        case PORT_STATUS:
        case STATS_REQUEST:
            return true;
        default:
            return false;
        }
    }

    public boolean canReceive(Channel channel, OFMessage m) {
        OFControllerRole r = this.currentState.get().get(channel);
        //System.out.println("canRecevive? Role="+ r);

        if (r == OFControllerRole.ROLE_MASTER || r == OFControllerRole.ROLE_EQUAL) {
            return true;
        }
        switch (m.getType()) {
        case GET_CONFIG_REPLY:
        case QUEUE_GET_CONFIG_REPLY:
        case PORT_STATUS:
        case STATS_REPLY:
            return true;
        default:
            return false;
        }
    }

    public OFControllerRole getRole(Channel channel) {
        return this.currentState.get().get(channel);
    }

    public U64 getGenerationId() {
        return this.generationId;
    }

    private void checkAndSend(Channel c, OFMessage m) {
        if (canReceive(c, m)) {
            if (c != null && c.isOpen()) {
                c.write(Collections.singletonList(m));
            }
        }

    }

    public void sendMsg(OFMessage msg, Channel c) {
        if (c != null) {
            checkAndSend(c, msg);
        } else {
            final Map<Channel, OFControllerRole> readOnly = Collections
                    .unmodifiableMap(this.currentState.get());
            for (Channel chan : readOnly.keySet()) {
                if (chan == null) {
                    continue;
                }
                checkAndSend(chan, msg);
            }
        }
    }

    public synchronized void removeChannel(Channel channel) {
        this.state = getState();
        this.state.remove(channel);
        setState();
    }

    public synchronized void shutDown() {
        this.state = getState();
        for (Channel c : state.keySet()) {
            if (c != null && c.isConnected()) {
                c.close();
            }
        }
        state.clear();
        setState();
    }

    @Override
    public String toString() {
        return this.currentState.get().toString();
    }
}

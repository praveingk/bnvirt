package net.onrc.openvirtex.messages.actions;

import org.projectfloodlight.openflow.protocol.action.OFActionPopVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionPushVlan;

/**
 * Created by pravein on 22/11/17.
 */
public interface OVXActionPushVlan extends OFActionPushVlan, VirtualizableActionV3{
}

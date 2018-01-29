package net.onrc.openvirtex.messages.actions;
import net.onrc.openvirtex.messages.Virtualizable;

import org.projectfloodlight.openflow.protocol.action.OFActionStripVlan;
public interface OVXActionStripVirtualLan extends
OFActionStripVlan, VirtualizableAction {

}
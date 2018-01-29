package net.onrc.openvirtex.messages.statistics;

import net.onrc.openvirtex.messages.Devirtualizable;

import org.projectfloodlight.openflow.protocol.OFExperimenterStatsReply;
import org.projectfloodlight.openflow.protocol.OFExperimenterStatsRequest;

public interface OVXExperimenterStatsRequest extends OFExperimenterStatsRequest<OFExperimenterStatsReply>,Devirtualizable{

}

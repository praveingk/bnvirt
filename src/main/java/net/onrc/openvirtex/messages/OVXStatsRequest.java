
package net.onrc.openvirtex.messages;


import org.projectfloodlight.openflow.protocol.OFStatsRequest;

public interface OVXStatsRequest<T extends OVXStatsReply> extends OFStatsRequest<OVXStatsReply>, Devirtualizable {


}

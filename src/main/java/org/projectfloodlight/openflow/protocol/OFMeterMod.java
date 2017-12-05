// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_interface.java
// Do not modify

package org.projectfloodlight.openflow.protocol;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.actionid.*;
import org.projectfloodlight.openflow.protocol.bsntlv.*;
import org.projectfloodlight.openflow.protocol.errormsg.*;
import org.projectfloodlight.openflow.protocol.meterband.*;
import org.projectfloodlight.openflow.protocol.instruction.*;
import org.projectfloodlight.openflow.protocol.instructionid.*;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.queueprop.*;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.util.*;
import org.projectfloodlight.openflow.exceptions.*;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;

public interface OFMeterMod extends OFObject, OFMessage {
    OFVersion getVersion();
    OFType getType();
    long getXid();
    int getCommand();
    int getFlags();
    long getMeterId();
    List<OFMeterBand> getMeters() throws UnsupportedOperationException;
    List<OFMeterBand> getBands() throws UnsupportedOperationException;


    void writeTo(ChannelBuffer channelBuffer);

    Builder createBuilder();
    public interface Builder extends OFMessage.Builder {
        OFMeterMod build();
        OFVersion getVersion();
        OFType getType();
        long getXid();
        Builder setXid(long xid);
        int getCommand();
        Builder setCommand(int command);
        int getFlags();
        Builder setFlags(int flags);
        long getMeterId();
        Builder setMeterId(long meterId);
        List<OFMeterBand> getMeters() throws UnsupportedOperationException;
        Builder setMeters(List<OFMeterBand> meters) throws UnsupportedOperationException;
        List<OFMeterBand> getBands() throws UnsupportedOperationException;
        Builder setBands(List<OFMeterBand> bands) throws UnsupportedOperationException;
    }
}

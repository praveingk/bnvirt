// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver10;

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
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;

public class OFStatsTypeSerializerVer10 {

    public final static short DESC_VAL = (short) 0x0;
    public final static short FLOW_VAL = (short) 0x1;
    public final static short AGGREGATE_VAL = (short) 0x2;
    public final static short TABLE_VAL = (short) 0x3;
    public final static short PORT_VAL = (short) 0x4;
    public final static short QUEUE_VAL = (short) 0x5;
    public final static short EXPERIMENTER_VAL = (short) 0xffff;

    public static OFStatsType readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OFStatsType e) {
        bb.writeShort(toWireValue(e));
    }

    public static void putTo(OFStatsType e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OFStatsType ofWireValue(short val) {
        switch(val) {
            case DESC_VAL:
                return OFStatsType.DESC;
            case FLOW_VAL:
                return OFStatsType.FLOW;
            case AGGREGATE_VAL:
                return OFStatsType.AGGREGATE;
            case TABLE_VAL:
                return OFStatsType.TABLE;
            case PORT_VAL:
                return OFStatsType.PORT;
            case QUEUE_VAL:
                return OFStatsType.QUEUE;
            case EXPERIMENTER_VAL:
                return OFStatsType.EXPERIMENTER;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFStatsType in version 1.0: " + val);
        }
    }


    public static short toWireValue(OFStatsType e) {
        switch(e) {
            case DESC:
                return DESC_VAL;
            case FLOW:
                return FLOW_VAL;
            case AGGREGATE:
                return AGGREGATE_VAL;
            case TABLE:
                return TABLE_VAL;
            case PORT:
                return PORT_VAL;
            case QUEUE:
                return QUEUE_VAL;
            case EXPERIMENTER:
                return EXPERIMENTER_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFStatsType in version 1.0: " + e);
        }
    }

}

// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver13;

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
import org.projectfloodlight.openflow.protocol.OFOchSignalType;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;

public class OFOchSignalTypeSerializerVer13 {

    public final static short FIX_GRID_VAL = (short) 0x1;
    public final static short FLEX_GRID_VAL = (short) 0x2;

    public static OFOchSignalType readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(U8.f(bb.readByte()));
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OFOchSignalType e) {
        bb.writeByte(U8.t(toWireValue(e)));
    }

    public static void putTo(OFOchSignalType e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OFOchSignalType ofWireValue(short val) {
        switch(val) {
            case FIX_GRID_VAL:
                return OFOchSignalType.FIX_GRID;
            case FLEX_GRID_VAL:
                return OFOchSignalType.FLEX_GRID;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFOchSignalType in version 1.3: " + val);
        }
    }


    public static short toWireValue(OFOchSignalType e) {
        switch(e) {
            case FIX_GRID:
                return FIX_GRID_VAL;
            case FLEX_GRID:
                return FLEX_GRID_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFOchSignalType in version 1.3: " + e);
        }
    }

}

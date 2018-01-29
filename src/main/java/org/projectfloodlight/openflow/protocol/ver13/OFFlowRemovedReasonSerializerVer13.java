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
import org.projectfloodlight.openflow.protocol.OFFlowRemovedReason;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;

public class OFFlowRemovedReasonSerializerVer13 {

    public final static byte IDLE_TIMEOUT_VAL = (byte) 0x0;
    public final static byte HARD_TIMEOUT_VAL = (byte) 0x1;
    public final static byte DELETE_VAL = (byte) 0x2;
    public final static byte GROUP_DELETE_VAL = (byte) 0x3;

    public static OFFlowRemovedReason readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OFFlowRemovedReason e) {
        bb.writeByte(toWireValue(e));
    }

    public static void putTo(OFFlowRemovedReason e, PrimitiveSink sink) {
        sink.putByte(toWireValue(e));
    }

    public static OFFlowRemovedReason ofWireValue(byte val) {
        switch(val) {
            case IDLE_TIMEOUT_VAL:
                return OFFlowRemovedReason.IDLE_TIMEOUT;
            case HARD_TIMEOUT_VAL:
                return OFFlowRemovedReason.HARD_TIMEOUT;
            case DELETE_VAL:
                return OFFlowRemovedReason.DELETE;
            case GROUP_DELETE_VAL:
                return OFFlowRemovedReason.GROUP_DELETE;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFFlowRemovedReason in version 1.3: " + val);
        }
    }


    public static byte toWireValue(OFFlowRemovedReason e) {
        switch(e) {
            case IDLE_TIMEOUT:
                return IDLE_TIMEOUT_VAL;
            case HARD_TIMEOUT:
                return HARD_TIMEOUT_VAL;
            case DELETE:
                return DELETE_VAL;
            case GROUP_DELETE:
                return GROUP_DELETE_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFFlowRemovedReason in version 1.3: " + e);
        }
    }

}

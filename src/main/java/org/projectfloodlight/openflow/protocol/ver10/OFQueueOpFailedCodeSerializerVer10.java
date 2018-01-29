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
import org.projectfloodlight.openflow.protocol.OFQueueOpFailedCode;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;

public class OFQueueOpFailedCodeSerializerVer10 {

    public final static short BAD_PORT_VAL = (short) 0x0;
    public final static short BAD_QUEUE_VAL = (short) 0x1;
    public final static short EPERM_VAL = (short) 0x2;

    public static OFQueueOpFailedCode readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OFQueueOpFailedCode e) {
        bb.writeShort(toWireValue(e));
    }

    public static void putTo(OFQueueOpFailedCode e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OFQueueOpFailedCode ofWireValue(short val) {
        switch(val) {
            case BAD_PORT_VAL:
                return OFQueueOpFailedCode.BAD_PORT;
            case BAD_QUEUE_VAL:
                return OFQueueOpFailedCode.BAD_QUEUE;
            case EPERM_VAL:
                return OFQueueOpFailedCode.EPERM;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFQueueOpFailedCode in version 1.0: " + val);
        }
    }


    public static short toWireValue(OFQueueOpFailedCode e) {
        switch(e) {
            case BAD_PORT:
                return BAD_PORT_VAL;
            case BAD_QUEUE:
                return BAD_QUEUE_VAL;
            case EPERM:
                return EPERM_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFQueueOpFailedCode in version 1.0: " + e);
        }
    }

}

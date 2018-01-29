// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_set_serializer.java
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
import org.projectfloodlight.openflow.protocol.OFCalientCflowOperationalState;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFCalientCflowOperationalStateSerializerVer13 {

    public final static byte INSERVICE_VAL = (byte) 0x1;
    public final static byte OUTOFSERVICE_VAL = (byte) 0x2;
    public final static byte READY_VAL = (byte) 0x3;
    public final static byte DEGRADED_VAL = (byte) 0x4;

    public static Set<OFCalientCflowOperationalState> readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OFCalientCflowOperationalState> set) {
        bb.writeByte(toWireValue(set));
    }

    public static void putTo(Set<OFCalientCflowOperationalState> set, PrimitiveSink sink) {
        sink.putByte(toWireValue(set));
    }


    public static Set<OFCalientCflowOperationalState> ofWireValue(byte val) {
        EnumSet<OFCalientCflowOperationalState> set = EnumSet.noneOf(OFCalientCflowOperationalState.class);

        if((val & INSERVICE_VAL) != 0)
            set.add(OFCalientCflowOperationalState.INSERVICE);
        if((val & OUTOFSERVICE_VAL) != 0)
            set.add(OFCalientCflowOperationalState.OUTOFSERVICE);
        if((val & READY_VAL) != 0)
            set.add(OFCalientCflowOperationalState.READY);
        if((val & DEGRADED_VAL) != 0)
            set.add(OFCalientCflowOperationalState.DEGRADED);
        return Collections.unmodifiableSet(set);
    }

    public static byte toWireValue(Set<OFCalientCflowOperationalState> set) {
        byte wireValue = 0;

        for(OFCalientCflowOperationalState e: set) {
            switch(e) {
                case INSERVICE:
                    wireValue |= INSERVICE_VAL;
                    break;
                case OUTOFSERVICE:
                    wireValue |= OUTOFSERVICE_VAL;
                    break;
                case READY:
                    wireValue |= READY_VAL;
                    break;
                case DEGRADED:
                    wireValue |= DEGRADED_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFCalientCflowOperationalState in version 1.3: " + e);
            }
        }
        return wireValue;
    }

}
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
import org.projectfloodlight.openflow.protocol.OFCalientOpticalPortOperCapability;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFCalientOpticalPortOperCapabilitySerializerVer13 {

    public final static byte NOHARDWARE_VAL = (byte) 0x1;
    public final static byte OK_VAL = (byte) 0x2;
    public final static byte FAILED_VAL = (byte) 0x3;
    public final static byte INITIALIZING_VAL = (byte) 0x4;

    public static Set<OFCalientOpticalPortOperCapability> readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OFCalientOpticalPortOperCapability> set) {
        bb.writeByte(toWireValue(set));
    }

    public static void putTo(Set<OFCalientOpticalPortOperCapability> set, PrimitiveSink sink) {
        sink.putByte(toWireValue(set));
    }


    public static Set<OFCalientOpticalPortOperCapability> ofWireValue(byte val) {
        EnumSet<OFCalientOpticalPortOperCapability> set = EnumSet.noneOf(OFCalientOpticalPortOperCapability.class);

        if((val & NOHARDWARE_VAL) != 0)
            set.add(OFCalientOpticalPortOperCapability.NOHARDWARE);
        if((val & OK_VAL) != 0)
            set.add(OFCalientOpticalPortOperCapability.OK);
        if((val & FAILED_VAL) != 0)
            set.add(OFCalientOpticalPortOperCapability.FAILED);
        if((val & INITIALIZING_VAL) != 0)
            set.add(OFCalientOpticalPortOperCapability.INITIALIZING);
        return Collections.unmodifiableSet(set);
    }

    public static byte toWireValue(Set<OFCalientOpticalPortOperCapability> set) {
        byte wireValue = 0;

        for(OFCalientOpticalPortOperCapability e: set) {
            switch(e) {
                case NOHARDWARE:
                    wireValue |= NOHARDWARE_VAL;
                    break;
                case OK:
                    wireValue |= OK_VAL;
                    break;
                case FAILED:
                    wireValue |= FAILED_VAL;
                    break;
                case INITIALIZING:
                    wireValue |= INITIALIZING_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFCalientOpticalPortOperCapability in version 1.3: " + e);
            }
        }
        return wireValue;
    }

}

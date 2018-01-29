// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_set_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver14;

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
import org.projectfloodlight.openflow.protocol.OFInstructionType;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFInstructionTypeSerializerVer14 {

    public final static short GOTO_TABLE_VAL = (short) 0x1;
    public final static short WRITE_METADATA_VAL = (short) 0x2;
    public final static short WRITE_ACTIONS_VAL = (short) 0x3;
    public final static short APPLY_ACTIONS_VAL = (short) 0x4;
    public final static short CLEAR_ACTIONS_VAL = (short) 0x5;
    public final static short EXPERIMENTER_VAL = (short) 0xffff;
    public final static short METER_VAL = (short) 0x6;

    public static Set<OFInstructionType> readFrom(ChannelBuffer bb) throws OFParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OFInstructionType> set) {
        bb.writeShort(toWireValue(set));
    }

    public static void putTo(Set<OFInstructionType> set, PrimitiveSink sink) {
        sink.putShort(toWireValue(set));
    }


    public static Set<OFInstructionType> ofWireValue(short val) {
        EnumSet<OFInstructionType> set = EnumSet.noneOf(OFInstructionType.class);

        if((val & GOTO_TABLE_VAL) != 0)
            set.add(OFInstructionType.GOTO_TABLE);
        if((val & WRITE_METADATA_VAL) != 0)
            set.add(OFInstructionType.WRITE_METADATA);
        if((val & WRITE_ACTIONS_VAL) != 0)
            set.add(OFInstructionType.WRITE_ACTIONS);
        if((val & APPLY_ACTIONS_VAL) != 0)
            set.add(OFInstructionType.APPLY_ACTIONS);
        if((val & CLEAR_ACTIONS_VAL) != 0)
            set.add(OFInstructionType.CLEAR_ACTIONS);
        if((val & EXPERIMENTER_VAL) != 0)
            set.add(OFInstructionType.EXPERIMENTER);
        if((val & METER_VAL) != 0)
            set.add(OFInstructionType.METER);
        return Collections.unmodifiableSet(set);
    }

    public static short toWireValue(Set<OFInstructionType> set) {
        short wireValue = 0;

        for(OFInstructionType e: set) {
            switch(e) {
                case GOTO_TABLE:
                    wireValue |= GOTO_TABLE_VAL;
                    break;
                case WRITE_METADATA:
                    wireValue |= WRITE_METADATA_VAL;
                    break;
                case WRITE_ACTIONS:
                    wireValue |= WRITE_ACTIONS_VAL;
                    break;
                case APPLY_ACTIONS:
                    wireValue |= APPLY_ACTIONS_VAL;
                    break;
                case CLEAR_ACTIONS:
                    wireValue |= CLEAR_ACTIONS_VAL;
                    break;
                case EXPERIMENTER:
                    wireValue |= EXPERIMENTER_VAL;
                    break;
                case METER:
                    wireValue |= METER_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFInstructionType in version 1.4: " + e);
            }
        }
        return wireValue;
    }

}

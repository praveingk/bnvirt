// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFOxmIcmpv6CodeVer13 implements OFOxmIcmpv6Code {
    private static final Logger logger = LoggerFactory.getLogger(OFOxmIcmpv6CodeVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 5;

        private final static U8 DEFAULT_VALUE = U8.ZERO;

    // OF message fields
    private final U8 value;
//
    // Immutable default instance
    final static OFOxmIcmpv6CodeVer13 DEFAULT = new OFOxmIcmpv6CodeVer13(
        DEFAULT_VALUE
    );

    // package private constructor - used by readers, builders, and factory
    OFOxmIcmpv6CodeVer13(U8 value) {
        if(value == null) {
            throw new NullPointerException("OFOxmIcmpv6CodeVer13: property value cannot be null");
        }
        this.value = value;
    }

    // Accessors for OF message fields
    @Override
    public long getTypeLen() {
        return 0x80003c01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.ICMPV6_CODE;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    public OFOxm<U8> getCanonical() {
        // exact match OXM is always canonical
        return this;
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



    public OFOxmIcmpv6Code.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFOxmIcmpv6Code.Builder {
        final OFOxmIcmpv6CodeVer13 parentMessage;

        // OF message fields
        private boolean valueSet;
        private U8 value;

        BuilderWithParent(OFOxmIcmpv6CodeVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public long getTypeLen() {
        return 0x80003c01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public OFOxmIcmpv6Code.Builder setValue(U8 value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.ICMPV6_CODE;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<U8> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.3");
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



        @Override
        public OFOxmIcmpv6Code build() {
                U8 value = this.valueSet ? this.value : parentMessage.value;
                if(value == null)
                    throw new NullPointerException("Property value must not be null");

                //
                return new OFOxmIcmpv6CodeVer13(
                    value
                );
        }

    }

    static class Builder implements OFOxmIcmpv6Code.Builder {
        // OF message fields
        private boolean valueSet;
        private U8 value;

    @Override
    public long getTypeLen() {
        return 0x80003c01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public OFOxmIcmpv6Code.Builder setValue(U8 value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.ICMPV6_CODE;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<U8> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.3");
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

//
        @Override
        public OFOxmIcmpv6Code build() {
            U8 value = this.valueSet ? this.value : DEFAULT_VALUE;
            if(value == null)
                throw new NullPointerException("Property value must not be null");


            return new OFOxmIcmpv6CodeVer13(
                    value
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFOxmIcmpv6Code> {
        @Override
        public OFOxmIcmpv6Code readFrom(ChannelBuffer bb) throws OFParseError {
            // fixed value property typeLen == 0x80003c01L
            int typeLen = bb.readInt();
            if(typeLen != (int) 0x80003c01)
                throw new OFParseError("Wrong typeLen: Expected=0x80003c01L(0x80003c01L), got="+typeLen);
            U8 value = U8.of(bb.readByte());

            OFOxmIcmpv6CodeVer13 oxmIcmpv6CodeVer13 = new OFOxmIcmpv6CodeVer13(
                    value
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", oxmIcmpv6CodeVer13);
            return oxmIcmpv6CodeVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFOxmIcmpv6CodeVer13Funnel FUNNEL = new OFOxmIcmpv6CodeVer13Funnel();
    static class OFOxmIcmpv6CodeVer13Funnel implements Funnel<OFOxmIcmpv6CodeVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFOxmIcmpv6CodeVer13 message, PrimitiveSink sink) {
            // fixed value property typeLen = 0x80003c01L
            sink.putInt((int) 0x80003c01);
            message.value.putTo(sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFOxmIcmpv6CodeVer13> {
        @Override
        public void write(ChannelBuffer bb, OFOxmIcmpv6CodeVer13 message) {
            // fixed value property typeLen = 0x80003c01L
            bb.writeInt((int) 0x80003c01);
            bb.writeByte(message.value.getRaw());


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFOxmIcmpv6CodeVer13(");
        b.append("value=").append(value);
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFOxmIcmpv6CodeVer13 other = (OFOxmIcmpv6CodeVer13) obj;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

}

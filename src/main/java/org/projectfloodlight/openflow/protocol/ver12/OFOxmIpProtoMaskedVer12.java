// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver12;

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

class OFOxmIpProtoMaskedVer12 implements OFOxmIpProtoMasked {
    private static final Logger logger = LoggerFactory.getLogger(OFOxmIpProtoMaskedVer12.class);
    // version: 1.2
    final static byte WIRE_VERSION = 3;
    final static int LENGTH = 6;

        private final static IpProtocol DEFAULT_VALUE = IpProtocol.NONE;
        private final static IpProtocol DEFAULT_VALUE_MASK = IpProtocol.NONE;

    // OF message fields
    private final IpProtocol value;
    private final IpProtocol mask;
//
    // Immutable default instance
    final static OFOxmIpProtoMaskedVer12 DEFAULT = new OFOxmIpProtoMaskedVer12(
        DEFAULT_VALUE, DEFAULT_VALUE_MASK
    );

    // package private constructor - used by readers, builders, and factory
    OFOxmIpProtoMaskedVer12(IpProtocol value, IpProtocol mask) {
        if(value == null) {
            throw new NullPointerException("OFOxmIpProtoMaskedVer12: property value cannot be null");
        }
        if(mask == null) {
            throw new NullPointerException("OFOxmIpProtoMaskedVer12: property mask cannot be null");
        }
        this.value = value;
        this.mask = mask;
    }

    // Accessors for OF message fields
    @Override
    public long getTypeLen() {
        return 0x80001502L;
    }

    @Override
    public IpProtocol getValue() {
        return value;
    }

    @Override
    public IpProtocol getMask() {
        return mask;
    }

    @Override
    public MatchField<IpProtocol> getMatchField() {
        return MatchField.IP_PROTO;
    }

    @Override
    public boolean isMasked() {
        return true;
    }

    public OFOxm<IpProtocol> getCanonical() {
        if (IpProtocol.NO_MASK.equals(mask)) {
            return new OFOxmIpProtoVer12(value);
        } else if(IpProtocol.FULL_MASK.equals(mask)) {
            return null;
        } else {
            return this;
        }
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }



    public OFOxmIpProtoMasked.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFOxmIpProtoMasked.Builder {
        final OFOxmIpProtoMaskedVer12 parentMessage;

        // OF message fields
        private boolean valueSet;
        private IpProtocol value;
        private boolean maskSet;
        private IpProtocol mask;

        BuilderWithParent(OFOxmIpProtoMaskedVer12 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public long getTypeLen() {
        return 0x80001502L;
    }

    @Override
    public IpProtocol getValue() {
        return value;
    }

    @Override
    public OFOxmIpProtoMasked.Builder setValue(IpProtocol value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public IpProtocol getMask() {
        return mask;
    }

    @Override
    public OFOxmIpProtoMasked.Builder setMask(IpProtocol mask) {
        this.mask = mask;
        this.maskSet = true;
        return this;
    }
    @Override
    public MatchField<IpProtocol> getMatchField() {
        return MatchField.IP_PROTO;
    }

    @Override
    public boolean isMasked() {
        return true;
    }

    @Override
    public OFOxm<IpProtocol> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.2");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }



        @Override
        public OFOxmIpProtoMasked build() {
                IpProtocol value = this.valueSet ? this.value : parentMessage.value;
                if(value == null)
                    throw new NullPointerException("Property value must not be null");
                IpProtocol mask = this.maskSet ? this.mask : parentMessage.mask;
                if(mask == null)
                    throw new NullPointerException("Property mask must not be null");

                //
                return new OFOxmIpProtoMaskedVer12(
                    value,
                    mask
                );
        }

    }

    static class Builder implements OFOxmIpProtoMasked.Builder {
        // OF message fields
        private boolean valueSet;
        private IpProtocol value;
        private boolean maskSet;
        private IpProtocol mask;

    @Override
    public long getTypeLen() {
        return 0x80001502L;
    }

    @Override
    public IpProtocol getValue() {
        return value;
    }

    @Override
    public OFOxmIpProtoMasked.Builder setValue(IpProtocol value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public IpProtocol getMask() {
        return mask;
    }

    @Override
    public OFOxmIpProtoMasked.Builder setMask(IpProtocol mask) {
        this.mask = mask;
        this.maskSet = true;
        return this;
    }
    @Override
    public MatchField<IpProtocol> getMatchField() {
        return MatchField.IP_PROTO;
    }

    @Override
    public boolean isMasked() {
        return true;
    }

    @Override
    public OFOxm<IpProtocol> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.2");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }

//
        @Override
        public OFOxmIpProtoMasked build() {
            IpProtocol value = this.valueSet ? this.value : DEFAULT_VALUE;
            if(value == null)
                throw new NullPointerException("Property value must not be null");
            IpProtocol mask = this.maskSet ? this.mask : DEFAULT_VALUE_MASK;
            if(mask == null)
                throw new NullPointerException("Property mask must not be null");


            return new OFOxmIpProtoMaskedVer12(
                    value,
                    mask
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFOxmIpProtoMasked> {
        @Override
        public OFOxmIpProtoMasked readFrom(ChannelBuffer bb) throws OFParseError {
            // fixed value property typeLen == 0x80001502L
            int typeLen = bb.readInt();
            if(typeLen != (int) 0x80001502)
                throw new OFParseError("Wrong typeLen: Expected=0x80001502L(0x80001502L), got="+typeLen);
            IpProtocol value = IpProtocol.readByte(bb);
            IpProtocol mask = IpProtocol.readByte(bb);

            OFOxmIpProtoMaskedVer12 oxmIpProtoMaskedVer12 = new OFOxmIpProtoMaskedVer12(
                    value,
                      mask
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", oxmIpProtoMaskedVer12);
            return oxmIpProtoMaskedVer12;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFOxmIpProtoMaskedVer12Funnel FUNNEL = new OFOxmIpProtoMaskedVer12Funnel();
    static class OFOxmIpProtoMaskedVer12Funnel implements Funnel<OFOxmIpProtoMaskedVer12> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFOxmIpProtoMaskedVer12 message, PrimitiveSink sink) {
            // fixed value property typeLen = 0x80001502L
            sink.putInt((int) 0x80001502);
            message.value.putTo(sink);
            message.mask.putTo(sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFOxmIpProtoMaskedVer12> {
        @Override
        public void write(ChannelBuffer bb, OFOxmIpProtoMaskedVer12 message) {
            // fixed value property typeLen = 0x80001502L
            bb.writeInt((int) 0x80001502);
            message.value.writeByte(bb);
            message.mask.writeByte(bb);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFOxmIpProtoMaskedVer12(");
        b.append("value=").append(value);
        b.append(", ");
        b.append("mask=").append(mask);
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
        OFOxmIpProtoMaskedVer12 other = (OFOxmIpProtoMaskedVer12) obj;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (mask == null) {
            if (other.mask != null)
                return false;
        } else if (!mask.equals(other.mask))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((mask == null) ? 0 : mask.hashCode());
        return result;
    }

}

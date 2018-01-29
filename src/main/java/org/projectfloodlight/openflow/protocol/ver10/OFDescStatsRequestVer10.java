// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

public class OFDescStatsRequestVer10 implements OFDescStatsRequest {
    private static final Logger logger = LoggerFactory.getLogger(OFDescStatsRequestVer10.class);
    // version: 1.0
    final static byte WIRE_VERSION = 1;
    final static int LENGTH = 12;

        private final static long DEFAULT_XID = 0x0L;
        private final static Set<OFStatsRequestFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsRequestFlags>of();

    // OF message fields
    private final long xid;
    private final Set<OFStatsRequestFlags> flags;
//
    // Immutable default instance
    final static OFDescStatsRequestVer10 DEFAULT = new OFDescStatsRequestVer10(
        DEFAULT_XID, DEFAULT_FLAGS
    );

    // package private constructor - used by readers, builders, and factory
    protected OFDescStatsRequestVer10(long xid, Set<OFStatsRequestFlags> flags) {
        if(flags == null) {
            throw new NullPointerException("OFDescStatsRequestVer10: property flags cannot be null");
        }
        this.xid = xid;
        this.flags = flags;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.DESC;
    }

    @Override
    public Set<OFStatsRequestFlags> getFlags() {
        return flags;
    }



    public OFDescStatsRequest.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFDescStatsRequest.Builder {
        final OFDescStatsRequestVer10 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean flagsSet;
        private Set<OFStatsRequestFlags> flags;

        BuilderWithParent(OFDescStatsRequestVer10 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFDescStatsRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.DESC;
    }

    @Override
    public Set<OFStatsRequestFlags> getFlags() {
        return flags;
    }

    @Override
    public OFDescStatsRequest.Builder setFlags(Set<OFStatsRequestFlags> flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }


        @Override
        public OFDescStatsRequest build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                Set<OFStatsRequestFlags> flags = this.flagsSet ? this.flags : parentMessage.flags;
                if(flags == null)
                    throw new NullPointerException("Property flags must not be null");

                //
                return new OFDescStatsRequestVer10(
                    xid,
                    flags
                );
        }

    }

    static class Builder implements OFDescStatsRequest.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean flagsSet;
        private Set<OFStatsRequestFlags> flags;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFDescStatsRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.DESC;
    }

    @Override
    public Set<OFStatsRequestFlags> getFlags() {
        return flags;
    }

    @Override
    public OFDescStatsRequest.Builder setFlags(Set<OFStatsRequestFlags> flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }
//
        @Override
        public OFDescStatsRequest build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            Set<OFStatsRequestFlags> flags = this.flagsSet ? this.flags : DEFAULT_FLAGS;
            if(flags == null)
                throw new NullPointerException("Property flags must not be null");


            return new OFDescStatsRequestVer10(
                    xid,
                    flags
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFDescStatsRequest> {
        @Override
        public OFDescStatsRequest readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 1
            byte version = bb.readByte();
            if(version != (byte) 0x1)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_10(1), got="+version);
            // fixed value property type == 16
            byte type = bb.readByte();
            if(type != (byte) 0x10)
                throw new OFParseError("Wrong type: Expected=OFType.STATS_REQUEST(16), got="+type);
            int length = U16.f(bb.readShort());
            if(length != 12)
                throw new OFParseError("Wrong length: Expected=12(12), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            // fixed value property statsType == 0
            short statsType = bb.readShort();
            if(statsType != (short) 0x0)
                throw new OFParseError("Wrong statsType: Expected=OFStatsType.DESC(0), got="+statsType);
            Set<OFStatsRequestFlags> flags = OFStatsRequestFlagsSerializerVer10.readFrom(bb);

            OFDescStatsRequestVer10 descStatsRequestVer10 = new OFDescStatsRequestVer10(
                    xid,
                      flags
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", descStatsRequestVer10);
            return descStatsRequestVer10;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFDescStatsRequestVer10Funnel FUNNEL = new OFDescStatsRequestVer10Funnel();
    static class OFDescStatsRequestVer10Funnel implements Funnel<OFDescStatsRequestVer10> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFDescStatsRequestVer10 message, PrimitiveSink sink) {
            // fixed value property version = 1
            sink.putByte((byte) 0x1);
            // fixed value property type = 16
            sink.putByte((byte) 0x10);
            // fixed value property length = 12
            sink.putShort((short) 0xc);
            sink.putLong(message.xid);
            // fixed value property statsType = 0
            sink.putShort((short) 0x0);
            OFStatsRequestFlagsSerializerVer10.putTo(message.flags, sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFDescStatsRequestVer10> {
        @Override
        public void write(ChannelBuffer bb, OFDescStatsRequestVer10 message) {
            // fixed value property version = 1
            bb.writeByte((byte) 0x1);
            // fixed value property type = 16
            bb.writeByte((byte) 0x10);
            // fixed value property length = 12
            bb.writeShort((short) 0xc);
            bb.writeInt(U32.t(message.xid));
            // fixed value property statsType = 0
            bb.writeShort((short) 0x0);
            OFStatsRequestFlagsSerializerVer10.writeTo(bb, message.flags);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFDescStatsRequestVer10(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("flags=").append(flags);
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
        OFDescStatsRequestVer10 other = (OFDescStatsRequestVer10) obj;

        if( xid != other.xid)
            return false;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        return result;
    }

}

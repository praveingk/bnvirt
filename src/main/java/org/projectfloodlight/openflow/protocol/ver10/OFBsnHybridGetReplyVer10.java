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
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFBsnHybridGetReplyVer10 implements OFBsnHybridGetReply {
    private static final Logger logger = LoggerFactory.getLogger(OFBsnHybridGetReplyVer10.class);
    // version: 1.0
    final static byte WIRE_VERSION = 1;
    final static int LENGTH = 24;

        private final static long DEFAULT_XID = 0x0L;
        private final static short DEFAULT_HYBRID_ENABLE = (short) 0x0;
        private final static int DEFAULT_HYBRID_VERSION = 0x0;

    // OF message fields
    private final long xid;
    private final short hybridEnable;
    private final int hybridVersion;
//
    // Immutable default instance
    final static OFBsnHybridGetReplyVer10 DEFAULT = new OFBsnHybridGetReplyVer10(
        DEFAULT_XID, DEFAULT_HYBRID_ENABLE, DEFAULT_HYBRID_VERSION
    );

    // package private constructor - used by readers, builders, and factory
    OFBsnHybridGetReplyVer10(long xid, short hybridEnable, int hybridVersion) {
        this.xid = xid;
        this.hybridEnable = hybridEnable;
        this.hybridVersion = hybridVersion;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1cL;
    }

    @Override
    public short getHybridEnable() {
        return hybridEnable;
    }

    @Override
    public int getHybridVersion() {
        return hybridVersion;
    }



    public OFBsnHybridGetReply.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBsnHybridGetReply.Builder {
        final OFBsnHybridGetReplyVer10 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean hybridEnableSet;
        private short hybridEnable;
        private boolean hybridVersionSet;
        private int hybridVersion;

        BuilderWithParent(OFBsnHybridGetReplyVer10 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnHybridGetReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1cL;
    }

    @Override
    public short getHybridEnable() {
        return hybridEnable;
    }

    @Override
    public OFBsnHybridGetReply.Builder setHybridEnable(short hybridEnable) {
        this.hybridEnable = hybridEnable;
        this.hybridEnableSet = true;
        return this;
    }
    @Override
    public int getHybridVersion() {
        return hybridVersion;
    }

    @Override
    public OFBsnHybridGetReply.Builder setHybridVersion(int hybridVersion) {
        this.hybridVersion = hybridVersion;
        this.hybridVersionSet = true;
        return this;
    }


        @Override
        public OFBsnHybridGetReply build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                short hybridEnable = this.hybridEnableSet ? this.hybridEnable : parentMessage.hybridEnable;
                int hybridVersion = this.hybridVersionSet ? this.hybridVersion : parentMessage.hybridVersion;

                //
                return new OFBsnHybridGetReplyVer10(
                    xid,
                    hybridEnable,
                    hybridVersion
                );
        }

    }

    static class Builder implements OFBsnHybridGetReply.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean hybridEnableSet;
        private short hybridEnable;
        private boolean hybridVersionSet;
        private int hybridVersion;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnHybridGetReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x1cL;
    }

    @Override
    public short getHybridEnable() {
        return hybridEnable;
    }

    @Override
    public OFBsnHybridGetReply.Builder setHybridEnable(short hybridEnable) {
        this.hybridEnable = hybridEnable;
        this.hybridEnableSet = true;
        return this;
    }
    @Override
    public int getHybridVersion() {
        return hybridVersion;
    }

    @Override
    public OFBsnHybridGetReply.Builder setHybridVersion(int hybridVersion) {
        this.hybridVersion = hybridVersion;
        this.hybridVersionSet = true;
        return this;
    }
//
        @Override
        public OFBsnHybridGetReply build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            short hybridEnable = this.hybridEnableSet ? this.hybridEnable : DEFAULT_HYBRID_ENABLE;
            int hybridVersion = this.hybridVersionSet ? this.hybridVersion : DEFAULT_HYBRID_VERSION;


            return new OFBsnHybridGetReplyVer10(
                    xid,
                    hybridEnable,
                    hybridVersion
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBsnHybridGetReply> {
        @Override
        public OFBsnHybridGetReply readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 1
            byte version = bb.readByte();
            if(version != (byte) 0x1)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_10(1), got="+version);
            // fixed value property type == 4
            byte type = bb.readByte();
            if(type != (byte) 0x4)
                throw new OFParseError("Wrong type: Expected=OFType.EXPERIMENTER(4), got="+type);
            int length = U16.f(bb.readShort());
            if(length != 24)
                throw new OFParseError("Wrong length: Expected=24(24), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            // fixed value property experimenter == 0x5c16c7L
            int experimenter = bb.readInt();
            if(experimenter != 0x5c16c7)
                throw new OFParseError("Wrong experimenter: Expected=0x5c16c7L(0x5c16c7L), got="+experimenter);
            // fixed value property subtype == 0x1cL
            int subtype = bb.readInt();
            if(subtype != 0x1c)
                throw new OFParseError("Wrong subtype: Expected=0x1cL(0x1cL), got="+subtype);
            short hybridEnable = U8.f(bb.readByte());
            // pad: 1 bytes
            bb.skipBytes(1);
            int hybridVersion = U16.f(bb.readShort());
            // pad: 4 bytes
            bb.skipBytes(4);

            OFBsnHybridGetReplyVer10 bsnHybridGetReplyVer10 = new OFBsnHybridGetReplyVer10(
                    xid,
                      hybridEnable,
                      hybridVersion
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bsnHybridGetReplyVer10);
            return bsnHybridGetReplyVer10;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBsnHybridGetReplyVer10Funnel FUNNEL = new OFBsnHybridGetReplyVer10Funnel();
    static class OFBsnHybridGetReplyVer10Funnel implements Funnel<OFBsnHybridGetReplyVer10> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBsnHybridGetReplyVer10 message, PrimitiveSink sink) {
            // fixed value property version = 1
            sink.putByte((byte) 0x1);
            // fixed value property type = 4
            sink.putByte((byte) 0x4);
            // fixed value property length = 24
            sink.putShort((short) 0x18);
            sink.putLong(message.xid);
            // fixed value property experimenter = 0x5c16c7L
            sink.putInt(0x5c16c7);
            // fixed value property subtype = 0x1cL
            sink.putInt(0x1c);
            sink.putShort(message.hybridEnable);
            // skip pad (1 bytes)
            sink.putInt(message.hybridVersion);
            // skip pad (4 bytes)
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBsnHybridGetReplyVer10> {
        @Override
        public void write(ChannelBuffer bb, OFBsnHybridGetReplyVer10 message) {
            // fixed value property version = 1
            bb.writeByte((byte) 0x1);
            // fixed value property type = 4
            bb.writeByte((byte) 0x4);
            // fixed value property length = 24
            bb.writeShort((short) 0x18);
            bb.writeInt(U32.t(message.xid));
            // fixed value property experimenter = 0x5c16c7L
            bb.writeInt(0x5c16c7);
            // fixed value property subtype = 0x1cL
            bb.writeInt(0x1c);
            bb.writeByte(U8.t(message.hybridEnable));
            // pad: 1 bytes
            bb.writeZero(1);
            bb.writeShort(U16.t(message.hybridVersion));
            // pad: 4 bytes
            bb.writeZero(4);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBsnHybridGetReplyVer10(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("hybridEnable=").append(hybridEnable);
        b.append(", ");
        b.append("hybridVersion=").append(hybridVersion);
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
        OFBsnHybridGetReplyVer10 other = (OFBsnHybridGetReplyVer10) obj;

        if( xid != other.xid)
            return false;
        if( hybridEnable != other.hybridEnable)
            return false;
        if( hybridVersion != other.hybridVersion)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + hybridEnable;
        result = prime * result + hybridVersion;
        return result;
    }

}

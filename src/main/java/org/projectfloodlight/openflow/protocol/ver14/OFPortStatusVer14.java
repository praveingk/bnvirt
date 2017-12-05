// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFPortStatusVer14 implements OFPortStatus {
    private static final Logger logger = LoggerFactory.getLogger(OFPortStatusVer14.class);
    // version: 1.4
    final static byte WIRE_VERSION = 5;
    final static int MINIMUM_LENGTH = 56;

        private final static long DEFAULT_XID = 0x0L;

    // OF message fields
    private final long xid;
    private final OFPortReason reason;
    private final OFPortDesc desc;
//

    // package private constructor - used by readers, builders, and factory
    OFPortStatusVer14(long xid, OFPortReason reason, OFPortDesc desc) {
        if(reason == null) {
            throw new NullPointerException("OFPortStatusVer14: property reason cannot be null");
        }
        if(desc == null) {
            throw new NullPointerException("OFPortStatusVer14: property desc cannot be null");
        }
        this.xid = xid;
        this.reason = reason;
        this.desc = desc;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }

    @Override
    public OFType getType() {
        return OFType.PORT_STATUS;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFPortReason getReason() {
        return reason;
    }

    @Override
    public OFPortDesc getDesc() {
        return desc;
    }



    public OFPortStatus.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFPortStatus.Builder {
        final OFPortStatusVer14 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean reasonSet;
        private OFPortReason reason;
        private boolean descSet;
        private OFPortDesc desc;

        BuilderWithParent(OFPortStatusVer14 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }

    @Override
    public OFType getType() {
        return OFType.PORT_STATUS;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFPortStatus.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFPortReason getReason() {
        return reason;
    }

    @Override
    public OFPortStatus.Builder setReason(OFPortReason reason) {
        this.reason = reason;
        this.reasonSet = true;
        return this;
    }
    @Override
    public OFPortDesc getDesc() {
        return desc;
    }

    @Override
    public OFPortStatus.Builder setDesc(OFPortDesc desc) {
        this.desc = desc;
        this.descSet = true;
        return this;
    }


        @Override
        public OFPortStatus build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                OFPortReason reason = this.reasonSet ? this.reason : parentMessage.reason;
                if(reason == null)
                    throw new NullPointerException("Property reason must not be null");
                OFPortDesc desc = this.descSet ? this.desc : parentMessage.desc;
                if(desc == null)
                    throw new NullPointerException("Property desc must not be null");

                //
                return new OFPortStatusVer14(
                    xid,
                    reason,
                    desc
                );
        }

    }

    static class Builder implements OFPortStatus.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean reasonSet;
        private OFPortReason reason;
        private boolean descSet;
        private OFPortDesc desc;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }

    @Override
    public OFType getType() {
        return OFType.PORT_STATUS;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFPortStatus.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFPortReason getReason() {
        return reason;
    }

    @Override
    public OFPortStatus.Builder setReason(OFPortReason reason) {
        this.reason = reason;
        this.reasonSet = true;
        return this;
    }
    @Override
    public OFPortDesc getDesc() {
        return desc;
    }

    @Override
    public OFPortStatus.Builder setDesc(OFPortDesc desc) {
        this.desc = desc;
        this.descSet = true;
        return this;
    }
//
        @Override
        public OFPortStatus build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            if(!this.reasonSet)
                throw new IllegalStateException("Property reason doesn't have default value -- must be set");
            if(reason == null)
                throw new NullPointerException("Property reason must not be null");
            if(!this.descSet)
                throw new IllegalStateException("Property desc doesn't have default value -- must be set");
            if(desc == null)
                throw new NullPointerException("Property desc must not be null");


            return new OFPortStatusVer14(
                    xid,
                    reason,
                    desc
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFPortStatus> {
        @Override
        public OFPortStatus readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 5
            byte version = bb.readByte();
            if(version != (byte) 0x5)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_14(5), got="+version);
            // fixed value property type == 12
            byte type = bb.readByte();
            if(type != (byte) 0xc)
                throw new OFParseError("Wrong type: Expected=OFType.PORT_STATUS(12), got="+type);
            int length = U16.f(bb.readShort());
            if(length < MINIMUM_LENGTH)
                throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            OFPortReason reason = OFPortReasonSerializerVer14.readFrom(bb);
            // pad: 7 bytes
            bb.skipBytes(7);
            OFPortDesc desc = OFPortDescVer14.READER.readFrom(bb);

            OFPortStatusVer14 portStatusVer14 = new OFPortStatusVer14(
                    xid,
                      reason,
                      desc
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", portStatusVer14);
            return portStatusVer14;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFPortStatusVer14Funnel FUNNEL = new OFPortStatusVer14Funnel();
    static class OFPortStatusVer14Funnel implements Funnel<OFPortStatusVer14> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFPortStatusVer14 message, PrimitiveSink sink) {
            // fixed value property version = 5
            sink.putByte((byte) 0x5);
            // fixed value property type = 12
            sink.putByte((byte) 0xc);
            // FIXME: skip funnel of length
            sink.putLong(message.xid);
            OFPortReasonSerializerVer14.putTo(message.reason, sink);
            // skip pad (7 bytes)
            message.desc.putTo(sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFPortStatusVer14> {
        @Override
        public void write(ChannelBuffer bb, OFPortStatusVer14 message) {
            int startIndex = bb.writerIndex();
            // fixed value property version = 5
            bb.writeByte((byte) 0x5);
            // fixed value property type = 12
            bb.writeByte((byte) 0xc);
            // length is length of variable message, will be updated at the end
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            bb.writeInt(U32.t(message.xid));
            OFPortReasonSerializerVer14.writeTo(bb, message.reason);
            // pad: 7 bytes
            bb.writeZero(7);
            message.desc.writeTo(bb);

            // update length field
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);

        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFPortStatusVer14(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("reason=").append(reason);
        b.append(", ");
        b.append("desc=").append(desc);
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
        OFPortStatusVer14 other = (OFPortStatusVer14) obj;

        if( xid != other.xid)
            return false;
        if (reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (desc == null) {
            if (other.desc != null)
                return false;
        } else if (!desc.equals(other.desc))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        return result;
    }

}

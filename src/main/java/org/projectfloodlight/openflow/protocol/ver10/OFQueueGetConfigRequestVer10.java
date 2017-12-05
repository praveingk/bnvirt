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

public class OFQueueGetConfigRequestVer10 implements OFQueueGetConfigRequest {
    private static final Logger logger = LoggerFactory.getLogger(OFQueueGetConfigRequestVer10.class);
    // version: 1.0
    final static byte WIRE_VERSION = 1;
    final static int LENGTH = 12;

        private final static long DEFAULT_XID = 0x0L;
        private final static OFPort DEFAULT_PORT = OFPort.ANY;

    // OF message fields
    public final long xid;
    public final OFPort port;
//
    // Immutable default instance
    final static OFQueueGetConfigRequestVer10 DEFAULT = new OFQueueGetConfigRequestVer10(
        DEFAULT_XID, DEFAULT_PORT
    );

    // package private constructor - used by readers, builders, and factory
    protected OFQueueGetConfigRequestVer10(long xid, OFPort port) {
        if(port == null) {
            throw new NullPointerException("OFQueueGetConfigRequestVer10: property port cannot be null");
        }
        this.xid = xid;
        this.port = port;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.QUEUE_GET_CONFIG_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFPort getPort() {
        return port;
    }



    public OFQueueGetConfigRequest.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFQueueGetConfigRequest.Builder {
        final OFQueueGetConfigRequestVer10 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean portSet;
        private OFPort port;

        BuilderWithParent(OFQueueGetConfigRequestVer10 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.QUEUE_GET_CONFIG_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFQueueGetConfigRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFPort getPort() {
        return port;
    }

    @Override
    public OFQueueGetConfigRequest.Builder setPort(OFPort port) {
        this.port = port;
        this.portSet = true;
        return this;
    }


        @Override
        public OFQueueGetConfigRequest build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                OFPort port = this.portSet ? this.port : parentMessage.port;
                if(port == null)
                    throw new NullPointerException("Property port must not be null");

                //
                return new OFQueueGetConfigRequestVer10(
                    xid,
                    port
                );
        }

    }

    public static class Builder implements OFQueueGetConfigRequest.Builder {
        // OF message fields
        protected boolean xidSet;
        protected long xid;
        protected boolean portSet;
        protected OFPort port;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_10;
    }

    @Override
    public OFType getType() {
        return OFType.QUEUE_GET_CONFIG_REQUEST;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFQueueGetConfigRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFPort getPort() {
        return port;
    }

    @Override
    public OFQueueGetConfigRequest.Builder setPort(OFPort port) {
        this.port = port;
        this.portSet = true;
        return this;
    }
//
        @Override
        public OFQueueGetConfigRequest build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            OFPort port = this.portSet ? this.port : DEFAULT_PORT;
            if(port == null)
                throw new NullPointerException("Property port must not be null");


            return new OFQueueGetConfigRequestVer10(
                    xid,
                    port
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFQueueGetConfigRequest> {
        @Override
        public OFQueueGetConfigRequest readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 1
            byte version = bb.readByte();
            if(version != (byte) 0x1)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_10(1), got="+version);
            // fixed value property type == 20
            byte type = bb.readByte();
            if(type != (byte) 0x14)
                throw new OFParseError("Wrong type: Expected=OFType.QUEUE_GET_CONFIG_REQUEST(20), got="+type);
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
            OFPort port = OFPort.read2Bytes(bb);
            // pad: 2 bytes
            bb.skipBytes(2);

            OFQueueGetConfigRequestVer10 queueGetConfigRequestVer10 = new OFQueueGetConfigRequestVer10(
                    xid,
                      port
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", queueGetConfigRequestVer10);
            return queueGetConfigRequestVer10;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFQueueGetConfigRequestVer10Funnel FUNNEL = new OFQueueGetConfigRequestVer10Funnel();
    static class OFQueueGetConfigRequestVer10Funnel implements Funnel<OFQueueGetConfigRequestVer10> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFQueueGetConfigRequestVer10 message, PrimitiveSink sink) {
            // fixed value property version = 1
            sink.putByte((byte) 0x1);
            // fixed value property type = 20
            sink.putByte((byte) 0x14);
            // fixed value property length = 12
            sink.putShort((short) 0xc);
            sink.putLong(message.xid);
            message.port.putTo(sink);
            // skip pad (2 bytes)
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFQueueGetConfigRequestVer10> {
        @Override
        public void write(ChannelBuffer bb, OFQueueGetConfigRequestVer10 message) {
            // fixed value property version = 1
            bb.writeByte((byte) 0x1);
            // fixed value property type = 20
            bb.writeByte((byte) 0x14);
            // fixed value property length = 12
            bb.writeShort((short) 0xc);
            bb.writeInt(U32.t(message.xid));
            message.port.write2Bytes(bb);
            // pad: 2 bytes
            bb.writeZero(2);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFQueueGetConfigRequestVer10(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("port=").append(port);
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
        OFQueueGetConfigRequestVer10 other = (OFQueueGetConfigRequestVer10) obj;

        if( xid != other.xid)
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        return result;
    }

}

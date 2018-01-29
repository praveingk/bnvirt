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
import java.util.List;
import com.google.common.collect.ImmutableList;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

public class OFMeterModVer13 implements OFMeterMod {
    private static final Logger logger = LoggerFactory.getLogger(OFMeterModVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int MINIMUM_LENGTH = 16;

        private final static long DEFAULT_XID = 0x0L;
        private final static int DEFAULT_COMMAND = 0x0;
        private final static int DEFAULT_FLAGS = 0x0;
        private final static long DEFAULT_METER_ID = 0x0L;
        private final static List<OFMeterBand> DEFAULT_METERS = ImmutableList.<OFMeterBand>of();

    // OF message fields
    private final long xid;
    private final int command;
    private final int flags;
    private final long meterId;
    private final List<OFMeterBand> meters;
//
    // Immutable default instance
    final static OFMeterModVer13 DEFAULT = new OFMeterModVer13(
        DEFAULT_XID, DEFAULT_COMMAND, DEFAULT_FLAGS, DEFAULT_METER_ID, DEFAULT_METERS
    );

    // package private constructor - used by readers, builders, and factory
    protected OFMeterModVer13(long xid, int command, int flags, long meterId, List<OFMeterBand> meters) {
        if(meters == null) {
            throw new NullPointerException("OFMeterModVer13: property meters cannot be null");
        }
        this.xid = xid;
        this.command = command;
        this.flags = flags;
        this.meterId = meterId;
        this.meters = meters;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.METER_MOD;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public int getCommand() {
        return command;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public long getMeterId() {
        return meterId;
    }

    @Override
    public List<OFMeterBand> getMeters() {
        return meters;
    }

    @Override
    public List<OFMeterBand> getBands()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property bands not supported in version 1.3");
    }



    public OFMeterMod.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFMeterMod.Builder {
        final OFMeterModVer13 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean commandSet;
        private int command;
        private boolean flagsSet;
        private int flags;
        private boolean meterIdSet;
        private long meterId;
        private boolean metersSet;
        private List<OFMeterBand> meters;

        BuilderWithParent(OFMeterModVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.METER_MOD;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFMeterMod.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public int getCommand() {
        return command;
    }

    @Override
    public OFMeterMod.Builder setCommand(int command) {
        this.command = command;
        this.commandSet = true;
        return this;
    }
    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public OFMeterMod.Builder setFlags(int flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }
    @Override
    public long getMeterId() {
        return meterId;
    }

    @Override
    public OFMeterMod.Builder setMeterId(long meterId) {
        this.meterId = meterId;
        this.meterIdSet = true;
        return this;
    }
    @Override
    public List<OFMeterBand> getMeters() {
        return meters;
    }

    @Override
    public OFMeterMod.Builder setMeters(List<OFMeterBand> meters) {
        this.meters = meters;
        this.metersSet = true;
        return this;
    }
    @Override
    public List<OFMeterBand> getBands()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property bands not supported in version 1.3");
    }

    @Override
    public OFMeterMod.Builder setBands(List<OFMeterBand> bands) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property bands not supported in version 1.3");
    }


        @Override
        public OFMeterMod build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                int command = this.commandSet ? this.command : parentMessage.command;
                int flags = this.flagsSet ? this.flags : parentMessage.flags;
                long meterId = this.meterIdSet ? this.meterId : parentMessage.meterId;
                List<OFMeterBand> meters = this.metersSet ? this.meters : parentMessage.meters;
                if(meters == null)
                    throw new NullPointerException("Property meters must not be null");

                //
                return new OFMeterModVer13(
                    xid,
                    command,
                    flags,
                    meterId,
                    meters
                );
        }

    }

    static class Builder implements OFMeterMod.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean commandSet;
        private int command;
        private boolean flagsSet;
        private int flags;
        private boolean meterIdSet;
        private long meterId;
        private boolean metersSet;
        private List<OFMeterBand> meters;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

    @Override
    public OFType getType() {
        return OFType.METER_MOD;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFMeterMod.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public int getCommand() {
        return command;
    }

    @Override
    public OFMeterMod.Builder setCommand(int command) {
        this.command = command;
        this.commandSet = true;
        return this;
    }
    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public OFMeterMod.Builder setFlags(int flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }
    @Override
    public long getMeterId() {
        return meterId;
    }

    @Override
    public OFMeterMod.Builder setMeterId(long meterId) {
        this.meterId = meterId;
        this.meterIdSet = true;
        return this;
    }
    @Override
    public List<OFMeterBand> getMeters() {
        return meters;
    }

    @Override
    public OFMeterMod.Builder setMeters(List<OFMeterBand> meters) {
        this.meters = meters;
        this.metersSet = true;
        return this;
    }
    @Override
    public List<OFMeterBand> getBands()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property bands not supported in version 1.3");
    }

    @Override
    public OFMeterMod.Builder setBands(List<OFMeterBand> bands) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property bands not supported in version 1.3");
    }
//
        @Override
        public OFMeterMod build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            int command = this.commandSet ? this.command : DEFAULT_COMMAND;
            int flags = this.flagsSet ? this.flags : DEFAULT_FLAGS;
            long meterId = this.meterIdSet ? this.meterId : DEFAULT_METER_ID;
            List<OFMeterBand> meters = this.metersSet ? this.meters : DEFAULT_METERS;
            if(meters == null)
                throw new NullPointerException("Property meters must not be null");


            return new OFMeterModVer13(
                    xid,
                    command,
                    flags,
                    meterId,
                    meters
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFMeterMod> {
        @Override
        public OFMeterMod readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 4
            byte version = bb.readByte();
            if(version != (byte) 0x4)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_13(4), got="+version);
            // fixed value property type == 29
            byte type = bb.readByte();
            if(type != (byte) 0x1d)
                throw new OFParseError("Wrong type: Expected=OFType.METER_MOD(29), got="+type);
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
            int command = U16.f(bb.readShort());
            int flags = U16.f(bb.readShort());
            long meterId = U32.f(bb.readInt());
            List<OFMeterBand> meters = ChannelUtils.readList(bb, length - (bb.readerIndex() - start), OFMeterBandVer13.READER);

            OFMeterModVer13 meterModVer13 = new OFMeterModVer13(
                    xid,
                      command,
                      flags,
                      meterId,
                      meters
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", meterModVer13);
            return meterModVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFMeterModVer13Funnel FUNNEL = new OFMeterModVer13Funnel();
    static class OFMeterModVer13Funnel implements Funnel<OFMeterModVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFMeterModVer13 message, PrimitiveSink sink) {
            // fixed value property version = 4
            sink.putByte((byte) 0x4);
            // fixed value property type = 29
            sink.putByte((byte) 0x1d);
            // FIXME: skip funnel of length
            sink.putLong(message.xid);
            sink.putInt(message.command);
            sink.putInt(message.flags);
            sink.putLong(message.meterId);
            FunnelUtils.putList(message.meters, sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFMeterModVer13> {
        @Override
        public void write(ChannelBuffer bb, OFMeterModVer13 message) {
            int startIndex = bb.writerIndex();
            // fixed value property version = 4
            bb.writeByte((byte) 0x4);
            // fixed value property type = 29
            bb.writeByte((byte) 0x1d);
            // length is length of variable message, will be updated at the end
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            bb.writeInt(U32.t(message.xid));
            bb.writeShort(U16.t(message.command));
            bb.writeShort(U16.t(message.flags));
            bb.writeInt(U32.t(message.meterId));
            ChannelUtils.writeList(bb, message.meters);

            // update length field
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);

        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFMeterModVer13(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("command=").append(command);
        b.append(", ");
        b.append("flags=").append(flags);
        b.append(", ");
        b.append("meterId=").append(meterId);
        b.append(", ");
        b.append("meters=").append(meters);
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
        OFMeterModVer13 other = (OFMeterModVer13) obj;

        if( xid != other.xid)
            return false;
        if( command != other.command)
            return false;
        if( flags != other.flags)
            return false;
        if( meterId != other.meterId)
            return false;
        if (meters == null) {
            if (other.meters != null)
                return false;
        } else if (!meters.equals(other.meters))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + command;
        result = prime * result + flags;
        result = prime *  (int) (meterId ^ (meterId >>> 32));
        result = prime * result + ((meters == null) ? 0 : meters.hashCode());
        return result;
    }

}

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

class OFBsnTableChecksumStatsEntryVer13 implements OFBsnTableChecksumStatsEntry {
    private static final Logger logger = LoggerFactory.getLogger(OFBsnTableChecksumStatsEntryVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 9;

        private final static TableId DEFAULT_TABLE_ID = TableId.ALL;
        private final static U64 DEFAULT_CHECKSUM = U64.ZERO;

    // OF message fields
    private final TableId tableId;
    private final U64 checksum;
//
    // Immutable default instance
    final static OFBsnTableChecksumStatsEntryVer13 DEFAULT = new OFBsnTableChecksumStatsEntryVer13(
        DEFAULT_TABLE_ID, DEFAULT_CHECKSUM
    );

    // package private constructor - used by readers, builders, and factory
    OFBsnTableChecksumStatsEntryVer13(TableId tableId, U64 checksum) {
        if(tableId == null) {
            throw new NullPointerException("OFBsnTableChecksumStatsEntryVer13: property tableId cannot be null");
        }
        if(checksum == null) {
            throw new NullPointerException("OFBsnTableChecksumStatsEntryVer13: property checksum cannot be null");
        }
        this.tableId = tableId;
        this.checksum = checksum;
    }

    // Accessors for OF message fields
    @Override
    public TableId getTableId() {
        return tableId;
    }

    @Override
    public U64 getChecksum() {
        return checksum;
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



    public OFBsnTableChecksumStatsEntry.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBsnTableChecksumStatsEntry.Builder {
        final OFBsnTableChecksumStatsEntryVer13 parentMessage;

        // OF message fields
        private boolean tableIdSet;
        private TableId tableId;
        private boolean checksumSet;
        private U64 checksum;

        BuilderWithParent(OFBsnTableChecksumStatsEntryVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public TableId getTableId() {
        return tableId;
    }

    @Override
    public OFBsnTableChecksumStatsEntry.Builder setTableId(TableId tableId) {
        this.tableId = tableId;
        this.tableIdSet = true;
        return this;
    }
    @Override
    public U64 getChecksum() {
        return checksum;
    }

    @Override
    public OFBsnTableChecksumStatsEntry.Builder setChecksum(U64 checksum) {
        this.checksum = checksum;
        this.checksumSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



        @Override
        public OFBsnTableChecksumStatsEntry build() {
                TableId tableId = this.tableIdSet ? this.tableId : parentMessage.tableId;
                if(tableId == null)
                    throw new NullPointerException("Property tableId must not be null");
                U64 checksum = this.checksumSet ? this.checksum : parentMessage.checksum;
                if(checksum == null)
                    throw new NullPointerException("Property checksum must not be null");

                //
                return new OFBsnTableChecksumStatsEntryVer13(
                    tableId,
                    checksum
                );
        }

    }

    static class Builder implements OFBsnTableChecksumStatsEntry.Builder {
        // OF message fields
        private boolean tableIdSet;
        private TableId tableId;
        private boolean checksumSet;
        private U64 checksum;

    @Override
    public TableId getTableId() {
        return tableId;
    }

    @Override
    public OFBsnTableChecksumStatsEntry.Builder setTableId(TableId tableId) {
        this.tableId = tableId;
        this.tableIdSet = true;
        return this;
    }
    @Override
    public U64 getChecksum() {
        return checksum;
    }

    @Override
    public OFBsnTableChecksumStatsEntry.Builder setChecksum(U64 checksum) {
        this.checksum = checksum;
        this.checksumSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

//
        @Override
        public OFBsnTableChecksumStatsEntry build() {
            TableId tableId = this.tableIdSet ? this.tableId : DEFAULT_TABLE_ID;
            if(tableId == null)
                throw new NullPointerException("Property tableId must not be null");
            U64 checksum = this.checksumSet ? this.checksum : DEFAULT_CHECKSUM;
            if(checksum == null)
                throw new NullPointerException("Property checksum must not be null");


            return new OFBsnTableChecksumStatsEntryVer13(
                    tableId,
                    checksum
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBsnTableChecksumStatsEntry> {
        @Override
        public OFBsnTableChecksumStatsEntry readFrom(ChannelBuffer bb) throws OFParseError {
            TableId tableId = TableId.readByte(bb);
            U64 checksum = U64.ofRaw(bb.readLong());

            OFBsnTableChecksumStatsEntryVer13 bsnTableChecksumStatsEntryVer13 = new OFBsnTableChecksumStatsEntryVer13(
                    tableId,
                      checksum
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bsnTableChecksumStatsEntryVer13);
            return bsnTableChecksumStatsEntryVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBsnTableChecksumStatsEntryVer13Funnel FUNNEL = new OFBsnTableChecksumStatsEntryVer13Funnel();
    static class OFBsnTableChecksumStatsEntryVer13Funnel implements Funnel<OFBsnTableChecksumStatsEntryVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBsnTableChecksumStatsEntryVer13 message, PrimitiveSink sink) {
            message.tableId.putTo(sink);
            message.checksum.putTo(sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBsnTableChecksumStatsEntryVer13> {
        @Override
        public void write(ChannelBuffer bb, OFBsnTableChecksumStatsEntryVer13 message) {
            message.tableId.writeByte(bb);
            bb.writeLong(message.checksum.getValue());


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBsnTableChecksumStatsEntryVer13(");
        b.append("tableId=").append(tableId);
        b.append(", ");
        b.append("checksum=").append(checksum);
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
        OFBsnTableChecksumStatsEntryVer13 other = (OFBsnTableChecksumStatsEntryVer13) obj;

        if (tableId == null) {
            if (other.tableId != null)
                return false;
        } else if (!tableId.equals(other.tableId))
            return false;
        if (checksum == null) {
            if (other.checksum != null)
                return false;
        } else if (!checksum.equals(other.checksum))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        return result;
    }

}
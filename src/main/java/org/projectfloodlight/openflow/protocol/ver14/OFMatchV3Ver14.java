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
import java.util.List;
import java.util.Iterator;
import com.google.common.collect.AbstractIterator;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFMatchV3Ver14 implements OFMatchV3 {
    private static final Logger logger = LoggerFactory.getLogger(OFMatchV3Ver14.class);
    // version: 1.4
    final static byte WIRE_VERSION = 5;
    final static int MINIMUM_LENGTH = 4;

        private final static OFOxmList DEFAULT_OXM_LIST = OFOxmList.EMPTY;

    // OF message fields
    private final OFOxmList oxmList;
//
    // Immutable default instance
    final static OFMatchV3Ver14 DEFAULT = new OFMatchV3Ver14(
        DEFAULT_OXM_LIST
    );

    // package private constructor - used by readers, builders, and factory
    OFMatchV3Ver14(OFOxmList oxmList) {
        if(oxmList == null) {
            throw new NullPointerException("OFMatchV3Ver14: property oxmList cannot be null");
        }
        this.oxmList = oxmList;
    }

    // Accessors for OF message fields
    @Override
    public int getType() {
        return 0x1;
    }

    @Override
    public OFOxmList getOxmList() {
        return oxmList;
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }


    @Override
    public <F extends OFValueType<F>> F get(MatchField<F> field)
            throws UnsupportedOperationException {
        if (!supports(field))
            throw new UnsupportedOperationException("OFMatchV3Ver14 does not support matching on field " + field.getName());

        OFOxm<F> oxm = this.oxmList.get(field);

        if (oxm == null || !field.arePrerequisitesOK(this))
            return null;

        return oxm.getValue();
    }

    @Override
    public <F extends OFValueType<F>> Masked<F> getMasked(MatchField<F> field)
            throws UnsupportedOperationException {
        if (!supportsMasked(field))
            throw new UnsupportedOperationException("OFMatchV3Ver14 does not support masked matching on field " + field.getName());

        OFOxm<F> oxm = this.oxmList.get(field);

        if (oxm == null || !field.arePrerequisitesOK(this))
            return null;

        if (oxm.getMask() == null)
            return null;

        // TODO: Make OfOxm extend Masked and just return the OXM?
        return Masked.of(oxm.getValue(), oxm.getMask());
    }

    private static boolean supportsField(MatchField<?> field) {
        switch (field.id) {
            case ARP_OP:
            case ARP_SHA:
            case ARP_SPA:
            case ARP_THA:
            case ARP_TPA:
            case BSN_EGR_PORT_GROUP_ID:
            case BSN_GLOBAL_VRF_ALLOWED:
            case BSN_INGRESS_PORT_GROUP_ID:
            case BSN_INNER_ETH_DST:
            case BSN_INNER_ETH_SRC:
            case BSN_IN_PORTS_128:
            case BSN_IN_PORTS_512:
            case BSN_L2_CACHE_HIT:
            case BSN_L3_DST_CLASS_ID:
            case BSN_L3_INTERFACE_CLASS_ID:
            case BSN_L3_SRC_CLASS_ID:
            case BSN_LAG_ID:
            case BSN_TCP_FLAGS:
            case BSN_UDF0:
            case BSN_UDF1:
            case BSN_UDF2:
            case BSN_UDF3:
            case BSN_UDF4:
            case BSN_UDF5:
            case BSN_UDF6:
            case BSN_UDF7:
            case BSN_VLAN_XLATE_PORT_GROUP_ID:
            case BSN_VRF:
            case BSN_VXLAN_NETWORK_ID:
            case ETH_DST:
            case ETH_SRC:
            case ETH_TYPE:
            case ICMPV4_CODE:
            case ICMPV4_TYPE:
            case ICMPV6_CODE:
            case ICMPV6_TYPE:
            case IN_PHY_PORT:
            case IN_PORT:
            case IPV4_DST:
            case IPV4_SRC:
            case IPV6_DST:
            case IPV6_EXTHDR:
            case IPV6_FLABEL:
            case IPV6_ND_SLL:
            case IPV6_ND_TARGET:
            case IPV6_ND_TLL:
            case IPV6_SRC:
            case IP_DSCP:
            case IP_ECN:
            case IP_PROTO:
            case METADATA:
            case MPLS_BOS:
            case MPLS_LABEL:
            case MPLS_TC:
            case OCH_SIGID:
            case OCH_SIGID_BASIC:
            case OCH_SIGTYPE:
            case OCH_SIGTYPE_BASIC:
            case PBB_UCA:
            case SCTP_DST:
            case SCTP_SRC:
            case TCP_DST:
            case TCP_SRC:
            case TUNNEL_ID:
            case TUNNEL_IPV4_DST:
            case TUNNEL_IPV4_SRC:
            case UDP_DST:
            case UDP_SRC:
            case VLAN_PCP:
            case VLAN_VID:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean supports(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean supportsMasked(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean isExact(MatchField<?> field) {
        if (!supports(field))
            throw new UnsupportedOperationException("OFMatchV3Ver14 does not support matching on field " + field.getName());

        OFOxm<?> oxm = this.oxmList.get(field);

        return oxm != null && !oxm.isMasked();
    }

    @Override
    public boolean isFullyWildcarded(MatchField<?> field) {
        if (!supports(field))
            throw new UnsupportedOperationException("OFMatchV3Ver14 does not support matching on field " + field.getName());

        OFOxm<?> oxm = this.oxmList.get(field);

        return oxm == null;
    }

    @Override
    public boolean isPartiallyMasked(MatchField<?> field) {
        if (!supports(field))
            throw new UnsupportedOperationException("OFMatchV3Ver14 does not support matching on field " + field.getName());

        OFOxm<?> oxm = this.oxmList.get(field);

        return oxm != null && oxm.isMasked();
    }

    private class MatchFieldIterator extends AbstractIterator<MatchField<?>> {
        private Iterator<OFOxm<?>> oxmIterator;

        MatchFieldIterator() {
            oxmIterator = oxmList.iterator();
        }

        @Override
        protected MatchField<?> computeNext() {
            while(oxmIterator.hasNext()) {
                OFOxm<?> oxm = oxmIterator.next();
                if(oxm.getMatchField().arePrerequisitesOK(OFMatchV3Ver14.this))
                   return oxm.getMatchField();
            }
            endOfData();
            return null;
        }
    }

    @Override
    public Iterable<MatchField<?>> getMatchFields() {
        return new Iterable<MatchField<?>>() {
            public Iterator<MatchField<?>> iterator() {
                return new MatchFieldIterator();
            }
        };
    }

    public OFMatchV3.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFMatchV3.Builder {
        final OFMatchV3Ver14 parentMessage;

        // OF message fields
        private boolean oxmListSet;
        private OFOxmList oxmList;

        BuilderWithParent(OFMatchV3Ver14 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public int getType() {
        return 0x1;
    }

    @Override
    public OFOxmList getOxmList() {
        return oxmList;
    }

    @Override
    public OFMatchV3.Builder setOxmList(OFOxmList oxmList) {
        this.oxmList = oxmList;
        this.oxmListSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }



        @Override
        public OFMatchV3 build() {
                OFOxmList oxmList = this.oxmListSet ? this.oxmList : parentMessage.oxmList;
                if(oxmList == null)
                    throw new NullPointerException("Property oxmList must not be null");

                //
                return new OFMatchV3Ver14(
                    oxmList
                );
        }

    private OFOxmList.Builder oxmListBuilder;

    private void initBuilder() {
        if (oxmListBuilder != null)
            return;
        oxmListBuilder = new OFOxmList.Builder();
    }

    private void updateOxmList() {
        this.oxmList = this.oxmListBuilder.build();
        this.oxmListSet = true;
    }

    private <F extends OFValueType<F>> OFOxm<F> getOxm(MatchField<F> field) {
        return this.oxmListSet ? this.oxmList.get(field) : null;
    }

    @Override
    public <F extends OFValueType<F>> F get(MatchField<F> field)
            throws UnsupportedOperationException {
        OFOxm<F> value = getOxm(field);
        if (value == null)
            return null;
        return value.getValue();
    }

    @Override
    public <F extends OFValueType<F>> Masked<F> getMasked(MatchField<F> field)
            throws UnsupportedOperationException {
        OFOxm<F> value = getOxm(field);
        if (value == null || !value.isMasked())
            return null;
        // TODO: If changing OXMs to extend Masked, then use it here
        return Masked.of(value.getValue(), value.getMask());
    }

    @Override
    public boolean supports(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean supportsMasked(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean isExact(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value != null && !value.isMasked());
    }

    @Override
    public boolean isFullyWildcarded(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value == null);
    }

    @Override
    public boolean isPartiallyMasked(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value != null && value.isMasked());
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setExact(
            MatchField<F> field, F value) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromValue(value, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setMasked(
            MatchField<F> field, F value, F mask) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromValueAndMask(value, mask, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setMasked(
            MatchField<F> field, Masked<F> valueWithMask) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromMasked(valueWithMask, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder wildcard(MatchField<F> field) {
        initBuilder();
        this.oxmListBuilder.unset(field);
        updateOxmList();
        return this;
    }

    }

    static class Builder implements OFMatchV3.Builder {
        // OF message fields
        private boolean oxmListSet;
        private OFOxmList oxmList;

    @Override
    public int getType() {
        return 0x1;
    }

    @Override
    public OFOxmList getOxmList() {
        return oxmList;
    }

    @Override
    public OFMatchV3.Builder setOxmList(OFOxmList oxmList) {
        this.oxmList = oxmList;
        this.oxmListSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }

//
        @Override
        public OFMatchV3 build() {
            OFOxmList oxmList = this.oxmListSet ? this.oxmList : DEFAULT_OXM_LIST;
            if(oxmList == null)
                throw new NullPointerException("Property oxmList must not be null");


            return new OFMatchV3Ver14(
                    oxmList
                );
        }

    private OFOxmList.Builder oxmListBuilder;

    private void initBuilder() {
        if (oxmListBuilder != null)
            return;
        oxmListBuilder = new OFOxmList.Builder();
    }

    private void updateOxmList() {
        this.oxmList = this.oxmListBuilder.build();
        this.oxmListSet = true;
    }

    private <F extends OFValueType<F>> OFOxm<F> getOxm(MatchField<F> field) {
        return this.oxmListSet ? this.oxmList.get(field) : null;
    }

    @Override
    public <F extends OFValueType<F>> F get(MatchField<F> field)
            throws UnsupportedOperationException {
        OFOxm<F> value = getOxm(field);
        if (value == null)
            return null;
        return value.getValue();
    }

    @Override
    public <F extends OFValueType<F>> Masked<F> getMasked(MatchField<F> field)
            throws UnsupportedOperationException {
        OFOxm<F> value = getOxm(field);
        if (value == null || !value.isMasked())
            return null;
        // TODO: If changing OXMs to extend Masked, then use it here
        return Masked.of(value.getValue(), value.getMask());
    }

    @Override
    public boolean supports(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean supportsMasked(MatchField<?> field) {
        return supportsField(field);
    }

    @Override
    public boolean isExact(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value != null && !value.isMasked());
    }

    @Override
    public boolean isFullyWildcarded(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value == null);
    }

    @Override
    public boolean isPartiallyMasked(MatchField<?> field) {
        OFOxm<?> value = getOxm(field);
        return (value != null && value.isMasked());
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setExact(
            MatchField<F> field, F value) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromValue(value, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setMasked(
            MatchField<F> field, F value, F mask) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromValueAndMask(value, mask, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder setMasked(
            MatchField<F> field, Masked<F> valueWithMask) {
        initBuilder();
        OFOxm<F> oxm = OFFactories.getFactory(OFVersion.OF_14).oxms().fromMasked(valueWithMask, field);
        this.oxmListBuilder.set(oxm);
        updateOxmList();
        return this;
    }

    @Override
    public <F extends OFValueType<F>> Match.Builder wildcard(MatchField<F> field) {
        initBuilder();
        this.oxmListBuilder.unset(field);
        updateOxmList();
        return this;
    }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFMatchV3> {
        @Override
        public OFMatchV3 readFrom(ChannelBuffer bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property type == 0x1
            short type = bb.readShort();
            if(type != (short) 0x1)
                throw new OFParseError("Wrong type: Expected=0x1(0x1), got="+type);
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
            OFOxmList oxmList = OFOxmList.readFrom(bb, length - (bb.readerIndex() - start), OFOxmVer14.READER);
            // align message to 8 bytes (length does not contain alignment)
            bb.skipBytes(((length + 7)/8 * 8 ) - length );

            OFMatchV3Ver14 matchV3Ver14 = new OFMatchV3Ver14(
                    oxmList
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", matchV3Ver14);
            return matchV3Ver14;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFMatchV3Ver14Funnel FUNNEL = new OFMatchV3Ver14Funnel();
    static class OFMatchV3Ver14Funnel implements Funnel<OFMatchV3Ver14> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFMatchV3Ver14 message, PrimitiveSink sink) {
            // fixed value property type = 0x1
            sink.putShort((short) 0x1);
            // FIXME: skip funnel of length
            message.oxmList.putTo(sink);
        }
    }


    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFMatchV3Ver14> {
        @Override
        public void write(ChannelBuffer bb, OFMatchV3Ver14 message) {
            int startIndex = bb.writerIndex();
            // fixed value property type = 0x1
            bb.writeShort((short) 0x1);
            // length is length of variable message, will be updated at the end
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            message.oxmList.writeTo(bb);

            // update length field
            int length = bb.writerIndex() - startIndex;
            int alignedLength = ((length + 7)/8 * 8);
            bb.setShort(lengthIndex, length);
            // align message to 8 bytes
            bb.writeZero(alignedLength - length);

        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFMatchV3Ver14(");
        boolean first = true;
        for(MatchField<?> field : getMatchFields()) {
            if(first)
                first = false;
            else
                b.append(", ");
            String name = field.getName();
            b.append(name).append('=').append(this.get(field));
            if(isPartiallyMasked(field)) {
                b.append('/').append(this.getMasked(field).getMask());
            }
        }
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
        OFMatchV3Ver14 other = (OFMatchV3Ver14) obj;

        if (oxmList == null) {
            if (other.oxmList != null)
                return false;
        } else if (!oxmList.equals(other.oxmList))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((oxmList == null) ? 0 : oxmList.hashCode());
        return result;
    }

}

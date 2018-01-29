/*******************************************************************************
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.onrc.openvirtex.protocol;

import java.util.HashMap;

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerDestinationVer10;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerSourceVer10;

import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.ver10.OFMatchV1Ver10;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.util.HexString;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.types.U8;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanPcp;

/**
 * The Class OVXMatch. This class extends the OFMatchV1 class, in order to carry
 * some useful informations for OpenVirteX, as the cookie (used by flowMods
 * messages) and the packet data (used by packetOut messages)
 */
public class OVXMatch extends OFMatchV1Ver10 {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The cookie. */
    protected long cookie=0;

    /** The pkt data. */
    protected byte[] pktData=null;

    /**
     * Instantiates a new void OVXatch.
     */
    public OVXMatch(int wildcards, OFPort inPort, MacAddress ethSrc, MacAddress ethDst, OFVlanVidMatch vlanVid, VlanPcp vlanPcp, EthType ethType, IpDscp ipDscp, IpProtocol ipProto, IPv4Address ipv4Src, IPv4Address ipv4Dst, TransportPort tcpSrc, TransportPort tcpDst,long cookie,byte[] pktData) 
    {
        super(wildcards, inPort, ethSrc, ethDst, vlanVid, vlanPcp, ethType, ipDscp, ipProto, ipv4Src, ipv4Dst, tcpDst, tcpDst);
        this.cookie = cookie;
        this.pktData = pktData;
    }

    /**
     * Instantiates a new OVXmatch from an OFMatchV1 instance.
     *
     * @param match
     *            the match
     */
    public OVXMatch(final OFMatchV1 match) {
    	this(match.getWildcards(), match.getInPort(), match.getEthSrc(), match.getEthDst(), match.getVlanVid(), match.getVlanPcp(), match.getEthType(), match.getIpDscp(), match.getIpProto(), match.getIpv4Src(), match.getIpv4Dst(), match.getTcpDst(), match.getTcpDst(),0,null);
        
    }



    /**
     * Get cookie.
     *
     * @return the cookie
     */
    public long getCookie() {
        return this.cookie;
    }

    /**
     * Set cookie.
     *
     * @param cookie
     *            the cookie
     * @return the oVX match
     */
    public OVXMatch setCookie(final long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * Gets the pkt data.
     *
     * @return the pkt data
     */
    public byte[] getPktData() {
        return this.pktData;
    }

    /**
     * Sets the pkt data.
     *
     * @param pktData
     *            the new pkt data
     */
    public void setPktData(final byte[] pktData) {
        this.pktData = pktData;
    }

    /**
     * Checks if this match belongs to a flow mod (e.g. the cookie is not zero).
     *
     * @return true, if is flow mod
     */
    public boolean isFlowMod() {
        return this.cookie != 0;
    }

    /**
     * Checks if this match belongs to a packet out (e.g. the packet data is not
     * null).
     *
     * @return true, if is packet out
     */
    public boolean isPacketOut() {
        return this.pktData != null;
    }

    public static class CIDRToIP {
        public static String cidrToString(final int ip, final int prefix) {
            String str;
            if (prefix >= 32) {
                str = IPv4Address.of(ip).toString();
            } else {
                // use the negation of mask to fake endian magic
                final int mask = ~((1 << 32 - prefix) - 1);
                str = IPv4Address.of(ip & mask).toString() + "/" + prefix;
            }

            return str;
        }
    }
    /* List of Strings for marshalling and unmarshalling to human readable forms */
    final public static String STR_IN_PORT = "in_port";
    final public static String STR_DL_DST = "dl_dst";
    final public static String STR_DL_SRC = "dl_src";
    final public static String STR_DL_TYPE = "dl_type";
    final public static String STR_DL_VLAN = "dl_vlan";
    final public static String STR_DL_VLAN_PCP = "dl_vlan_pcp";
    final public static String STR_NW_DST = "nw_dst";
    final public static String STR_NW_SRC = "nw_src";
    final public static String STR_NW_PROTO = "nw_proto";
    final public static String STR_NW_TOS = "nw_tos";
    final public static String STR_TP_DST = "tp_dst";
    final public static String STR_TP_SRC = "tp_src";
    
    public HashMap<String, Object> toMap() {

        final HashMap<String, Object> ret = new HashMap<String, Object>();

        ret.put("wildcards", this.getWildcards());

        // l1
        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_IN_PORT) == 0) {
            ret.put(STR_IN_PORT, U16.f(this.getInPort().getShortPortNumber()));
        }

        // l2
        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_DL_DST) == 0) {
            ret.put(STR_DL_DST,
                    HexString.toHexString(this.getEthDst().getBytes()));
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_DL_SRC) == 0) {
            ret.put(STR_DL_SRC,
                    HexString.toHexString(this.getEthSrc().getBytes()));
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_DL_TYPE) == 0) {
            ret.put(STR_DL_TYPE, this.getEthType().getValue());
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_DL_VLAN) == 0) {
            ret.put(STR_DL_VLAN, U16.f(this.getVlanVid().getRawVid()));
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_DL_VLAN_PCP) == 0) {
            ret.put(STR_DL_VLAN_PCP,
                    U8.f(this.getVlanPcp().getValue()));
        }

        // l3
        if (this.getIpv4DstCidrMaskLen() > 0) {
            ret.put(STR_NW_DST,
                    CIDRToIP.cidrToString(this.getIpv4Dst().getInt(),
                            this.getIpv4DstCidrMaskLen()));
        }

        if (this.getIpv4DstCidrMaskLen() > 0) {
            ret.put(STR_NW_SRC,
                    CIDRToIP.cidrToString(this.getIpv4Src().getInt(),
                            this.getIpv4SrcCidrMaskLen()));
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_NW_PROTO) == 0) {
            ret.put(STR_NW_PROTO, this.getIpProto());
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_NW_TOS) == 0) {
            ret.put(STR_NW_TOS, this.getIpDscp());
        }

        // l4
        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_TP_DST) == 0) {
            ret.put(STR_TP_DST, this.getTcpDst());
        }

        if ((this.getWildcards() & OFMatchV1Ver10.OFPFW_TP_SRC) == 0) {
            ret.put(STR_TP_SRC, this.getTcpSrc());
        }

        return ret;
    }

	/**
	 * Return an OFAction associated with nw_src
	 *
	 * @param tenantId
	 * @return OFAction or null
	 */
	public OFAction getNetworkSrcAction(int tenantId) {
		OVXActionNetworkLayerSourceVer10 srcAct = null;
		if (!this.isFullyWildcarded(MatchField.IPV4_SRC)) {
			srcAct = new OVXActionNetworkLayerSourceVer10(IPv4Address.of(IPMapper.getPhysicalIp(tenantId, this.getIpv4Src().getInt())));
		}
		return srcAct;
	}

	/**
	 * Return an OFAction associated with nw_dst
	 *
	 * @param tenantId
	 * @return OFAction or null
	 */
	public OFAction getNetworkDstAction(int tenantId) {
		OVXActionNetworkLayerDestinationVer10 dstAct = null;
		if (!this.isFullyWildcarded(MatchField.IPV4_DST)) {
			dstAct = new OVXActionNetworkLayerDestinationVer10(IPv4Address.of(IPMapper.getPhysicalIp(tenantId, this.getIpv4Dst().getInt())));
		}
		return dstAct;
	}
}

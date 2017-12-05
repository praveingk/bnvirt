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

import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.messages.actions.OVXActionSetField;
import net.onrc.openvirtex.messages.actions.ver13.OVXActionSetFieldVer13;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Dst;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpv4Src;
import org.projectfloodlight.openflow.protocol.ver13.OFMatchV3Ver13;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmIpv4DstVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmIpv4SrcVer13;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.util.HexString;

import java.util.HashMap;

/**
 * The Class OVXMatchV3. This class extends the OFMatchV3 class, in order to carry
 * some useful informations for OpenVirteX, as the cookie (used by flowMods
 * messages) and the packet data (used by packetOut messages)
 */
public class OVXMatchV3 extends OFMatchV3Ver13 {

    /** The cookie. */
    protected long cookie=0;

    /** The pkt data. */
    protected byte[] pktData=null;

    /**
     * Instantiates a new void OVXatch.
     */
    public OVXMatchV3(OFOxmList oxmList, long cookie, byte[] pktData)
    {
        super(oxmList);
        this.cookie = cookie;
        this.pktData = pktData;
    }

    /**
     * Instantiates a new OVXmatch from an OFMatchV3 instance.
     *
     * @param match
     *            the match
     */
    public OVXMatchV3(final OFMatchV3 match) {
    	this(match.getOxmList(),0,null);

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
    public OVXMatchV3 setCookie(final long cookie) {
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

    public HashMap<String, Object> toMap() {

        final HashMap<String, Object> ret = new HashMap<String, Object>();

        for (MatchField mf : this.getMatchFields()) {
            ret.put(mf.getName(), this.get(mf));
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
		//OVXActionNetworkLayerSourceVer13 srcAct = null;
        OVXActionSetField srcAct = null;
		if (!this.isFullyWildcarded(MatchField.IPV4_SRC)) {
            OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(IPMapper.getPhysicalIp(tenantId, this.get(MatchField.IPV4_SRC).getInt())));
            srcAct = new OVXActionSetFieldVer13(oxmIpv4Src);

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
        OVXActionSetField dstAct = null;
        if (!this.isFullyWildcarded(MatchField.IPV4_DST)) {
            OFOxmIpv4Dst oxmIpv4Dst = new OFOxmIpv4DstVer13(IPv4Address.of(IPMapper.getPhysicalIp(tenantId, this.get(MatchField.IPV4_DST).getInt())));
            dstAct = new OVXActionSetFieldVer13(oxmIpv4Dst);
		}
		return dstAct;
	}

}

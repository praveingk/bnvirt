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
package net.onrc.openvirtex.elements.address;

import java.util.*;

import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.messages.actions.OVXActionSetField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmEthSrcVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmInPortVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmIpv4DstVer13;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmIpv4SrcVer13;
import org.projectfloodlight.openflow.types.IPv4Address;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerDestinationVer10;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionNetworkLayerSourceVer10;

/**
 * Utility class for IP mapping operations. Implements methods
 * rewrite or add actions for IP translation.
 */
public final class IPMapper {
    private static Logger log = LogManager.getLogger(IPMapper.class.getName());

    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private IPMapper() {
    }

    public static Integer getPhysicalIp(Integer tenantId, Integer virtualIP) {
        final Mappable map = OVXMap.getInstance();
        final OVXIPAddress vip = new OVXIPAddress(tenantId, virtualIP);
        try {
            PhysicalIPAddress pip;
            if (map.hasPhysicalIP(vip, tenantId)) {
                pip = map.getPhysicalIP(vip, tenantId);
            } else {
                pip = new PhysicalIPAddress(map.getVirtualNetwork(tenantId)
                        .nextIP());
                log.debug("Adding IP mapping {} -> {} for tenant {}", vip, pip,
                        tenantId);
                map.addIP(pip, vip);
            }
            return pip.getIp();
        } catch (IndexOutOfBoundException e) {
            log.error(
                    "No available physical IPs for virtual ip {} in tenant {}",
                    vip, tenantId);
        } catch (NetworkMappingException e) {
            log.error(e);
        } catch (AddressMappingException e) {
            log.error("Inconsistency in Physical-Virtual mapping : {}", e);
        }
        return 0;
    }

    public static Match rewriteMatch(final Integer tenantId, Match match) {
        if (OVXFactoryInst.ofversion == 10) {
            match = ((OFMatchV1) match).createBuilder()
                    .setIpv4Src(IPv4Address.of(getPhysicalIp(tenantId, ((OFMatchV1) match).getIpv4Src().getInt())))
                    .setIpv4Dst(IPv4Address.of(getPhysicalIp(tenantId, ((OFMatchV1) match).getIpv4Dst().getInt())))
                    .build();
        } else {
            OFMatchV3 newMatch;
            OFOxmList myList = ((OFMatchV3)match).getOxmList();
            if (match.get(MatchField.IPV4_SRC) != null && match.get(MatchField.IPV4_DST) != null) {
                OFOxmIpv4Src oxmIpSrc = new OFOxmIpv4SrcVer13(IPv4Address.of(getPhysicalIp(tenantId, match.get(MatchField.IPV4_SRC).getInt())));
                OFOxmIpv4Dst oxmIpDst = new OFOxmIpv4DstVer13(IPv4Address.of(getPhysicalIp(tenantId, match.get(MatchField.IPV4_DST).getInt())));
                Map<MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
                for (OFOxm<?> ofOxm : myList) {
                    if (ofOxm instanceof OFOxmIpv4Src) {
                        oxmMap.put(MatchFields.IPV4_SRC, oxmIpSrc);
                        continue;
                    }
                    if (ofOxm instanceof OFOxmIpv4Dst) {
                        oxmMap.put(MatchFields.IPV4_DST, oxmIpDst);
                        continue;
                    }
                    oxmMap.put(ofOxm.getMatchField().id, ofOxm);
                }
                OFOxmList oxmList = new OFOxmList(oxmMap);
                newMatch = OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();
                match = newMatch;
                return newMatch;
            }
        }
      return match;
    }

    public static List<OFAction> prependRewriteActions(final Integer tenantId,
            final Match match) {
        final List<OFAction> actions = new LinkedList<OFAction>();
        if (OVXFactoryInst.ofversion == 10) {
            if (!match.isFullyWildcarded(MatchField.IPV4_SRC)) {
                final OVXActionNetworkLayerSourceVer10 srcAct = new OVXActionNetworkLayerSourceVer10(IPv4Address.of(getPhysicalIp(tenantId,
                        ((OFMatchV1) match).getIpv4Src().getInt())));
                actions.add(srcAct);
            }
            if (!match.isFullyWildcarded(MatchField.IPV4_DST)) {
                final OVXActionNetworkLayerDestinationVer10 dstAct = new OVXActionNetworkLayerDestinationVer10(IPv4Address.of(getPhysicalIp(tenantId,
                        ((OFMatchV1) match).getIpv4Dst().getInt())));
                actions.add(dstAct);
            }
        } else {
            if (match.get(MatchField.IPV4_SRC) != null) {
                OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(IPMapper.getPhysicalIp(tenantId,
                        ((OFMatchV3)match).get(MatchField.IPV4_SRC).getInt())));
                final OVXActionSetField srcAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Src);
                actions.add(srcAct);
            }

            if (match.get(MatchField.IPV4_DST) != null) {
                OFOxmIpv4Dst oxmIpv4Dst = new OFOxmIpv4DstVer13(IPv4Address.of(IPMapper.getPhysicalIp(tenantId,
                        ((OFMatchV3)match).get(MatchField.IPV4_DST).getInt())));
                final OVXActionSetField dstAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Dst);
                actions.add(dstAct);
            }
        }
        return actions;
    }

    public static List<OFAction> prependUnRewriteActions(final Match match) {
        final List<OFAction> actions = new LinkedList<OFAction>();
        if (OVXFactoryInst.ofversion == 10) {
            if (!match.isFullyWildcarded(MatchField.IPV4_SRC)) {
                final OVXActionNetworkLayerSourceVer10 srcAct = new OVXActionNetworkLayerSourceVer10(((OFMatchV1) match).getIpv4Src());
                actions.add(srcAct);
            }
            if (!match.isFullyWildcarded(MatchField.IPV4_DST)) {
                final OVXActionNetworkLayerDestinationVer10 dstAct = new OVXActionNetworkLayerDestinationVer10(((OFMatchV1) match).getIpv4Dst());
                actions.add(dstAct);
            }
        } else {
            if (match.get(MatchField.IPV4_SRC) != null) {
                OFOxmIpv4Src oxmIpv4Src = new OFOxmIpv4SrcVer13(IPv4Address.of(((OFMatchV3)match).get(MatchField.IPV4_SRC).getInt()));
                final OVXActionSetField srcAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Src);
                actions.add(srcAct);
            }

            if (match.get(MatchField.IPV4_DST) != null) {
                OFOxmIpv4Dst oxmIpv4Dst = new OFOxmIpv4DstVer13(IPv4Address.of(((OFMatchV3)match).get(MatchField.IPV4_DST).getInt()));
                final OVXActionSetField dstAct = OVXFactoryInst.myOVXFactory.buildOVXActionSetField(oxmIpv4Dst);
                actions.add(dstAct);
            }
        }

        return actions;
    }
}

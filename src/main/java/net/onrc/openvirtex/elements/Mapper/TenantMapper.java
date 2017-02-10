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
package net.onrc.openvirtex.elements.Mapper;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkTypeOfService;
import net.onrc.openvirtex.messages.actions.OVXActionStripVirtualLan;
import net.onrc.openvirtex.messages.actions.OVXActionVirtualLanIdentifier;
import net.onrc.openvirtex.packet.Ethernet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.Wildcards;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for IP mapping operations. Implements methods
 * rewrite or add actions for IP translation.
 */
public final class TenantMapper {
    private static Logger log = LogManager.getLogger(TenantMapper.class.getName());


    private static ConcurrentHashMap<OVXVlan, Short> OVXVlanMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Short, OVXVlan> ReverseOVXVlanMap = new ConcurrentHashMap<>();

    private static HashSet<Short> usedVlans = new HashSet<>();
    private static short startVlan = 2010;
    private static short endVlan = 4094;
    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private TenantMapper() {
    }

    public static synchronized short getPhysicalVlan(Integer tenantId, short vlan) {
        OVXVlan myOVXVlan = new OVXVlan(tenantId, vlan);
        System.out.println("Getting the phys vlan for "+ myOVXVlan.toString());
        if (OVXVlanMap.get(myOVXVlan) != null) {
            System.out.println("Returning existing vlan..");
            return OVXVlanMap.get(myOVXVlan);
        }

        short myPhysVlan = getFreeVlan();
        OVXVlanMap.put(myOVXVlan,myPhysVlan);
        ReverseOVXVlanMap.put(myPhysVlan, myOVXVlan);
        return myPhysVlan;
    }

    public static short getOrigVlan(short vlan) {
        return (short) ReverseOVXVlanMap.get(vlan).getVlan();
    }

    public static synchronized void clearTenantVlans(int tenantId) {
        Set<OVXVlan> ovxvlans = OVXVlanMap.keySet();
        for (OVXVlan ovxvlan : ovxvlans) {
            if (ovxvlan.getTenantId() == tenantId) {
                Short vlan = OVXVlanMap.get(ovxvlan);
                System.out.println("Removing vlan "+ vlan);
                usedVlans.remove(vlan);
                ReverseOVXVlanMap.remove(vlan);
                OVXVlanMap.remove(ovxvlan);
                System.out.println("Cleared Vlan tables of   tenant "+ tenantId);

            }
        }
    }

    public static synchronized short getFreeVlan() {
        for (short i=startVlan;i <= endVlan ;i++) {
            if (!usedVlans.contains(i)) {
                usedVlans.add(i);
                return i;
            } else {
                System.out.println("Used vlan : "+ i);
            }

        }
        return -1;
    }

    public static void rewriteMatch(final Integer tenantId, final OFMatch match) {
//        byte tos = tenantId.byteValue();
//        System.out.println("Pravein: Rewriting match.. setting tos to "+ tos);
//        int wcard = match.getWildcards()
//                & (~OFMatch.OFPFW_NW_TOS);
//        match.setWildcards(wcard);
//        match.setNetworkTypeOfService(tos);


    }


    public static void prependRewriteActions(final Integer tenantId, final OFMatch match, List<OFAction> approvedActions) {
//        final List<OFAction> actions = new LinkedList<OFAction>();
//        byte tos  = tenantId.byteValue();
//        final OVXActionNetworkTypeOfService ovtos = new OVXActionNetworkTypeOfService();
////        if (!match.getWildcardObj().isWildcarded(Wildcards.Flag.DL_VLAN)) {
////            vlan = match.getDataLayerVirtualLan();
////        }
//        System.out.println("Pravein: Rewriting Action.. to set tos to " + tenantId);
////
//        ovtos.setNetworkTypeOfService(tos);
//
////
//        approvedActions.add(0, ovtos);
//        System.out.println("Actions : "+ approvedActions.toString());
//        return actions;
    }


    public static void prependUnRewriteActions(final OFMatch match, List<OFAction> approvedActions) {


    }

}

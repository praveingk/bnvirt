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

import net.onrc.openvirtex.messages.actions.OVXActionStripVirtualLan;
import net.onrc.openvirtex.messages.actions.OVXActionVirtualLanIdentifier;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionStripVirtualLanVer10;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionVirtualLanIdentifierVer10;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.protocol.OVXMatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.projectfloodlight.openflow.protocol.OFActionType;
import org.projectfloodlight.openflow.protocol.OFFlowWildcards;
import org.projectfloodlight.openflow.protocol.OFMatchV1;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.VlanVid;

import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for IP mapping operations. Implements methods
 * rewrite or add actions for IP translation.
 */
public final class TenantMapperVlan {
    private static Logger log = LogManager.getLogger(TenantMapperVlan.class.getName());


    private static ConcurrentHashMap<OVXVlan, Short> OVXVlanMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Short, OVXVlan> ReverseOVXVlanMap = new ConcurrentHashMap<>();

    private static HashSet<Short> usedVlans = new HashSet<>();
    private static short startVlan = 2010;
    private static short endVlan = 4094;
    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private TenantMapperVlan() {
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
        dumpOVXVlanMap();
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
        dumpOVXVlanMap();
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

    public static void rewriteMatch(final Integer tenantId, OFMatchV1 match) {
        short vlan = 1;
        if (!match.isFullyWildcarded(MatchField.VLAN_VID)) {
            vlan  = match.getVlanVid().getVlan();
        }
        System.out.println("Pravein: Rewriting match.. setting vlan to "+getPhysicalVlan(tenantId, vlan));
        int wcard = match.getWildcards()
                & (~OVXMatch.OFPFW_DL_VLAN);

        match = match.createBuilder().setVlanVid(OFVlanVidMatch.ofVlan(getPhysicalVlan(tenantId, vlan))).setWildcards(wcard).build();

    }

    public static void rewriteMatch(final Integer tenantId, OFMatchV3 match) {
        short vlan = 1;
        // TODO: Fill this
//        if (!match.isFullyWildcarded(MatchField.VLAN_VID)) {
//            vlan  = match.getVlanVid().getVlan();
//        }
//        System.out.println("Pravein: Rewriting match.. setting vlan to "+getPhysicalVlan(tenantId, vlan));
//        int wcard = match.getWildcards()
//                & (~OVXMatch.OFPFW_DL_VLAN);
//
//        match = match.createBuilder().setVlanVid(OFVlanVidMatch.ofVlan(getPhysicalVlan(tenantId, vlan))).setWildcards(wcard).build();

    }
    public static void dumpOVXVlanMap() {
        Set<OVXVlan> ovxVlanSet = OVXVlanMap.keySet();
        System.out.println("OVX VLan Map :");
        for (OVXVlan ovxvlan : ovxVlanSet) {
            System.out.println(ovxvlan.toString() + " -> "+ OVXVlanMap.get(ovxvlan));
        }

    }

    public static void prependRewriteActions(final Integer tenantId, final OFMatchV1 match, List<OFAction> approvedActions) {
        boolean match_vlan = false;
        VlanVid vlan = VlanVid.ofVlan(1);
        int index = -1;
        int stripindex = -1;
        if (!match.isFullyWildcarded(MatchField.VLAN_VID)) {
            match_vlan = true;
        }
        System.out.println("Inside PrependRewriteActions .. Approved Actions so far :"+ approvedActions.toString());
        for (int i=0;i< approvedActions.size();i++) {
            OFAction action = approvedActions.get(i);
            if (action.getType() == OFActionType.SET_VLAN_VID) {
                OVXActionVirtualLanIdentifier existingVlan = (OVXActionVirtualLanIdentifier) action;
                vlan = existingVlan.getVlanVid();
                index = i;
                System.out.println("Found Vlan action at index "+ i+ " with vlan tag "+ vlan);
                break;
            }
            if (action.getType() == OFActionType.STRIP_VLAN) {
                vlan = VlanVid.ofVlan(1);
                stripindex = i;
                System.out.println("Found Strip Vlan at index "+ i);
            }
        }
        if (index > -1) {
            approvedActions.remove(index);
        }
        if (stripindex > -1) {
            approvedActions.remove(stripindex);
        }
        final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifierVer10(VlanVid.ofVlan(TenantMapperVlan.getPhysicalVlan(tenantId, vlan.getVlan())));

        approvedActions.add(0, ovlan);

        System.out.println("Pravein: Rewriting Action.. to set vlan ID "+ vlan+" to "+ TenantMapperVlan.getPhysicalVlan(tenantId, vlan.getVlan()));

    }

    public static void prependRewriteActions(final Integer tenantId, final OFMatchV3 match, List<OFAction> approvedActions) {
        boolean match_vlan = false;
        VlanVid vlan = VlanVid.ofVlan(1);
        int index = -1;
        int stripindex = -1;
        // TODO: Fill this
//        if (!match.isFullyWildcarded(MatchField.VLAN_VID)) {
//            match_vlan = true;
//        }
//        System.out.println("Inside PrependRewriteActions .. Approved Actions so far :"+ approvedActions.toString());
//        for (int i=0;i< approvedActions.size();i++) {
//            OFAction action = approvedActions.get(i);
//            if (action.getType() == OFActionType.SET_VLAN_VID) {
//                OVXActionVirtualLanIdentifier existingVlan = (OVXActionVirtualLanIdentifier) action;
//                vlan = existingVlan.getVlanVid();
//                index = i;
//                System.out.println("Found Vlan action at index "+ i+ " with vlan tag "+ vlan);
//                break;
//            }
//            if (action.getType() == OFActionType.STRIP_VLAN) {
//                vlan = VlanVid.ofVlan(1);
//                stripindex = i;
//                System.out.println("Found Strip Vlan at index "+ i);
//            }
//        }
//        if (index > -1) {
//            approvedActions.remove(index);
//        }
//        if (stripindex > -1) {
//            approvedActions.remove(stripindex);
//        }
//        final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifierVer10(VlanVid.ofVlan(TenantMapperVlan.getPhysicalVlan(tenantId, vlan.getVlan())));
//
//        approvedActions.add(0, ovlan);
//
//        System.out.println("Pravein: Rewriting Action.. to set vlan ID "+ vlan+" to "+ TenantMapperVlan.getPhysicalVlan(tenantId, vlan.getVlan()));

    }
    public static void prependUnRewriteActions(final OFMatchV1 match, List<OFAction> approvedActions) {
        short vlan = 1;
        boolean match_vlan = false;
        int index = -1;
        int stripindex = -1;
        if (!match.isFullyWildcarded((MatchField.VLAN_VID))) {

            vlan = match.getVlanVid().getVlan();
            System.out.println("Match : "+ match.toString());
            System.out.println("VLAN : "+  vlan);

            if (vlan == Ethernet.VLAN_UNTAGGED ) {
                /* Untagged. */
                /* Nothing */
            } else {
                match_vlan = true;
            }
        }

        System.out.println("Inside PrependUnRewriteActions .. Approved Actions so far :"+ approvedActions.toString());
        for (int i=0;i< approvedActions.size();i++) {
            OFAction action = approvedActions.get(i);
            if (action.getType() == OFActionType.SET_VLAN_VID) {
                OVXActionVirtualLanIdentifier existingVlan = (OVXActionVirtualLanIdentifier) action;
                vlan = existingVlan.getVlanVid().getVlan();
                index = i;
                System.out.println("Found Vlan action at index "+ i+ " with vlan tag "+ vlan);
                break;
            }
            if (action.getType() == OFActionType.STRIP_VLAN) {
                vlan = 1;
                stripindex = i;
                System.out.println("Found Strip Vlan at index "+ i);
            }
        }

        if (stripindex > -1) {
            /* Nothing to do..*/
            return;
        } else if (index > -1) {
            /* Again Nothing to do */
            return;
        } else if (match_vlan == true){
            /* We have a match, and no strip.. so maintain the vlan.. */
            System.out.println("Match_vlan is true...");
            //short origVlan = getOrigVlan(vlan);
            if (vlan != 1) {
                final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifierVer10(VlanVid.ofVlan(vlan));
                approvedActions.add(0, ovlan);
                System.out.println("Found a match of VLAN ID with no "+ vlan + " Hence, Changing to that vlan");
            } else {
                System.out.println("Found a match with VLAN ID of "+ 1 + "Stripping Vlan");
                final OVXActionStripVirtualLan vlanstrip = new OVXActionStripVirtualLanVer10();
                approvedActions.add(0, vlanstrip);
            }
        } else {
            System.out.println("UnRewriting Action.. Stripping vlan");
            final OVXActionStripVirtualLan vlanstrip = new   OVXActionStripVirtualLanVer10();
            approvedActions.add(0, vlanstrip);
        }

    }


    public static void prependUnRewriteActions(final OFMatchV3 match, List<OFAction> approvedActions) {
        short vlan = 1;
        boolean match_vlan = false;
        int index = -1;
        int stripindex = -1;
        // TODO: Fill this
//        if (!match.isFullyWildcarded((MatchField.VLAN_VID))) {
//
//            vlan = match.getVlanVid().getVlan();
//            System.out.println("Match : "+ match.toString());
//            System.out.println("VLAN : "+  vlan);
//
//            if (vlan == Ethernet.VLAN_UNTAGGED ) {
//                /* Untagged. */
//                /* Nothing */
//            } else {
//                match_vlan = true;
//            }
//        }
//
//        System.out.println("Inside PrependUnRewriteActions .. Approved Actions so far :"+ approvedActions.toString());
//        for (int i=0;i< approvedActions.size();i++) {
//            OFAction action = approvedActions.get(i);
//            if (action.getType() == OFActionType.SET_VLAN_VID) {
//                OVXActionVirtualLanIdentifier existingVlan = (OVXActionVirtualLanIdentifier) action;
//                vlan = existingVlan.getVlanVid().getVlan();
//                index = i;
//                System.out.println("Found Vlan action at index "+ i+ " with vlan tag "+ vlan);
//                break;
//            }
//            if (action.getType() == OFActionType.STRIP_VLAN) {
//                vlan = 1;
//                stripindex = i;
//                System.out.println("Found Strip Vlan at index "+ i);
//            }
//        }
//
//        if (stripindex > -1) {
//            /* Nothing to do..*/
//            return;
//        } else if (index > -1) {
//            /* Again Nothing to do */
//            return;
//        } else if (match_vlan == true){
//            /* We have a match, and no strip.. so maintain the vlan.. */
//            System.out.println("Match_vlan is true...");
//            //short origVlan = getOrigVlan(vlan);
//            if (vlan != 1) {
//                final OVXActionVirtualLanIdentifier ovlan = new OVXActionVirtualLanIdentifierVer10(VlanVid.ofVlan(vlan));
//                approvedActions.add(0, ovlan);
//                System.out.println("Found a match of VLAN ID with no "+ vlan + " Hence, Changing to that vlan");
//            } else {
//                System.out.println("Found a match with VLAN ID of "+ 1 + "Stripping Vlan");
//                final OVXActionStripVirtualLan vlanstrip = new OVXActionStripVirtualLanVer10();
//                approvedActions.add(0, vlanstrip);
//            }
//        } else {
//            System.out.println("UnRewriting Action.. Stripping vlan");
//            final OVXActionStripVirtualLan vlanstrip = new   OVXActionStripVirtualLanVer10();
//            approvedActions.add(0, vlanstrip);
//        }

    }
}

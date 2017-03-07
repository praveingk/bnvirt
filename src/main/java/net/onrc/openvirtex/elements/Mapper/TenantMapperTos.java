/**
 * BNV - TenantMapperTos
 * Created by pravein on 4/12/16.
 */
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
 * Utility class for Tenant isolation of flows using ToS bit.
 */
public final class TenantMapperTos {
    private static Logger log = LogManager.getLogger(TenantMapperTos.class.getName());


    private static ConcurrentHashMap<OVXVlan, Short> OVXVlanMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Short, OVXVlan> ReverseOVXVlanMap = new ConcurrentHashMap<>();

    private static HashSet<Short> usedVlans = new HashSet<>();
    private static short startVlan = 2010;
    private static short endVlan = 4094;
    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private TenantMapperTos() {
    }

    public static synchronized short getPhysicalTag(Integer tenantId) {
        return tenantId.shortValue();
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
    }


    public static void prependUnRewriteActions(final OFMatch match, List<OFAction> approvedActions) {


    }

}
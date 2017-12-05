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
package net.onrc.openvirtex.elements.link;

import java.util.*;

import net.onrc.openvirtex.api.Global.GlobalConfig;
import net.onrc.openvirtex.api.Global.TAG;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.Mapper.TenantMapperTos;
import net.onrc.openvirtex.elements.Mapper.TenantMapperVlan;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.address.IPMapper;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.LinkMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.PortMappingException;
import net.onrc.openvirtex.messages.OVXFlowAdd;
import net.onrc.openvirtex.messages.OVXFlowDelete;
import net.onrc.openvirtex.messages.OVXFlowDeleteStrict;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXFlowModify;
import net.onrc.openvirtex.messages.OVXFlowModifyStrict;
import net.onrc.openvirtex.messages.OVXPacketOut;
import net.onrc.openvirtex.messages.actions.ver10.OVXActionOutputVer10;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.routing.RoutingAlgorithms;
import net.onrc.openvirtex.routing.RoutingAlgorithms.RoutingType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmInPort;
import org.projectfloodlight.openflow.protocol.ver13.OFOxmInPortVer13;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.U8;
import org.projectfloodlight.openflow.types.OFBufferId;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Implementation of a virtual link, which adds a unique ID, stores the tenant
 * ID, a priority and a routing algorithm.
 *
 */
public class OVXLink extends Link<OVXPort, OVXSwitch> {
    private Logger log = LogManager.getLogger(OVXLink.class.getName());

    @SerializedName("linkId")
    @Expose
    private final Integer linkId;

    @SerializedName("tenantId")
    @Expose
    private final Integer tenantId;
    private byte priority;
    private RoutingAlgorithms alg;
    private final TreeMap<Byte, List<PhysicalLink>> backupLinks;
    private final TreeMap<Byte, List<PhysicalLink>> unusableLinks;
    private Mappable map = null;

    /**
     * Instantiates a new virtual link. Sets its priority to 0.
     *
     * @param linkId
     *            the unique link id
     * @param tenantId
     *            the tenant id
     * @param srcPort
     *            virtual source port
     * @param dstPort
     *            virtual destination port
     * @param alg
     *            routing algorithm for the virtual link
     * @throws PortMappingException
     *             if one of the ports is invalid
     */
    public OVXLink(final Integer linkId, final Integer tenantId,
            final OVXPort srcPort, final OVXPort dstPort, RoutingAlgorithms alg)
            throws PortMappingException {
        super(srcPort, dstPort);
        this.linkId = linkId;
        this.tenantId = tenantId;
        srcPort.setOutLink(this);
        dstPort.setInLink(this);
        this.backupLinks = new TreeMap<>();
        this.unusableLinks = new TreeMap<>();
        this.priority = (byte) 0;
        this.alg = alg;
        this.map = OVXMap.getInstance();
        if (this.alg.getRoutingType() != RoutingType.NONE) {
            this.alg.getRoutable().setLinkPath(this);
        }
        this.srcPort.getPhysicalPort().removeOVXPort(this.srcPort);
        this.srcPort.getPhysicalPort().setOVXPort(this.srcPort);
    }

    /**
     * Gets the unique link id.
     *
     * @return the link id
     */
    public Integer getLinkId() {
        return this.linkId;
    }

    /**
     * Gets the tenant id.
     *
     * @return the tenant id
     */
    public Integer getTenantId() {
        return this.tenantId;
    }

    /**
     * Gets the priority value.
     *
     * @return the priority value
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Sets the priority value.
     *
     * @param priority
     *            the priority value
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * Gets the routing algorithm.
     *
     * @return the algorithm
     */
    public RoutingAlgorithms getAlg() {
        return this.alg;
    }

    /**
     * Sets the routing algorithm.
     *
     * @param alg
     *            the algorithm
     */
    public void setAlg(final RoutingAlgorithms alg) {
        this.alg = alg;
    }

    /**
     * Register mapping between virtual link and physical path.
     *
     * @param physicalLinks
     *            the path as a list of physical links
     * @param priority
     *            the priority value
     */
    public void register(final List<PhysicalLink> physicalLinks, byte priority) {
        if (U8.f(this.getPriority()) >= U8.f(priority)) {
            this.backupLinks.put(priority, physicalLinks);
            log.debug(
                    "Add virtual link {} backup path (priority {}) between ports {}/{} - {}/{} in virtual network {}."
                            + "Path: {}", this.getLinkId(), U8.f(priority),
                    this.getSrcSwitch().getSwitchName(), this.srcPort
                            .getPortNo(), this.getDstSwitch()
                            .getSwitchName(), this.dstPort.getPortNo(),
                    this.getTenantId(), physicalLinks);
        } else {
            try {
                this.backupLinks.put(this.getPriority(),
                        map.getPhysicalLinks(this));
                log.debug(
                        "Replace virtual link {} with a new primary path (priority {}) between ports {}/{} - {}/{}"
                                + "in virtual network {}. Path: {}", this
                                .getLinkId(), U8.f(priority), this
                                .getSrcSwitch().getSwitchName(), this.srcPort
                                .getPortNo(), this.getDstSwitch()
                                .getSwitchName(), this.dstPort.getPortNo(),
                        this.getTenantId(), physicalLinks);
                log.info(
                        "Switch all existing flow-mods crossing the virtual link {} between ports ({}/{},{}/{})"
                                + "to new path", this.getLinkId(), this
                                .getSrcSwitch().getSwitchName(), this
                                .getSrcPort().getPortNo(), this
                                .getDstSwitch().getSwitchName(), this
                                .getDstPort().getPortNo());
            } catch (LinkMappingException e) {
                log.debug(
                        "Create virtual link {} primary path (priority {}) between ports {}/{} - {}/{}"
                                + "in virtual network {}. Path: {}", this
                                .getLinkId(), U8.f(priority), this
                                .getSrcSwitch().getSwitchName(), this.srcPort
                                .getPortNo(), this.getDstSwitch()
                                .getSwitchName(), this.dstPort.getPortNo(),
                        this.getTenantId(), physicalLinks);
            }
            this.switchPath(physicalLinks, priority);
        }

        DBManager.getInstance().save(this);
    }

    @Override
    public void unregister() {

        try {
            DBManager.getInstance().remove(this);
            this.tearDown();
            map.removeVirtualLink(this);
            map.getVirtualNetwork(this.tenantId).removeLink(this);
        } catch (NetworkMappingException e) {
            log.warn(
                    "[unregister()]: could not remove this link from map \n{}",
                    e.getMessage());
        }
    }

    /**
     * Disables the virtual link by disabling its end points.
     */
    public void tearDown() {
        this.srcPort.tearDown();
        this.dstPort.tearDown();
    }

    /**
     * Switch the link to the given path and priority.
     *
     * @param physicalLinks
     *            the path as a list of physical links
     * @param priority
     *            the priority value
     */
    public void switchPath(List<PhysicalLink> physicalLinks, byte priority) {
        // register the primary link in the map
        this.srcPort.getParentSwitch().getMap().removeVirtualLink(this);
        this.srcPort.getParentSwitch().getMap().addLinks(physicalLinks, this);

        this.setPriority(priority);

        Collection<OVXFlowMod> flows = this.getSrcSwitch().getFlowTable()
                .getFlowTable();
        for (OVXFlowMod fe : flows) {
            for (OFAction act : fe.getActions()) {
                if (act.getType() == OFActionType.OUTPUT) {
                    if (((OFActionOutput) act).getPort() == this.getSrcPort()
                            .getPortNo()) {
                        try {
                            Integer flowId = this.map
                                    .getVirtualNetwork(this.tenantId)
                                    .getFlowManager()
                                    .storeFlowValues(
                                            ((OFMatchV1)fe.getMatch()).getEthSrc().getBytes(),
                                            ((OFMatchV1)fe.getMatch()).getEthDst().getBytes()
                                                    );
                            if (fe.getCommand().equals(OFFlowModCommand.ADD))
                            {
                            OVXFlowAdd fm = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            }
                            if (fe.getCommand().equals(OFFlowModCommand.ADD))
                            {
                            OVXFlowAdd fm = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            }
                            else if (fe.getCommand().equals(OFFlowModCommand.DELETE))
                            {
                            OVXFlowDelete fm = OVXFactoryInst.myOVXFactory.buildOVXFlowDelete(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            } if (fe.getCommand().equals(OFFlowModCommand.DELETE_STRICT))
                            {
                            OVXFlowDeleteStrict fm = OVXFactoryInst.myOVXFactory.buildOVXFlowDeleteStrict(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            } if (fe.getCommand().equals(OFFlowModCommand.MODIFY))
                            {
                            OVXFlowModify fm = OVXFactoryInst.myOVXFactory.buildOVXFlowModify(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            }
                            if (fe.getCommand().equals(OFFlowModCommand.MODIFY_STRICT))
                            {
                            OVXFlowModifyStrict fm = OVXFactoryInst.myOVXFactory.buildOVXFlowModifyStrict(fe.getXid(), fe.getMatch(), U64.of(((OVXFlowTable) this.getSrcPort()
                                    .getParentSwitch().getFlowTable())
                                    .getCookie(fe, true)), fe.getIdleTimeout(), fe.getHardTimeout(), fe.getPriority(), fe.getBufferId(), fe.getOutPort(), fe.getFlags(), fe.getActions());
 
                            this.generateLinkFMs(fm, flowId);
                            }
                            
                            
                        } catch (IndexOutOfBoundException e) {
                            log.error(
                                    "Too many hosts to generate the flow pairs in this virtual network {}. "
                                            + "Dropping flow-mod {} ",
                                    this.getTenantId(), fe);
                        } catch (NetworkMappingException e) {
                            log.warn("{}: skipping processing of OFAction", e);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> getDBIndex() {
        Map<String, Object> index = new HashMap<String, Object>();
        index.put(TenantHandler.TENANT, this.tenantId);
        return index;
    }

    @Override
    public String getDBKey() {
        return Link.DB_KEY;
    }

    @Override
    public String getDBName() {
        return DBManager.DB_VNET;
    }

    @Override
    public Map<String, Object> getDBObject() {
        Map<String, Object> dbObject = super.getDBObject();
        dbObject.put(TenantHandler.LINK, this.linkId);
        dbObject.put(TenantHandler.PRIORITY, this.priority);
        dbObject.put(TenantHandler.ALGORITHM, this.alg.getRoutingType()
                .getValue());
        dbObject.put(TenantHandler.BACKUPS, this.alg.getBackups());
        try {
            // Build path list
            List<PhysicalLink> links = map.getPhysicalLinks(this);
            List<Map<String, Object>> path = new ArrayList<Map<String, Object>>();
            for (PhysicalLink link : links) {
                // Physical link id's are meaningless when restarting OVX
                Map<String, Object> obj = link.getDBObject();
                obj.remove(TenantHandler.LINK);
                path.add(obj);
            }
            dbObject.put(TenantHandler.PATH, path);
        } catch (LinkMappingException e) {
            dbObject.put(TenantHandler.PATH, null);
        }
        return dbObject;
    }

    /**
     * Push the flow mod to all the intermediate switches of the virtual link.
     *
     * @param fm_clone
     *            the original flow mod
     * @param flowId
     *            the flow identifier
     */
    public void generateLinkFMs(OFFlowMod fm_clone, final Integer flowId) {
        /*
         * Change the packet match: 1) change the fields where the virtual link
         * info are stored 2) change the fields where the physical IPs are
         * stored
         */
        final OVXLinkUtils lUtils = new OVXLinkUtils(this.tenantId,
                this.linkId, flowId);
        lUtils.rewriteMatch(fm_clone.getMatch());
        long cookie = tenantId;
        fm_clone=fm_clone.createBuilder().setCookie(U64.of(cookie << 32)).build();

        if (((OFMatchV1)fm_clone.getMatch()).getEthType().getValue() == Ethernet.TYPE_IPV4) {
			if (GlobalConfig.bnvTagType == TAG.TOS) {
                TenantMapperTos.rewriteMatch(this.tenantId, (OFMatchV1) fm_clone.getMatch());
            } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                TenantMapperVlan.rewriteMatch(this.tenantId, (OFMatchV1) fm_clone.getMatch());
            } else if (GlobalConfig.bnvTagType == TAG.IP) {
				IPMapper.rewriteMatch(this.tenantId, fm_clone.getMatch());
			} else if (GlobalConfig.bnvTagType == TAG.NOTAG) {
                    /* Do Nothing */
            }
            
        }

        /*
         * Get the list of physical links mapped to this virtual link, in
         * REVERSE ORDER
         */
        PhysicalPort inPort = null;
        PhysicalPort outPort = null;
        fm_clone=fm_clone.createBuilder().setBufferId(OFBufferId.NO_BUFFER).build();
        //.setCommand(OFFlowModCommand.MODIFY);
        
        List<PhysicalLink> plinks = new LinkedList<PhysicalLink>();
        try {
            final OVXLink link = this.map.getVirtualNetwork(this.tenantId)
                    .getLink(this.srcPort, this.dstPort);
            for (final PhysicalLink phyLink : OVXMap.getInstance()
                    .getPhysicalLinks(link)) {
                plinks.add(new PhysicalLink(phyLink.getDstPort(), phyLink
                        .getSrcPort()));
            }
        } catch (LinkMappingException | NetworkMappingException e) {
            log.warn("No physical Links mapped to OVXLink? : {}", e);
            return;
        }

        Collections.reverse(plinks);

        for (final PhysicalLink phyLink : plinks) {
            if (outPort != null) {
                inPort = phyLink.getSrcPort();
                Match fm_clone_match=((OFMatchV1)fm_clone.getMatch())
                .createBuilder()
                .setInPort(inPort.getPortNo())
                .build();
                
                OFAction fm_clone_action=OVXFactoryInst.myFactory.actions()
                		.output(outPort.getPortNo(), (short) 0xffff);
                fm_clone=fm_clone.createBuilder().setMatch(fm_clone_match)
                		.setActions(Arrays.asList(fm_clone_action))
                		.build();
                
                phyLink.getSrcPort().getParentSwitch()
                        .sendMsg(fm_clone, phyLink.getSrcPort().getParentSwitch());
                this.log.debug(
                        "Sending virtual link intermediate fm to sw {}: {}",
                        phyLink.getSrcPort().getParentSwitch().getSwitchName(),
                        fm_clone);
            }
            outPort = phyLink.getDstPort();
        }
        // TODO: With POX we need to put a timeout between this flows and the
        // first flow mod. Check how to solve.
        try {
            Thread.sleep(5);
        } catch (final InterruptedException e) {
            log.warn("Timeout interrupted; might be a problem if you are running POX.");
        }
    }

    /**
     * Push the flow mod to all the intermediate switches of the virtual link.
     *
     * @param fm_clone
     *            the original flow mod
     * @param flowId
     *            the flow identifier
     */
    public void generateLinkFMsV3(OFFlowMod fm_clone, final Integer flowId) {
        /*
         * Change the packet match: 1) change the fields where the virtual link
         * info are stored 2) change the fields where the physical IPs are
         * stored
         */
        System.out.println("Generating link fms");
        final OVXLinkUtils lUtils = new OVXLinkUtils(this.tenantId,
                this.linkId, flowId);
        lUtils.rewriteMatch(fm_clone.getMatch());
        long cookie = tenantId;
        fm_clone=fm_clone.createBuilder().setCookie(U64.of(cookie << 32)).build();
        System.out.println(fm_clone.getMatch().toString());
        if (((OFMatchV3)fm_clone.getMatch()).get(MatchField.ETH_TYPE).getValue() == Ethernet.TYPE_IPV4) {
			if (GlobalConfig.bnvTagType == TAG.TOS) {
                TenantMapperTos.rewriteMatch(this.tenantId, (OFMatchV3) fm_clone.getMatch());
            } else if (GlobalConfig.bnvTagType == TAG.VLAN) {
                TenantMapperVlan.rewriteMatch(this.tenantId, (OFMatchV3) fm_clone.getMatch());
            } else if (GlobalConfig.bnvTagType == TAG.IP) {
				IPMapper.rewriteMatch(this.tenantId, fm_clone.getMatch());
			} else if (GlobalConfig.bnvTagType == TAG.NOTAG) {
                    /* Do Nothing */
            }
        }

        System.out.println("After rewritematch" +fm_clone.getMatch().toString());

        /*
         * Get the list of physical links mapped to this virtual link, in
         * REVERSE ORDER
         */
        PhysicalPort inPort = null;
        PhysicalPort outPort = null;
        fm_clone=fm_clone.createBuilder().setBufferId(OFBufferId.NO_BUFFER).build();
        //.setCommand(OFFlowModCommand.MODIFY);

        List<PhysicalLink> plinks = new LinkedList<PhysicalLink>();
        try {
            final OVXLink link = this.map.getVirtualNetwork(this.tenantId)
                    .getLink(this.srcPort, this.dstPort);
            for (final PhysicalLink phyLink : OVXMap.getInstance()
                    .getPhysicalLinks(link)) {
                plinks.add(new PhysicalLink(phyLink.getDstPort(), phyLink
                        .getSrcPort()));
            }
        } catch (LinkMappingException | NetworkMappingException e) {
            log.warn("No physical Links mapped to OVXLink? : {}", e);
            return;
        }

        Collections.reverse(plinks);

        for (final PhysicalLink phyLink : plinks) {
            if (outPort != null) {
                inPort = phyLink.getSrcPort();
                Match fm_clone_match=addtoMatch((OFMatchV3)fm_clone.getMatch(), inPort.getPortNo());

                System.out.println("Match after ading inport "+ fm_clone_match);

                OFAction fm_clone_action=OVXFactoryInst.myFactory.actions()
                        .output(outPort.getPortNo(), (short) 0xffff);
                fm_clone=fm_clone.createBuilder().setMatch(fm_clone_match)
                        .setActions(Arrays.asList(fm_clone_action))
                        .build();

                phyLink.getSrcPort().getParentSwitch()
                        .sendMsg(fm_clone, phyLink.getSrcPort().getParentSwitch());
                this.log.debug(
                        "Sending virtual link intermediate fm to sw {}: {}",
                        phyLink.getSrcPort().getParentSwitch().getSwitchName(),
                        fm_clone);
            }
            outPort = phyLink.getDstPort();
        }
        // TODO: With POX we need to put a timeout between this flows and the
        // first flow mod. Check how to solve.
        try {
            Thread.sleep(5);
        } catch (final InterruptedException e) {
            log.warn("Timeout interrupted; might be a problem if you are running POX.");
        }
    }

    private OFMatchV3 addtoMatch(OFMatchV3 myMatch, OFPort port) {
        OFMatchV3 newMatch;
        OFOxmList myList = myMatch.getOxmList();
        OFOxmInPort oxmInPort = new OFOxmInPortVer13(port);
        Map <MatchFields, OFOxm<?>> oxmMap = new LinkedHashMap<>();
        oxmMap.put(MatchFields.IN_PORT, oxmInPort);
        for (OFOxm<?> ofOxm : myList) {
            if (ofOxm instanceof OFOxmInPort) continue;
            System.out.println(ofOxm.getMatchField().id.toString());
            oxmMap.put(ofOxm.getMatchField().id, ofOxm);
        }

        OFOxmList oxmList = new OFOxmList(oxmMap);
        System.out.println(myList.toString());

        System.out.println(oxmList.toString());
        newMatch = OVXFactoryInst.myFactory.buildMatchV3().setOxmList(oxmList).build();
        return newMatch;

    }

    /**
     * Tries to switch link to a backup path, and updates mappings to "correct"
     * string of PhysicalLinks to use for this link.
     *
     * @param plink
     *            the failed PhysicalLink
     * @return true if successful, false otherwise
     */
    public boolean tryRecovery(PhysicalLink plink) {
        log.info("Try recovery for virtual link {} in virtual network {} ",
                this.linkId, this.tenantId);
        if (this.backupLinks.size() > 0) {
            try {
                List<PhysicalLink> unusableLinks = new ArrayList<>(
                        map.getPhysicalLinks(this));
                Collections.copy(unusableLinks, map.getPhysicalLinks(this));
                this.unusableLinks.put(this.getPriority(), unusableLinks);
            } catch (LinkMappingException e) {
                log.warn("No physical Links mapped to OVXLink? : {}", e);
                return false;
            }
            byte priority = this.backupLinks.lastKey();
            List<PhysicalLink> phyLinks = this.backupLinks.get(priority);
            this.switchPath(phyLinks, priority);
            this.backupLinks.remove(priority);
            return true;
        }
        return false;
    }

    /**
     * Attempts to switch this link back to the original path.
     *
     * @param plink
     *            the restored physical link
     * @return true if successful, false otherwise.
     */
    public boolean tryRevert(PhysicalLink plink) {
        Iterator<Byte> it = this.unusableLinks.descendingKeySet().iterator();
        while (it.hasNext()) {
            Byte curPriority = it.next();
            if (this.unusableLinks.get(curPriority).contains(plink)) {
                log.info(
                        "Reactivate all inactive paths for virtual link {} in virtual network {} ",
                        this.linkId, this.tenantId);

                if (U8.f(this.getPriority()) >= U8.f(curPriority)) {
                    this.backupLinks.put(curPriority,
                            this.unusableLinks.get(curPriority));
                } else {
                    try {
                        List<PhysicalLink> backupLinks = new ArrayList<>(
                                map.getPhysicalLinks(this));
                        Collections.copy(backupLinks,
                                map.getPhysicalLinks(this));
                        this.backupLinks.put(this.getPriority(), backupLinks);
                        this.switchPath(this.unusableLinks.get(curPriority),
                                curPriority);
                    } catch (LinkMappingException e) {
                        log.warn(
                                "No physical Links mapped to SwitchRoute? : {}",
                                e);
                        return false;
                    }
                }
                it.remove();
            }
        }
        return true;
    }

}

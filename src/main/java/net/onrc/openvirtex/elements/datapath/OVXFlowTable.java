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
package net.onrc.openvirtex.elements.datapath;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.onrc.openvirtex.core.OVXFactoryInst;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowModCommandSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFFlowModFlagsSerializerVer10;
import org.projectfloodlight.openflow.protocol.ver10.OFMatchV1Ver10;
import org.projectfloodlight.openflow.types.OFPort;

import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.OVXFlowAdd;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;

/**
 * Virtualized version of the switch flow table.
 */
public class OVXFlowTable implements FlowTable {

    private final Logger log = LogManager.getLogger(OVXFlowTable.class
            .getName());

    // OVXSwitch tied to this table
    protected OVXSwitch vswitch;
    // Map of FlowMods to physical cookies for vlinks
    protected ConcurrentHashMap<Long, OVXFlowMod> flowmodMap;
    // Reverse map of FlowMod hashcode to cookie
    protected ConcurrentHashMap<Integer, Long> cookieMap;

    /**
     * Temporary solution that should be replaced by something that doesn't
     * fragment.
     */
    private AtomicInteger cookieCounter;

    /**
     * Stores previously used cookies so we only generate one when this list is
     * empty.
     */
    private LinkedList<Long> freeList;
    private static final int FREELIST_SIZE = 1024;

    /* statistics per specs */
    protected int activeEntries;
    protected long lookupCount;
    protected long matchCount;

    /**
     * Instantiates a new flow table associated to the given
     * virtual switch. Initializes flow mod and cookie mappings,
     * and some counters and statistics.
     *
     * @param vsw the virtual switch
     */
    public OVXFlowTable(OVXSwitch vsw) {
        this.flowmodMap = new ConcurrentHashMap<Long, OVXFlowMod>();
        this.cookieMap = new ConcurrentHashMap<Integer, Long>();
        this.cookieCounter = new AtomicInteger(1);
        this.freeList = new LinkedList<Long>();
        this.vswitch = vsw;

        /* initialise stats */
        this.activeEntries = 0;
        this.lookupCount = 0;
        this.matchCount = 0;
    }

    /**
     * Checks if the flow table is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return this.flowmodMap.isEmpty();
    }

    /**
     * Processes FlowMods according to command field, writing out FlowMods south
     * if needed.
     *
     * @param fm1 The FlowMod to apply to this table
     * @return if the FlowMod needs to be sent south during devirtualization.
     */
    public boolean handleFlowMods(OVXFlowMod fm1) {
        if (fm1 == null) {
            System.out.println("fm is null");
            return false;
        }
        switch (fm1.getCommand()) {
        case ADD:
            return doFlowModAdd(fm1);
        case MODIFY:
        case MODIFY_STRICT:
            return doFlowModModify(fm1);
        case DELETE:
            return doFlowModDelete(fm1, false);
        case DELETE_STRICT:
            return doFlowModDelete(fm1, true);
        default:
            /* we don't know what it is. drop. */
            return false;
        }
    }

    /**
     * Deletes an existing FlowEntry, expanding out a OFPFW_ALL delete sent
     * initially be a controller. If not, checks for entries, and only allows
     * entries that exist here to be deleted.
     *
     * @param fm the flowmod
     * @param strict true if a STRICT match
     * @return true if FlowMod should be written south
     */
    private boolean doFlowModDelete(OVXFlowMod fm, boolean strict) {
        /* don't do anything if FlowTable is empty */
        if (this.flowmodMap.isEmpty()) {
            return false;
        }
        /* fetch our vswitches */
        try {
            /*
             * expand wildcard delete, remove all entries pertaining just to
             * this tenant
             */
            if (OVXFactoryInst.ofversion == 10) {
                if (((OFMatchV1) fm.getMatch()).getWildcards() == OFMatchV1Ver10.OFPFW_ALL) {
                    List<PhysicalSwitch> pList = this.vswitch.getMap()
                            .getPhysicalSwitches(this.vswitch);
                    final short OFPP_ANY_SHORT = (short) 0xFFff;
                    for (PhysicalSwitch psw : pList) {
                        /* do FlowMod cleanup like when port dies. */
                        psw.cleanUpTenant(this.vswitch.getTenantId(),
                                OFPP_ANY_SHORT);
                    }
                    this.flowmodMap.clear();
                    this.cookieMap.clear();
                    return false;
                }
            } else {
                OFOxmList oxmList = ((OFMatchV3) fm.getMatch()).getOxmList();
                if (oxmList.get(MatchField.ETH_TYPE) == null && oxmList.get(MatchField.ETH_TYPE) == null) {
                    List<PhysicalSwitch> pList = this.vswitch.getMap()
                            .getPhysicalSwitches(this.vswitch);
                    final short OFPP_ANY_SHORT = (short) 0xFFff;
                    for (PhysicalSwitch psw : pList) {
                        /* do FlowMod cleanup like when port dies. */
                        psw.cleanUpTenant(this.vswitch.getTenantId(),
                                OFPP_ANY_SHORT);
                    }
                    this.flowmodMap.clear();
                    this.cookieMap.clear();
                    return false;
                }
            }
                /* remove matching flow entries, and let FlowMod be sent down */
                Iterator<Map.Entry<Long, OVXFlowMod>> itr = this.flowmodMap
                        .entrySet().iterator();
                OVXFlowEntry fe = new OVXFlowEntry();
                while (itr.hasNext()) {
                    Map.Entry<Long, OVXFlowMod> entry = itr.next();
                    fe.setFlowMod(entry.getValue());
                    int overlap = fe.compare(fm.getMatch(), strict);
                    if (overlap == OVXFlowEntry.EQUAL) {
                        this.cookieMap.remove(entry.getValue().hashCode());
                        itr.remove();
                    }
                }
                return true;

        } catch (SwitchMappingException e) {
            log.warn("Could not clear PhysicalSwitch tables: {}", e);
        }
        return false;
    }

    /**
     * Adds a flow entry to the FlowTable. The FlowMod is checked for overlap if
     * its flag says so.
     *
     * @param fm the flow mod
     * @return true if FlowMod should be written south
     */
    private boolean doFlowModAdd(OVXFlowMod fm) {
        if (fm.getFlags().contains(OFFlowModFlags.CHECK_OVERLAP)) {
            OVXFlowEntry fe = new OVXFlowEntry();
            for (OVXFlowMod fmod : this.flowmodMap.values()) {
                /*
                 * if not disjoint AND same priority send up OVERLAP error and
                 * drop it
                 */
                fe.setFlowMod(fmod);
                int res = fe.compare(fm.getMatch(), false);
                if ((res != OVXFlowEntry.DISJOINT)
                        & (fm.getPriority() == fe.getPriority())) {
                    this.vswitch.sendMsg(OVXMessageUtil.makeErrorMsg(
                            OFFlowModFailedCode.OVERLAP, fm),
                            this.vswitch);
                    return false;
                }
            }
        }
        return doFlowModModify(fm);
    }

    /**
     * Adds the FlowMod to the table.
     *
     * @param fm the flow mod
     * @return true if FlowMod should be written South
     */
    private boolean doFlowModModify(OVXFlowMod fm) {
        OVXFlowEntry fe = new OVXFlowEntry();
        int res;
        for (Map.Entry<Long, OVXFlowMod> fmod : this.flowmodMap.entrySet()) {
            fe.setFlowMod(fmod.getValue());
            res = fe.compare(fm.getMatch(), true);
            /* replace table entry that strictly matches with given FlowMod. */
            if (res == OVXFlowEntry.EQUAL) {
                long c = fmod.getKey();
                log.info("replacing equivalent FlowEntry [cookie={}]", c);
                OVXFlowMod old = this.flowmodMap.get(c);
                this.cookieMap.remove(old.hashCode());
                this.addFlowMod(fm, c);
                /* return cookie to pool and use the previous cookie */
                return true;
            }
        }
        /* make a new cookie, add FlowMod */
        long newc = this.getCookie();
        this.addFlowMod(fm.clone(), newc);
        return true;
    }

    /**
     * Gets a copy of the FlowMod out of the flow table without removing it.
     *
     * @param cookie the physical cookie
     * @return a clone of the stored FlowMod
     * @throws MappingException if the cookie is not found
     */
    public OVXFlowMod getFlowMod(Long cookie) throws MappingException {
        OVXFlowMod fm = this.flowmodMap.get(cookie);
        if (fm == null) {
            throw new MappingException(cookie, OVXFlowMod.class);
        }
        return fm.clone();
    }

    /**
     * Checks if the cookie is present in the flow table.
     *
     * @param cookie the cookie
     * @return true if cookie is present, false otherwise
     */
    public boolean hasFlowMod(long cookie) {
        return this.flowmodMap.containsKey(cookie);
    }

    /**
     * Gets a new cookie.
     *
     * @return the cookie
     */
    public long getCookie() {
        return this.generateCookie();
    }

    /**
     * Gets a cookie based on the given flow mod.
     *
     * @param fe2 the flow mod
     * @param cflag TODO
     * @return the cookie
     */
    public final long getCookie(OVXFlowMod fe2, Boolean cflag) {
        if (cflag) {
            long cookie = this.getCookie();
            OVXFlowEntry fe = new OVXFlowEntry();
            int res;
            for (Map.Entry<Long, OVXFlowMod> fmod : this.flowmodMap.entrySet()) {
                fe.setFlowMod(fmod.getValue());
                res = fe.compare(fe2.getMatch(), true);
                /* replace table entry that strictly matches with given FlowMod. */
                if (res == OVXFlowEntry.EQUAL) {
                    long c = fmod.getKey();
                    log.info(
                            "replacing equivalent FlowEntry with new [cookie={}]",
                            cookie);
                    OVXFlowMod old = this.flowmodMap.get(c);
                    this.cookieMap.remove(old.hashCode());
                    this.flowmodMap.remove(c);
                    this.addFlowMod(fe2, cookie);
                    /* return cookie to pool and use the previous cookie */
                    return cookie;
                }
            }

        }
        Long cookie = this.cookieMap.get(fe2.hashCode());
        if (cookie == null) {
            cookie = this.getCookie();
        }
        return cookie;
    }

    /**
     * Adds the given flow mod and associate it to the given cookie.
     *
     * @param flowmod the flow mod
     * @param cookie the cookie
     * @return the cookie
     */
    public long addFlowMod(final OVXFlowMod flowmod, long cookie) {
        System.out.println("Adding FlowMod");
        this.flowmodMap.put(cookie, flowmod);
        this.cookieMap.put(flowmod.hashCode(), cookie);
        return cookie;
    }

    /**
     * Deletes the flow mod associated with the given cookie.
     *
     * @param cookie the cookie
     * @return the flow mod
     */
    public OVXFlowMod deleteFlowMod(final Long cookie) {
        synchronized (this.freeList) {
            if (this.freeList.size() <= OVXFlowTable.FREELIST_SIZE) {
                // add/return cookie to freelist IF list is below FREELIST_SIZE
                this.freeList.add(cookie);
            } else {
                // remove head element, then add
                this.freeList.remove();
                this.freeList.add(cookie);
            }
            OVXFlowMod ret = this.flowmodMap.remove(cookie);
            if (ret != null) {
                this.cookieMap.remove(ret.hashCode());
            }
            return ret;
        }
    }

    /**
     * Fetches a usable cookie for FlowMod storage. If no cookies are available,
     * generate a new physical cookie from the OVXSwitch tenant ID and
     * OVXSwitch-unique cookie counter.
     *
     * @return a physical cookie
     */
    private long generateCookie() {
        try {
            return this.freeList.remove();
        } catch (final NoSuchElementException e) {
            // none in queue - generate new cookie
            // TODO double-check that there's no duplicate in flowmod map.
            final int cookie = this.cookieCounter.getAndIncrement();
            return (long) this.vswitch.getTenantId() << 32 | cookie;
        }
    }

    /**
     * Dumps the contents of the FlowTable.
     */
    public void dump() {
        String ret = "";
        for (final Map.Entry<Long, OVXFlowMod> fe : this.flowmodMap.entrySet()) {
            ret += "cookie[" + fe.getKey() + "] :" + fe.getValue().toString()
                    + "\n";
        }
        this.log.info("OVXFlowTable \n========================\n" + ret
                + "========================\n");
    }

    /**
     * Gets an unmodifiable view of the flow table.
     *
     * @return the flow table
     */
    public Collection<OVXFlowMod> getFlowTable() {
        return Collections.unmodifiableCollection(this.flowmodMap.values());
    }

}

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
package net.onrc.openvirtex.api.service.handlers.monitoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.MonitoringHandler;
import net.onrc.openvirtex.core.OVXFactoryInst;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.messages.OVXFlowAdd;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXFlowModify;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatsReply;
import net.onrc.openvirtex.messages.statistics.ver10.OVXFlowStatsReplyVer10;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

public class GetPhysicalFlowtable extends ApiHandler<Map<String, Object>> {

    private JSONRPC2Response resp = null;

    @Override
    public JSONRPC2Response process(final Map<String, Object> params) {
        try {
            final Number dpid = HandlerUtils.<Number>fetchField(
                    MonitoringHandler.DPID, params, false, -1);
            final OVXMap map = OVXMap.getInstance();
            LinkedList<OVXFlowStatsReply> flows = new LinkedList<OVXFlowStatsReply>();

            if (dpid.longValue() == -1) {
                HashMap<String, List<Map<String, Object>>> res = new HashMap<String, List<Map<String, Object>>>();
                for (PhysicalSwitch sw : PhysicalNetwork.getInstance()
                        .getSwitches()) {
                    flows = aggregateFlowsBySwitch(sw.getSwitchId(), map);
                    res.put(sw.getSwitchName(), flowModsToMap(flows));
                }
                this.resp = new JSONRPC2Response(res, 0);
            } else {
                flows = aggregateFlowsBySwitch(dpid.longValue(), map);
                this.resp = new JSONRPC2Response(flowModsToMap(flows), 0);
            }

        } catch (ClassCastException | MissingRequiredField e) {
            this.resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Unable to fetch virtual topology : "
                            + e.getMessage()), 0);
        } catch (final InvalidDPIDException e) {
            this.resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Unable to fetch virtual topology : "
                            + e.getMessage()), 0);
        }

        return this.resp;

    }

    @Override
    public JSONRPC2ParamsType getType() {
        return JSONRPC2ParamsType.OBJECT;
    }

    private List<Map<String, Object>> flowModsToMap(
            LinkedList<OVXFlowStatsReply> flows) {
        final List<Map<String, Object>> res = new LinkedList<Map<String, Object>>();
        for (OVXFlowStatsReply frep : flows) {
        	for(OFFlowStatsEntry f:  frep.getEntries())
        	{
                OVXFlowAdd fm = null;
                if (OVXFactoryInst.ofversion == 10) {
                    fm = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(0, f.getMatch(), null, 0, 0, 0, null, null, null, f.getActions());
                } else {
                    fm = OVXFactoryInst.myOVXFactory.buildOVXFlowAdd(0, f.getCookie(), U64.ZERO, f.getTableId(), f.getIdleTimeout(), f.getHardTimeout(), f.getPriority(), OFBufferId.NO_BUFFER, OFPort.ALL, OFGroup.ZERO, f.getFlags(), f.getMatch(), f.getInstructions());
                }
        	    res.add(fm.toMap());//TODO:replaced FlowMod with Flow Add. Check whether it affects the functionality or not
        	}
            
        }
        return res;
    }

    private LinkedList<OVXFlowStatsReply> aggregateFlowsBySwitch(
            long dpid, Mappable map) {
        LinkedList<OVXFlowStatsReply> flows = new LinkedList<OVXFlowStatsReply>();
        final PhysicalSwitch sw = PhysicalNetwork.getInstance().getSwitch(dpid);
        for (Integer tid : map.listVirtualNetworks().keySet()) {
            if (sw.getFlowStats(tid) != null) {
                flows.addAll(sw.getFlowStats(tid));
            }
        }
        return flows;
    }

}

// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const.java
// Do not modify

package org.projectfloodlight.openflow.protocol;

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

public enum OFBsnPktinFlag {
     BSN_PKTIN_FLAG_PDU,
     BSN_PKTIN_FLAG_NEW_HOST,
     BSN_PKTIN_FLAG_STATION_MOVE,
     BSN_PKTIN_FLAG_ARP,
     BSN_PKTIN_FLAG_DHCP,
     BSN_PKTIN_FLAG_L2_CPU,
     BSN_PKTIN_FLAG_DEBUG,
     BSN_PKTIN_FLAG_TTL_EXPIRED,
     BSN_PKTIN_FLAG_L3_MISS,
     BSN_PKTIN_FLAG_L3_CPU,
     BSN_PKTIN_FLAG_INGRESS_ACL,
     BSN_PKTIN_FLAG_SFLOW,
     BSN_PKTIN_FLAG_ARP_CACHE,
     BSN_PKTIN_FLAG_ARP_TARGET;
}

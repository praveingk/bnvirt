package net.onrc.openvirtex.messages;

import org.projectfloodlight.openflow.protocol.*;


public final class OVXFactories {


 public static OVXFactory getFactory(OFVersion version) {
     switch(version) {
         case OF_10:
             return net.onrc.openvirtex.messages.ver10.OVXFactoryVer10.INSTANCE;
//         case OF_11:
//             return net.onrc.openvirtex.messages.ver11.OVXFactoryVer11.INSTANCE;
//         case OF_12:
//             return net.onrc.openvirtex.messages.ver12.OVXFactoryVer12.INSTANCE;
         case OF_13:
             return net.onrc.openvirtex.messages.ver13.OVXFactoryVer13.INSTANCE;
//         case OF_14:
//             return net.onrc.openvirtex.messages.ver14.OVXFactoryVer14.INSTANCE;
         default:
             throw new IllegalArgumentException("Unknown version: "+version);
         }
 }

}

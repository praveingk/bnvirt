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
package net.onrc.openvirtex.elements.port;

import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFPortFeatures;
import org.projectfloodlight.openflow.protocol.ver10.OFPortFeaturesSerializerVer10;
/**
 * The Class PortFeatures. This class is useful to translate the port features
 * exposed by the port to sub-features, to simplify get/set operations
 */
public class PortFeatures {

    /** The speed 10 Mbps half-duplex. */
    private boolean speed10MHD = false;

    /** The speed 10 Mbps full-duplex. */
    private boolean speed10MFD = false;

    /** The speed 100 Mbps half-duplex. */
    private boolean speed100MHD = false;

    /** The speed 100 Mbps full-duplex. */
    private boolean speed100MFD = false;

    /** The speed 1 Gbps half-duplex. */
    private boolean speed1GHD = false;

    /** The speed 1 Gbps full-duplex. */
    private boolean speed1GFD = false;

    /** The speed 10 Gbps full-duplex. */
    private boolean speed10GFD = false;

    /** The copper interface. */
    private boolean copper = false;

    /** The fiber interface. */
    private boolean fiber = false;

    /** The autonegotiation. */
    private boolean autonegotiation = false;

    /** The pause. */
    private boolean pause = false;

    /** The pause asym. */
    private boolean pauseAsym = false;

    /**
     * Instantiates a new port features.
     */
    public PortFeatures() {
        this.speed10MHD = false;
        this.speed10MFD = false;
        this.speed100MHD = false;
        this.speed100MFD = false;
        this.speed1GHD = false;
        this.speed1GFD = false;
        this.speed10GFD = false;
        this.copper = false;
        this.fiber = false;
        this.autonegotiation = false;
        this.pause = false;
        this.pauseAsym = false;
    }

    /**
     * Instantiates a new port features.
     *
     * @param set
     *            the features
     */
    public PortFeatures(final Set<OFPortFeatures> set) {
    	
    	
        if (set.contains(OFPortFeatures.PF_10MB_HD)) {
            this.speed10MHD = true;
        }
        if (set.contains(OFPortFeatures.PF_10MB_FD)) {
            this.speed10MFD = true;
        }
        if (set.contains(OFPortFeatures.PF_100MB_HD)) {
            this.speed100MHD = true;
        }
        if (set.contains(OFPortFeatures.PF_100MB_FD)) {
            this.speed100MFD = true;
        }
        if (set.contains(OFPortFeatures.PF_1GB_HD)) {
            this.speed1GHD = true;
        }
        if (set.contains(OFPortFeatures.PF_1GB_FD)) {
            this.speed1GFD = true;
        }
        if (set.contains(OFPortFeatures.PF_10GB_FD)) {
            this.speed10GFD = true;
        }
        if (set.contains(OFPortFeatures.PF_COPPER)) {
            this.copper = true;
        }
        if (set.contains(OFPortFeatures.PF_FIBER)) {
            this.fiber = true;
        }
        if (set.contains(OFPortFeatures.PF_AUTONEG)) {
            this.autonegotiation = true;
        }
        if (set.contains(OFPortFeatures.PF_PAUSE)) {
            this.pause = true;
        }
        if (set.contains(OFPortFeatures.PF_PAUSE_ASYM)) {
            this.pauseAsym = true;
        }
    }

    /**
     * Sets the current ovx port features.
     */
    public PortFeatures setCurrentOVXPortFeatures() {
        this.speed10MHD = false;
        this.speed10MFD = false;
        this.speed100MHD = false;
        this.speed100MFD = false;
        this.speed1GHD = false;
        this.speed1GFD = true;
        this.speed10GFD = false;
        this.copper = true;
        this.fiber = false;
        this.autonegotiation = false;
        this.pause = false;
        this.pauseAsym = false;
        return this;
    }

    /**
     * Sets the supported ovx port features.
     */
    public PortFeatures setSupportedOVXPortFeatures() {
        this.speed10MHD = true;
        this.speed10MFD = true;
        this.speed100MHD = true;
        this.speed100MFD = true;
        this.speed1GHD = true;
        this.speed1GFD = true;
        this.speed10GFD = false;
        this.copper = true;
        this.fiber = false;
        this.autonegotiation = false;
        this.pause = false;
        this.pauseAsym = false;
        return this;
    }

    /**
     * Sets the advertised ovx port features.
     */
    public PortFeatures setAdvertisedOVXPortFeatures() {
        this.speed10MHD = false;
        this.speed10MFD = true;
        this.speed100MHD = false;
        this.speed100MFD = true;
        this.speed1GHD = false;
        this.speed1GFD = true;
        this.speed10GFD = false;
        this.copper = true;
        this.fiber = false;
        this.autonegotiation = false;
        this.pause = false;
        this.pauseAsym = false;
        return this;
    }

    /**
     * Sets the peer ovx port features.
     */
    public PortFeatures setPeerOVXPortFeatures() {
        this.speed10MHD = false;
        this.speed10MFD = false;
        this.speed100MHD = false;
        this.speed100MFD = false;
        this.speed1GHD = false;
        this.speed1GFD = false;
        this.speed10GFD = false;
        this.copper = false;
        this.fiber = false;
        this.autonegotiation = false;
        this.pause = false;
        this.pauseAsym = false;
        return this;
    }

    /**
     * Gets the oVX features.
     *
     * @return the oVX features
     */
    public Integer getOVXFeatures() {
        Integer features = 0;
        if (this.speed10MHD) {
            features +=  OFPortFeaturesSerializerVer10.PF_10MB_HD_VAL;
           
        }
        if (this.speed10MFD) {
            features += OFPortFeaturesSerializerVer10.PF_10MB_FD_VAL;
        }
        if (this.speed100MHD) {
            features += OFPortFeaturesSerializerVer10.PF_100MB_HD_VAL;
        }
        if (this.speed100MFD) {
            features += OFPortFeaturesSerializerVer10.PF_100MB_FD_VAL;
        }
        if (this.speed1GHD) {
            features += OFPortFeaturesSerializerVer10.PF_1GB_HD_VAL;
        }
        if (this.speed1GFD) {
            features += OFPortFeaturesSerializerVer10.PF_1GB_FD_VAL;
        }
        if (this.speed10GFD) {
            features += OFPortFeaturesSerializerVer10.PF_10GB_FD_VAL;
        }
        if (this.copper) {
            features += OFPortFeaturesSerializerVer10.PF_COPPER_VAL;
        }
        if (this.fiber) {
            features += OFPortFeaturesSerializerVer10.PF_FIBER_VAL;
        }
        if (this.autonegotiation) {
            features += OFPortFeaturesSerializerVer10.PF_AUTONEG_VAL;
        }
        if (this.pause) {
            features += OFPortFeaturesSerializerVer10.PF_PAUSE_VAL;
        }
        if (this.pauseAsym) {
            features += OFPortFeaturesSerializerVer10.PF_PAUSE_ASYM_VAL;
        }
        return features;
    }

    /**
     * Checks if is speed10 mhd.
     *
     * @return true, if is speed10 mhd
     */
    public boolean isSpeed10MHD() {
        return this.speed10MHD;
    }

    /**
     * Sets the speed10 mhd.
     *
     * @param speed10mhd
     *            the new speed10 mhd
     */
    public void setSpeed10MHD(final boolean speed10mhd) {
        this.speed10MHD = speed10mhd;
    }

    /**
     * Checks if is speed10 mfd.
     *
     * @return true, if is speed10 mfd
     */
    public boolean isSpeed10MFD() {
        return this.speed10MFD;
    }

    /**
     * Sets the speed10 mfd.
     *
     * @param speed10mfd
     *            the new speed10 mfd
     */
    public void setSpeed10MFD(final boolean speed10mfd) {
        this.speed10MFD = speed10mfd;
    }

    /**
     * Checks if is speed100 mhd.
     *
     * @return true, if is speed100 mhd
     */
    public boolean isSpeed100MHD() {
        return this.speed100MHD;
    }

    /**
     * Sets the speed100 mhd.
     *
     * @param speed100mhd
     *            the new speed100 mhd
     */
    public void setSpeed100MHD(final boolean speed100mhd) {
        this.speed100MHD = speed100mhd;
    }

    /**
     * Checks if is speed100 mfd.
     *
     * @return true, if is speed100 mfd
     */
    public boolean isSpeed100MFD() {
        return this.speed100MFD;
    }

    /**
     * Sets the speed100 mfd.
     *
     * @param speed100mfd
     *            the new speed100 mfd
     */
    public void setSpeed100MFD(final boolean speed100mfd) {
        this.speed100MFD = speed100mfd;
    }

    /**
     * Checks if is speed1 ghd.
     *
     * @return true, if is speed1 ghd
     */
    public boolean isSpeed1GHD() {
        return this.speed1GHD;
    }

    /**
     * Sets the speed1 ghd.
     *
     * @param speed1ghd
     *            the new speed1 ghd
     */
    public void setSpeed1GHD(final boolean speed1ghd) {
        this.speed1GHD = speed1ghd;
    }

    /**
     * Checks if is speed1 gfd.
     *
     * @return true, if is speed1 gfd
     */
    public boolean isSpeed1GFD() {
        return this.speed1GFD;
    }

    /**
     * Sets the speed1 gfd.
     *
     * @param speed1gfd
     *            the new speed1 gfd
     */
    public void setSpeed1GFD(final boolean speed1gfd) {
        this.speed1GFD = speed1gfd;
    }

    /**
     * Checks if is speed10 gfd.
     *
     * @return true, if is speed10 gfd
     */
    public boolean isSpeed10GFD() {
        return this.speed10GFD;
    }

    /**
     * Sets the speed10 gfd.
     *
     * @param speed10gfd
     *            the new speed10 gfd
     */
    public void setSpeed10GFD(final boolean speed10gfd) {
        this.speed10GFD = speed10gfd;
    }

    /**
     * Checks if is copper.
     *
     * @return true, if is copper
     */
    public boolean isCopper() {
        return this.copper;
    }

    /**
     * Sets the copper.
     *
     * @param copper
     *            the new copper
     */
    public void setCopper(final boolean copper) {
        this.copper = copper;
    }

    /**
     * Checks if is fiber.
     *
     * @return true, if is fiber
     */
    public boolean isFiber() {
        return this.fiber;
    }

    /**
     * Sets the fiber.
     *
     * @param fiber
     *            the new fiber
     */
    public void setFiber(final boolean fiber) {
        this.fiber = fiber;
    }

    /**
     * Checks if is autonegotiation.
     *
     * @return true, if is autonegotiation
     */
    public boolean isAutonegotiation() {
        return this.autonegotiation;
    }

    /**
     * Sets the autonegotiation.
     *
     * @param autonegotiation
     *            the new autonegotiation
     */
    public void setAutonegotiation(final boolean autonegotiation) {
        this.autonegotiation = autonegotiation;
    }

    /**
     * Checks if is pause.
     *
     * @return true, if is pause
     */
    public boolean isPause() {
        return this.pause;
    }

    /**
     * Sets the pause.
     *
     * @param pause
     *            the new pause
     */
    public void setPause(final boolean pause) {
        this.pause = pause;
    }

    /**
     * Checks if is pause asym.
     *
     * @return true, if is pause asym
     */
    public boolean isPauseAsym() {
        return this.pauseAsym;
    }

    /**
     * Sets the pause asym.
     *
     * @param pauseAsym
     *            the new pause asym
     */
    public void setPauseAsym(final boolean pauseAsym) {
        this.pauseAsym = pauseAsym;
    }

    /**
     * Gets the highest throughput exposed by the port.
     *
     * @return the highest throughput
     */
    public Integer getHighestThroughput() {
        Integer thr = 1;
        if (this.speed10MHD) {
            thr = 5;
        }
        if (this.speed10MFD) {
            thr = 10;
        }
        if (this.speed100MHD) {
            thr = 50;
        }
        if (this.speed100MFD) {
            thr = 100;
        }
        if (this.speed1GHD) {
            thr = 500;
        }
        if (this.speed1GFD) {
            thr = 1000;
        }
        if (this.speed10GFD) {
            thr = 10000;
        }
        return thr;
    }
}

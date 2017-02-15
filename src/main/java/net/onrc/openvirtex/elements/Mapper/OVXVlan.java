/**
 * BNV - OVXVlan
 * Created by pravein on 4/12/16.
 */
package net.onrc.openvirtex.elements.Mapper;


public class OVXVlan {
    private final int tenantId;
    private final int vlan;
    String hc = new String();
    public OVXVlan(final int tenantId, final int vlan) {
        this.tenantId = tenantId;
        this.vlan = vlan;
        hc = this.tenantId + "," + this.vlan;
    }

    public int getTenantId() {
        return this.tenantId;
    }

    public int getVlan() {
        return this.vlan;
    }


    @Override
    public int hashCode() {
        return hc.hashCode();
    }
    @Override
    public boolean equals(Object arg) {
        OVXVlan compareVlan = (OVXVlan) arg;
        if (this.tenantId== compareVlan.getTenantId() &&
                this.vlan == compareVlan.getVlan()) {
            return true;
        }
        return false;
    }
    public String toString() {
        return hc;
    }

}

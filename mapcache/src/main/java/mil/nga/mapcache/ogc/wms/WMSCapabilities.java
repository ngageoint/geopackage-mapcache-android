package mil.nga.mapcache.ogc.wms;

/**
 * The wms getCapabilities xml.
 */
public class WMSCapabilities {

    /**
     * The capability from the getCapabilities xml.
     */
    private Capability capability = new Capability();

    /**
     * Gets the capability from the getCapabilities xml.
     *
     * @return The capability.
     */
    public Capability getCapability() {
        return capability;
    }

    /**
     * Sets the capability from the getCapabilites xml.
     *
     * @param capability The capability.
     */
    public void setCapability(Capability capability) {
        this.capability = capability;
    }
}

package mil.nga.mapcache.repository;

public interface GeoPackageModifier {

    /**
     * Callback to be used on completion of deleting a layer from a GeoPackage
     *
     * @param gpName GeoPackage name
     */
    void onLayerDeleted(String gpName);

}

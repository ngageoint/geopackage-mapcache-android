package mil.nga.mapcache;

import java.util.HashMap;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.map.geom.FeatureShapes;
import mil.nga.geopackage.map.geom.GoogleMapShape;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.MarkerFeature;

/**
 * Contains various states involving the map and its data.
 */
public class MapModel {
    /**
     * Bounding box around the features on the map
     */
    private BoundingBox featuresBoundingBox;

    /**
     * Bounding box around the tiles on the map
     */
    private BoundingBox tilesBoundingBox;

    /**
     * Active GeoPackages
     */
    private GeoPackageDatabases active;

    /**
     * True when a tile layer is drawn from features
     */
    private boolean featureOverlayTiles = false;

    /**
     * Feature shapes
     */
    private final FeatureShapes featureShapes = new FeatureShapes();

    /**
     * Edit features mode
     */
    private boolean editFeaturesMode = false;

    /**
     * Mapping between marker ids and the feature ids
     */
    private final Map<String, Long> editFeatureIds = new HashMap<>();

    /**
     * Mapping between marker ids and feature objects
     */
    private final Map<String, GoogleMapShape> editFeatureObjects = new HashMap<>();

    /**
     * Edit features table
     */
    private String editFeaturesTable;

    /**
     * Edit features database
     */
    private String editFeaturesDatabase;

    /**
     * Mapping of open GeoPackage feature DAOs
     */
    private final Map<String, Map<String, FeatureDao>> featureDaos = new HashMap<>();

    /**
     * Mapping between marker ids and the features
     */
    private final Map<String, MarkerFeature> markerIds = new HashMap<>();

    /**
     * Gets the bounding box around the features on the map
     *
     * @return The bounding box around the features on the map
     */
    public BoundingBox getFeaturesBoundingBox() {
        return featuresBoundingBox;
    }

    /**
     * Sets the bounding box around the features on the map
     *
     * @param featuresBoundingBox The bounding box around the features on the map
     */
    public void setFeaturesBoundingBox(BoundingBox featuresBoundingBox) {
        this.featuresBoundingBox = featuresBoundingBox;
    }

    /**
     * Gets the bounding box around the tiles on the map
     *
     * @return The bounding box around the tiles on the map
     */
    public BoundingBox getTilesBoundingBox() {
        return tilesBoundingBox;
    }

    /**
     * Sets the bounding box around the tiles on the map
     *
     * @param tilesBoundingBox The bounding box around the tiles on the map
     */
    public void setTilesBoundingBox(BoundingBox tilesBoundingBox) {
        this.tilesBoundingBox = tilesBoundingBox;
    }

    /**
     * Gets the active geoPackages.
     *
     * @return The active geoPackages.
     */
    public GeoPackageDatabases getActive() {
        return active;
    }

    /**
     * Sets the active geoPackages.
     *
     * @param active The active geoPackages.
     */
    public void setActive(GeoPackageDatabases active) {
        this.active = active;
    }

    /**
     * Gets the flag indicating if a layer is drawn from features.
     *
     * @return True when a tile layer is drawn from features.
     */
    public boolean isFeatureOverlayTiles() {
        return featureOverlayTiles;
    }

    /**
     * Sets the flag indicating if a layer is drawn from features.
     *
     * @param featureOverlayTiles True when a tile layer is drawn from features.
     */
    public void setFeatureOverlayTiles(boolean featureOverlayTiles) {
        this.featureOverlayTiles = featureOverlayTiles;
    }

    /**
     * Gets the feature shapes.
     *
     * @return The feature shapes.
     */
    public FeatureShapes getFeatureShapes() {
        return featureShapes;
    }

    /**
     * Indicates if we are editing features.
     *
     * @return True if editing features, false if not.
     */
    public boolean isEditFeaturesMode() {
        return editFeaturesMode;
    }

    /**
     * Indicates if we are editing features.
     *
     * @param editFeaturesMode True if editing features, false if not.
     */
    public void setEditFeaturesMode(boolean editFeaturesMode) {
        this.editFeaturesMode = editFeaturesMode;
    }

    /**
     * Gets the mapping between marker ids and the feature ids
     *
     * @return The mapping between marker ids and the feature ids
     */
    public Map<String, Long> getEditFeatureIds() {
        return editFeatureIds;
    }

    /**
     * Gets the mapping between marker ids and feature objects
     *
     * @return The mapping between marker ids and feature objects
     */
    public Map<String, GoogleMapShape> getEditFeatureObjects() {
        return editFeatureObjects;
    }

    /**
     * Gets the edit feature table.
     *
     * @return The name of the table being edited.
     */
    public String getEditFeaturesTable() {
        return editFeaturesTable;
    }

    /**
     * Sets the edit feature table.
     *
     * @param editFeaturesTable The name of the table being edited.
     */
    public void setEditFeaturesTable(String editFeaturesTable) {
        this.editFeaturesTable = editFeaturesTable;
    }

    /**
     * Gets the geoPackage name being edited.
     *
     * @return The geoPackage name.
     */
    public String getEditFeaturesDatabase() {
        return editFeaturesDatabase;
    }

    /**
     * Sets the geoPackage name being edited.
     *
     * @param editFeaturesDatabase The geoPackage name.
     */
    public void setEditFeaturesDatabase(String editFeaturesDatabase) {
        this.editFeaturesDatabase = editFeaturesDatabase;
    }

    /**
     * Gets the feature data access objects.
     *
     * @return The feature data access objects.
     */
    public Map<String, Map<String, FeatureDao>> getFeatureDaos() {
        return featureDaos;
    }

    /**
     * Gets the mapping between marker ids and the features.
     *
     * @return The mapping between marker ids and the features.
     */
    public Map<String, MarkerFeature> getMarkerIds() {
        return markerIds;
    }
}

package mil.nga.mapcache.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.repository.GeoPackageModifier;
import mil.nga.mapcache.repository.GeoPackageRepository;
import mil.nga.mapcache.view.map.feature.FeatureViewObjects;
import mil.nga.sf.GeometryType;


public class GeoPackageViewModel extends AndroidViewModel implements IIndexerTask {

    /**
     * Repository to access GeoPackages and provide data
     */
    private GeoPackageRepository repository;

    /**
     * List of active tables
     */
    private final MutableLiveData<List<GeoPackageTable>> activeTables = new MutableLiveData<>();

    /**
     * List of GeoPackageTable objects organized by GeoPackage Name
     */
    private final MutableLiveData<List<List<GeoPackageTable>>> geoPackageTables = new MutableLiveData<>();

    /**
     * List of (closed) GeoPackage objects
     */
    private final MutableLiveData<List<GeoPackage>> geoPackages = new MutableLiveData<>();

    /**
     * geos is a GeoPackageDatabases object powered by the repository.  Contains a list of all
     * GeoPackageTables opened in this project
     */
    private MutableLiveData<GeoPackageDatabases> geos = new MutableLiveData<>();


    /**
     * Constructor
     * @param application application
     */
    public GeoPackageViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Init
     * Create our live data objects and generate the data for the first time
     */
    public void init() {
        repository = new GeoPackageRepository(getApplication());
        activeTables.setValue(new ArrayList<>());
        geos = getGeos();
        regenerateGeoPackageTableList();
    }

    /**
     *  Get geos live data from repository
     */
    public MutableLiveData<GeoPackageDatabases> getGeos(){
        return repository.getGeos();
    }

    /**
     * Returns a GeoPackageDatabase from the geos list
     * @param geoPackageName The name of the geoPackage to return
     * @return a GeoPackageDatabase object
     */
    public GeoPackageDatabase getGeoByName(String geoPackageName){
        return geos.getValue() != null ? geos.getValue().getDatabase(geoPackageName) : null;
    }

    /**
     * Gets the geoPackage by name.
     * @param name The name of the geoPackage to get.
     * @return The geoPackage.
     */
    public GeoPackage getGeoPackage(String name) {
        return repository.getGeoPackageByName(name);
    }

    /**
     * Returns true if the given table name exists in the given geoPackage name
     */
    public boolean tableExistsInGeoPackage(String geoName, String tableName){
        return repository.tableExistsInGeoPackage(geoName, tableName);
    }

    /**
     *  Get active live data from repository
     */
    public MutableLiveData<GeoPackageDatabases> getActive(){
        return repository.getActive();
    }

    /**
     * Sets the layer's active state to the given value
     * @param table GeoPackageTable type
     */
    public void setLayerActive(GeoPackageTable table){
        repository.setLayerActive(table);
    }

    /**
     * Sets all the layers active in the given geoPackage
     * @param db GeoPackageDatabase to add
     * @param active should all layers be active or inactive
     */
    public void setAllLayersActive(boolean active, GeoPackageDatabase db){
        repository.setAllLayersActive(active, getGeoByName(db.getDatabase()));
    }

    /**
     * Remove all active layers for the given database
     */
    public void removeActiveTableLayers(String geoPackageName){
        repository.removeActiveForGeoPackage(geoPackageName);
    }

    /**
     * Search for the layer name in the GeoPackage and return true if it's found and deleted
     * @param geoPackageName Name of the GeoPackage to remove the active layer from
     * @param layerName Name of the layer to remove
     */
    public void removeActiveLayer(String geoPackageName, String layerName){
        repository.removeActiveLayer(geoPackageName, layerName);
    }

    /**
     * Remove all active tables
     */
    public void clearAllActive(){
        repository.clearAllActive();
    }

    /**
     * List<GeoPackage>
     */
    public MutableLiveData<List<GeoPackage>> getGeoPackages() {
        return geoPackages;
    }
    public void setGeoPackages(List<GeoPackage> geoPackages) {
        this.geoPackages.setValue(geoPackages);
    }

    /**
     * Opens a geoPackage and pulls out all objects needed for a view created by clicking on a
     * Feature point.
     * @return FeatureViewObjects object containing only the needed parts of the geoPackage
     */
    public FeatureViewObjects getFeatureViewObjects(MarkerFeature markerFeature){
        return repository.getFeatureViewObjects(markerFeature);
    }

    /**
     * Remove the given layer from a geoPackage, then call the callback after the geoPackage lists
     * have been updated
     */
    public void removeLayerFromGeo(String geoPackageName, String layerName,
                                                 GeoPackageModifier callback){
        if(repository.removeLayerFromGeo(geoPackageName, layerName)) {
            regenerateGeoPackageTableList();
            callback.onLayerDeleted(geoPackageName);
        }
    }

    /**
     * Rename a layer in a geoPackage
     */
    public GeoPackageDatabase renameLayer(String geoPackageName, String layerName, String newLayerName){
        if(repository.renameLayer(geoPackageName, layerName, newLayerName)){
            GeoPackageDatabase db = repository.getDatabaseByName(geoPackageName);
            regenerateGeoPackageTableList();
            return db;
        }
        return null;
    }

    /**
     * Rename a GeoPackage, then find and change that old name in the activeTables list
     * @param oldName The current name of the geoPackage.
     * @param newName The new name for the geoPackage.
     * @return True if the rename was successful, false if not.
     */
    public boolean setGeoPackageName(String oldName, String newName){
        if(repository == null){
            repository = new GeoPackageRepository(getApplication());
        }
        if(repository.setGeoPackageName(oldName, newName)) {
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Update the List of GeoPackageTable by asking the repository to update
     */
    public void regenerateGeoPackageTableList(){
        List<List<GeoPackageTable>> databaseTables = repository.regenerateTableList();
         geoPackageTables.postValue(databaseTables);
//         generateGeoPackageList();
        geoPackages.postValue(repository.getGeoPackages());
//        geos.postValue(repository.getGeos().getValue());
    }

    /**
     * Delete GeoPackage and regenerate the list of GeoPackages
     */
    public boolean deleteGeoPackage(String geoPackageName){
        if(repository.deleteGeoPackage(geoPackageName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Create GeoPackage and regenerate the list of GeoPackages
     */
    public boolean createGeoPackage(String geoPackageName){
        if(repository.createGeoPackage(geoPackageName)){
            //generateGeoPackageList();
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * import a geoPackage from url.  GeoPackageProgress should be an instance of DownloadTask
     */
    public boolean importGeoPackage(String name, URL source, GeoPackageProgress progress){
        if(repository.importGeoPackage(name, source, progress)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }


    /**
     * Copy GeoPackage and regenerate the list of GeoPackages
     */
    public boolean copyGeoPackage(String geoPackageName, String newName){
        if(repository.copyGeoPackage(geoPackageName, newName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Copy a layer and regenerate the list of GeoPackages
     */
    public boolean copyLayer(String geoPackageName, String currentLayer, String newLayerName){
        if(repository.copyLayer(geoPackageName, currentLayer, newLayerName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Create a new Feature Column in the given GeoPackage Layer
     */
    public boolean createFeatureColumnLayer(String geoPackageName, String layer,
                                            String columnName, GeoPackageDataType type){
        if(repository.createFeatureColumn(geoPackageName, layer, columnName, type)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Delete the given Feature Column in the GeoPackage Layer
     */
    public boolean deleteFeatureColumnLayer(String geoPackageName, String layer,
                                            String columnName){
        if(repository.deleteFeatureColumn(geoPackageName, layer, columnName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }


    /**
     *  Returns a database file
     */
    public File getDatabaseFile(String database){
        return repository.getDatabaseFile(database);
    }

    /**
     *  Returns true if it's an external db
     */
    public boolean isExternal(String database){
        return repository.isExternal(database);
    }

    /**
     *  Returns true if the db exists
     */
    public boolean exists(String database){
        return repository.exists(database);
    }

    /**
     *  Returns true if the database name exists (without opening the manager, this is a fast name check)
     */
    public boolean geoPackageNameExists(String database){
        return repository.geoPackageNameExists(database);
    }

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @return true if imported successfully
     */
    public boolean importGeoPackageAsExternalLink(String path, String database){
        if(repository.importGeoPackageAsExternalLink(path, database)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @param progress progress tracker
     * @return true if loaded
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    GeoPackageProgress progress){
        if(repository.importGeoPackage(database, stream, progress)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Gets the list of geoPackage database names.
     * @return The geoPackage names available to user.
     */
    public List<String> getDatabases() {
        return repository.getDatabases();
    }

    /**
     * Create feature table in the given geoPackage
     */
    public boolean createFeatureTable(String gpName, BoundingBox boundingBox, GeometryType geometryType, String tableName){
        if(repository.createFeatureTable(gpName, boundingBox, geometryType, tableName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Get an alert dialog filled with a GeoPackage's details
     * @param geoPackageName The name of the geoPackage to get details for.
     * @param activity The activity to own the dialog.
     * @return The detail dialog.
     */
    public AlertDialog getGeoPackageDetailDialog(String geoPackageName, Activity activity){
        return repository.getGeoPackageDetailDialog(geoPackageName, activity);
    }

    // Indexing functions
    @Override
    public void onIndexerCancelled(String result) {

    }
    @Override
    public void onIndexerPostExecute(String result) {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            repository.close();
        } catch (IOException e) {
            Log.e(GeoPackageViewModel.class.getSimpleName(), e.getMessage(), e);
        }
    }
}

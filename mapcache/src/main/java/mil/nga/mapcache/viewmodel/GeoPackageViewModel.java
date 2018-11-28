package mil.nga.mapcache.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.mapcache.GeoPackageManagerFragment;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.indexer.IndexerTask;
import mil.nga.mapcache.repository.GeoPackageRepository;
import mil.nga.sf.GeometryType;


public class GeoPackageViewModel extends AndroidViewModel implements IIndexerTask {

    private GeoPackageRepository repository;

//    private MutableLiveData<GeoPackageDatabases> active = new MutableLiveData<GeoPackageDatabases>();
    private MutableLiveData<List<GeoPackageTable>> activeTables = new MutableLiveData<>();
    private MutableLiveData<List<List<GeoPackageTable>>> geoPackageTables = new MutableLiveData<List<List<GeoPackageTable>>>();
    private MutableLiveData<List<GeoPackage>> geoPackages = new MutableLiveData<>();

    public GeoPackageViewModel(@NonNull Application application) {
        super(application);
    }


    public void init() {
        repository = new GeoPackageRepository(getApplication());
        activeTables.setValue(new ArrayList<GeoPackageTable>());
//        generateGeoPackageList();
        regenerateGeoPackageTableList();
//        geoPackageTables.setValue(geoList);
//        geoPackages.setValue(geoPackageList);
    }


    /**
     * List<List<GeoPackageTable>
     */
    public void setGeoPackageTables(List<List<GeoPackageTable>> newGeoPackageTables) {
        geoPackageTables.setValue(newGeoPackageTables);
    }
    public MutableLiveData<List<List<GeoPackageTable>>> getGeoPackageTables() {
        return geoPackageTables;
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

    public MutableLiveData<List<GeoPackageTable>> getActiveTables() {
        return activeTables;
    }
    public void setActiveTables(List<GeoPackageTable> newTables){
        activeTables.setValue(newTables);
    }

    /**
     *  Return true if the given table is in the list of active tables
     * @param geoPackageName
     * @param tableName
     * @return
     */
    public boolean isTableActive(String geoPackageName, String tableName){
        if(getActiveTables().getValue() != null)
        {
            for(GeoPackageTable table : getActiveTables().getValue()){
                if(table.getDatabase().equalsIgnoreCase(geoPackageName) && table.getName().equalsIgnoreCase(tableName)){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Add the table to the activeTables list (used to enable a layer on the map)
     * @param newTable
     */
    public void addToTables(GeoPackageTable newTable){
        List<GeoPackageTable> newTables = activeTables.getValue();
        newTables.add(newTable);
        activeTables.postValue(newTables);
    }

    /**
     * Find the given table in the table list, and add to activeTables if found
     * @param tableName
     * @param geoPackageName
     * @return true if the table was added
     */
    public boolean addTableByName(String tableName, String geoPackageName){
        // Use tableName and GeoPackageName to find the geoPackageTable in the livedata list
        for(List<GeoPackageTable> geoTableList : getGeoPackageTables().getValue()){
            if(geoTableList.size() > 0) {
              if(geoTableList.get(0).getDatabase().equalsIgnoreCase(geoPackageName)) {
                  for (GeoPackageTable table : geoTableList) {
                      if (table.getName().equalsIgnoreCase(tableName)){
                          // Save the geopackage with the layer as active
                          repository.getTableObject(geoPackageName, tableName, true);
                          // Add to our list of active tables
                          addToTables(table);
                          return true;
                      }
                  }
              }
            }
        }
        return false;
    }

//    /**
//     * Rename the given layer in the geopackage
//     */
//    public boolean renameLayer(String layerName, String geoPackageName, String newName){
//        if(repository.renameLayer(geoPackageName, layerName, newName)) {
//            regenerateGeoPackageTableList();
//            return true;
//        }
//        return false;
//    }


    /**
     * Find the given table in the table list, and remove from activeTables if found
     * @param tableName
     * @param geoPackageName
     * @return true if the table was removed
     */
    public boolean removeActiveTableByName(String tableName, String geoPackageName){
        List<GeoPackageTable> currentTables = activeTables.getValue();
        if(currentTables != null && currentTables.size() > 0) {
            for (GeoPackageTable table : currentTables) {
                if(table.getName().equalsIgnoreCase(tableName) && table.getDatabase().equalsIgnoreCase(geoPackageName)){
                    currentTables.remove(table);
                    activeTables.postValue(currentTables);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove all active layers for the given database
     */
    public boolean removeActiveTableLayers(String geoPackageName){
        List<GeoPackageTable> currentTables = activeTables.getValue();
        if(currentTables != null && currentTables.size() > 0) {
            Iterator<GeoPackageTable> tableIterator = currentTables.iterator();
            while(tableIterator.hasNext()){
                // Only delete if the geopackage name matches
                GeoPackageTable table = tableIterator.next();
                if(table.getDatabase().equalsIgnoreCase(geoPackageName)) {
                    tableIterator.remove();
                }
            }
        }
        activeTables.postValue(currentTables);
        return true;
    }

    /**
     * Remove the given layer from a geopackage
     */
    public boolean removeLayerFromGeo(String geoPackageName, String layerName){
        if(repository.removeLayerFromGeo(geoPackageName, layerName)) {
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     * Enable all layers of the given geopackage name
     */
    public boolean enableAllLayers(String geoPackageName){
        List<GeoPackageTable> currentTables = activeTables.getValue();
        if(currentTables != null && currentTables.size() > 0) {
            Iterator<GeoPackageTable> tableIterator = currentTables.iterator();
            // First remove all layers from the list that match the given name
            while (tableIterator.hasNext()) {
                // Only delete if the geopackage name matches
                GeoPackageTable table = tableIterator.next();
                if (table.getDatabase().equalsIgnoreCase(geoPackageName)) {
                    tableIterator.remove();
                }
            }
        }

        // Then just add all layers to the active list
        for(List<GeoPackageTable> geoTableList : getGeoPackageTables().getValue()){
            if(geoTableList.size() > 0) {
                if(geoTableList.get(0).getDatabase().equalsIgnoreCase(geoPackageName)) {
                    for (GeoPackageTable table : geoTableList) {
                            currentTables.add(table);
                    }
                }
            }
        }


        activeTables.postValue(currentTables);
        return true;
    }





    /**
     * Get a single GeoPackage by name
     * @param name
     * @return
     */
    public GeoPackage getGeoPackageByName(String name){
        if(repository == null){
            repository = new GeoPackageRepository(getApplication());
        }
        return repository.getGeoPackageByName(name);
    }

    /**
     * Rename a GeoPackage, then find and change that old name in the activeTables list
     * @param oldName
     * @param newName
     * @return
     */
    public boolean setGeoPackageName(String oldName, String newName){
        if(repository == null){
            repository = new GeoPackageRepository(getApplication());
        }
        if(repository.setGeoPackageName(oldName, newName)) {
            regenerateGeoPackageTableList();
            renameActiveGeoPackages(oldName, newName);
            return true;
        }
        return false;
    }


    /**
     * Iterate through the current list of active tables.  Find any table that matches the old
     * geopackage (database) name, and rename it to the new one
     * @param oldName
     * @param newName
     * @return
     */
    private boolean renameActiveGeoPackages(String oldName, String newName){
        boolean updated = false;
        if(getActiveTables().getValue() != null)
        {
            List<GeoPackageTable> activeGeos = getActiveTables().getValue();
            for(GeoPackageTable table : activeGeos){
                if(table.getDatabase().equalsIgnoreCase(oldName)){
                    updated = true;
                    table.setDatabase(newName);
                }
            }
            if(updated) {
                setActiveTables(activeGeos);
            }
            return updated;
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
        for(List<GeoPackageTable> tableList : databaseTables){
            for(GeoPackageTable table : tableList){
                if(table.isActive()){
                    addToTables(table);
                }
            }

        }
        geoPackages.postValue(repository.getGeoPackages());

    }

    /**
     * Generate the list of geopackage objects
     */
    public void generateGeoPackageList(){
        if(repository == null){
            repository = new GeoPackageRepository(getApplication());
        }
        geoPackages.postValue(repository.getGeoPackages());

    }



    /**
     * Delete GeoPackage and regenerate the list of GeoPackages
     */
    public boolean deleteGeoPackage(String geoPackageName){
        if(repository.deleteGeoPackage(geoPackageName)){
            generateGeoPackageList();
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
     * import a geopackage from url.  GeoPackageProgress should be an instance of DownloadTask
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
     *  Returns the GeoPackage's size
     */
    public String getGeoPackageSize(String geoPackageName){
        return repository.getGeoPackageSize(geoPackageName);
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
//            // Then index any feature tables
//            List<String> newFeatures = repository.getFeatureTables(database);
//            if(!newFeatures.isEmpty()){
//                for(String tableName : newFeatures){
//                    indexFeatures(activity, database, tableName);
//
//                }
//            }
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

    /**
     *  Returns the list of tile tables for a geopackage
     */
    public List<String> getTileTables(String database){
        return repository.getTileTables(database);
    }

    /**
     *  Returns the list of feature tables for a geopackage
     */
    public List<String> getFeatureTables(String database){
        return repository.getFeatureTables(database);
    }

    /**
     * Get a GeoPackageTable object and set the active state
     */
    public GeoPackageTable getTableObjectActive(String gpName, String layerName){
        GeoPackageTable table = repository.getTableObject(gpName, layerName, null);
        table.setActive(isTableActive(gpName, layerName));
        return table;
    }

    /**
     * Get a GeoPackageTable object
     */
    public GeoPackageTable getTableObject(String gpName, String layerName){
        return repository.getTableObject(gpName, layerName, null);
    }

    public Contents getTableContents(String gpName, String tableName){
        return repository.getTableContents(gpName, tableName);
    }


    /**
     * Index the given features table
     */
    public boolean indexFeatures(Activity activity, String database, String tableName){
        IndexerTask.indexFeatures(activity, GeoPackageViewModel.this, database, tableName, FeatureIndexType.GEOPACKAGE);
        return true;
    }

    /**
     * Create feature table in the given geopackage
     */
    public boolean createFeatureTable(String gpName, BoundingBox boundingBox, GeometryType geometryType, String tableName){
        if(repository.createFeatureTable(gpName, boundingBox, geometryType, tableName)){
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }


    // Indexing functions
    @Override
    public void onIndexerCancelled(String result) {

    }
    @Override
    public void onIndexerPostExecute(String result) {

    }
}

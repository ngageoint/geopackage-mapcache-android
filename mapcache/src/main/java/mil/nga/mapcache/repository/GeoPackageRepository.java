package mil.nga.mapcache.repository;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.extension.nga.scale.TileTableScaling;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.extension.related.media.MediaDao;
import mil.nga.geopackage.extension.related.media.MediaRow;
import mil.nga.geopackage.extension.related.media.MediaTable;
import mil.nga.geopackage.extension.related.media.MediaTableMetadata;
import mil.nga.geopackage.extension.rtree.RTreeIndexExtension;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.FeatureTableMetadata;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileTableMetadata;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.mapcache.GeoPackageMapFragment;
import mil.nga.mapcache.R;
import mil.nga.mapcache.data.GeoPackageDatabase;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageFeatureTable;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.data.GeoPackageTileTable;
import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.load.LoadTilesTask;
import mil.nga.mapcache.view.map.feature.FeatureViewObjects;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.sf.GeometryType;

/**
 *  Repository to provide access to stored GeoPackages.  Most of the data in the app is powered by
 *  the 'geos' object, and instance of GeoPackageDatabases
 */
public class GeoPackageRepository {

    private GeoPackageManager manager;
    private List<GeoPackage> geoPackages = new ArrayList<>();

    /**
     * Live object ONLY tracks active GeoPackage tables
     */
    private MutableLiveData<GeoPackageDatabases> active = new MutableLiveData<>();

    /**
     * Live object to track currently opened GeoPackages in the application.  Inside this object,
     * it's tracked as a map:
     * Map<String, GeoPackageDatabase>
     */
    private MutableLiveData<GeoPackageDatabases> geos = new MutableLiveData<>();

    /**
     * Set this to application context for the GeoPackageDatabases object
     */
    private Context context;


    /**
     * Constructor
     * @param application the running application
     */
    public GeoPackageRepository(@NonNull Application application) {
        context = application.getApplicationContext();
        manager = GeoPackageFactory.getManager(application);
        active.setValue(new GeoPackageDatabases(context, "active"));
        geos.setValue(new GeoPackageDatabases(context, "all"));
    }


    /**
     * GeoPackageDatabases Live Data (geos) ----
     */

    /**
     * Get GeopackageDatabases object for tracking all geoPackages open in the application
     * @return GeoPackageDatabases object
     */
    public MutableLiveData<GeoPackageDatabases> getGeos(){
        return geos;
    }

    /**
     * Add GeoPackageTables to the GeoPackageDatabases live data object
     * @param tables - List of GeoPackageTables to add to the list
     */
    private void addTablesToDatabases(List<GeoPackageTable> tables){
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            for(GeoPackageTable table : tables) {
                currentGeos.addTable(table);
            }
            geos.postValue(currentGeos);
        }
    }

    /**
     * Add a GeoPackageDatabase with no tables to the Databases list
     * @param dbName
     */
    private void addEmptyDatabase(String dbName){
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.addEmptyDatabase(dbName);
            geos.postValue(currentGeos);
        }
    }

    /**
     * Returns true of the given table name exists in the given geopackage name
     */
    public boolean tableExistsInGeoPackage(String geoName, String tableName){
        GeoPackageDatabases currentGeos = geos.getValue();
        return currentGeos.exists(geoName, tableName);
    }

    /**
     * Finds the given database in the stored list and sets the size field
     * @param databaseName - name of the geopackage to find
     * @param size - size of the geopackage in string format
     */
    private void setGeoSize(String databaseName, String size){
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.setDatabaseSize(databaseName, size);
            geos.postValue(currentGeos);
        }
    }

    /**
     * Gets the GeoPackageDatabase object by name from the geos list
     * @param gpName Name of the Database to find
     * @return A GeoPackageDatabaseObject or null
     */
    public GeoPackageDatabase getDatabaseByName(String gpName){
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            return currentGeos.getDatabase(gpName);
        }
        return null;
    }

    /**
     * Remove the given layer from the open geos list
     * @param geoPackageName Name of the GeoPackage containing the layer
     * @param layerName Name of the layer to remove
     * @return true if it was found and removed
     */
    public boolean removeLayerFromGeos(String geoPackageName, String layerName){
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.removeTable(geoPackageName, layerName);
            regenerateTableList();
//            geos.postValue(currentGeos);
            return true;
        }
        return false;
    }


    /**
     * Active GeoPackage Layers --------------
     */

    public MutableLiveData<GeoPackageDatabases> getActive(){
        return active;
    }

    /**
     * If the table is active, it's added to the currentActive table's object.  If it's not active,
     * it's removed from currentActive.  Then the new currentActive object value is posted
     * @param table GeoPackageTable
     */
    public boolean setLayerActive(GeoPackageTable table){
        GeoPackageDatabases currentActive = active.getValue();
        if(currentActive != null) {
            if(table.isActive()) {
                currentActive.addTable(table);
            } else {
                currentActive.removeTable(table);
            }
            active.postValue(currentActive);
        }

        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.setTableActive(table);
            geos.postValue(currentGeos);
        }
        return true;
    }

    /**
     * Remove all active tables for the given GeoPackage name
     * @param geoPackageName name of the geopackage to remove all active layers for
     * @return true if the geopackage was found and deleted
     */
    public boolean removeActiveForGeoPackage(String geoPackageName){
        GeoPackageDatabases currentActive = active.getValue();
        if(currentActive != null) {
            currentActive.removeDatabase(geoPackageName, false);
            active.postValue(currentActive);
        }
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.setDatabaseLayersActive(false, geoPackageName);
            geos.postValue(currentGeos);
        }
        return true;
    }

    /**
     * Remove the given layer from the active table list
     * @param geoPackageName Name of the GeoPackage containing the layer
     * @param layerName Name of the layer to set to inactive
     * @return true if it was found and removed
     */
    public boolean removeActiveLayer(String geoPackageName, String layerName){
        GeoPackageDatabases currentActive = active.getValue();
        if(currentActive != null) {
            currentActive.removeTable(geoPackageName, layerName);
            active.postValue(currentActive);
        }
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.setLayerActive(false, geoPackageName, layerName);
            geos.postValue(currentGeos);
        }
        return true;
    }

    /**
     * Clear all active tables
     */
    public void clearAllActive(){
        GeoPackageDatabases currentActive = active.getValue();
        if(currentActive != null) {
            currentActive.clearAllDatabases();
            active.postValue(currentActive);
        }
        GeoPackageDatabases currentGeos = geos.getValue();
        if(currentGeos != null) {
            currentGeos.setAllDatabaseLayersActive(false);
            geos.postValue(currentGeos);
        }
    }



    /**
     * Set all layers active in the given geopackage
     * @param db GeoPackageDatabase object to add
     * @param enable should all layers be active or inactive
     * @return true if all layers are now active
     */
    public boolean setAllLayersActive(boolean enable, GeoPackageDatabase db){
        GeoPackageDatabases currentActive = active.getValue();
        GeoPackageDatabases currentGeos = geos.getValue();

        if(enable) {
            if (currentActive != null) {
                currentActive.addAll(db);
            }
            if(currentGeos != null) {
                currentGeos.addAll(db);
            }
        }else{
            if (currentActive != null) {
                // the active list needs the tables removed
                currentActive.removeDatabase(db.getDatabase(), false);
            }
            if(currentGeos != null) {
                // The current list needs them set to false
                currentGeos.setDatabaseLayersActive(false, db.getDatabase());
            }
        }
        active.postValue(currentActive);
        geos.postValue(currentGeos);

        return true;
    }




    public GeoPackage getGeoPackageByName(String name) {
        try {
            GeoPackage geo = manager.open(name);
            geo.close();
            return geo;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Quick way to check if the geopackage name is already taken (ignoring caps) without using the manager
     * @param name name of the Geopackage to look for
     * @return true if the name is already taken
     */
    public boolean geoPackageNameExists(String name){
        if(getGeos().getValue().geoPackageNameExists(name)){
            return true;
        }
        return false;
    }


    public boolean setGeoPackageName(String oldName, String newName) {
        // If the new name already exists, but it's not renaming the old name, then exit to
        // prevent duplicating GeoPackages
        if (getGeos().getValue().geoPackageNameExists(newName)) {
            if(!oldName.equalsIgnoreCase(newName)){
                return false;
            }
        }

        // Rename the GeoPackage in the active list first
        GeoPackageDatabases currentActive = active.getValue();
        if(currentActive != null) {
            currentActive.renameDatabase(oldName, newName);
        }
        return manager.rename(oldName, newName);
    }

    public List<GeoPackage> getGeoPackages() {
        return geoPackages;
    }


    /**
     * Using the Manager to generate our list of GeoPackages.  This should populate live data for
     * the application to use GeoPackages.
     * @return List of GeoPackageTables for every GeoPackage
     */
    public List<List<GeoPackageTable>> regenerateTableList() {
        geoPackages.clear();
        geos.getValue().getDatabases().clear();
        List<List<GeoPackageTable>> databaseTables = new ArrayList<List<GeoPackageTable>>();
        StringBuilder errorMessage = new StringBuilder();
        Iterator<String> databasesIterator = manager.databases().iterator();
        while (databasesIterator.hasNext()) {
            boolean invalidGP = false;
            String database = databasesIterator.next();

            // Delete any databases with invalid headers
            if (!manager.validateHeader(database)) {
                if (manager.delete(database)) {
                    databasesIterator.remove();
                }
            } else {

                // Read the feature and tile tables from the GeoPackage
                List<Exception> exceptions = new ArrayList<>();
                GeoPackage geoPackage = null;
                List<GeoPackageTable> tables = new ArrayList<GeoPackageTable>();
                // This is a simple list of layer names (will be assigned to the 'allTables' var)
                List<String> tableNames = new ArrayList<>();
                try {
                    geoPackage = manager.open(database, false);
                    ContentsDao contentsDao = geoPackage.getContentsDao();
                    // Make sure the Database is added even if it has no tables
                    addEmptyDatabase(geoPackage.getName());
                    List<String> featureTables = null;
                    try {
                        featureTables = geoPackage.getFeatureTables();
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                    if (featureTables != null) {
                        try {
                            for (String tableName : featureTables) {
                                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                                int count = featureDao.count();

                                GeometryType geometryType = null;
                                String description = "";
                                try {
                                    Contents contents = contentsDao.queryForId(tableName);
                                    GeometryColumns geometryColumns = contents
                                            .getGeometryColumns();
                                    geometryType = geometryColumns.getGeometryType();
                                    description = contents.getDescription();
                                } catch (Exception e) {
                                }
                                GeoPackageFeatureTable table = new GeoPackageFeatureTable(database,
                                        tableName, geometryType, count);
                                table.setDescription(description);
                                table.setActive(active.getValue().exists(table));
                                table.setFeatureColumns(featureDao.getColumns());
                                tables.add(table);
                                // Update simple list of layer names
                                tableNames.add(table.getName());
                            }
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

                    List<String> tileTables = null;
                    try {
                        tileTables = geoPackage.getTileTables();
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                    if (tileTables != null) {
                        try {
                            for (String tableName : tileTables) {
                                TileDao tileDao = geoPackage.getTileDao(tableName);
                                int count = tileDao.count();
                                GeoPackageTable table = new GeoPackageTileTable(database,
                                        tableName, count);
                                table.setDescription("An image layer with " + count + " tiles");
                                table.setActive(active.getValue().exists(table));
                                ((GeoPackageTileTable) table).setMaxZoom(tileDao.getMaxZoom());
                                ((GeoPackageTileTable) table).setMinZoom(tileDao.getMinZoom());
                                tables.add(table);
                                // Update simple list of layer names
                                tableNames.add(table.getName());

                            }
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

//                    for (GeoPackageFeatureOverlayTable table : active.featureOverlays(database)) {
//                        try {
//                            FeatureDao featureDao = geoPackage.getFeatureDao(table.getFeatureTable());
//                            int count = featureDao.count();
//                            table.setCount(count);
//                            tables.add(table);
//                        } catch (Exception e) {
//                            exceptions.add(e);
//                        }
//                    }

                } catch (Exception e) {
                    // If the error message contains "invalid geopackage", this GP will be labeled as invalid
                    invalidGP = e.toString().indexOf("Invalid GeoPackage") != -1 ? true : false;
                    exceptions.add(e);
                }

                if (geoPackage != null) {
                    geoPackage.close();
                    geoPackages.add(geoPackage);

                }


                // If There are no tables under the database, create a blank table so that we can at
                // least pass the database name up to the recycler view
//                if (tables.isEmpty() && exceptions.isEmpty()) {
//                    GeoPackageTable table = new GeoPackageFeatureTable(database, "", GeometryType.GEOMETRY, 0);
//                    tables.add(table);
//                }

                if (exceptions.isEmpty()) {
                    databaseTables.add(tables);
                    addTablesToDatabases(tables);
//                    geoAdapter.insertToEnd(tables);
                    // Set the size field of the database in our stored list
                    setGeoSize(database, manager.readableSize(database));
                } else {
                    // On exception, check the integrity of the database and delete if not valid
                    if (!manager.validateIntegrity(database) && manager.delete(database)) {
                        databasesIterator.remove();
                    } else {
                        // If a geopackage is missing tables, it's invalid, don't add to the list.
                        // make sure it's deleteed
                        if (!invalidGP) {
                            databaseTables.add(tables);
                            addTablesToDatabases(tables);
//                        geoAdapter.insertToEnd(tables);
                        } else {
                            manager.delete(database);
                        }

                    }

                    if (errorMessage.length() > 0) {
                        errorMessage.append("\n\n\n");
                    }
                    errorMessage.append(database).append(" Errors:");
                    for (Exception exception : exceptions) {
                        errorMessage.append("\n\n");
                        errorMessage.append(exception.getMessage());
                    }
                }
            }
        }
        // Make sure to still post the value of Geos if it's empty
        if(geos.getValue().isEmpty()){
            geos.setValue(geos.getValue());
        }
        return databaseTables;
    }


    /**
     * Returns the GeoPackage's size
     */
    public String getGeoPackageSize(String geoPackageName) {
        return manager.readableSize(geoPackageName);
    }

    /**
     * Delete a geoPackage by name
     */
    public boolean deleteGeoPackage(String geoPackageName) {
        removeActiveForGeoPackage(geoPackageName);
        return manager.delete(geoPackageName);
    }

    /**
     * Delete a layer from a geopackage in both the manager and make sure it's removed from the
     * geos list
     */
    public boolean removeLayerFromGeo(String geoPackageName, String layerName) {
        try {
            removeLayerFromGeos(geoPackageName, layerName);
            GeoPackage geo = manager.open(geoPackageName);
            if (geo != null) {
                geo.deleteTable(layerName);
                geo.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rename a layer in a geopackage
     */
    public boolean renameLayer(String geoPackageName, String layerName, String newLayerName){
        try {
            GeoPackage geo = manager.open(geoPackageName);
            if (geo != null) {
                geo.renameTable(layerName, newLayerName);
                geo.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a geoPackage by name
     */
    public boolean createGeoPackage(String geoPackageName) {
        return manager.create(geoPackageName);
    }

    /**
     * Copy a geoPackage by name
     */
    public boolean copyGeoPackage(String geoPackageName, String newName) {
        return manager.copy(geoPackageName, newName);
    }

    /**
     * Copy a layer by name
     */
    public boolean copyLayer(String geoPackageName, String layerName, String newLayerName) {
        try {
            GeoPackage geo = manager.open(geoPackageName);
            if (geo != null) {
                geo.copyTable(layerName, newLayerName);
                geo.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Opens a geopackage and pulls out all objects needed for a view created by clicking on a
     * Feature point.
     * @return FeatureViewObjects object containing only the needed parts of the geopackage
     */
    public FeatureViewObjects getFeatureViewObjects(MarkerFeature markerFeature){
        FeatureViewObjects featureObjects = new FeatureViewObjects();
        featureObjects.setGeopackageName(markerFeature.getDatabase());
        featureObjects.setLayerName(markerFeature.getTableName());
        final GeoPackage geoPackage = manager.open(markerFeature.getDatabase(), false);
        if(geoPackage != null) {
            final FeatureDao featureDao = geoPackage
                    .getFeatureDao(markerFeature.getTableName());

            final FeatureRow featureRow = featureDao.queryForIdRow(markerFeature.getFeatureId());

            // If it has RTree extensions, it's indexed and we can't save feature column data.
            // Not currently supported for Android
            RTreeIndexExtension extension = new RTreeIndexExtension(geoPackage);
            boolean hasExtension = extension.has(markerFeature.getTableName());

            if (featureRow != null) {
                final GeoPackageGeometryData geomData = featureRow.getGeometry();
                final GeometryType geometryType = geomData.getGeometry()
                        .getGeometryType();
                DataColumnsDao dataColumnsDao = (new SchemaExtension(geoPackage)).getDataColumnsDao();
                try {
                    if (!dataColumnsDao.isTableExists()) {
                        dataColumnsDao = null;
                    }
                } catch (SQLException e) {
                    dataColumnsDao = null;
                    Log.e(GeoPackageMapFragment.class.getSimpleName(),
                            "Failed to check if Data Columns table exists for GeoPackage: "
                                    + geoPackage.getName(), e);
                }

                // Get extensions for attachments
                HashMap<Long,Bitmap> bitmaps = new HashMap<>();
                RelatedTablesExtension related = new RelatedTablesExtension(geoPackage);
                List<ExtendedRelation> relationList = related.getRelationships();
                for(ExtendedRelation relation : relationList){
                    String tableName = relation.getBaseTableName();
                    if(tableName.equalsIgnoreCase(markerFeature.getTableName())){
                        MediaDao mediaDao = related.getMediaDao(relation);
                        // Get list of mediaIds from related table instead of iterating mediaCursor
                        String relatedTableName = relation.getBaseTableName();
                        List<Long> mediaIds = new ArrayList<>();
                        if(tableName.equalsIgnoreCase(relatedTableName)){
                            mediaIds = related.getMappingsForBase(relation,featureRow.getId());
                        }
                        List<MediaRow> mediaRows = mediaDao.getRows(mediaIds);
                        for(MediaRow row : mediaRows){
                            Bitmap bitmap = row.getDataBitmap();
                            bitmaps.put(row.getId(),bitmap);
                        }
                    }
                }
                featureObjects.getBitmaps().putAll(bitmaps);
                featureObjects.setFeatureRow(featureRow);
                featureObjects.setHasExtension(hasExtension);
                featureObjects.setGeometryType(geometryType);
                featureObjects.setDataColumnsDao(dataColumnsDao);
            }
            geoPackage.close();
        }
            return featureObjects;
    }

    /**
     * Open the geopackage and update the featureDao with the given featureViewObjects data
     * @param featureViewObjects a FeatureViewObjects item containing a feature row to update
     * @return true if it updates
     */
    public HashMap<Long,Bitmap> saveFeatureObjectValues(FeatureViewObjects featureViewObjects){
        HashMap<Long,Bitmap> addedImages = new HashMap();
        if(featureViewObjects.isValid()){
            try {
                String tableName = featureViewObjects.getLayerName();
                String mediaTableName = context.getString(R.string.media_table_tag);
                final GeoPackage geoPackage = manager.open(featureViewObjects.getGeopackageName());
                if (geoPackage != null) {
                    final FeatureDao featureDao = geoPackage
                            .getFeatureDao(featureViewObjects.getLayerName());
                    featureDao.update(featureViewObjects.getFeatureRow());
                    // Add attachments
                    RelatedTablesExtension related = new RelatedTablesExtension(geoPackage);
                    if(!related.has()){
                        // Populate and validate a media table
                        List<UserCustomColumn> additionalMediaColumns = new ArrayList<>();
                        MediaTable mediaTable = MediaTable.create(
                                MediaTableMetadata.create(mediaTableName, additionalMediaColumns));
                        // Create and validate a mapping table
                        List<UserCustomColumn> additionalMappingColumns = new ArrayList<>();
                        final String mappingTableName = tableName + "_" + mediaTableName;
                        UserMappingTable userMappingTable = UserMappingTable.create(
                                mappingTableName, additionalMappingColumns);
                        related.addMediaRelationship(tableName, mediaTable,userMappingTable);
                    }
                    List<ExtendedRelation> relationList = related.getRelationships();
                    for(ExtendedRelation relation : relationList) {
                        if (relation.getBaseTableName().equalsIgnoreCase(featureViewObjects.getLayerName())) {
                            MediaDao mediaDao = related.getMediaDao(relation);
                            UserMappingDao userMappingDao = related.getMappingDao(relation);
                            int totalMappedCount = userMappingDao.count();
                            int totalMediaCount = mediaDao.count();
                            for(Map.Entry map  :  featureViewObjects.getAddedBitmaps().entrySet() ){
                                Bitmap image = (Bitmap)map.getValue();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] mediaData = stream.toByteArray();
                                String contentType = "image/png";
                                MediaRow mediaRow = mediaDao.newRow();
                                mediaRow.setData(mediaData);
                                mediaRow.setContentType(contentType);
                                mediaDao.create(mediaRow);
                                final String mappingTableName = tableName + "_" + mediaTableName;
                                UserMappingDao mappingDao = related.getMappingDao(relation);
                                UserMappingRow userMappingRow = mappingDao.newRow();
                                long featureRowId = featureViewObjects.getFeatureRow().getId();
                                long mediaRowId = mediaRow.getId();
                                userMappingRow.setBaseId(featureViewObjects.getFeatureRow().getId());
                                userMappingRow.setRelatedId(mediaRow.getId());
                                mappingDao.create(userMappingRow);
                                addedImages.put(mediaRowId,image);
                            }
                        }
                    }
                    geoPackage.close();
                    return addedImages;
                }
            }catch (Exception e){
                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                        "Error saving feature data: ", e);
            }
        }
        return addedImages;
    }



    /**
     * Open the geopackage and remove a bitmap image from a feature layer object
     * @param featureViewObjects a FeatureViewObjects item containing a feature row to update
     * @return true if it is deleted
     */
    public boolean deleteImageFromFeature(FeatureViewObjects featureViewObjects, long rowId){
        boolean deleted = false;
        if(featureViewObjects.isValid()){
            try {
                String tableName = featureViewObjects.getLayerName();
                String mediaTableName = "media_table";
                final GeoPackage geoPackage = manager.open(featureViewObjects.getGeopackageName());
                if (geoPackage != null) {
                    final FeatureDao featureDao = geoPackage
                            .getFeatureDao(featureViewObjects.getLayerName());
                    RelatedTablesExtension related = new RelatedTablesExtension(geoPackage);
                    List<ExtendedRelation> relationList = related.getRelationships();
                    for(ExtendedRelation relation : relationList) {
                        if (relation.getBaseTableName().equalsIgnoreCase(featureViewObjects.getLayerName())) {
                            MediaDao mediaDao = related.getMediaDao(relation);
                            UserMappingDao userMappingDao = related.getMappingDao(relation);
                            List<Long> mappingIds = related.getMappingsForBase(relation, featureViewObjects.getFeatureRow().getId());
                            int count = mappingIds.size();
                            List<MediaRow> rowList = mediaDao.getRows(mappingIds);
                            for(MediaRow row : rowList){
                                long id = row.getId();
                                if(row.getId() == rowId){
                                    Log.i("test","deleting");
                                    int deletedValue = mediaDao.deleteById(row.getId());
                                    deleted = true;
                                    Log.i("test","deleted: " + deleted);
                                }
                            }
                        }
                    }
//                            // Take a look at the media rows
//                            UserCustomCursor mediaCursor = mediaDao.queryForAll();
//                            int mediaCount = mediaCursor.getCount();
//                            MediaRow row;
//                            while(mediaCursor.moveToNext()){
//                                row = mediaDao.getRow(mediaCursor);
//                                Log.i("test","test");
//                            }

                    // Take a look at the existing rows - read only
//                                UserCustomCursor mappingCursor = userMappingDao.queryForAll();
//                                int cursCount = mappingCursor.getCount();
//                                long featureRowId = featureViewObjects.getFeatureRow().getId();
//                                UserMappingRow userMappingRow;
//                                while (mappingCursor.moveToNext()) {
//                                    userMappingRow = userMappingDao.getRow(mappingCursor);
//                                    long rowBaseId = userMappingRow.getBaseId();
//                                    long relatedId = userMappingRow.getRelatedId();
//                                    int index = userMappingRow.getBaseIdColumnIndex();
//                                    Log.i("Log", "test");
//                                }
                }

                    geoPackage.close();
            }catch (Exception e){
                Log.e(GeoPackageMapFragment.class.getSimpleName(),
                        "Error deleting feature image: ", e);
            }
        }
        return deleted;
    }




        /**
         * import a geopackage from url.  GeoPackageProgress should be an instance of DownloadTask
         */
    public boolean importGeoPackage(String name, URL source, GeoPackageProgress progress) {
        return manager.importGeoPackage(name, source, progress);
    }

    /**
     * Returns a database file
     */
    public File getDatabaseFile(String database) {
        return manager.getFile(database);
    }

    /**
     * Returns true if it's an external db
     */
    public boolean isExternal(String database) {
        return manager.isExternal(database);
    }

    /**
     * Returns true if exists
     */
    public boolean exists(String database) {
        return manager.exists(database);
    }

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @return true if imported successfully
     */
    public boolean importGeoPackageAsExternalLink(String path, String database) {
        return manager.importGeoPackageAsExternalLink(path, database);
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
                                    GeoPackageProgress progress) {
        return manager.importGeoPackage(database, stream, progress);
    }

    /**
     * Returns the list of tile tables for a geopackage
     */
    public List<String> getTileTables(String database){
        GeoPackage geo = manager.open(database);
        if(geo != null) {
            List<String> tiles = geo.getTileTables();
            geo.close();
            return tiles;
        }
        return null;
    }

    /**
     *  Returns the list of feature tables for a geopackage
     */
    public List<String> getFeatureTables(String database){
        GeoPackage geo = manager.open(database);
        if(geo != null) {
            List<String> features = geo.getFeatureTables();
            geo.close();
            return features;
        }
        return null;
    }


    /**
     * Create feature table in the given geopackage
     */
    public boolean createFeatureTable(String gpName, BoundingBox boundingBox, GeometryType geometryType, String tableName){
        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey(tableName,
                "geom"));
        geometryColumns.setGeometryType(geometryType);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);

        GeoPackage geoPackage = manager.open(gpName);
        try {
            SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
                    .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            geometryColumns.setSrs(srs);
            FeatureTable created = geoPackage.createFeatureTable(FeatureTableMetadata.create(
                    geometryColumns, boundingBox));
            if (created != null) {
                return true;
            }
        }catch (SQLException e){
            Log.i("Exception", e.toString());
        } finally {
            geoPackage.close();
        }
        return false;
    }

    /**
     * Create feature column in layer
     */
    public boolean createFeatureColumn(String gpName, String layerName, String columnName,
                                       GeoPackageDataType type){
        boolean created = false;
        try (GeoPackage geoPackage = manager.open(gpName)) {
            FeatureDao featureDao = geoPackage.getFeatureDao(layerName);
            featureDao.addColumn(FeatureColumn.createColumn(columnName, type));
            created = true;
        } catch (Exception e) {
            Log.i("Feature Column Error", e.toString());
        }
        return created;
    }


    /**
     * Delete feature column from a layer
     */
    public boolean deleteFeatureColumn(String gpName, String layerName, String columnName){
        boolean deleted= false;
        try (GeoPackage geoPackage = manager.open(gpName)) {
            FeatureDao featureDao = geoPackage.getFeatureDao(layerName);
            featureDao.dropColumn(columnName);
            deleted = true;
        } catch (Exception e) {
            Log.i("Feature Column Error", e.toString());
        }
        return deleted;
    }


    /**
     * Get feature columns from table
     */
    public List<FeatureColumn> getFeatureColumnsFromTable(String gpName, String layerName){
        try (GeoPackage geoPackage = manager.open(gpName)) {
            FeatureDao featureDao = geoPackage.getFeatureDao(layerName);
            return featureDao.getColumns();
        } catch (Exception e) {
            Log.i("Column Fetch Error", e.toString());
        }
        return null;
    }


    /**
     * Create a tile table in the given GeoPackage
     * @return
     */
    public boolean createTileTable(String gpName, BoundingBox boundingBox, long epsg, String tableName, TileScaling scaling){
        GeoPackage geoPackage = manager.open(gpName);
        try {
            // Create the srs if needed
            SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
            SpatialReferenceSystem srs = srsDao.getOrCreateFromEpsg(epsg);
            // Create the tile table
            mil.nga.proj.Projection projection = ProjectionFactory.getProjection(epsg);
            BoundingBox bbox = LoadTilesTask.transform(boundingBox, projection);
            geoPackage.createTileTable(
                    TileTableMetadata.create(tableName, bbox, srs.getSrsId()));

            TileTableScaling tileTableScaling = new TileTableScaling(geoPackage, tableName);
            tileTableScaling.createOrUpdate(scaling);
        } catch (Exception e) {
            Log.i("Exception", e.toString());
            return false;
        } finally {
            geoPackage.close();
        }
        return true;
    }

    /**
     * Get the given layer name
     */
    public GeoPackageTable getTableObject(String gpName, String tableName, Boolean setActive){
        GeoPackage geo = manager.open(gpName);
        if(geo != null) {
            ContentsDao contentsDao = geo.getContentsDao();
            List<String> features = geo.getFeatureTables();
            if (features.contains(tableName)) {
                FeatureDao featureDao = geo.getFeatureDao(tableName);
                int count = featureDao.count();

                GeometryType geometryType = null;
                try {
                    Contents contents = contentsDao.queryForId(tableName);
                    GeometryColumns geometryColumns = contents
                            .getGeometryColumns();
                    geometryType = geometryColumns.getGeometryType();
                } catch (Exception e) {
                }

                GeoPackageTable table = new GeoPackageFeatureTable(gpName,
                        tableName, geometryType, count);
                // If saveTable boolean is set, set the table's active status to that given value
                if(setActive != null){
                    table.setActive(setActive);
                }
                geo.close();
                return table;
            }
            List<String> tiles = geo.getTileTables();
            if (tiles.contains(tableName)) {
                List<String> tileTables = null;
                try {
                    tileTables = geo.getTileTables();
                } catch (Exception e) {
                }
                if (tileTables != null) {
                    try {
                        for (String tileTableName : tileTables) {
                            TileDao tileDao = geo.getTileDao(tileTableName);
                            int count = tileDao.count();
                            GeoPackageTable table = new GeoPackageTileTable(gpName,
                                    tileTableName, count);
                            if(setActive != null){
                                table.setActive(setActive);
                            }
                            geo.close();
                            return table;
                        }
                    } catch (Exception e) {

                    }
                }
            }
            geo.close();
            return null;
        }
        return null;
    }


    /**
     * Get table Contents object
     */
    public Contents getTableContents(String gpName, String tableName) {
        GeoPackage geo = null;
        try{
            geo = manager.open(gpName);
            if(geo != null) {
                ContentsDao contentsDao = geo.getContentsDao();
                Contents contents = contentsDao.queryForId(tableName);
                return contents;
            }

        } catch (Exception e){

        } finally {
            if(geo !=  null){
                geo.close();
            }
        }
        return null;
    }


    /**
     * Create an alert dialog with a GeoPackage's details for viewing
     *
     * @param geoPackageName
     * @param activity
     * @return
     */
    public AlertDialog getGeoPackageDetailDialog(String geoPackageName, Activity activity) {
        StringBuilder databaseInfo = new StringBuilder();
        GeoPackage geoPackage = manager.open(geoPackageName, false);
        try {
            SpatialReferenceSystemDao srsDao = geoPackage
                    .getSpatialReferenceSystemDao();

            List<SpatialReferenceSystem> srsList = srsDao.queryForAll();
            databaseInfo.append("Size: ")
                    .append(manager.readableSize(geoPackageName));
            databaseInfo.append("\n\nLocation: ").append(
                    manager.isExternal(geoPackageName) ? "External" : "Local");
            databaseInfo.append("\nPath: ").append(manager.getPath(geoPackageName));
            databaseInfo.append("\n\nFeature Tables: ").append(
                    geoPackage.getFeatureTables().size());
            databaseInfo.append("\nTile Tables: ").append(
                    geoPackage.getTileTables().size());
            databaseInfo.append("\n\nSpatial Reference Systems: ").append(
                    srsList.size());
            for (SpatialReferenceSystem srs : srsList) {
                databaseInfo.append("\n");
                addSrs(databaseInfo, srs);
            }

        } catch (Exception e) {
            databaseInfo.append(e.getMessage());
        } finally {
            geoPackage.close();
        }
        AlertDialog viewDialog = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
                .setTitle(geoPackageName)
                .setPositiveButton(R.string.button_ok_label,

                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setMessage(databaseInfo.toString()).create();

        return viewDialog;

    }

    /**
     * Add Spatial Reference System to the info
     *
     * @param info
     * @param srs
     */
    private void addSrs(StringBuilder info, SpatialReferenceSystem srs) {
        info.append("\nSRS Name: ").append(srs.getSrsName());
        info.append("\nSRS ID: ").append(srs.getSrsId());
        info.append("\nOrganization: ").append(srs.getOrganization());
        info.append("\nCoordsys ID: ").append(srs.getOrganizationCoordsysId());
        info.append("\nDefinition: ").append(srs.getDefinition());
        info.append("\nDescription: ").append(srs.getDescription());
    }
}
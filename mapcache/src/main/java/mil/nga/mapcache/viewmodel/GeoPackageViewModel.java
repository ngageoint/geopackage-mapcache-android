package mil.nga.mapcache.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;
import mil.nga.mapcache.repository.GeoPackageRepository;


public class GeoPackageViewModel extends AndroidViewModel {

    private GeoPackageRepository repository;

    private MutableLiveData<List<List<GeoPackageTable>>> geoPackageTables = new MutableLiveData<List<List<GeoPackageTable>>>();
    private MutableLiveData<List<GeoPackage>> geoPackages = new MutableLiveData<>();

    public GeoPackageViewModel(@NonNull Application application) {
        super(application);
    }


    public void init() {
        repository = new GeoPackageRepository(getApplication());

        generateGeoPackageList();
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

    public void generateGeoPackageList(){
        if(repository == null){
            repository = new GeoPackageRepository(getApplication());
        }
        geoPackages.postValue(repository.getGeoPackages());

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
     * Rename a GeoPackage
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
               return true;
        }
//        }
        return false;

    }

    /**
     * Update the List of GeoPackageTable by asking the repository to update
     */
    public void regenerateGeoPackageTableList(){
        List<List<GeoPackageTable>> databaseTables = repository.regenerateTableList();
        // Post new value
         geoPackageTables.postValue(databaseTables);
    }

    /**
     *  Returns the GeoPackage's size
     */
    public String getGeoPackageSize(String geoPackageName){
        return repository.getGeoPackageSize(geoPackageName);
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
            generateGeoPackageList();
            regenerateGeoPackageTableList();
            return true;
        }
        return false;
    }

}

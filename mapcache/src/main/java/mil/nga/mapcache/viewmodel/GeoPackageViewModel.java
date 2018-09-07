package mil.nga.mapcache.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.mapcache.data.GeoPackageDatabases;
import mil.nga.mapcache.data.GeoPackageTable;


public class GeoPackageViewModel extends AndroidViewModel {

    private MutableLiveData<List<String>> databases = new MutableLiveData<List<String>>();
    public MutableLiveData<String> dbName = new MutableLiveData<>();
    private MutableLiveData<List<List<GeoPackageTable>>> geoPackageTables = new MutableLiveData<List<List<GeoPackageTable>>>();

    private GeoPackageManager manager;

//    private GeoPackageDatabases active;

    public GeoPackageViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(List<String> databaseList, List<List<GeoPackageTable>> geoList){
        manager = GeoPackageFactory.getManager(getApplication());

        databases.setValue(new ArrayList<>());
        dbName.setValue("init");
        geoPackageTables.setValue(geoList);
//        active = GeoPackageDatabases.getInstance(getActivity());
    }

    public void setDatabases(List<String> dbList){ databases.setValue(dbList);}
    public void setDbName(String newName){
        dbName.setValue(newName);
    }
    public void setGeoPackageTables(List<List<GeoPackageTable>> newGeoPackageTables) {
        geoPackageTables.setValue(newGeoPackageTables);
    }

    public MutableLiveData<List<String>> getDatabases(){
        return databases;
    }
    public MutableLiveData<String> getTheDb() { return dbName; }
    public MutableLiveData<List<List<GeoPackageTable>>> getGeoPackageTables() {
        return geoPackageTables;
    }

    public void addGeoPackageTable(List<GeoPackageTable> tables){
//        geoPackageTables.setValue(geoPackageTables.getValue().add(tables));

    }
}

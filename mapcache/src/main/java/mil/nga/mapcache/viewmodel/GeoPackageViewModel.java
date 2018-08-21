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


public class GeoPackageViewModel extends ViewModel {

    private List<String> databases = new ArrayList<String>();
    public MutableLiveData<String> dbName = new MutableLiveData<>();
    private GeoPackageDatabases active;


    public void init(List<String> databaseList){
        databases = databaseList;
        dbName.setValue("inited");
//        active = GeoPackageDatabases.getInstance(getActivity());

    }

    public List<String> getDatabases(){
        return databases;
    }

    public void setDbName(String newName){
        dbName.setValue(newName);
    }

    public LiveData<String> getTheDb() { return dbName; }
}

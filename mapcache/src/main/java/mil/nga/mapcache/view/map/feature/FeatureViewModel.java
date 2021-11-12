package mil.nga.mapcache.view.map.feature;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.nga.mapcache.data.MarkerFeature;
import mil.nga.mapcache.indexer.IIndexerTask;
import mil.nga.mapcache.repository.GeoPackageRepository;

/**
 * View model for getting data on a single feature.  Used in FetaureViewActivity
 */
public class FeatureViewModel extends AndroidViewModel {

    /**
     * Objects representing a single feature point
     */
    private MutableLiveData<FeatureViewObjects> featureViewObjects = new MutableLiveData<>();

    /**
     * Reference to repository for saving / reading geopackages
     */
    private GeoPackageRepository repository;

    public FeatureViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    /**
     * Initialize repository and get initial feature point data based on information passed in with
     * the FeatureMarker
     * @param markerFeature - represents a feature that was clicked on
     */
    public void init(MarkerFeature markerFeature){
        repository = new GeoPackageRepository(getApplication());
        setFeatureViewObjects(markerFeature);
    }


    public MutableLiveData<FeatureViewObjects> getFeatureViewObjects(){
        return featureViewObjects;
    }

    /**
     * Save a feature point's data, as well as images
     * @param featureViewObjects Object with a feature's data
     * @param markerFeature A markerFeature to reference when opening the geopackage
     */
    public void saveFeatureObjectValues(FeatureViewObjects featureViewObjects,
                                           MarkerFeature markerFeature){
        repository.saveFeatureObjectValues(featureViewObjects);
        setFeatureViewObjects(markerFeature);
    }

    /**
     * Set the live data
     * @param markerFeature reference for opening the geopacakge to retreive data
     */
    private void setFeatureViewObjects(MarkerFeature markerFeature){
        featureViewObjects.setValue(repository.getFeatureViewObjects(markerFeature));
    }

    /**
     * Delete the associated media row from the given feature
     * @param featureViewObjects
     * @param rowId
     */
    public void deleteImageFromFeature(FeatureViewObjects featureViewObjects, long rowId,
                                          MarkerFeature markerFeature) {
        boolean deleted =  repository.deleteImageFromFeature(featureViewObjects, rowId);
        setFeatureViewObjects(markerFeature);
    }


}

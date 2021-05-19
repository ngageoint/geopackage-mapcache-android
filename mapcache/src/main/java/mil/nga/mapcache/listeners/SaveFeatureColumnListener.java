package mil.nga.mapcache.listeners;

import android.view.View;

import java.util.List;

import mil.nga.mapcache.view.layer.FeatureColumnDetailObject;
import mil.nga.mapcache.view.map.feature.FcColumnDataObject;

public interface SaveFeatureColumnListener {

    void onClick(View view, List<FcColumnDataObject> values);

}

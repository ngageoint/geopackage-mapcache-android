package mil.nga.mapcache.view;

import android.view.View;

import mil.nga.geopackage.GeoPackage;
import mil.nga.mapcache.data.GeoPackageTable;

public interface LayerActiveSwitchListener {
    void onClick(boolean active, GeoPackageTable table);

}

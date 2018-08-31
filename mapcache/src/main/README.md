# Android code snippets
---
## Code samples from components I built and may have chosen to not use


======

### Segmented Control

'''
fragment.xml:

<LinearLayout
    android:id="@+id/mapScrollViewSelect"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_gravity="center">
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center_horizontal">
        <Button
            android:id="@+id/btn_map"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical"
            android:text="Default" />
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Satellite"
            android:id="@+id/btn_satellite"
            android:layout_gravity="center_vertical" />
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Terrain"
            android:id="@+id/btn_terrain"
            android:layout_gravity="center_vertical" />
    </LinearLayout>
</LinearLayout>

'''

'''
controler.java

/**
 * sets the listeners for the map type buttons
 * (this is for the old 3 button select style of map view type)
 */
public void setMapViewButtonListeners(){
    mapButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setMapType(GoogleMap.MAP_TYPE_NORMAL);
            setViewSelected(mapButton);
            setViewDeselected(satelliteButton);
            setViewDeselected(terrainButton);
            geoPackageViewModel.setDbName("map");

        }
    });
    satelliteButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            setViewSelected(satelliteButton);
            setViewDeselected(mapButton);
            setViewDeselected(terrainButton);
            geoPackageViewModel.setDbName("satellite");
        }
    });
    terrainButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            setViewSelected(terrainButton);
            setViewDeselected(satelliteButton);
            setViewDeselected(mapButton);
            geoPackageViewModel.setDbName("terrain");

        }
    });

    mapButton.performClick();
}
/**
 * Set the view button style to selected
 * @param selected
 */
public void setViewSelected(Button selected){
    selected.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.nga_primary_light));
    selected.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
}

/**
 * set the view button type to deselected
 * @param deselected
 */
public void setViewDeselected(Button deselected){
    deselected.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.btn_light_background));
    deselected.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
}

'''



======

package mil.nga.mapcache.view.map.feature;

//Object for image items in the feature columns attachment slider

import android.graphics.Bitmap;

class SliderItem {
    private final Bitmap image;

    public SliderItem(Bitmap image){
        this.image = image;
    }

    public Bitmap getImage(){
        return image;
    }
}

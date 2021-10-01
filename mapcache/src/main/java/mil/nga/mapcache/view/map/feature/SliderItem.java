package mil.nga.mapcache.view.map.feature;

/**
 * Object for image items in the feature columns attachment slider
 */

import android.graphics.Bitmap;

class SliderItem {
    private final long mediaId;
    private final Bitmap image;

    public SliderItem(long mediaId, Bitmap image){
        this.image = image;
        this.mediaId = mediaId;
    }

    public Bitmap getImage(){
        return image;
    }

    public long getMediaId() {
        return mediaId;
    }
}

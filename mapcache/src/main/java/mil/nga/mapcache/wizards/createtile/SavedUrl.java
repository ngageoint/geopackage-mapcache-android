package mil.nga.mapcache.wizards.createtile;

import mil.nga.mapcache.R;

/**
 * Object containing a url string and an icon to represent it.  used in the saved url menu
 */
class SavedUrl {

   private int mIcon;
   private String mUrl;
   private boolean connected = false;

   public SavedUrl(String url){
      this.mUrl = url;
      this.mIcon = R.drawable.cloud_layers_grey;
   }

   public String getmUrl() {
      return mUrl;
   }

   public void setmUrl(String mUrl) {
      this.mUrl = mUrl;
   }

   public int getmIcon() {
      return mIcon;
   }

   public void setmIcon(int mIcon) {
      this.mIcon = mIcon;
   }

   public boolean isConnected() {
      return connected;
   }

   public void setConnected(boolean connected) {
      this.connected = connected;
   }
}

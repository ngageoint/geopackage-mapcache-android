package mil.nga.mapcache.wizards.createtile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mil.nga.mapcache.R;
import mil.nga.mapcache.utils.HttpUtils;

/**
 * Adapter for the alertview that pops up during the new tile layer wizard showing
 * your currently saved urls
 */
class SavedUrlAdapter extends ArrayAdapter<SavedUrl> {

   private Context mContext;
   private List<SavedUrl> urlList = new ArrayList<>();
   /**
    * For testing http connections
    */
   private HttpUtils httpUtils = HttpUtils.getInstance();

   public SavedUrlAdapter(@NonNull Context context, ArrayList<SavedUrl> list) {
      super(context, 0, list);
      mContext = context;
      urlList = list;
   }

   @NonNull
   @Override
   public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
      View listItem = convertView;
      if(listItem == null) {
         listItem = LayoutInflater.from(mContext).inflate(R.layout.layout_saved_url, parent, false);
      }
      SavedUrl savedUrl = urlList.get(position);

      // Icon
      ImageView urlIcon = (ImageView)listItem.findViewById(R.id.url_icon);
      urlIcon.setImageResource(savedUrl.getmIcon());

      // Url text
      TextView urlText = (TextView)listItem.findViewById(R.id.saved_url);
      urlText.setText(savedUrl.getmUrl());

      return listItem;
   }

   /**
    * Takes a list of SavedUrl objects, and tests the connection of each url.  Once complete, it
    * will call the adapter's updateIcon method to change the icon to the connection result and
    * refresh
    * @param list list of SavedUrl objects
    */
   public void updateConnections(ArrayList<SavedUrl> list){
      if(!list.isEmpty()) {
         for (SavedUrl savedUrl : list) {
            testConnection(savedUrl, this);
         }
      }
   }

   /**
    * Finds the given url in the urlList and updates the icon in that SavedUrl object, then refresh
    * @param url
    * @param icon
    */
   private void updateIcon(String url, int icon){
      for (SavedUrl savedUrl : urlList) {
         if(savedUrl.getmUrl().equalsIgnoreCase(url)){
            savedUrl.setmIcon(icon);
            this.notifyDataSetChanged();
         }
      }
   }

   /**
    * Tests connection to a url and call for an icon update
    */
   private boolean testConnection(SavedUrl newUrl, SavedUrlAdapter adapter){
      ExecutorService threadEx = Executors.newSingleThreadExecutor();
      Handler handler = new Handler(Looper.getMainLooper());
      threadEx.execute(new Runnable() {
         @Override
         public void run() {
            // Test connection
            boolean connected = httpUtils.isServerAvailable(newUrl.getmUrl());
            // Update icon to show connection status
            handler.post(new Runnable() {
               @Override
               public void run() {
                  if(connected) {
                     adapter.updateIcon(newUrl.getmUrl(), R.drawable.cloud_layers_blue);
                  } else {
                     adapter.updateIcon(newUrl.getmUrl(), R.drawable.cloud_layers_red);

                  }
               }
            });
         }
      });
      return true;
   }
}

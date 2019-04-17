package mil.nga.mapcache.view.detail;

import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import mil.nga.mapcache.R;


/**
 * View holder for the Header section of the detail page recycler view
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder {

    /**
     * Selected GeoPackage name
     */
    public TextView textName;

    /**
     * Selected GeoPackage size
     */
    public TextView textSize;

    /**
     * Header object containing basic info about the GeoPackage to populate our fields with
     */
    public DetailPageHeaderObject header;

    /**
     * Back button to take the RecyclerView back to the list of GeoPackages
     */
    private ImageButton backArrow;


    /**
     * Constructor
     * @param itemView View to be created
     * @param backListener - A click listener which should set the RecyclerView to contain the
     *                     list of GeoPackages again
     */
    public HeaderViewHolder(View itemView, View.OnClickListener backListener) {
        super(itemView);
        textName = (TextView) itemView.findViewById(R.id.headerTitle);
        textSize = (TextView) itemView.findViewById(R.id.headerSize);
        backArrow = (ImageButton) itemView.findViewById(R.id.detailPageBackButton);
        backArrow.setOnClickListener(backListener);
    }

    /**
     * Populate the header ui fields with data from the provided DetailPageHeaderObject
     * @param data a DetailPageHeaderObject containing basic data about the GeoPackage
     */
    public void setData(Object data) {
        if(data instanceof DetailPageHeaderObject) {
            header = (DetailPageHeaderObject) data;
            textName.setText(header.getGeopackageName());
            textSize.setText(header.getSize());
        } else{
            textName.setText("No header text given");
            textSize.setText("-");
        }
    }
}
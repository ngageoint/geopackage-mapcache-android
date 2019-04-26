package mil.nga.mapcache.view.detail;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.DetailActionListener;


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
     * Text view for feature count
     */
    private TextView textFeatures;

    /**
     * Text view for tile count
     */
    private TextView textTiles;

    /**
     * Action buttons for detail, rename, share, copy, and delete GeoPackage
     */
    private Button detailButton, renameButton, shareButton, copyButton, deleteButton;

    /**
     * A shared action listener to use on the buttons for detail, rename, share, copy, and delete
     */
    private DetailActionListener actionListener;


    /**
     * Constructor
     * @param itemView View to be created
     * @param backListener - A click listener which should set the RecyclerView to contain the
     *                     list of GeoPackages again
     */
    public HeaderViewHolder(View itemView, View.OnClickListener backListener,
                            DetailActionListener actionListener) {
        super(itemView);
        textName = (TextView) itemView.findViewById(R.id.headerTitle);
        textSize = (TextView) itemView.findViewById(R.id.headerSize);
        textFeatures = (TextView) itemView.findViewById(R.id.header_text_features);
        textTiles = (TextView) itemView.findViewById(R.id.header_text_tiles);
        backArrow = (ImageButton) itemView.findViewById(R.id.detailPageBackButton);
        backArrow.setOnClickListener(backListener);
        this.actionListener = actionListener;
        setActionButtonListeners();
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
            textFeatures.setText(pluralize(header.getFeatureCount(), "Feature"));
            textTiles.setText(pluralize(header.getTileCount(), "Tile"));
        } else{
            textName.setText("No header text given");
            textSize.setText("-");
        }
    }

    /**
     * Sets the funtions for all the buttons.  Pass back to the MapFragment via the actionListener
     */
    private void setActionButtonListeners(){
        // Detail
        detailButton = (Button) itemView.findViewById(R.id.header_detail_info);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onClick(view, DetailActionListener.DETAIL_GP, textName.getText().toString(), "");
            }
        });

        // Rename
        renameButton = (Button) itemView.findViewById(R.id.header_detail_rename);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onClick(view, DetailActionListener.RENAME_GP, textName.getText().toString(), "");
            }
        });

        // Share
        shareButton = (Button) itemView.findViewById(R.id.header_detail_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onClick(view, DetailActionListener.SHARE_GP, textName.getText().toString(), "");
            }
        });

        // Copy
        copyButton = (Button) itemView.findViewById(R.id.header_detail_copy);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onClick(view, DetailActionListener.COPY_GP, textName.getText().toString(), "");
            }
        });

        // Delete
        deleteButton = (Button) itemView.findViewById(R.id.header_detail_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onClick(view, DetailActionListener.DELETE_GP, textName.getText().toString(), "");
            }
        });
    }

    /**
     * makes a string plural based on the count
     * @param count
     * @param text singular word
     * @return
     */
    private String pluralize(int count, String text){
        if(count == 1){
            return count + " " + text;
        } else {
            return count + " " + text + "s";
        }
    }
}
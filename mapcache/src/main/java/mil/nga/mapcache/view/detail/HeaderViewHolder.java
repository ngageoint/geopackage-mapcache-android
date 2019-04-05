package mil.nga.mapcache.view.detail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import mil.nga.mapcache.R;


/**
 * View holder for the Header section of the detail page recycler view
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textName;
    public TextView textSize;
    public DetailPageHeaderObject header;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        textName = (TextView) itemView.findViewById(R.id.headerTitle);
        textSize = (TextView) itemView.findViewById(R.id.headerSize);

    }

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

    @Override
    public void onClick(View v) {
//        if (mListener != null) {
//            //mListener.onItemClick(item);
//        }
    }
}
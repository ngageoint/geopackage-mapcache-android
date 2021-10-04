package mil.nga.mapcache.view.map.feature;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.DeleteImageListener;

/**
 * Adapter to handle the image gallery side scrolling view
 */
class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private final List<SliderItem> sliderItems;
    private final DeleteImageListener mDeleteImageListener;

    public SliderAdapter(List<SliderItem> sliderItems, ViewPager2 viewPager2,
                         DeleteImageListener deleteImageListener) {
        this.sliderItems = sliderItems;
        mDeleteImageListener = deleteImageListener;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_view_item, parent, false),
                mDeleteImageListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setData(sliderItems.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onClick(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    public List<SliderItem> getSliderItems(){
        return sliderItems;
    }

    public void remove(long rowId){
        int removeIndex = -1;
        for(SliderItem item : sliderItems){
            if(item.getMediaId() == rowId){
                removeIndex = sliderItems.indexOf(item);
            }
        }
        if(removeIndex >= 0){
            sliderItems.remove(removeIndex);
            notifyDataSetChanged();
        }
    }



    class SliderViewHolder extends ViewHolder implements View.OnClickListener{

        private final ImageView imageView;
        private final DeleteImageListener mDeleteImageListener;
        private long mediaId = -1;

        public SliderViewHolder(@NonNull View itemView, DeleteImageListener deleteImageListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
            mDeleteImageListener = deleteImageListener;
        }

        public void setData(SliderItem sliderItem){
            imageView.setImageBitmap(sliderItem.getImage());
            mediaId = sliderItem.getMediaId();
        }

        /**
         * Sets up the click listener
         * @param view image click listener
         */
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            LayoutInflater inflater = LayoutInflater.from(view.getContext());
            View expandedImageView = inflater.inflate(R.layout.basic_img_view, null);
            builder.setView(expandedImageView);
            ImageView imgView = (ImageView)expandedImageView.findViewById(R.id.basic_image);
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            imgView.setImageBitmap(drawable.getBitmap());
            AlertDialog dialog = builder.create();

            MaterialButton closeButton = (MaterialButton)expandedImageView.findViewById(R.id.close_image_button);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            MaterialButton deleteButton = (MaterialButton)expandedImageView.findViewById(R.id.delete_image_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeleteImageListener.onClick(v, DeleteImageListener.DELETE_IMAGE, mediaId);
                    dialog.cancel();
                }
            });

            dialog.show();
        }
    }
}

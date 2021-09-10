package mil.nga.mapcache.view.map.feature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import mil.nga.mapcache.R;

class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder>{

    private final List<SliderItem> sliderItems;

    public SliderAdapter(List<SliderItem> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_view_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setImage(sliderItems.get(position));
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    public List<SliderItem> getSliderItems(){
        return sliderItems;
    }

    class SliderViewHolder extends ViewHolder{

        private final ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
        }

        public void setImage(SliderItem sliderItem){
            imageView.setImageBitmap(sliderItem.getImage());
        }
    }
}

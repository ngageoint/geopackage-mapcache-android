package mil.nga.mapcache.tutorial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mil.nga.mapcache.R;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialCardViewHolder>{

    List<String> mPages = new ArrayList<>();
    List<Integer> colors = new ArrayList<>();

    Context context;

    public TutorialAdapter(Context context, List<String> pages){
        this.context = context;
        this.mPages = pages;
        colors.add(context.getResources().getColor(R.color.nga_accent_bright));
        colors.add(context.getResources().getColor(R.color.nga_accent_light));
        colors.add(context.getResources().getColor(R.color.nga_accent_primary));
    }

    @NonNull
    @Override
    public TutorialCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tutorial_card_layout, parent, false);
        TutorialCardViewHolder holder = new TutorialCardViewHolder(v, context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialCardViewHolder holder, int position) {
        holder.setData(mPages.get(position), colors.get(position));
    }

    @Override
    public int getItemCount() {
        return mPages.size();
    }

}

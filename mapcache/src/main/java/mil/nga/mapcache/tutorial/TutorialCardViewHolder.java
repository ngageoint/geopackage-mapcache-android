package mil.nga.mapcache.tutorial;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import mil.nga.mapcache.R;

public class TutorialCardViewHolder extends RecyclerView.ViewHolder {

    TextView pageText;
    View parentView;
    Context context;

    public TutorialCardViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        pageText = (TextView) itemView.findViewById(R.id.tutorial_card_text);
        parentView = (View) itemView.findViewById(R.id.tutorial_card_parent);
        pageText.setText("text set");
    }

    public void setData(String newText, int color){
        pageText.setText(newText);
        parentView.setBackgroundColor(color);

    }
}

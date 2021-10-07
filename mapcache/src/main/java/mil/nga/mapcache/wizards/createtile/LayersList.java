package mil.nga.mapcache.wizards.createtile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mil.nga.mapcache.R;

/**
 * A custom ArrayAdapter for LayerModel items.
 */
public class LayersList extends ArrayAdapter<LayerModel> {

    /**
     * Contains all the layers.
     */
    private LayersModel model;

    /**
     * Constructor.
     *
     * @param context The application context.
     * @param model   Contains all the layers.
     */
    public LayersList(@NonNull Context context, LayersModel model) {
        super(context, R.layout.layer_row_description);
        this.model = model;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.layer_row_description, null, true);

        LayerModel layer = model.getLayers()[position];
        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        TextView txtDescription = (TextView) rowView.findViewById(R.id.description);
        txtTitle.setText(layer.getName());
        txtDescription.setText(layer.getDescription());

        return rowView;
    }
}

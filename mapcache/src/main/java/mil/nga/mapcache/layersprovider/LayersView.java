package mil.nga.mapcache.layersprovider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import mil.nga.mapcache.R;

/**
 * Shows the user all of the available layers and allows the user to pick one.
 */
public abstract class LayersView {

    /**
     * The application context.
     */
    private Context context;

    /**
     * Contains the layers to select from.
     */
    private LayersModel model;

    /**
     * Used as a custom adapter for the list view.
     */
    private LayersList adapter;

    /**
     * The android view.
     */
    private View view;

    /**
     * The close x button in top left.
     */
    private ImageView closeLogo;

    /**
     * Constructor.
     *
     * @param context The application context.
     * @param model   The layers model.
     */
    public LayersView(Context context, LayersModel model) {
        this.context = context;
        this.model = model;
        adapter = new LayersList(context, model);
    }

    /**
     * Shows the layers to the user for the user to make a selection.
     */
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.layers_pick_list, null);
        ListView layersView = view.findViewById(R.id.layersPickView);
        layersView.setAdapter(adapter);

        TextView title = view.findViewById(R.id.title);
        title.setText(model.getTitle());



        layersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LayerModel layer = model.getLayers()[i];
                LayerModel[] layers = {layer};
                model.setSelectedLayers(layers);
            }
        });

        closeLogo = (ImageView) view.findViewById(R.id.new_layer_close_logo);
    }

    /**
     * Gets the application context.
     * @return The application context.
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Gets the main view.
     * @return The layers pick view.
     */
    protected View getView() {
        return view;
    }

    /**
     * Gets the close x button in top left.
     * @return The close button.
     */
    protected ImageView getCloseLogo() {
        return closeLogo;
    }
}

package mil.nga.mapcache.wizards.createtile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import mil.nga.mapcache.R;

/**
 * Shows the user all of the available layers and allows the user to pick one.
 */
public class LayersView {

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
        View view = inflater.inflate(R.layout.layers_pick_list, null);
        ListView layersView = view.findViewById(R.id.layersPickView);
        layersView.setAdapter(adapter);

        AlertDialog.Builder dialog = new AlertDialog.Builder(
                context, R.style.AppCompatAlertDialogStyle)
                .setView(view);
        final AlertDialog alertDialog = dialog.create();
        alertDialog.setCanceledOnTouchOutside(false);

        layersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LayerModel layer = model.getLayers()[i];
                model.setSelectedLayer(layer);
                alertDialog.dismiss();
            }
        });

        ImageView closeLogo = (ImageView) view.findViewById(R.id.new_layer_close_logo);
        // Click listener for close button
        closeLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
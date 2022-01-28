package mil.nga.mapcache.layersprovider;

import android.app.Activity;
import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import mil.nga.mapcache.io.network.AuthorizationConsumer;
import mil.nga.mapcache.io.network.HttpClient;
import mil.nga.mapcache.io.network.IResponseHandler;
import mil.nga.mapcache.ogc.wms.CapabilitiesParser;
import mil.nga.mapcache.ogc.wms.Layer;
import mil.nga.mapcache.ogc.wms.WMSCapabilities;

/**
 * Provides the layers from a particular tile server if that tile server has multiple layers.
 * Otherwise it will populate the model with an empty array of layers.
 */
public class LayersProvider implements IResponseHandler, AuthorizationConsumer {

    /**
     * The activity used to get back on the main thread.
     */
    private Activity activity;

    /**
     * The server url.
     */
    private String url;

    /**
     * The model to populate with layer information.
     */
    private LayersModel model;

    /**
     * Constructs a new layer provider.
     *
     * @param activity The activity used to get back on the main thread.
     * @param model    The model to populate with layer information.
     */
    public LayersProvider(Activity activity, LayersModel model) {
        this.activity = activity;
        this.model = model;
    }

    /**
     * Retrieves a list of layers from the specified url.  It does this on a background thread
     * and the layer will then be populated within the model and that population is done on the
     * main thread.
     *
     * @param url The url to retrieve layers from.
     */
    public void retrieveLayers(String url) {
        if (url.toLowerCase().contains("wms")) {
            String capabilitiesUrl = url + "?request=GetCapabilities&version=1.3.0&service=WMS";
            this.url = capabilitiesUrl;
            HttpClient.getInstance().sendGet(capabilitiesUrl, this, this.activity);
        } else {
            model.setLayers(new LayerModel[0]);
        }
    }

    @Override
    public void handleResponse(InputStream stream, int responseCode) {
        if(stream != null && responseCode == HttpURLConnection.HTTP_OK) {
            CapabilitiesParser parser = new CapabilitiesParser();
            try {
                final WMSCapabilities capabilities = parser.parse(stream);
                List<LayerModel> allLayers = new ArrayList<>();
                Stack<Layer> parents = new Stack<>();
                for (Layer layer : capabilities.getCapability().getLayer()) {
                    getLayers(allLayers, layer, parents);
                }
                final LayerModel[] allLayersArray = allLayers.toArray(new LayerModel[allLayers.size()]);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        model.setImageFormats(capabilities.getCapability().getRequest().getGetMap()
                                .getFormat().toArray(new String[0]));
                        model.setLayers(allLayersArray);
                    }
                });
            } catch (ParserConfigurationException | SAXException | IOException e) {
                model.setLayers(new LayerModel[0]);
                Log.e(LayersProvider.class.getSimpleName(),
                        "Unable to parse WMS GetCapabilities document for " + this.url, e);
            }
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    model.setLayers(new LayerModel[0]);
                }
            });
            Log.e(
                    LayersProvider.class.getSimpleName(),
                    "Unable to download WMS GetCapabilities document http response " + responseCode);
        }
    }

    @Override
    public void handleException(IOException exception) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                model.setLayers(new LayerModel[0]);
            }
        });
        Log.e(LayersProvider.class.getSimpleName(), "WMS GetCapabilities failed: ", exception);
    }

    /**
     * Recursively gets the layer's and their info.
     *
     * @param allLayers The list to add the layer info too.
     * @param layer     The current layer we are gathering info for.
     * @param parents   The stack of parents for the layer.
     */
    private void getLayers(List<LayerModel> allLayers, Layer layer, Stack<Layer> parents) {
        if (!layer.getName().isEmpty()) {
            LayerModel model = new LayerModel();
            String title = parents.get(0).getTitle();
            String description = "";
            for (int i = 1; i < parents.size(); i++) {
                description += parents.get(i).getTitle() + " ";
            }
            description += layer.getTitle();
            model.setName(layer.getName());
            model.setTitle(title);
            model.setDescription(description);

            int index = 0;
            long[] epsgs = new long[layer.getCRS().size()];
            for (String crs : layer.getCRS()) {
                String[] splitCRS = crs.split(":");
                try {
                    epsgs[index] = Long.valueOf(splitCRS[splitCRS.length - 1]);
                } catch (NumberFormatException e) {
                    Log.e(
                            LayersProvider.class.getSimpleName(),
                            "Error parsing the EPSG for crs of " + crs,
                            e);
                }
                index++;
            }
            model.setEpsgs(epsgs);

            allLayers.add(model);
        }

        if (!layer.getLayers().isEmpty()) {
            parents.push(layer);
            for (Layer child : layer.getLayers()) {
                getLayers(allLayers, child, parents);
            }
            parents.pop();
        }
    }

    @Override
    public void setAuthorizationValue(String value) {
        model.setAuthorization(value);
    }
}

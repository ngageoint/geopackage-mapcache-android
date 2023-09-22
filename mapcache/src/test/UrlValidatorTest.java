import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mil.nga.mapcache.utils.UrlValidator;

public class UrlValidatorTest {
    @Before
    public void setUp(){System.out.println("UrlValidator test ready");}

    @After
    public void tearDown(){
        System.out.println("UrlValidator test tear down");
    }

    @Test
    public void testBadFormats(){
        String url = "bad";
        boolean isBad = UrlValidator.hasWms(url);
        assertFalse("UrlValidator should not approve a bad url format", isBad);
    }

    @Test
    public void testGetCapabilities(){
        String goodUrl = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetCapabilities&service=WMS";
        assertTrue("UrlValidator did not approve a good url format", UrlValidator.hasWms(goodUrl));

        String missingService = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetCapabilities";
        String missingRequest = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&service=WMS";
        String basicUrl = "http://sampleserver1.arcgisonline.com";
        assertFalse("UrlValidator should not approve getCapabilities request when missing Service param", UrlValidator.hasWms(missingService));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getCapabilities request when given a url missing params", UrlValidator.hasWms(basicUrl));
    }

    @Test
    public void testGetMap(){
        String goodUrl = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&styles=default&format=image/png";
        assertTrue("UrlValidator did not approve a good getMap url format", UrlValidator.hasWms(goodUrl));
        String missingVersion = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&styles=default&format=image/png";
        String missingRequest = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&styles=default&format=image/png";
        String missingLayers = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&styles=default&format=image/png";
        String missingStyles = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&format=image/png";
        String missingCrs = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&styles=default&format=image/png";
        String missingBbox = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&width=760&height=360&layers=0&styles=default&format=image/png";
        String missingWidth = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&height=360&layers=0&styles=default&format=image/png";
        String missingHeight = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&layers=0&styles=default&format=image/png";
        String missingFormat = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.3.0&request=GetMap&CRS=CRS:84&bbox=-178.217598,18.924782,-66.969271,71.406235&width=760&height=360&layers=0&styles=default";
        String basicUrl = "http://sampleserver1.arcgisonline.com";
        assertFalse("UrlValidator should not approve getMap request when missing Version param", UrlValidator.hasWms(missingVersion));
        assertFalse("UrlValidator should not approve getMap request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getMap request when missing Layers param", UrlValidator.hasWms(missingLayers));
        assertFalse("UrlValidator should not approve getMap request when missing Styles param", UrlValidator.hasWms(missingStyles));
        assertFalse("UrlValidator should not approve getMap request when missing CRS/SRS param", UrlValidator.hasWms(missingCrs));
        assertFalse("UrlValidator should not approve getMap request when missing bbox param", UrlValidator.hasWms(missingBbox));
        assertFalse("UrlValidator should not approve getMap request when missing Width param", UrlValidator.hasWms(missingWidth));
        assertFalse("UrlValidator should not approve getMap request when missing Height param", UrlValidator.hasWms(missingHeight));
        assertFalse("UrlValidator should not approve getMap request when missing Format param", UrlValidator.hasWms(missingFormat));
        assertFalse("UrlValidator should not approve getMap request when given a url missing params", UrlValidator.hasWms(basicUrl));
    }

    @Test
    public void testGetFeatureInfo(){
        String goodUrl = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetFeatureInfo&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&X=500&Y=400&query_layers=0";
        assertTrue("UrlValidator did not approve a good getFeatureInfo url format", UrlValidator.hasWms(goodUrl));
        String missingVersion = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?request=GetFeatureInfo&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&X=500&Y=400&query_layers=0";
        String missingRequest = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&X=500&Y=400&query_layers=0";
        String missingQueryLayers = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetFeatureInfo&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&X=500&Y=400";
        String missingIX = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetFeatureInfo&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&Y=400&query_layers=0";
        String missingJY = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetFeatureInfo&layers=0&styles=default&SRS=EPSG:4326&bbox=-125.192865,11.2289864971264,-66.105824,62.5056715028736&width=1044&height=906&format=text/html&X=500&query_layers=0";
        assertFalse("UrlValidator should not approve getFeatureInfo request when missing Version param", UrlValidator.hasWms(missingVersion));
        assertFalse("UrlValidator should not approve getFeatureInfo request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getFeatureInfo request when missing queary_layers param", UrlValidator.hasWms(missingQueryLayers));
        assertFalse("UrlValidator should not approve getFeatureInfo request when missing i or x param", UrlValidator.hasWms(missingIX));
        assertFalse("UrlValidator should not approve getFeatureInfo request when missing j or y param", UrlValidator.hasWms(missingJY));

    }

    @Test
    public void testGetStyles(){
        String goodUrl = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StateCityHighway_USA/MapServer/WMSServer?version=1.3.0&request=GetStyles&layers=0,1,2";
        assertTrue("UrlValidator did not approve a good getStyles url format", UrlValidator.hasWms(goodUrl));
        String missingVersion = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StateCityHighway_USA/MapServer/WMSServer?request=GetStyles&layers=0,1,2";
        String missingRequest = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StateCityHighway_USA/MapServer/WMSServer?version=1.3.0&layers=0,1,2";
        String missingLayers = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StateCityHighway_USA/MapServer/WMSServer?version=1.3.0&request=GetStyles";
        assertFalse("UrlValidator should not approve getStyles request when missing Version param", UrlValidator.hasWms(missingVersion));
        assertFalse("UrlValidator should not approve getStyles request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getStyles request when missing layers param", UrlValidator.hasWms(missingLayers));
    }

    @Test
    public void testGetLegendGraphic(){
        String goodUrl = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetLegendGraphic&layer=0&style=default&format=image/png&width=95&height=65";
        assertTrue("UrlValidator did not approve a good getLegendGraphic url format", UrlValidator.hasWms(goodUrl));
        String missingVersion = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?request=GetLegendGraphic&layer=0&style=default&format=image/png&width=95&height=65";
        String missingRequest = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&layer=0&style=default&format=image/png&width=95&height=65";
        String missingLayer = "http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?version=1.1.1&request=GetLegendGraphic&style=default&format=image/png&width=95&height=65";
        assertFalse("UrlValidator should not approve getLegendGraphic request when missing Version param", UrlValidator.hasWms(missingVersion));
        assertFalse("UrlValidator should not approve getLegendGraphic request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getLegendGraphic request when missing layer param", UrlValidator.hasWms(missingLayer));

    }
}

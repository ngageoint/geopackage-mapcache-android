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
        assertFalse("UrlValidator should not approve getCapabilities request when missing Version param", UrlValidator.hasWms(missingVersion));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Request param", UrlValidator.hasWms(missingRequest));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Layers param", UrlValidator.hasWms(missingLayers));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Styles param", UrlValidator.hasWms(missingStyles));
        assertFalse("UrlValidator should not approve getCapabilities request when missing CRS/SRS param", UrlValidator.hasWms(missingCrs));
        assertFalse("UrlValidator should not approve getCapabilities request when missing bbox param", UrlValidator.hasWms(missingBbox));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Width param", UrlValidator.hasWms(missingWidth));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Height param", UrlValidator.hasWms(missingHeight));
        assertFalse("UrlValidator should not approve getCapabilities request when missing Format param", UrlValidator.hasWms(missingFormat));
        assertFalse("UrlValidator should not approve getCapabilities request when given a url missing params", UrlValidator.hasWms(basicUrl));
    }
}

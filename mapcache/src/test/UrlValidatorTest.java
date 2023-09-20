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
}

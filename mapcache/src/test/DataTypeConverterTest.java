import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.mapcache.utils.DataTypeConverter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataTypeConverterTest {

    @Before
    public void setUp(){System.out.println("DataTypeConverter test ready");}

    @After
    public void tearDown(){
        System.out.println("DataTypeConverter test tear down");
    }

    @Test
    public void testText(){
        GeoPackageDataType type = DataTypeConverter.getGeoPackageDataType("text");
        GeoPackageDataType typeCaps = DataTypeConverter.getGeoPackageDataType("TEXT");
        assertEquals("Expected GeoPackageDataType for text not correct", GeoPackageDataType.TEXT, type);
        assertEquals("Expected GeoPackageDataType for text not correct", GeoPackageDataType.TEXT, typeCaps);
    }

    @Test
    public void testNumber(){
        GeoPackageDataType type = DataTypeConverter.getGeoPackageDataType("number");
        GeoPackageDataType typeCaps = DataTypeConverter.getGeoPackageDataType("NUMBER");
        assertEquals("Expected GeoPackageDataType for number not correct", GeoPackageDataType.DOUBLE, type);
        assertEquals("Expected GeoPackageDataType for number not correct", GeoPackageDataType.DOUBLE, typeCaps);
    }

    @Test
    public void testCheckbox(){
        GeoPackageDataType type = DataTypeConverter.getGeoPackageDataType("checkbox");
        GeoPackageDataType typeCaps = DataTypeConverter.getGeoPackageDataType("CHECKBOX");
        GeoPackageDataType typeSpace = DataTypeConverter.getGeoPackageDataType("check box");
        GeoPackageDataType typeSpaceCaps = DataTypeConverter.getGeoPackageDataType("CHECK BOX");
        assertEquals("Expected GeoPackageDataType for text not correct", GeoPackageDataType.BOOLEAN, type);
        assertEquals("Expected GeoPackageDataType for text not correct", GeoPackageDataType.BOOLEAN, typeCaps);
    }

    @Test
    public void testBadText(){
        GeoPackageDataType type = DataTypeConverter.getGeoPackageDataType("bad");
        assertNull("Expected GeoPackageDataType for number not correct", type);
    }

}

package mil.nga.mapcache;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeoPackageDetailDrawerTest{

    @Test
    public void onCreate() {
        GeoPackageDetailDrawer detailDrawer = new GeoPackageDetailDrawer();
        assertNotNull(detailDrawer);
    }

}
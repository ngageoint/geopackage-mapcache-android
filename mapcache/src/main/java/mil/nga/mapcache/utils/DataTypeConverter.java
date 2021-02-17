package mil.nga.mapcache.utils;

import mil.nga.geopackage.db.GeoPackageDataType;

public class DataTypeConverter {

    /**
     * Convert string to GeoPackageDataType to make sure we match the enum
     * @param givenType String of the GeoPackageDataType expected
     * @return null if it doesn't match
     */
    public static GeoPackageDataType getGeoPackageDataType(String givenType){
        if(givenType.equalsIgnoreCase("text")){
            return GeoPackageDataType.TEXT;
        } else if(givenType.equalsIgnoreCase("number")){
            return GeoPackageDataType.DOUBLE;
        } else if(givenType.equalsIgnoreCase("check box")){
            return GeoPackageDataType.BOOLEAN;
        } else if(givenType.equalsIgnoreCase("checkbox")){
            return GeoPackageDataType.BOOLEAN;
        }
        return null;
    }
}

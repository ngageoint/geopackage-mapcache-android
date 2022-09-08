package mil.nga.mapcache.load;

/**
 * Interface for load tile callbacks
 * 
 * @author osbornb
 */
public interface ILoadTilesTask {

	/**
	 * On cancellation of loading tiles
	 *
	 * */
	void onLoadTilesCancelled();

	/**
	 * On completion of loading tiles
	 * 
	 * @param result A message to display to the user if needed.
	 */
	void onLoadTilesPostExecute(String result);

}

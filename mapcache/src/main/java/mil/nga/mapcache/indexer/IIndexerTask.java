package mil.nga.mapcache.indexer;

/**
 * Interface for feature indexer callbacks
 * 
 * @author osbornb
 */
public interface IIndexerTask {

	/**
	 * On cancellation of indexing features
	 * 
	 * @param result
	 */
	public void onIndexerCancelled(String result);

	/**
	 * On completion of indexing features
	 * 
	 * @param result
	 */
	public void onIndexerPostExecute(String result);

}

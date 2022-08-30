package mil.nga.mapcache.io.network;

import java.io.InputStream;

/**
 * Given a web pages html, the extractor will retrieve certain content from the web page to be used
 * within an InputStream.
 */
public interface WebViewExtractor {

    /**
     * Indicates if the web page is ready for its contents to be extracted.
     *
     * @param html The html of the web page.
     * @return True if we can extract, false if we need to wait a while longer.
     */
    boolean readyForExtraction(String html);

    /**
     * Extracts certain contents from the given html.
     *
     * @param html The html to extract data from.
     * @return The extracted content.
     */
    InputStream extractContent(String html);
}

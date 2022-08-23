package mil.nga.mapcache.io.network;

import java.io.InputStream;

/**
 * Given a web pages html, the extractor will retrieve certain content from the web page to be used
 * within an InputStream.
 */
public interface WebViewExtractor {

    /**
     * Extracts certain contents from the given html.
     *
     * @param html The html to extract data from.
     * @return The extracted content.
     */
    InputStream extractContent(String html);
}

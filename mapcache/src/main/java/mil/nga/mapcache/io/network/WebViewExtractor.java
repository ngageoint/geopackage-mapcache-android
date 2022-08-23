package mil.nga.mapcache.io.network;

import java.io.InputStream;

public interface WebViewExtractor {

    InputStream extractContent(String html);
}

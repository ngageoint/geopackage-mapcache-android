package mil.nga.mapcache.io.network;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gets the image displayed on the web page and returns it in an input stream.
 */
public class WebViewImageExtractor implements WebViewExtractor {

    /**
     * The web view to get the image from.
     */
    private final WebView webView;

    /**
     * Constructor.
     *
     * @param view The web view to get the image from.
     */
    public WebViewImageExtractor(WebView view) {
        this.webView = view;
    }

    @Override
    public InputStream extractContent(String html) {
        ByteArrayInputStream is = null;
        webView.measure(MeasureSpec.makeMeasureSpec(
                        MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(),
                webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        if(webView.getMeasuredHeight() > 0 && webView.getMeasuredWidth() > 0) {
            Bitmap b = Bitmap.createBitmap(webView.getMeasuredWidth() ,
                    webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);

            Paint paint = new Paint();
            int iHeight = b.getHeight();
            c.drawBitmap(b, 0, iHeight, paint);
            webView.draw(c);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, os);
            try {
                os.flush();
                byte[] theBytes = os.toByteArray();
                os.close();
                is = new ByteArrayInputStream(theBytes);
            } catch (IOException e) {
                Log.e(WebViewImageExtractor.class.getSimpleName(), e.getMessage(), e);
            }
        }

        return is;
    }
}

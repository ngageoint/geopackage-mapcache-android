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
        webView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = webView.getMeasuredWidth();
        int height = webView.getMeasuredHeight();
        webView.layout(0, 0, width, height);
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        if (height > 0 && width > 0) {
            Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);

            Paint paint = new Paint();
            int iHeight = b.getHeight();
            int leftOffset = (width - height) / 2 + 1;
            c.drawBitmap(b, 0, iHeight, paint);
            webView.draw(c);

            if(leftOffset > 0) {
                //noinspection SuspiciousNameCombination
                b = Bitmap.createBitmap(b, leftOffset, 0, height, height);
            }

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

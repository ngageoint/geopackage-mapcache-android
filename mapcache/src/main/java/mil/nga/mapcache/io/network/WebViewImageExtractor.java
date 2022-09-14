package mil.nga.mapcache.io.network;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
     * The bitmap of the web page.
     */
    private Bitmap bitmap;

    /**
     * The x, y start locations and the width and height of the image within the page.
     */
    private int[] offsetsWidthHeight;

    /**
     * Constructor.
     *
     * @param view The web view to get the image from.
     */
    public WebViewImageExtractor(WebView view) {
        this.webView = view;
    }

    @Override
    public boolean readyForExtraction(String html) {
        boolean isReady = false;

        webView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = webView.getMeasuredWidth();
        int height = webView.getMeasuredHeight();
        webView.layout(0, 0, width, height);
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        if (height > 0 && width > 0) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);

            Paint paint = new Paint();
            int iHeight = bitmap.getHeight();
            c.drawBitmap(bitmap, 0, iHeight, paint);
            webView.draw(c);

            int halfY = height / 2;
            int halfX = width / 2;
            int pixel = bitmap.getPixel(halfX, halfY);
            isReady = isPixelNotEmpty(pixel);

            if(isReady) {
                offsetsWidthHeight = getOffsetsWidthHeight(bitmap, width, height);
                isReady = offsetsWidthHeight[0] >= 0;
            }
        }

        return isReady;
    }

    @Override
    public InputStream extractContent(String html) {
        InputStream is = null;

        bitmap = Bitmap.createBitmap(
                bitmap,
                offsetsWidthHeight[0],
                offsetsWidthHeight[1],
                offsetsWidthHeight[2],
                offsetsWidthHeight[3]);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        try {
            os.flush();
            byte[] theBytes = os.toByteArray();
            os.close();
            is = new ByteArrayInputStream(theBytes);
        } catch (IOException e) {
            Log.e(WebViewImageExtractor.class.getSimpleName(), e.getMessage(), e);
        }

        return is;
    }

    /**
     * Gets the offsets and width and height of just the image within the web page.
     *
     * @param b      The current bitmap of the web page.
     * @param width  The width of the web page bitmap.
     * @param height The height of the web page bitmap.
     * @return The offsets, width, and height to use when extracting just the image from the web page bitmap.
     */
    private int[] getOffsetsWidthHeight(Bitmap b, int width, int height) {
        int[] offsetsWidthHeight = {0, 0, width, height};

        int halfX = width / 2;
        int topMiddlePixel = b.getPixel(halfX, 0);

        if (isPixelNotEmpty(topMiddlePixel)) {
            offsetsWidthHeight[0] = (width - height) / 2 + 1;
            offsetsWidthHeight[2] = height - 1;
        } else {
            int topOffset = 0;
            int halfY = height / 2;
            for (int i = 1; i < halfY; i++) {
                int pixel = b.getPixel(halfX, i);
                if (isPixelNotEmpty(pixel)) {
                    topOffset = i;
                    break;
                }
            }

            int leftOffset = 0;
            for (int i = 0; i < halfX; i++) {
                int pixel = b.getPixel(i, halfY);
                if (isPixelNotEmpty(pixel)) {
                    leftOffset = i;
                    break;
                }
            }

            int newWidth = width - leftOffset * 2;
            int newHeight = height - topOffset * 2;

            offsetsWidthHeight[0] = leftOffset;
            offsetsWidthHeight[1] = topOffset;
            offsetsWidthHeight[2] = newWidth;
            offsetsWidthHeight[3] = newHeight;
        }

        if(offsetsWidthHeight[0] < 0) {
            Log.e(WebViewImageExtractor.class.getSimpleName(), "Left offset is negative");
        }

        return offsetsWidthHeight;
    }

    /**
     * Checks to see if the current pixel has any color info.
     *
     * @param pixel The pixel to check.
     * @return False if it has no color info, true if it has a color.
     */
    private boolean isPixelNotEmpty(int pixel) {
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);

        return redValue > 14 || blueValue > 14 || greenValue > 14;
    }
}

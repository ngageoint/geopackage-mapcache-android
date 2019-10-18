package mil.nga.mapcache.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import mil.nga.mapcache.R;
import mil.nga.mapcache.listeners.EnableAllLayersListener;
import mil.nga.mapcache.view.GeoPackageViewHolder;

/**
 * Swipe controller handles swiping motions on a Recyclerview.  It will draw "enable/disable" next
 * to the card as the user swipes.  When released, it'll enable or disable all layers inside of a
 * geopackage.  If the user swipes anything but a GeoPackage card, it'll ignore it
 */
public class SwipeController {

    /**
     * A View is currently being swiped.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int ACTION_STATE_SWIPE = 1;

    /**
     * Background image drawable for the swipe button
     */
    private ColorDrawable background;

    /**
     * Boolean to tell us if the card should swipe back into place after release
     */
    private boolean swipeBack = false;

    /**
     * Listener for releasing the swipe and enabling all the layers of that geopackage
     */
    private EnableAllLayersListener activateListener;

    /**
     * Default width of the swipe stop point
     */
    private int buttonWidth = 250;

    /**
     * Resources needed for accessing colors
     */
    Context context;

    /**
     * Lets us know when the user has swiped far enough to perform the enable/disable
     */
    private boolean reachedFullSwipe = false;

    /**
     * Screen vibrator
     */
    private Vibrator vibrator;


    /**
     * Constructor
     * @param context needed for accessing colors
     * @param activeListener listener for when a user swipes to enable all layers of a geopackage
     */
    public SwipeController(Context context, EnableAllLayersListener activeListener){
        activateListener = activeListener;
        this.context = context;
        background = new ColorDrawable(context.getResources().getColor(R.color.nga_accent_primary));
        vibrator = (Vibrator) context.getSystemService(
                Context.VIBRATOR_SERVICE);
    }

    public ItemTouchHelper getTouchHelper(){
        ItemTouchHelper toucher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)  {

            /**
             * save the widest point that the user swiped before releasing.  Used to determine if we
             * should ignore the swipe
             */
            private float widestSwipe = 0;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                if (swipeBack) {
                    swipeBack = false;
                    return 0;
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }

            /**
             * Called every time the card is swiped more or less (as the click and drag)
             * Draws a label with "enable/disable" next to the card as they drag
             * @param c
             * @param recyclerView
             * @param viewHolder
             * @param dX
             * @param dY
             * @param actionState
             * @param isCurrentlyActive
             */
            @Override
            public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
                // Keep track of how far the user has swiped (only while their finger is still on the screen)
                if(isCurrentlyActive) {
                    widestSwipe = dX;
                    // The first time they reach a full swipe, vibrate
                    if(reachedFullSwipe == false && widestSwipe >= buttonWidth){
                        reachedFullSwipe = true;
                        vibrator.vibrate(context.getResources().getInteger(
                                R.integer.map_tiles_long_click_vibrate));
                    }
                }

                // Only draw if they're swiping a geopackage on the main recycler
                if(viewHolder instanceof GeoPackageViewHolder) {
                    GeoPackageViewHolder holder = (GeoPackageViewHolder)viewHolder;
                    String activeLabel = "Enable";
                    if(holder.isActive()){
                        activeLabel = "Disable";
                        // Color lighter before they reach a full swipe, then use a solid color
                        if(widestSwipe > buttonWidth) {
                            background.setColor(context.getResources().getColor(R.color.grey_blue_secondary));
                        } else{
                            background.setColor(context.getResources().getColor(R.color.grey_blue_secondary_light));
                        }
                    } else {
                        if(widestSwipe > buttonWidth) {
                            background.setColor(context.getResources().getColor(R.color.nga_accent_primary));
                        } else{
                            background.setColor(context.getResources().getColor(R.color.nga_accent_bright));

                        }
                    }
                    if(holder.getmDatabase().getTableCount() == 0){
                        activeLabel = "No layers";
                        background.setColor(context.getResources().getColor(R.color.grey_blue_secondary));
                    }

                    if (actionState == ACTION_STATE_SWIPE) {
                        setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                    if (dX < buttonWidth) {
                        background.setBounds(0, viewHolder.itemView.getTop()+10, Math.round(viewHolder.itemView.getLeft() + dX), viewHolder.itemView.getBottom()-10);
                        background.draw(c);
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    } else {
                        background.setBounds(0, viewHolder.itemView.getTop()+10, buttonWidth, viewHolder.itemView.getBottom()-10);
                        background.draw(c);
                        super.onChildDraw(c, recyclerView, viewHolder, buttonWidth, dY, actionState, isCurrentlyActive);
                    }

                    // Draw the text
                    float textSize = 50;
                    Paint p = new Paint();
                    float buttonWidthWithoutPadding = buttonWidth;
                    RectF button = new RectF(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getLeft() + buttonWidthWithoutPadding, viewHolder.itemView.getBottom());
                    p.setColor(Color.WHITE);
                    p.setAntiAlias(true);
                    p.setTextSize(textSize);
                    float textWidth = p.measureText(activeLabel);
                    c.drawText(activeLabel, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
                }
            }

            /**
             * Called once a user releases their finger from a swipe action.  If the user swiped far
             * enough, call the "enable all layers" functions through the listener.  Also update the
             * layers in the viewholder to make sure that they're current for when we open the geopackage
             * detail page
             * @param recyclerView
             * @param viewHolder
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
                // Only run the enable/disable command if the user swiped enough to show the full
                // message button.  If partial swipe, just do nothing
                if(widestSwipe > buttonWidth) {
                    if (viewHolder instanceof GeoPackageViewHolder) {
                        GeoPackageViewHolder holder = (GeoPackageViewHolder) viewHolder;
                        boolean newActiveValue = !holder.isActive();
                        activateListener.onClick(newActiveValue, holder.getmDatabase());
                        holder.getmDatabase().setAllTablesActiveState(newActiveValue);
                        super.clearView(recyclerView, viewHolder);
                        widestSwipe = 0;
                        reachedFullSwipe = false;
//                        ViewAnimation.setBounceAnimatiom(holder.itemView, 1000);
//                        holder.animate();
                    }
                }
            }
        });

        return toucher;
    }


    private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  float dX, float dY, int actionState, boolean isCurrentlyActive) {

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                return false;
            }
        });
    }



}

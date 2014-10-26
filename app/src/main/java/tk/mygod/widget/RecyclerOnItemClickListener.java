package tk.mygod.widget;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author   Mygod
 * Based on: http://stackoverflow.com/a/26196831/2245107
 */
public abstract class RecyclerOnItemClickListener implements RecyclerView.OnItemTouchListener {
    public void onItemClick(RecyclerView rv, View view) { }
    public void onItemLongClick(RecyclerView rv, View view) { }

    @Override
    public boolean onInterceptTouchEvent(final RecyclerView view, MotionEvent e) {
        final View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null) {
            new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    onItemClick(view, childView);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    onItemLongClick(view, childView);
                }
            }).onTouchEvent(e);
            onItemClick(view, childView);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }
}

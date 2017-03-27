package io.github.rathn.platap.TouchHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import io.github.rathn.platap.Adapters.TransactionAdaptor;


public class SwipeHellper extends ItemTouchHelper.Callback {

    private static final float ALPHA_FULL = 1.0f;
    private RecyclerView mRecyclerView;

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        return 0;
        mRecyclerView = recyclerView;
        if (viewHolder instanceof TransactionAdaptor.ViewHolder) {
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        } else
            return 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false; // ---------------------------
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int i = viewHolder.getAdapterPosition();
        /*((ClaseRecyclerAdaptador.ClaseViewHolder)viewHolder).onClick();
        ((ClaseRecyclerAdaptador) mRecyclerView.getAdapter()).LISTA.remove(i);
        mRecyclerView.getAdapter().notifyItemRemoved(i);*/
        TransactionAdaptor.ViewHolder viewHolder1 = (TransactionAdaptor.ViewHolder) viewHolder;
        View swipableView = viewHolder1.getSwipableView();
        Context context = mRecyclerView.getContext();

        viewHolder1.remover(context, swipableView, i);
    }


    /*
     @Override
     public void onSelectedChanged(RecyclerView.MonthHolder viewHolder, int actionState) {
 //        super.onSelectedChanged(viewHolder, actionState);
         if (viewHolder != null) {
             getDefaultUIUtil().onSelected(((ClaseRecyclerAdaptador.ClaseViewHolder) viewHolder).getSwipableView());
         }
     }

  @Override
     public void clearView(RecyclerView recyclerView, RecyclerView.MonthHolder viewHolder) {
 //        super.clearView(recyclerView, viewHolder);
         getDefaultUIUtil().clearView(((ClaseRecyclerAdaptador.ClaseViewHolder) viewHolder).getSwipableView());
     }
 */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - (2 * Math.abs(dX) / (float) viewHolder.itemView.getWidth());
//
//            int mitad = viewHolder.itemView.getWidth() / 2;
//            float diferencia = mitad - Math.abs(dX);

//            final float percent =  Math.abs(diferencia) / mitad;
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        }else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
/*
    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.MonthHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        getDefaultUIUtil().onDrawOver(c, recyclerView, ((ClaseRecyclerAdaptador.ClaseViewHolder) viewHolder)
                .getSwipableView(), dX, dY, actionState, isCurrentlyActive);
    }*/
}

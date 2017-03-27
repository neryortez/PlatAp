package io.github.rathn.platap.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class CalendarRecycler extends RecyclerView.Adapter<CalendarRecycler.MonthHolder> {
    @Override
    public MonthHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MonthHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class MonthHolder extends RecyclerView.ViewHolder {
        public MonthHolder(View itemView) {
            super(itemView);
        }
    }
}

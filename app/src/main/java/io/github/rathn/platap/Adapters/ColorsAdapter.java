package io.github.rathn.platap.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import io.github.rathn.platap.R;
import io.github.rathn.platap.TouchHelper.CategorySetupListener;
import io.github.rathn.platap.customViews.RectColorView;
import io.github.rathn.platap.utils.CategoryUtils;

/**
 * Created by Neri Ortez on 02/01/2017.
 */

public class ColorsAdapter extends BaseAdapter implements View.OnClickListener {
//    private int[] mColors = CategoryUtils.sPalettColors;
    private int[] mColors = CategoryUtils.sPalettColor;
    private LayoutInflater mInflater;
    private CategorySetupListener mListener;
    private int mPreviouslySelectedPosition = 0;
    private RectColorView mPreviouslySelectedView;


    private class ViewHolder {
        RectColorView view;

        private ViewHolder(View convertView) {
            this.view = (RectColorView) convertView.findViewById(R.id.color_view);
        }

        private void setup(int position) {
            this.view.setColor(ColorsAdapter.this.mColors[position]);
            this.view.setTag(position);
            this.view.setOnClickListener(ColorsAdapter.this);
            if (ColorsAdapter.this.mPreviouslySelectedPosition == position) {
                this.view.setChecked(true);
                if (ColorsAdapter.this.mPreviouslySelectedView == null) {
                    ColorsAdapter.this.mPreviouslySelectedView = this.view;
                    return;
                }
                return;
            }
            this.view.setChecked(false);
        }
    }
    public ColorsAdapter(Context context, int color) {
        this.mListener = (CategorySetupListener) context;
        this.mInflater = LayoutInflater.from(context);
        for (int colorIndex = 0; colorIndex < this.mColors.length; colorIndex++) {
            if (this.mColors[colorIndex] == color) {
                this.mPreviouslySelectedPosition = colorIndex;
                return;
            }
        }
    }

    public int getCount() {
        return this.mColors.length;
    }

    public Integer getItem(int position) {
        return this.mColors[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.color_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setup(position);
        return convertView;
    }

    public void onClick(View v) {
        if (this.mPreviouslySelectedView != null) {
            this.mPreviouslySelectedView.setChecked(false);
        }
        ((RectColorView) v).setChecked(true);
        this.mPreviouslySelectedView = (RectColorView) v;
        this.mPreviouslySelectedPosition = (Integer) v.getTag();

//        this.mListener.onColorSelected(CategoryUtils.sCategoryColorIndexes[this.mPreviouslySelectedPosition], this.mColors[this.mPreviouslySelectedPosition]);
        this.mListener.onColorSelected(this.mPreviouslySelectedPosition, this.mColors[this.mPreviouslySelectedPosition]);
    }
}

package io.github.rathn.platap.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import io.github.rathn.platap.TouchHelper.CategorySetupListener;
import io.github.rathn.platap.R;
import io.github.rathn.platap.customViews.CircleImageView;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.utils.CategoryUtils;

public class IconGridAdapter extends BaseAdapter implements OnClickListener {
    private int mDefaultColor;
    private int[] mIcons;
    private LayoutInflater mInflater;
    private CategorySetupListener mListener;
//    private AdapterView.OnItemClickListener mListener;
    private int mPreviouslySelectedPosition;
    private CircleImageView mPreviouslySelectedView;

    private class ViewHolder {
        private CircleImageView view;

        private ViewHolder(View convertView) {
            this.view = (CircleImageView) convertView.findViewById(R.id.category_item_circle);
            this.view.setColor(IconGridAdapter.this.mDefaultColor);
        }

        private void setup(int position) {
            if (this.view.getColor() != IconGridAdapter.this.mDefaultColor) {
                this.view.reset(IconGridAdapter.this.mDefaultColor, IconGridAdapter.this.mIcons[position]);
            } else {
                this.view.setIcon(IconGridAdapter.this.mIcons[position]);
            }
            this.view.setTag(position);
            this.view.setOnClickListener(IconGridAdapter.this);
            if (IconGridAdapter.this.mPreviouslySelectedPosition == position) {
                this.view.setChecked(true);
                if (IconGridAdapter.this.mPreviouslySelectedView == null) {
                    IconGridAdapter.this.mPreviouslySelectedView = this.view;
                    return;
                }
                return;
            }
            this.view.setChecked(false);
        }
    }

    public IconGridAdapter(Context context, Category category) {
        this.mListener = (CategorySetupListener) context;
        this.mInflater = LayoutInflater.from(context);
        this.mDefaultColor = CategoryUtils.getColorsResourceIdForIndex(category.getColorIndex(), context);
        if (PersistentStorage.isFreeVersionRunning()) {
            this.mIcons = CategoryUtils.sCategoryWhiteIcons;
        } else {
            this.mIcons = CategoryUtils.sAllCategoryWhiteIcons;
        }
        this.mPreviouslySelectedPosition = 0;
        for (int iconIndex = 0; iconIndex < this.mIcons.length; iconIndex++) {
            if (this.mIcons[iconIndex] == CategoryUtils.getWhiteIconResourceIdForIndex(category.getIconIndex())) {
                this.mPreviouslySelectedPosition = iconIndex;
                return;
            }
        }
    }

    public void refreshWithDefaultColor(int newColor) {
        if (this.mDefaultColor != newColor) {
            this.mDefaultColor = newColor;
            notifyDataSetChanged();
        }
    }

    public int getCount() {
        return this.mIcons.length;
    }

    public Integer getItem(int position) {
        return this.mIcons[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.circle_category, null);
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
        CircleImageView imageView = (CircleImageView) v;
        imageView.setChecked(true);
        this.mPreviouslySelectedView = imageView;
        this.mPreviouslySelectedPosition = (Integer) v.getTag();
        this.mListener.onIconSelected(this.mPreviouslySelectedPosition, this.mIcons[this.mPreviouslySelectedPosition]);
    }
}

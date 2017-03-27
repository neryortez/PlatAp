package io.github.rathn.platap.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;

import io.github.rathn.platap.R;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.utils.CategoryUtils;

/**
 * Created by Neri Ortez on 05/12/2016.
 */

public class SpinnerAdapter extends ArrayAdapter<Category> {

    public static final String ADD_NEW_CATEGORY_ID = "Add New Category";
    //    private final List<Category> mCategories;
    private final Context mContext;
    private ArrayList<Category> list = new ArrayList<>();

    public SpinnerAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        mContext = context;
    }

    @Override
    public void addAll(@NonNull Collection<? extends Category> collection) {
        list.addAll(collection);
        super.addAll(collection);
        Category addNewCategory = CategoryUtils.getAddNewCategory(true);
        addNewCategory.setName(mContext.getString(R.string.add_new_category));
        addNewCategory.setId(ADD_NEW_CATEGORY_ID);
        super.add(addNewCategory);
    }

    public void replace(@NonNull Category cat, @NonNull int index){
        list.set(index, cat);
        clear();
        addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(final int position, View convertView, @NonNull final ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        ImageView icon = ((ImageView) v.findViewById(R.id.spinner_image_view));
        int iconIndex;
        final Category item = getItem(position);
        assert item != null;
        iconIndex = item.getIconIndex();
        icon.setImageDrawable(mContext.getResources().getDrawable(CategoryUtils.getWhiteIconResourceIdForIndex(iconIndex)));
        int backg = (mContext.getResources().getColor(CategoryUtils.getColorsResourceIdForIndex(item.getColorIndex(), mContext)));
        Drawable b = mContext.getResources().getDrawable(R.drawable.circle_shap);
        b.setColorFilter(backg, PorterDuff.Mode.SRC_IN);
        icon.setBackground(b);

//        v.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                Intent intent = new Intent(getContext(), NewCategoryActivity.class);
//
//                Bundle b = new Bundle();
//                b.putParcelable(NewCategoryActivity.CATEGORY_TO_EDIT, item);
//
//                intent.putExtras(b);
//                intent.putExtra(NewCategoryActivity.IS_EXPENSE, item.isExpense());
//                intent.putExtra(POSICION_DE_CATEGORIA, position);
//                getContext().startActivity(intent);
//                return false;
//            }
//        });
        return v;
    }

    public interface clickeos{
        public void AddNewCategory();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        ImageView icon = ((ImageView) v.findViewById(R.id.spinner_image_view));
        int iconIndex;
        Category item = getItem(position);
        assert item != null;
        iconIndex = item.getIconIndex();
        icon.setImageDrawable(mContext.getResources().getDrawable(CategoryUtils.getWhiteIconResourceIdForIndex(iconIndex)));
        int backg = (mContext.getResources().getColor(CategoryUtils.getColorsResourceIdForIndex(item.getColorIndex(), mContext)));
        Drawable b = mContext.getResources().getDrawable(R.drawable.circle_shap);
        b.setColorFilter(backg, PorterDuff.Mode.SRC_IN);
        icon.setBackground(b);

        return v;
    }
}

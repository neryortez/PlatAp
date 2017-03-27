package io.github.rathn.platap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;

import java.util.UUID;

import io.github.rathn.platap.Adapters.ColorsAdapter;
import io.github.rathn.platap.Adapters.IconGridAdapter;
import io.github.rathn.platap.TouchHelper.CategorySetupListener;
import io.github.rathn.platap.databinding.ActivityNewCategoryBinding;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.persistent.DatabaseManager;
import io.github.rathn.platap.utils.CategoryUtils;

public class NewCategoryActivity extends AppCompatActivity implements CategorySetupListener {
    public static final String CATEGORY_TO_EDIT = "categoryToEdit";
    public static final String POSICION_DE_CATEGORIA = "posicion";
    public static final String IS_EXPENSE = "expense?";
    public static final String CATEGORIA_INSERTADA = "insertada_bien";
    private static final String IS_CHANGING = "cambiando";
    public static final String CATEGORIA_INSERTADA_COMPLETO = "completada_la_insercion";
    public static final String CATEGORIA_BORRADA = "borrada";

    ActivityNewCategoryBinding binding;
    IconGridAdapter categoryGridAdapter;
    ColorsAdapter colorsAdapter;
    Category mCategory;

    DatabaseManager databaseManager;
    private boolean isChanging;
    private int position;
    private boolean isExpense;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isChanging) {
            menu.add(R.string.create_category).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            createCategory();
                            return true;
                        }
                    });
        }
        if (isChanging){
            menu.add(R.string.save_category).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            createCategory();
                            return true;
                        }
                    });
            menu.add(R.string.delete_category).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    binding.categoryToolbar.requestFocus();
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    Snackbar.make(binding.gridLayout, R.string.category_delete_warning, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.delete_category, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    databaseManager.deleteCategory(mCategory);
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
        return true;
    }

    private void createCategory() {
        mCategory.setName(binding.newCategoryName.getEditText().getText().toString());
        if(binding.newCategoryName.getEditText().getText().toString().isEmpty()){
            binding.newCategoryName.setError(getString(R.string.monto_error_msg));
            return;
        }
        if (!isChanging) {
            databaseManager.insertCategory(mCategory);
        } else {
            databaseManager.updateCategory(mCategory);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseManager.close();
    }

    @Override
    protected void onResume() {
        super.onPostResume();
        databaseManager.open();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_new_category);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_category);

        databaseManager = new DatabaseManager(getApplicationContext());
        databaseManager.open();

        setSupportActionBar(binding.categoryToolbar);

        mCategory = new Category(UUID.randomUUID().toString(), "name");
        mCategory.setColorIndex(0);
        mCategory.setCategoryColor(getResources().getColor(CategoryUtils.getColorsResourceIdForIndex(0, this)));
        mCategory.setIconIndex(0);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getParcelable(CATEGORY_TO_EDIT) != null) {
                mCategory = getIntent().getExtras().getParcelable(CATEGORY_TO_EDIT);
                position = getIntent().getExtras().getInt(POSICION_DE_CATEGORIA);
                isChanging = true;
                isExpense = mCategory.isExpense();
                binding.newCategoryName.getEditText().setText(mCategory.getName());
            } else {
                isExpense = getIntent().getBooleanExtra(IS_EXPENSE, false);
                mCategory.setIsExpense(isExpense);
            }
        }

        binding.categoryIcon.setColor(CategoryUtils.getColorsResourceIdForIndex(mCategory.getColorIndex(), this));
        binding.categoryIcon.setIcon(CategoryUtils.getWhiteIconResourceIdForIndex(mCategory.getIconIndex()));

        categoryGridAdapter = new IconGridAdapter(this, mCategory);
        colorsAdapter = new ColorsAdapter(this, mCategory.getCategoryColor());

        binding.categoryIcons.setAdapter(categoryGridAdapter);
        binding.categoryIcons.setNumColumns(5);

        binding.toggleButton.setOnCheckedChangeListener(pedrito);

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCategoryActivity.this.finish();
            }
        });

        IntentFilter filter = new IntentFilter(CATEGORIA_INSERTADA);
        filter.addAction(CATEGORIA_BORRADA);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intent1 = new Intent(CATEGORIA_INSERTADA_COMPLETO);
            if (isChanging) intent1.putExtra(POSICION_DE_CATEGORIA, position);
            else intent1.putExtra(IS_CHANGING, true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
            NewCategoryActivity.this.finish();
        }
    };

    @Override
    public void onColorSelected(int position, int colorResource) {
        binding.categoryIcon.setColor(colorResource);
        mCategory.setColorIndex(position);
        categoryGridAdapter.refreshWithDefaultColor(colorResource);
    }

    @Override
    public void onIconSelected(int position, int iconSelected) {
        binding.categoryIcon.setIcon((iconSelected));
        mCategory.setIconIndex(position);
    }

    private CompoundButton.OnCheckedChangeListener pedrito = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                binding.categoryIcons.setAdapter(colorsAdapter);
                binding.categoryIcons.setNumColumns(7);
            } else {
                binding.categoryIcons.setAdapter(categoryGridAdapter);
                binding.categoryIcons.setNumColumns(5);
            }
        }
    };

}


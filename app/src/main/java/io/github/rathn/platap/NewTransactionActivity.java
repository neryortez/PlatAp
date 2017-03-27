package io.github.rathn.platap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.github.rathn.platap.Adapters.SpinnerAdapter;
import io.github.rathn.platap.databinding.NewActivityNewTransactionBinding;
import io.github.rathn.platap.dto.Account;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.dto.Factura;
import io.github.rathn.platap.dto.RepeatInfo;
import io.github.rathn.platap.dto.Transaction;
import io.github.rathn.platap.persistent.DatabaseManager;
import io.github.rathn.platap.utils.DateTimeUtils;

import static com.github.sundeepk.compactcalendarview.CompactCalendarView.TRANSACTIONS_UPDATED;
import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_WEEKENS;
import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_WEEK_DAY;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_DAYLY;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_IRREGULAR;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_MONTHLY;

public class NewTransactionActivity extends AppCompatActivity {
    public static final String TRANSACTION_TO_EDIT = "transaction";
    public static final String POSICION_DE_TRANSACTION = "position";
    public static final String CALENDAR = "calendar";
    public static final String TIME = "time";
    public static final String REPEATINFO_OF_TRANSACTION = "repeatInfo_of_transaction";
    static final int REQUEST_TAKE_PHOTO = 100;
    static final int REQUEST_IMAGE_GET = 200;
    Transaction transaction = new Transaction();
    DatabaseManager databaseManager;
    SpinnerAdapter spinnerAdapter;

    NewActivityNewTransactionBinding viewsBinding;
    /**
     * Beginning of the Facturas Files Handling
     ****************************************************************/

    String mCurrentPhotoPath;
    private Account calendar;
    private boolean isChanging;
    private boolean pendingChange;
    private String transactionID;
    private ArrayList<String> mPhotosPath = new ArrayList<>();
    private boolean onlyThis = false;
    private CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            spinnerAdapter.clear();
            viewsBinding.spinner.setSelection(0, true);
            if (isChecked) {
                spinnerAdapter.addAll(databaseManager.getIncomeCategories());
                String prev = viewsBinding.monto.getText().toString();
                if (prev.startsWith("-")) {
                    viewsBinding.monto.setText(prev.substring(1));
                }
            } else {
                spinnerAdapter.addAll(databaseManager.getExpenseCategories());
                String prev = viewsBinding.monto.getText().toString();
                if (!prev.contains("-")) {
                    try {
                        String nuevo = "-" + prev;
                        viewsBinding.monto.setText(nuevo);
                    } catch (IndexOutOfBoundsException e) {
                        viewsBinding.monto.setText("-0.0");
                    }
                }
            }
            spinnerAdapter.notifyDataSetChanged();
        }
    };
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String prev = s.toString();
            boolean isExpense = prev.startsWith("-");
            if (isExpense) {
                prev = prev.substring(1);
            }

            if (prev.startsWith(".")) {             /** Evitar los numero enteros iniciando en punto sin cero*/
                prev = "0" + prev;

                if (isExpense) prev = "-" + prev;
//                s.clear();
                s.replace(0, s.length(), prev);
            } else if (prev.startsWith("0") && prev.indexOf(".") != 1) { /** Evitar los numeros enteros iniciando en cero */
                prev = prev.substring(1);

                if (isExpense) prev = "-" + prev;
                s.replace(0, s.length(), prev);
            } else if (s.toString().isEmpty() && !viewsBinding.toggleButton.isChecked()) { /** Evitar qeu borren el '-' si es en gastos */
                s.append("-");
            } else if (s.toString().contains("-") && viewsBinding.toggleButton.isChecked()) { /** Evitar que pongan '-' si es entrada */
                s.replace(0, s.length(), s.toString(), 1, s.length());
            } else if (!s.toString().contains("-") && !viewsBinding.toggleButton.isChecked()) { /** Evitar que numeros positivos en gastos */
                s.replace(0, s.length(), "-" + s.toString(), 0, s.length() + 1);
            } /*else if ( prev.isEmpty() ){                                           *//** Evitar numeros vacios *//*
                s.replace(0, s.length(), viewsBinding.toggleButton.isChecked() ? "0" : "-0");
            }*/
            viewsBinding.textInputLayout.setErrorEnabled(false);
        }
    };
    private RepeatInfo repeatInfo;
    View.OnClickListener addNewTransaction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //<editor-fold desc="Process to add a new transaction, change the transaction, get the information from the activity up to the point of calling the databaseManager">
            Float aFloat;
            try {
                aFloat = Float.valueOf(viewsBinding.monto.getText().toString());
            } catch (NumberFormatException e) {
                Snackbar.make(viewsBinding.toggleButton, R.string.error_format_number, Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (aFloat == 0f) {
                viewsBinding.textInputLayout.setError(getString(R.string.monto_error_msg));
                requestFocus(viewsBinding.monto);
                Snackbar.make(viewsBinding.toggleButton, R.string.error_zero_amount, Snackbar.LENGTH_SHORT).show();
            } else {
                transaction.setCalendar(calendar);
//                t.setCategory(databaseManager.getFirstExepenseCategory());
                transaction.setPrice(Double.parseDouble(viewsBinding.monto.getText().toString()));
                if (!isChanging) {
                    transaction.setId(transactionID);
                } else {
                    if (transaction.isRepeating()) {
                        new AlertDialog.Builder(getApplicationContext())
                                .setMessage(R.string.changing_repetition_message_title)
                                .setMessage(R.string.changing_repetition_message)
                                .setPositiveButton(R.string.change_all, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onlyThis = false;
                                        continueAdding();
                                    }
                                })
                                .setNegativeButton(R.string.change_just_this, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onlyThis = true;
                                        continueAdding();
                                    }
                                })
                                .show();
                        return;
                    }
                }
                continueAdding();
//            NewTransactionActivity.this.finish();
            }
            //</editor-fold>
        }
    };
    private String repeatUUID;
    private AdapterView.OnItemSelectedListener repeatClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            repeatInfo = new RepeatInfo();
            repeatInfo.setId(repeatUUID);
            String rep = parent.getItemAtPosition(position).toString();
            String[] a = getResources().getStringArray(R.array.repetition_array);
            int i = 0;
            for (String b :
                    a) {
                if (b.equals(rep)) {
                    break;
                }
                i++;
            }
            int repeatType = 0;
            int interval = 0;
            switch (i) {
                case 1:/*<item>Diariamente</item>*/
                    repeatType = TYPE_DAYLY;
                    interval = 1;
                    break;
                case 2:/*<item>Día de por medio</item>*/
                    repeatType = TYPE_DAYLY;
                    interval = 2;
                    break;
                case 3:/*<item>Días de semana</item> */
                    repeatType = TYPE_IRREGULAR;
                    interval = INTERVAL_WEEK_DAY;
                    break;
                case 4:/*<item>Fines de semana</item> */
                    repeatType = TYPE_IRREGULAR;
                    interval = INTERVAL_WEEKENS;
                    break;
                case 5:/*<item>Semanalmente</item>     */
                    repeatType = TYPE_DAYLY;
                    interval = 7;
                    break;
                case 6:/*<item>Cada dos semanas</item> */
                    repeatType = TYPE_DAYLY;
                    interval = 14;
                    break;
                case 7: /*<item>Mensualmente</item>*//*   */
                    repeatType = TYPE_MONTHLY;
                    interval = 1;
                    break;
                case 0: /*None*/
                    repeatInfo = null;
                    return;
            }
            repeatInfo.setRepeat(repeatType, interval);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private DialogInterface.OnClickListener categoriaExpenseSeleccionadaListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Category category = databaseManager.getExpenseCategories().get(which);
            Intent intent = new Intent(getApplicationContext(), NewCategoryActivity.class);
            intent.putExtra(NewCategoryActivity.CATEGORY_TO_EDIT, category);
            intent.putExtra(NewCategoryActivity.POSICION_DE_CATEGORIA, which);
            startActivity(intent);
        }
    };
    private DialogInterface.OnClickListener categoriaIncomeSeleccionadaListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Category category = databaseManager.getIncomeCategories().get(which);
            Intent intent = new Intent(getApplicationContext(), NewCategoryActivity.class);
            intent.putExtra(NewCategoryActivity.CATEGORY_TO_EDIT, category);
            intent.putExtra(NewCategoryActivity.POSICION_DE_CATEGORIA, which);
            startActivity(intent);
        }
    };
    private PopupMenu.OnMenuItemClickListener categoriesPopupListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String string = item.getTitle().toString();
            if (string.equals(getString(R.string.create_category))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTransactionActivity.this);
                builder.setTitle(R.string.message_category_select)
                        .setItems(R.array.category_new_select, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), NewCategoryActivity.class);
                                intent.putExtra(NewCategoryActivity.IS_EXPENSE, which != 0);
                                startActivity(intent);
                            }
                        })
                        .create()
                        .show();

                return true;
            } else if (string.equals(getString(R.string.modify_categories))) {
                PopupMenu popup = new PopupMenu(NewTransactionActivity.this, viewsBinding.space);
                popup.getMenu().add(R.string.Gasto);
                popup.getMenu().add(R.string.Income);

                popup.setOnMenuItemClickListener(categoriesPopupListener);
                popup.show();
            } else if (string.equals(getString(R.string.Gasto))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTransactionActivity.this);
                builder.setTitle(R.string.select_a_category).setItems(databaseManager.getExpenseCategoriesString(), categoriaExpenseSeleccionadaListener)
                        .create()
                        .show();
            } else if (string.equals(getString(R.string.Income))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTransactionActivity.this);
                builder.setTitle(R.string.select_a_category).setItems(databaseManager.getIncomeCategoriesString(), categoriaIncomeSeleccionadaListener)
                        .create()
                        .show();

            }
            return false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        databaseManager.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseManager.open();
        if (pendingChange) {
            pendingChange = false;
            ((SpinnerAdapter) viewsBinding.spinner.getAdapter()).clear();
            if (viewsBinding.toggleButton.isChecked()) {
                ((SpinnerAdapter) viewsBinding.spinner.getAdapter()).addAll(databaseManager.getIncomeCategories());
            } else {
                ((SpinnerAdapter) viewsBinding.spinner.getAdapter()).addAll(databaseManager.getExpenseCategories());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.new_activity_new_transaction);
        viewsBinding =
                DataBindingUtil.setContentView(this, R.layout.new_activity_new_transaction);

        setSupportActionBar(viewsBinding.transactionToolbar);

        databaseManager = new DatabaseManager(getApplicationContext());
        databaseManager.open();

        // Create an ArrayAdapter using the string array and a default spinner layout
        spinnerAdapter = new SpinnerAdapter(this, R.layout.spinner_dropdown_item, android.R.id.text1);
        spinnerAdapter.addAll(databaseManager.getExpenseCategories());
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        viewsBinding.toggleButton.setOnCheckedChangeListener(toggleListener);

//        boton = ((Button) findViewById(R.id.button));
//        boton.setOnClickListener(addNewTransaction);

        viewsBinding.monto.addTextChangedListener(textWatcher);

//        datePicker = ((DatePicker) findViewById(R.id.datePicker));
        viewsBinding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.YEAR, year);
                view.setDate(date.getTimeInMillis());
                //view.setVisibility(View.GONE);
            }
        });

        Parcelable parcelable;
        viewsBinding.transactionToolbar.setTitle(R.string.new_transaction_title);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getParcelable(TRANSACTION_TO_EDIT) != null) {
                parcelable = getIntent().getExtras().getParcelable(TRANSACTION_TO_EDIT);
                transaction = ((Transaction) parcelable).copy();
                transactionID = transaction.getId();
                Calendar date = transaction != null ? transaction.getDate() : null;
                assert date != null;
                viewsBinding.calendarView.setDate(date.getTimeInMillis());
                viewsBinding.monto.setText(String.valueOf(((float) transaction.getPrice())));
                isChanging = true;
                viewsBinding.note.getEditText().setText(transaction.getNote());
                viewsBinding.toggleButton.setChecked(!transaction.isExpense());
                calendar = databaseManager.getCalendarWithId(transaction.getCalendarId());
                repeatInfo = getIntent().getExtras().getParcelable(REPEATINFO_OF_TRANSACTION);
                transaction.setRepeatInfo(repeatInfo);
                if (repeatInfo != null) {
                    repeatUUID = repeatInfo.getId();
                } else repeatUUID = UUID.randomUUID().toString();
            } else {
                viewsBinding.calendarView.setDate(getIntent().getLongExtra(TIME, Calendar.getInstance().getTimeInMillis()));
                isChanging = false;
                calendar = getIntent().getExtras().getParcelable(CALENDAR);
                repeatInfo = null;
                repeatUUID = UUID.randomUUID().toString();
                transactionID = UUID.randomUUID().toString();
            }
            getAndPlaceFacturas();
        }

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(TRANSACTIONS_UPDATED);
        mStatusIntentFilter.addAction(NewCategoryActivity.CATEGORIA_INSERTADA_COMPLETO);
        NewTransactionBroadcastReceiver broadcastReceiver = new NewTransactionBroadcastReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mStatusIntentFilter);

        viewsBinding.transactionToolbar.requestFocus();


        // Apply the adapter to the spinner
        viewsBinding.spinner.setAdapter(spinnerAdapter);
        viewsBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category cat = ((Category) parent.getItemAtPosition(position));
                if (cat.getId().equals(SpinnerAdapter.ADD_NEW_CATEGORY_ID)) {
                    Intent intent = new Intent(getApplicationContext(), NewCategoryActivity.class);
                    intent.putExtra(NewCategoryActivity.IS_EXPENSE, !viewsBinding.toggleButton.isChecked());
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //<editor-fold desc="Implement and set the repetito spinner">
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repetition_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewsBinding.repetitionSpinner.setAdapter(adapter);

        viewsBinding.repetitionSpinner.setOnItemSelectedListener(repeatClickListener);//</editor-fold>

        //<editor-fold desc="unfatefull try of set the OnItemLongClickL Listener of the category spinner">
        //        viewsBinding.spinner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Category cat = ((Category) parent.getItemAtPosition(position));
//
//                Intent intent = new Intent(NewTransactionActivity.this, NewCategoryActivity.class);
//
//                Bundle b = new Bundle();
//                b.putParcelable(NewCategoryActivity.CATEGORY_TO_EDIT, cat);
//
//                intent.putExtras(b);
//                intent.putExtra(NewCategoryActivity.IS_EXPENSE, cat.isExpense());
//                intent.putExtra(POSICION_DE_CATEGORIA, position);
//                startActivity(intent);
//                return true;
//            }
//        });
//</editor-fold>

        //<editor-fold desc="Setting of the category and repetition info on the Spinners">
        if (isChanging) {
            List<Category> cats;
            if (transaction.isExpense()) {
                cats = databaseManager.getExpenseCategories();
            } else {
                cats = databaseManager.getIncomeCategories();

            }
            for (Category cat : cats) {
                if (cat.getId().equals(transaction.getCategoryId())) {
                    viewsBinding.spinner.setSelection(cats.indexOf(cat), true);
                    break;
                }
            }
            if (repeatInfo != null) {
                viewsBinding.repetitionSpinner.setSelection(repeatInfo.getRepeat());
            }
        }
        //</editor-fold>


        viewsBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String path : mPhotosPath) {
                    new File(path).delete();
                }
                NewTransactionActivity.this.finish();
            }
        });
    }

    private void getAndPlaceFacturas() {
        viewsBinding.recyclerFacturas.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        transaction.setFacturas(databaseManager.getFacturasFromTransaction(transaction));
        RecyclerView.Adapter adapter = new FacturasAdapter(transaction.getFacturas());
        viewsBinding.recyclerFacturas.setAdapter(adapter);
    }

    private void continueAdding() {
        Calendar raitNau = Calendar.getInstance();
        Calendar d = Calendar.getInstance();
           /* d.set(YEAR, datePicker.getYear());
            d.set(Calendar.MONTH, datePicker.getMonth());
            d.set(Calendar.DATE, datePicker.getDayOfMonth());*/
        d.setTimeInMillis(viewsBinding.calendarView.getDate());

        boolean fore = true;
        if (DateTimeUtils.getCalendarToMidnight(raitNau).after(d)) {
            fore = false;
        }
        transaction.setForecasted(fore);
        transaction.setDate(d);
        transaction.setCategory((Category) viewsBinding.spinner.getSelectedItem());
        transaction.setNote(viewsBinding.note.getEditText().getText().toString());
        transaction.setExpense(!viewsBinding.toggleButton.isChecked());
        if (repeatInfo != null) {
            repeatInfo.setStartDate(transaction.getDate());
            if (!isChanging) {
                transaction.setOriginalTransactionId(transactionID);
            }
        }
        transaction.setRepeatInfo(repeatInfo);
        if (isChanging) {
            databaseManager.updateTransaction(transaction, transaction, onlyThis);
        } else {
            databaseManager.insertTransaction(transaction);
        }
    }

    //</editor-fold>

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isChanging) {
            getMenuInflater().inflate(R.menu.modify_transaction_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.transaction_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.modify_transaction) {
            addNewTransaction.onClick(item.getActionView());
            return true;
        } else if (id == R.id.add_new_transaction) {
            addNewTransaction.onClick(item.getActionView());
        }

        return super.onOptionsItemSelected(item);
    }

    //<editor-fold desc="Menu de edicion de categorias">
    public void categoriesPopup(MenuItem item) {
        PopupMenu popup = new PopupMenu(this, viewsBinding.space);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.categories_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(categoriesPopupListener);
        popup.show();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        File image = new File(
                storageDir,      /* directory */
                imageFileName +  /* prefix */
                        ".jpg"           /* suffix */
        );
        if (!image.exists()) {
            if (!image.createNewFile()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mPhotosPath.add(mCurrentPhotoPath);
        return image;
    }

    private void saveImageFromIntent(Intent data) throws IOException {
        File pictureFile;
        pictureFile = null;
        pictureFile = createImageFile();
        assert pictureFile != null;
        FileOutputStream outputStream = new FileOutputStream(pictureFile);
        FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(data.getData());
        byte[] buffer = new byte[1024];
        while (true) {
            int length = inputStream.read(buffer);
            if (length > 0) {
                outputStream.write(buffer, 0, length);
            } else {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                break;
            }
        }
    }

    private Uri getOutputMediaFileUri() throws IOException {
        return Uri.fromFile(createImageFile());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            Uri photoURI = null;
            try {
                photoURI = getOutputMediaFileUri();
            } catch (IOException e) {
                e.printStackTrace();
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            /*}*/
        }
    }

    private void dispatchSelectImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // Image captured and saved to fileUri specified in the Intent
                    Toast.makeText(this, "Image saved to:\n" +
                            mCurrentPhotoPath, Toast.LENGTH_LONG).show();
                    Factura factura = new Factura(mCurrentPhotoPath, transactionID, UUID.randomUUID().toString());
                    transaction.addFactura(factura);
                    viewsBinding.recyclerFacturas.getAdapter().notifyItemInserted(transaction.getFacturas().size() - 1);
                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                    Toast.makeText(this, "Image not saved CANCEKLED", Toast.LENGTH_LONG).show();
                    new File(mCurrentPhotoPath).delete();
                } else {
                    // Image capture failed, advise user
                    Toast.makeText(this, "Image not saved ERROR", Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_IMAGE_GET:
                if (resultCode == RESULT_OK) {
                    File pictureFile = null;
                    try {
                        saveImageFromIntent(data);
                    } catch (FileNotFoundException e) {
                        Log.d("ERROR", "File not found: " + e.getMessage());
                        pictureFile.delete();
                        return;
                    } catch (IOException e) {
                        Log.d("ERROR", "Error accessing file: " + e.getMessage());
                        pictureFile.delete();
                        return;
                    }
                    Factura factura = new Factura(mCurrentPhotoPath, transactionID, UUID.randomUUID().toString());
                    transaction.addFactura(factura);
                    viewsBinding.recyclerFacturas.getAdapter().notifyItemInserted(transaction.getFacturas().size() - 1);
                } else if (resultCode != RESULT_CANCELED) {
                    Snackbar.make(viewsBinding.transactionToolbar, R.string.image_selector_error, Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private class NewTransactionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case TRANSACTIONS_UPDATED:
                    NewTransactionActivity.this.finish();
                    break;
                case NewCategoryActivity.CATEGORIA_INSERTADA_COMPLETO:
                    pendingChange = true;
            }
        }
    }

    /**
     * END OF FACTURAS FILES HANDLING
     ********************************************************************/

    public class FacturasAdapter extends RecyclerView.Adapter<FacturasAdapter.ViewHolder> {
        private final List<Factura> facturas;

        FacturasAdapter(List<Factura> facturas) {
            super();
            Factura object = new Factura();
            object.setId("new");
            if (facturas == null) {
                facturas = new ArrayList<>();
            }
            facturas.add(0, object);
            this.facturas = facturas;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new CardView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final File file;
            RequestCreator load;
            final Factura factura = facturas.get(position);
            if (!factura.getId().equals("new")) {
                file = new File(factura.getFilePath());
                load = Picasso.with(getApplicationContext()).load(file);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView im = new ImageView(getApplicationContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int margin = dpToPixel(8, getResources());
                        params.setMargins(margin, margin, margin, margin);
                        im.setLayoutParams(params);
                        im.setScaleType(ImageView.ScaleType.FIT_XY);
                        im.setImageDrawable(((ImageView) v).getDrawable());
                        im.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri uri = Uri.parse("content://io.github.rathn.platap/" + new File(factura.getFilePath()).getName());
                                intent.setDataAndType(uri, "image/*");
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }
                        });
//                        ImageView im = ((ImageView) v);

                        new AlertDialog.Builder(NewTransactionActivity.this).setTitle(R.string.factura)
                                .setView(im)
                                .setNegativeButton(R.string.delete_factura, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new File(((FacturasAdapter) viewsBinding.recyclerFacturas.getAdapter())
                                                .facturas.remove(holder.getAdapterPosition())
                                                .getFilePath()).delete();
                                        viewsBinding.recyclerFacturas.getAdapter().notifyItemRemoved(holder.getAdapterPosition());
                                    }
                                })
                                .setPositiveButton(R.string.continuar, null)
                                .create().show();
                    }
                });
            } else {
                load = Picasso.with(NewTransactionActivity.this).load(R.drawable.ic_category_add_new);
                holder.imageView.setBackgroundColor(Color.GRAY);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(NewTransactionActivity.this)
                                .setItems(R.array.new_factura_options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            dispatchTakePictureIntent();
                                        } else dispatchSelectImageIntent();
                                    }
                                }).create().show();
                    }
                });
            }
            load.into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return facturas.size();
        }

        int dpToPixel(float dp, Resources resources) {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            return (int) (metrics.density * dp);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public String idFactura;
            public String filePath;
            ImageView imageView;


            ViewHolder(View itemView) {
                super(itemView);
                ((CardView) itemView).setCardBackgroundColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                int margin = dpToPixel(8, getResources());
                params.setMargins(margin, margin, margin, margin);
                itemView.setLayoutParams(params);
                imageView = new ImageView(itemView.getContext());
                imageView.setPadding(2, 2, 2, 2);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                CardView.LayoutParams layoutParams = new CardView.LayoutParams(128, 128);
                ((CardView) itemView).addView(imageView, layoutParams);

            }
        }
    }
}

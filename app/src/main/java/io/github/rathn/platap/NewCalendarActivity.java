package io.github.rathn.platap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;

import java.util.UUID;

import io.github.rathn.platap.databinding.NewCalendarDialogLayoutBinding;
import io.github.rathn.platap.dto.Account;
import io.github.rathn.platap.persistent.DatabaseManager;


public class NewCalendarActivity extends AppCompatActivity {
    public static final String CALENDAR_TO_EDIT = "calendar_to_edit";
    public static final String CALENDAR_UPDATE = "calendar_update";
    public static final String CALENDAR_UPDATED = "calendar_updated";
    public static final String CALENDAR_DELETED = "calendar_delete";
    public static final String CALENDAR_RE_INSERTED = "calendar_reinserted";

    DatabaseManager databaseManager;

    Account calendar;
    NewCalendarDialogLayoutBinding binding;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.new_calendar_dialog_layout);
        this.setFinishOnTouchOutside(true);

        databaseManager = new DatabaseManager(getApplicationContext());
        calendar = getIntent().getParcelableExtra(CALENDAR_TO_EDIT);
        if (calendar != null) {
            isChanging = true;
            id = calendar.getId();
            binding.setCalendar(calendar);
            binding.cancelButton.setText(R.string.delete);
            binding.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {delete(calendar);
                }
            });
            binding.okButton.setText(R.string.save_button);
        } else {
            calendar = new Account();
            isChanging = false;
            id = UUID.randomUUID().toString();
            binding.cancelButton.setOnClickListener(cancel);
            calendar.setBalance(0d);
        }

        binding.okButton.setOnClickListener(okbutton);



        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter();
        mStatusIntentFilter.addAction(CALENDAR_UPDATE);
        mStatusIntentFilter.addAction(CALENDAR_DELETED);
        mStatusIntentFilter.addAction(CALENDAR_RE_INSERTED);

        NewCalendarBroadcastReceiver broadcastReceiver = new NewCalendarActivity.NewCalendarBroadcastReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mStatusIntentFilter);
    }

    private boolean isChanging;

    private View.OnClickListener okbutton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Editable name = binding.textInputLayout2.getEditText().getText();
            if (name.length() < 3) {
                binding.textInputLayout2.setError(getString(R.string.error_category_name));
            } else {
                calendar = new Account(id, name.toString());
                calendar.setBalance(Double.valueOf(binding.balance.getEditText().getText().toString()));
                if (!isChanging) {
                    databaseManager.insertCalendar(calendar);
                } else {
                    databaseManager.updateCalendar(calendar);
                }
            }
        }
    };

    private void delete(final Account calendar){
        Snackbar.make(binding.cancelButton, R.string.delete_calendar_warning, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseManager.deleteCalendar(calendar);
                    }
                }).show();
    }

    private View.OnClickListener cancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };





    private class NewCalendarBroadcastReceiver extends BroadcastReceiver{
        NewCalendarBroadcastReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getData();
            switch (intent.getAction()) {
                case CALENDAR_UPDATE:
                    NewCalendarActivity.this.finish();
                    break;
                case CALENDAR_DELETED:
                    NewCalendarActivity.this.finish();
                    break;
                case CALENDAR_RE_INSERTED:
                    NewCalendarActivity.this.finish();
                    break;
            }
        }
    }
}


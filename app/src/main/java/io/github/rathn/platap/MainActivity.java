package io.github.rathn.platap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.github.rathn.platap.Adapters.TransactionAdaptor;
import io.github.rathn.platap.TouchHelper.SwipeHellper;
import io.github.rathn.platap.dto.Account;
import io.github.rathn.platap.dto.Balance;
import io.github.rathn.platap.dto.Transaction;
import io.github.rathn.platap.persistent.DatabaseManager;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.utils.DateTimeUtils;

import static com.github.sundeepk.compactcalendarview.CompactCalendarView.TRANSACTIONS_RE_INSERTED;
import static com.github.sundeepk.compactcalendarview.CompactCalendarView.TRANSACTIONS_UPDATED;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_DELETED;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_RE_INSERTED;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_TO_EDIT;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_UPDATE;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_UPDATED;


//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public DatabaseManager mDatabaseManager;
    //private FirebaseAnalytics mFirebaseAnalytics;

    public static SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    public RecyclerView mRecyclerView;
    public List<Transaction> transactionList;
    public CompactCalendarView calendarView;
    public Date diaActual;
    Toolbar toolbar;
    private DatabaseOpenHelper sDatabaseHelper;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Date mHoy;
    private DatabaseMan mDBManager;

    private HashMap<String, Double> balancesForTheMonth = new HashMap<>();
    private HashMap<String, Double> balances = new HashMap<>();
    private ArrayList<Balance> balancesForTheMonthList;
    private ArrayList<Balance> balancesList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private double diaActualBalance;
    private Account calendarAccount;
    CompactCalendarView.CompactCalendarViewListener calendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date dateClicked) {
            diaActual = dateClicked;

            String[] calendarId = new String[1];
            calendarId[0] = calendarAccount.getId();

            transactionList.clear();
//            transactionList.addAll(mDBManager.getList(dateClicked));
            transactionList.addAll(mDatabaseManager.getTransactionsForDay(calendarId, DateTimeUtils.getCalendarFromDate(diaActual)));
//            mAdapter.notifyDataSetChanged();
            balancesForTheMonthList = mDatabaseManager.getBalancesForEveryDayOnMonth(calendarId,
                    DateTimeUtils.getFirstDateOfMonth(DateTimeUtils.getCalendarFromDate(dateClicked)),
                    DateTimeUtils.getLastDateOfMonth(DateTimeUtils.getCalendarFromDate(dateClicked)));
            balancesForTheMonth = DatabaseMan.getBalancesHashMapFromList(balancesForTheMonthList);
            calendarView.setBalances(balancesForTheMonth);


//            calendarView.setBalanceForToday(mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCalendarFromDate(dateClicked), false));
            calendarView.setBalanceForCurrentDate(mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCalendarFromDate(calendarView.getCurrentDate()), false));
            transactionListChanged();
        }

        @Override
        public void onMonthScroll(Date date) {
            diaActual = date;

            String[] calendarId = new String[1];
            calendarId[0] = calendarAccount.getId();

            transactionList.clear();
//            transactionList.addAll(mDBManager.getList(date));
            Calendar aCalendar = DateTimeUtils.getCalendarFromDate(diaActual);
            transactionList.addAll(mDatabaseManager.getTransactionsForDay(calendarId, aCalendar));
//            mAdapter.notifyDataSetChanged();
            balancesForTheMonthList = mDatabaseManager.getBalancesForEveryDayOnMonth(calendarId,
                    aCalendar,
                    DateTimeUtils.getLastDateOfMonth(aCalendar));
            balancesForTheMonth = DatabaseMan.getBalancesHashMapFromList(balancesForTheMonthList);
            calendarView.setBalances(balancesForTheMonth);


            calendarView.setBalanceForToday(mDatabaseManager.getBalance(calendarId, Calendar.getInstance(), false));

            calendarView.setBalanceForCurrentDate(mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCalendarFromDate(date), false));


            transactionListChanged();
            cambiarTitulo();

//
//            calendarAccount.set(Calendar.HOUR_OF_DAY, 23);
//            calendarAccount.set(Calendar.MINUTE, 59);
//            calendarAccount.set(Calendar.SECOND, 59);
//            calendarAccount.set(Calendar.MILLISECOND, 999);
        }
    };
    private NavigationView navigationView;
    private SubMenu calendarMenu;
    private boolean calendarReInsertedPending;
    private boolean calendarUpdatePending;
    private boolean calendarDeletedPending;

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseManager.open();
        if (calendarView.isUpdatePending()) {
            String[] calendarId = new String[1];
            calendarId[0] = calendarAccount.getId();
            double balance = mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCurrentDateWithoutTime()/*(DateTimeUtils.getCalendarFromDate(calendarView.getCurrentDate()))*/, false);
            calendarView.setBalanceForToday(balance);
            calendarView.transactionUpdate(diaActual);
            if(calendarMenu.getItem(calendarAccount.getOrder()).getActionView() instanceof TextView){
                ((TextView) calendarMenu.getItem(calendarAccount.getOrder()).getActionView()).setText(String.valueOf(balance));
            }
        }
        if (calendarReInsertedPending) {
            calendarReInsertedPending = false;
            OnCalendarReInsertedPending();
        }
        if (calendarUpdatePending) {
            calendarUpdatePending = false;
            OnCalendarUpdatePending();
        }
        if (calendarDeletedPending) {
            calendarDeletedPending = false;
            OnCalendarDetedPending();
        }
        navigationView.setCheckedItem(calendarAccount.getOrder());
//        calendarView.invalidate();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseManager.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cambiarTitulo();
    }

    void cambiarTitulo() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getFirstDayOfCurrentMonth().getTime());
        toolbar.setTitle(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).toUpperCase() +
                " - " +
                calendar.get(Calendar.YEAR));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long i = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (diaActual == null) {
            diaActual = new Date();
            diaActual.setTime(Calendar.getInstance().getTimeInMillis());
            PersistentStorage.init(getApplicationContext());
            PersistentStorage.setRunMode(false);
            mDBManager = new DatabaseMan();
            mDatabaseManager = new DatabaseManager(getApplicationContext());
            mDatabaseManager.open();
            balancesForTheMonthList = mDatabaseManager.getBalancesForEveryDayOnMonth(new String[]{mDatabaseManager.getDefaultAccountId()},
                    DateTimeUtils.getFirstDateOfMonth(DateTimeUtils.getCalendarFromDate(diaActual)),
                    DateTimeUtils.getLastDateOfMonth(DateTimeUtils.getCalendarFromDate(diaActual)));
            balancesForTheMonth = DatabaseMan.getBalancesHashMapFromList(balancesForTheMonthList);
            calendarAccount = mDatabaseManager.getDefaultCalendar();
        }

        //Set the DatabaseManager and get the Dataset for the current date
//        transactionList = mDBManager.getList(diaActual);
        transactionList = mDatabaseManager.getTransactionsForDay(new String[]{mDatabaseManager.getDefaultAccountId()}, DateTimeUtils.getCalendarFromDate(diaActual));
        mAdapter = new TransactionAdaptor(this, transactionList);
        transactionListChanged();


        //Set the RecyclerView....... it continues bellow...
        mRecyclerView = ((RecyclerView) findViewById(R.id.reclerview));
        assert mRecyclerView != null;
        mRecyclerView.setHasFixedSize(true);

        FloatingActionButton fab2 = ((FloatingActionButton) findViewById(R.id.fab2));
        assert fab2 != null;
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewTransactionActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(NewTransactionActivity.CALENDAR, calendarAccount);
                b.putLong(NewTransactionActivity.TIME, diaActual.getTime());
                intent.putExtras(b);
                startActivity(intent);
            }
        });


        calendarView = ((CompactCalendarView) findViewById(R.id.view));
        calendarView.setDataSet(balances);
        calendarView.setBalances(balancesForTheMonth);
        String[] calendarId = new String[1];
        calendarId[0] = calendarAccount.getId();
        calendarView.setBalanceForToday(mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCalendarFromDate(calendarView.getCurrentDate()), false));

        assert calendarView != null;
        calendarView.setListener(calendarViewListener);

//        calendarView.setBackgroundColor(Color.WHITE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getFirstDayOfCurrentMonth().getTime());

        //calendarView.showCalendarWithAnimation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            calendarView.setElevation(10f);
        }


        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        assert toolbar != null;
        toolbar.setTitle(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).toUpperCase() +
                " - " +
                calendar.get(Calendar.YEAR));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeHellper());
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        IntentFilter mStatusIntentFilter = new IntentFilter(TRANSACTIONS_UPDATED);
        mStatusIntentFilter.addAction(TRANSACTIONS_RE_INSERTED);
        mStatusIntentFilter.addAction(CALENDAR_UPDATE);
        mStatusIntentFilter.addAction(CALENDAR_DELETED);
        mStatusIntentFilter.addAction(CALENDAR_RE_INSERTED);
        MainActivityBroadcastReceiver broadcastReceiver = new MainActivityBroadcastReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mStatusIntentFilter);

        assert navigationView != null;
        final Menu menu = navigationView.getMenu();
        List<Account> accounts = mDatabaseManager.getCalendarsWithBalance();
//        Account def = mDatabaseManager.getDefaultCalendar();
        calendarMenu = menu.addSubMenu(R.string.calendarios);
        calendarMenu.setGroupCheckable(Menu.NONE, true, true);
        for (Account ac : accounts) {
            TextView t = new TextView(this);
            t.setText(String.valueOf(ac.getBalance()));
            t.setGravity(Gravity.CENTER);
            if (ac.getId().equals(calendarAccount.getId())) {
                calendarMenu.add(R.id.calendarios, ac.getOrder(), Menu.NONE, ac.getName()).setCheckable(true).setActionView(t);
            } else
                calendarMenu.add(R.id.calendarios, ac.getOrder(), Menu.NONE, ac.getName()).setCheckable(true).setActionView(t);
        }
        menu.add(R.string.add_new_calendar).setIcon(R.drawable.ic_category_add_new);
        Log.e("TIEMPO TOMADO: ", String.valueOf(System.currentTimeMillis() - i));


//        calendarView.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (item.getTitle().equals(getString(R.string.add_new_calendar))) {
            startActivity(new Intent(getApplicationContext(), NewCalendarActivity.class));
        } else {
            for (Account cal : mDatabaseManager.getCalendars()) {
                if (cal.getName().equals(item.getTitle())) {
                    calendarAccount = cal;
                    calendarView.setCurrentDate(Calendar.getInstance().getTime());
                    break;
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public boolean transactionListChanged() {
        mAdapter.notifyDataSetChanged();
        if (transactionList.isEmpty()) {
            if ((findViewById(R.id.message)) != null) {
                (findViewById(R.id.message)).setVisibility(View.VISIBLE);
            }
            return false;
        } else {
            if ((findViewById(R.id.message)) != null) {
                (findViewById(R.id.message)).setVisibility(View.GONE);
            }
            return true;
        }
    }

    public void setCalendarReInsertedPending(boolean calendarReInsertedPending) {
        this.calendarReInsertedPending = calendarReInsertedPending;
    }

    public void setCalendarUpdatePending(boolean calendarUpdatePending) {
        this.calendarUpdatePending = calendarUpdatePending;
    }

    private void OnCalendarUpdatePending() {
        TextView t;
        t = new TextView(this);
        t.setText((String.valueOf(calendarAccount.getBalance())));
        t.setGravity(Gravity.CENTER);
        MainActivity.this.onNavigationItemSelected((calendarMenu.add(R.id.calendarios,
                    calendarAccount.getOrder(), Menu.NONE, calendarAccount.getName())
                .setCheckable(true)
                .setActionView(t)));
        navigationView.setCheckedItem(calendarAccount.getOrder());

    }

    private void OnCalendarDetedPending() {
        MainActivity.this.onNavigationItemSelected(calendarMenu.getItem(0));
    }

    private void OnCalendarReInsertedPending() {
        TextView t;
        calendarMenu.clear();
        List<Account> accounts = mDatabaseManager.getCalendarsWithBalance();
        t = new TextView(getApplicationContext());
        t.setGravity(Gravity.CENTER);
        for (Account ac : accounts) {
            t.setText(String.valueOf(ac.getBalance()));
            if (ac.getId().equals(calendarAccount.getId())) {
                navigationView.setCheckedItem(
                        calendarMenu.add(R.id.calendarios, Menu.NONE, Menu.NONE, ac.getName())
                                .setCheckable(true)
                                .setActionView(t)
                                .getItemId());
            } else {
                calendarMenu.add(R.id.calendarios, Menu.NONE, Menu.NONE, ac.getName()).setCheckable(true).setActionView(t);
            }
        }
    }

    public void editCalendar(MenuItem item) {
        //TODO: Create a Dialog to Select a Account to edit.
        Intent intent = new Intent(getApplicationContext(), NewCalendarActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(CALENDAR_TO_EDIT, calendarAccount);
        intent.putExtras(extras);
        startActivity(intent);
    }


    class MainActivityBroadcastReceiver extends BroadcastReceiver {

        MainActivityBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Account calendar;
            switch (intent.getAction()) {
                case TRANSACTIONS_UPDATED:
                    if (mDatabaseManager.isOpen()) {
                        String[] calendarId = new String[1];
                        calendarId[0] = calendarAccount.getId();
                        double balance = mDatabaseManager.getBalance(calendarId, DateTimeUtils.getCurrentDateWithoutTime()/*(DateTimeUtils.getCalendarFromDate(calendarView.getCurrentDate()))*/, false);
                        calendarView.setBalanceForToday(balance);
                        calendarView.transactionUpdate(diaActual);
                        if(calendarMenu.getItem(calendarAccount.getOrder()).getActionView() instanceof TextView){
                            ((TextView) calendarMenu.getItem(calendarAccount.getOrder()).getActionView()).setText(String.valueOf(balance));
                        }
                    } else {
                        calendarView.setUpdatePending(true);
                    }
                    break;
                case TRANSACTIONS_RE_INSERTED:
                    calendarView.transactionReInserted(intent.getDoubleExtra("value", 0));
                    break;


                case CALENDAR_RE_INSERTED:
                    if (mDatabaseManager.isOpen()) {
                        OnCalendarReInsertedPending();
                    } else {
                        setCalendarReInsertedPending(true);
                    }
                    calendarAccount = intent.getParcelableExtra(CALENDAR_UPDATED);
                    break;

                case CALENDAR_UPDATE:
                    calendarAccount = (Account) intent.getParcelableExtra(CALENDAR_UPDATED);
                    setCalendarUpdatePending(true);
                    break;

                case CALENDAR_DELETED:
                    calendar = (Account) intent.getParcelableExtra(CALENDAR_UPDATED);

                    for (int i = 0; i < calendarMenu.size(); i++) {
                        if (calendarMenu.getItem(i).getTitle().equals(calendar.getName())) {
                            calendarMenu.removeItem(calendarMenu.getItem(i).getItemId());
                            break;
                        }
                    }
//                    MainActivity.this.onNavigationItemSelected(calendarMenu.getItem(0));
//                    calendarAccount = calendarMenu.getItem(0);
                    calendarDeletedPending = true;
                    break;
            }
        }
    }
}
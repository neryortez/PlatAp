package com.github.sundeepk.compactcalendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;

import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.github.rathn.platap.DatabaseMan;
import io.github.rathn.platap.dto.Balance;

public class CompactCalendarView extends View {

    public static final int FILL_LARGE_INDICATOR = 1;
    public static final int NO_FILL_LARGE_INDICATOR = 2;
    public static final int SMALL_INDICATOR = 3;
    public static final String TRANSACTIONS_UPDATED = "TRANSACTIONSUPDATED";
    public static final String TRANSACTION_LIST = "transactions";
    public static final String TRANSACTIONS_RE_INSERTED = "reInserted";

    private final AnimationHandler animationHandler;
    private final Context mContext;
    private CompactCalendarController compactCalendarController;
    private GestureDetectorCompat gestureDetector;
    private boolean shouldScroll = true;
    public boolean updatePending;
//    private HashMap<DateTime, String> dataSet;


    public void setDataSet(HashMap<String, Double> hashMap) {
        compactCalendarController.setDataSet(hashMap);
        //invalidate();
    }

    public HashMap<String, Double> getDataSet(){
        return compactCalendarController.getDataSet();
    }

    public boolean isUpdatePending() {
        return updatePending;
    }

//    public class CompactCalendarViewBroadcastReceiver extends BroadcastReceiver{
//        // Prevents instantiation
//        private CompactCalendarViewBroadcastReceiver() {
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//                case TRANSACTIONS_UPDATED:
//                    CompactCalendarView.this.transactionUpdatePending();
//            }
//        }
//    }

    private void transactionUpdatePending() {
        updatePending = true;
    }

    public void transactionUpdate(Date dia) {
        compactCalendarController.transactionUpdate(dia);
        invalidate();
        this.updatePending = false;
    }

    public void setBalances(HashMap<String, Double> balances) {
        compactCalendarController.setDayBalances(balances);
    }

    public void setUpdatePending(boolean updatePending) {
        this.updatePending = updatePending;
    }

    public void transactionReInserted(double value) {
        compactCalendarController.updateBalance(value);
        invalidate();
    }

    public void setBalanceForToday(double balanceForToday) {
        compactCalendarController.setBalanceForToday(balanceForToday);
    }

    public void setBalanceForCurrentDate(double balance) {
        compactCalendarController.setBalanceForCurrentDate(balance);
    }

    public interface CompactCalendarViewListener {
        public void onDayClick(Date dateClicked);
        public void onMonthScroll(Date firstDayOfNewMonth);
    }

    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            compactCalendarController.onSingleTapUp(e);
            invalidate();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(shouldScroll) {
                if (Math.abs(distanceX) > 0) {
                    getParent().requestDisallowInterceptTouchEvent(true);

                    compactCalendarController.onScroll(e1, e2, distanceX, distanceY);
                    invalidate();
                    return true;
                }
            }

            return false;
        }
    };

    public CompactCalendarView(Context context) {
        this(context, null);
    }

    public CompactCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompactCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, null);
    }

    public CompactCalendarView(Context context, AttributeSet attrs, int defStyleAttr, HashMap<String, Double> data) {
        super(context, attrs, defStyleAttr);
        compactCalendarController = new CompactCalendarController(new Paint(), new OverScroller(getContext()),
                new Rect(), attrs, getContext(),  Color.argb(255, 233, 84, 81),
                Color.argb(255, 64, 64, 64), Color.argb(255, 219, 219, 219), VelocityTracker.obtain(),
                Color.argb(255, 100, 68, 65), new EventsContainer(Calendar.getInstance()),
                Locale.getDefault(), TimeZone.getDefault(), data);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        animationHandler = new AnimationHandler(compactCalendarController, this);

        // The filter's action is BROADCAST_ACTION
//        IntentFilter mStatusIntentFilter = new IntentFilter(TRANSACTIONS_UPDATED);
//        CompactCalendarViewBroadcastReceiver broadcastReceiver = new CompactCalendarViewBroadcastReceiver();
//
//        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, mStatusIntentFilter);
        mContext = context;
    }

    /**
     * Use a custom locale for compact calendar and reinitialise the view.
     * @param timeZone
     * @param locale
     */
    public void setLocale(TimeZone timeZone, Locale locale){
        compactCalendarController.setLocale(timeZone, locale);
        invalidate();
    }

    /**
    *Compact calendar will use the locale to determine the abbreviation to use as the day column names.
    *The default is to use the default locale and to abbreviate the day names to one character.
    *Setting this to true will displace the short weekday string provided by java.
     */
    public void setUseThreeLetterAbbreviation(boolean useThreeLetterAbbreviation){
        compactCalendarController.setUseWeekDayAbbreviation(useThreeLetterAbbreviation);
        invalidate();
    }

    /**
     * Sets the background color of the CompactCalendarView
     * @param background The color to set the background
     */
    public void setCalendarBackgroundColor(final int background) {
        compactCalendarController.setCalenderBackgroundColor(background);
        invalidate();
    }

    /**
     *
     Sets the name for each day of the week. No attempt is made to adjust width or text size based on the length of each day name.
     Works best with 3-4 characters for each day.
     * @param dayColumnNames The String array with the day names
     */
    public void setDayColumnNames(String[] dayColumnNames){
        compactCalendarController.setDayColumnNames(dayColumnNames);
    }

    /**
     * Used to set if Monday should be the first day of the week.
     * @param shouldShowMondayAsFirstDay If true MONDAY should be the first day of the week, is false SUNDAY should be used instead.
     */
    public void setShouldShowMondayAsFirstDay(boolean shouldShowMondayAsFirstDay) {
        compactCalendarController.setShouldShowMondayAsFirstDay(shouldShowMondayAsFirstDay);
        invalidate();
    }

    /**
     * Sets the background of the day the user selects
     * @param currentSelectedDayBackgroundColor Color of the background. These is an int containing also the alfa value
     */
    public void setCurrentSelectedDayBackgroundColor(int currentSelectedDayBackgroundColor) {
        compactCalendarController.setCurrentSelectedDayBackgroundColor(currentSelectedDayBackgroundColor);
        invalidate();
    }

    /**
     * Sets the background of the current day
     * @param currentDayBackgroundColor Color of the background. These is an int containing also the alfa value
     */
    public void setCurrentDayBackgroundColor(int currentDayBackgroundColor) {
        compactCalendarController.setCurrentDayBackgroundColor(currentDayBackgroundColor);
        invalidate();
    }

    /**
     * Gets the height of a day on the view
     * @return The height
     */
    public int getHeightPerDay(){
        return compactCalendarController.getHeightPerDay();
    }

    /**
     * Sets the listener to catch a day being selected or a month being scrolled
     * @param listener The CompactCalendarViewListener to attach
     */
    public void setListener(CompactCalendarViewListener listener){
        compactCalendarController.setListener(listener);
    }

    /**
     * Gets the Date of the current month on the view with the first day of that month
     * @return A Date with the current month on the view
     */
    public Date getFirstDayOfCurrentMonth(){
        return compactCalendarController.getFirstDayOfCurrentMonth();
    }

    public void shouldDrawIndicatorsBelowSelectedDays(boolean shouldDrawIndicatorsBelowSelectedDays){
        compactCalendarController.shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays);
    }

    /**
     * Jumps to the dateTimeMonth.
     * @param dateTimeMonth The Date to jump to.
     */
    public void setCurrentDate(Date dateTimeMonth){
        compactCalendarController.setCurrentDate(dateTimeMonth);
        invalidate();
    }

    /**
     * * Get the current Date shown on the view
     * @return The current Date shown on the view
     */
    public Date getCurrentDate(){
        return compactCalendarController.getCurrentDate();
    }
    /**
     * Gets the number of weeks on the current month on the view
     * @return The number of weeks on the month
     */
    public int getWeekNumberForCurrentMonth(){
        return compactCalendarController.getWeekNumberForCurrentMonth();
    }

    /**
     * Used to specify if the names of the weeks should be drawn on the calendar view.
     * @param shouldDrawDaysHeader A boolean to specify true or false.... What else were you waiting for?
     */
    public void setShouldDrawDaysHeader(boolean shouldDrawDaysHeader){
        compactCalendarController.setShouldDrawDaysHeader(shouldDrawDaysHeader);
    }

    /**
     * see {@link #addEvent(Event, boolean)} when adding single events
     * or {@link #addEvents(java.util.List)}  when adding multiple events
     * @param event
     */
    @Deprecated
    public void addEvent(Event event){
        addEvent(event, false);
    }

    /**
     *  Adds an event to be drawn as an indicator in the calendar.
     *  If adding multiple events see {@link #addEvents(List)}} method.
     * @param event to be added to the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    public void addEvent(Event event, boolean shouldInvalidate){
        compactCalendarController.addEvent(event);
        if(shouldInvalidate){
            invalidate();
        }
    }

    /**
     * Adds multiple events to the calendar and invalidates the view once all events are added.
     */
    public void addEvents(List<Event> events){
        compactCalendarController.addEvents(events);
        invalidate();
    }

    /**
     * Fetches the events for the date passed in
     * @param date
     * @return
     */
    public List<Event> getEvents(Date date){
        return compactCalendarController.getCalendarEventsFor(date.getTime());
    }

    /**
     * Fetches the events for the epochMillis passed in
     * @param epochMillis
     * @return
     */
    public List<Event> getEvents(long epochMillis){
        return compactCalendarController.getCalendarEventsFor(epochMillis);
    }

    /**
     * Fetches the events for the month of the epochMillis passed in and returns a sorted list of events
     * @param epochMillis
     * @return
     */
    public List<Event> getEventsForMonth(long epochMillis){
        return compactCalendarController.getCalendarEventsForMonth(epochMillis);
    }

    /**
     * Fetches the events for the month of the date passed in and returns a sorted list of events
     * @param date
     * @return
     */
    public List<Event> getEventsForMonth(Date date){
        return compactCalendarController.getCalendarEventsForMonth(date.getTime());
    }

    /**
     * Remove the event associated with the Date passed in
     * @param date
     */
    public void removeEvents(Date date){
        compactCalendarController.removeEventsFor(date.getTime());
    }

    public void removeEvents(long epochMillis){
        compactCalendarController.removeEventsFor(epochMillis);
    }

    /**
     * see {@link #removeEvent(Event, boolean)} when removing single events
     * or {@link #removeEvents(java.util.List)} (java.util.List)}  when removing multiple events
     * @param event
     */
    @Deprecated
    public void removeEvent(Event event){
        removeEvent(event, false);
    }

    /**
     * Removes an event from the calendar.
     * If removing multiple events see {@link #removeEvents(List)}
     *
     * @param event event to remove from the calendar
     * @param shouldInvalidate true if the view should invalidate
     */
    public void removeEvent(Event event, boolean shouldInvalidate){
        compactCalendarController.removeEvent(event);
        if(shouldInvalidate){
            invalidate();
        }
    }

    /**
     * Removes multiple events from the calendar and invalidates the view once all events are added.
     */
    public void removeEvents(List<Event> events){
        compactCalendarController.removeEvents(events);
        invalidate();
    }

    /**
     * Clears all Events from the calendar.
     */
    public void removeAllEvents() {
        compactCalendarController.removeAllEvents();
        invalidate();
    }

    public void setCurrentSelectedDayIndicatorStyle(final int currentSelectedDayIndicatorStyle){
        compactCalendarController.setCurrentSelectedDayIndicatorStyle(currentSelectedDayIndicatorStyle);
        invalidate();
    }

    public void setCurrentDayIndicatorStyle(final int currentDayIndicatorStyle){
        compactCalendarController.setCurrentDayIndicatorStyle(currentDayIndicatorStyle);
        invalidate();
    }

    public void setEventIndicatorStyle(final int eventIndicatorStyle){
        compactCalendarController.setEventIndicatorStyle(eventIndicatorStyle);
        invalidate();
    }

    private void checkTargetHeight() {
        if (compactCalendarController.getTargetHeight() <= 0) {
            throw new IllegalStateException("Target height must be set in xml properties in order to expand/collapse CompactCalendar.");
        }
    }

    public void displayOtherMonthDays(boolean displayOtherMonthDays) {
        compactCalendarController.setDisplayOtherMonthDays(displayOtherMonthDays);
        invalidate();
    }

    public void setTargetHeight(int targetHeight){
        compactCalendarController.setTargetHeight(targetHeight);
        checkTargetHeight();
    }

    public void showCalendar(){
        checkTargetHeight();
        animationHandler.openCalendar();
    }

    public void hideCalendar(){
        checkTargetHeight();
        animationHandler.closeCalendar();
    }

    public void showCalendarWithAnimation(){
        checkTargetHeight();
        animationHandler.openCalendarWithAnimation();
    }

    public void hideCalendarWithAnimation(){
        checkTargetHeight();
        animationHandler.closeCalendarWithAnimation();
    }

    public void showNextMonth(){
        compactCalendarController.showNextMonth();
        invalidate();
    }

    private void refreshView() {
        compactCalendarController.refreshView();
        invalidate();
    }

    public void showPreviousMonth(){
        compactCalendarController.showPreviousMonth();
        invalidate();
    }

    @Override
    protected void onMeasure(int parentWidth, int parentHeight) {
        super.onMeasure(parentWidth, parentHeight);
        int width = MeasureSpec.getSize(parentWidth);
        int height = MeasureSpec.getSize(parentHeight);
        if(width > 0 && height > 0) {
            compactCalendarController.onMeasure(width, height, getPaddingRight(), getPaddingLeft());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()){
            HashMap<String, Double> balances = DatabaseMan.getBalancesHashMapFromList(new ArrayList<Balance>(31));
            setBalances(balances);
        }
        compactCalendarController.onDraw(canvas);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(compactCalendarController.computeScroll()){
            invalidate();
        }
    }

    public void shouldScrollMonth(boolean shouldDisableScroll){
        this.shouldScroll = shouldDisableScroll;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (shouldScroll) {
            compactCalendarController.onTouch(event);
            invalidate();
        }

        // on touch action finished (CANCEL or UP), we re-allow the parent container to intercept touch events (scroll inside ViewPager + RecyclerView issue #82)
        if((event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) && shouldScroll) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        // always allow gestureDetector to detect onSingleTap and scroll events
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        // Prevents ViewPager from scrolling horizontally by announcing that (issue #82)
        return true;
    }

}

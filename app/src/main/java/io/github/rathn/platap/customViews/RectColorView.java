package io.github.rathn.platap.customViews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import io.github.rathn.platap.R;

public class RectColorView extends CheckedTextView {
    private static final float OVAL_RADIUS = 0.0f;
    private Paint mCheckmarkPaint;
    private Bitmap mImage;
    private float mImageCenter;
    private RectF mRectangle;
    private Paint mRectanglePaint;

    public RectColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public RectColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @TargetApi(11)
    private void initialize(Context context, AttributeSet attrs) {
        if (VERSION.SDK_INT > 11) {
            setLayerType(1, null);
        }
        this.mRectanglePaint = new Paint();
        this.mRectanglePaint.setAntiAlias(true);
        this.mRectanglePaint.setDither(true);
        this.mRectanglePaint.setStyle(Style.FILL);
        this.mRectanglePaint.setColor(context.getResources().getColor(R.color.colorPrimary));
        this.mCheckmarkPaint = new Paint();
        this.mCheckmarkPaint.setAntiAlias(true);
        this.mCheckmarkPaint.setDither(true);
        this.mImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_cancel);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, w, oldw, oldh);
        this.mRectangle = new RectF(0.0f, 0.0f, (float) w, (float) w);
        this.mImageCenter = ((float) (w - this.mImage.getWidth())) / 2.0f;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(this.mRectangle, 0.0f, 0.0f, this.mRectanglePaint);
        if (isChecked()) {
            canvas.drawBitmap(this.mImage, this.mImageCenter, this.mImageCenter, this.mCheckmarkPaint);
        }
    }

    public void setColor(int colorResourceId) {
        this.mRectanglePaint.setColor(getContext().getResources().getColor(colorResourceId));
        invalidate();
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
        invalidate();
    }
}

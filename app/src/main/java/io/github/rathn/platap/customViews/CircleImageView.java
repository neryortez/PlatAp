package io.github.rathn.platap.customViews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.CheckedTextView;
import io.github.rathn.platap.R;

public class CircleImageView extends CheckedTextView {
    private Paint mCirclePaint;
    private int mFillColor;
    private Bitmap mImage;
    private Paint mImagePaint;
    private Resources mResources;
    private Paint mStrokePaint;

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        setLayerType(1, null);
        this.mFillColor = context.getResources().getColor(R.color.colorPrimary);
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setDither(true);
        this.mCirclePaint.setStyle(Style.FILL);
        this.mCirclePaint.setColor(this.mFillColor);
        this.mStrokePaint = new Paint();
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setDither(true);
        this.mStrokePaint.setStyle(Style.STROKE);
        this.mStrokePaint.setStrokeWidth(2.0f);
        this.mStrokePaint.setColor(context.getResources().getColor(R.color.secondary_text));
        this.mImagePaint = new Paint();
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setDither(true);
        this.mResources = context.getResources();
        this.mImage = BitmapFactory.decodeResource(this.mResources, R.drawable.ic_action_cancel);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, w, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = ((float) getWidth()) / 2.0f;
        canvas.drawCircle(radius, radius, radius - 8.0f, this.mCirclePaint);
        if (isChecked()) {
            canvas.drawCircle(radius, radius, radius - 2.0f, this.mStrokePaint);
        }
        float imageCenter = ((float) (getWidth() - this.mImage.getWidth())) / 2.0f;
        canvas.drawBitmap(this.mImage, imageCenter, imageCenter, this.mImagePaint);
    }

    public void setColor(int colorResourceId) {
        this.mFillColor = this.mResources.getColor(colorResourceId);
        this.mCirclePaint.setColor(this.mFillColor);
        invalidate();
    }

    public int getColor() {
        return this.mCirclePaint.getColor();
    }

    public void setIcon(int resourceId) {
        this.mImage = BitmapFactory.decodeResource(this.mResources, resourceId);
        invalidate();
    }

    public void reset(int colorResourceId, int iconResourceId) {
        this.mFillColor = this.mResources.getColor(colorResourceId);
        this.mCirclePaint.setColor(this.mFillColor);
        this.mImage = BitmapFactory.decodeResource(this.mResources, iconResourceId);
        invalidate();
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
        invalidate();
    }
}

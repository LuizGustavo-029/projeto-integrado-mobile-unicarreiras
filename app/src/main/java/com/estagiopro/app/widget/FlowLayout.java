package com.estagiopro.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.estagiopro.app.R;

/** Layout que quebra linha automaticamente (usado para chips de competências). */
public class FlowLayout extends ViewGroup {

    private int hGap;
    private int vGap;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int def = (int) (6 * context.getResources().getDisplayMetrics().density);
        hGap = def;
        vGap = def;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
            hGap = a.getDimensionPixelSize(R.styleable.FlowLayout_hGap, def);
            vGap = a.getDimensionPixelSize(R.styleable.FlowLayout_vGap, def);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        if (widthMode == MeasureSpec.UNSPECIFIED) maxWidth = Integer.MAX_VALUE;

        int x = 0;
        int y = 0;
        int lineHeight = 0;
        int usedWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            if (x > 0 && x + cw > maxWidth) {
                x = 0;
                y += lineHeight + vGap;
                lineHeight = 0;
            }
            x += cw + hGap;
            usedWidth = Math.max(usedWidth, x - hGap);
            lineHeight = Math.max(lineHeight, ch);
        }
        int height = y + lineHeight + getPaddingTop() + getPaddingBottom();
        int width = usedWidth + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int maxWidth = r - l - getPaddingLeft() - getPaddingRight();
        int x = 0;
        int y = 0;
        int lineHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            if (x > 0 && x + cw > maxWidth) {
                x = 0;
                y += lineHeight + vGap;
                lineHeight = 0;
            }
            int left = getPaddingLeft() + x;
            int top = getPaddingTop() + y;
            child.layout(left, top, left + cw, top + ch);
            x += cw + hGap;
            lineHeight = Math.max(lineHeight, ch);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
}

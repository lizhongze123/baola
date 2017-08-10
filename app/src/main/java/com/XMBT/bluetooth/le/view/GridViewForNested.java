package com.XMBT.bluetooth.le.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 重写gridView以解决和scrollview滑动冲突
 */

public class GridViewForNested extends GridView{

    public GridViewForNested(Context context) {
        super(context);
    }

    public GridViewForNested(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewForNested(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}

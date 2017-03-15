package com.dlc.morepet.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dlc.morepet.R;

/**
 * Created by Mjf on 2017/1/30.
 */

public class TitleView extends RelativeLayout {

    public ImageView iv_left, iv_right;
    public TextView tv_center;

    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.view_title, this);
        iv_left = (ImageView) view.findViewById(R.id.iv_left);
        iv_right = (ImageView) view.findViewById(R.id.iv_right);
        tv_center = (TextView) view.findViewById(R.id.tv_center);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.view_title);
        Drawable left_src = array.getDrawable(R.styleable.view_title_left_src);
        Drawable right_src = array.getDrawable(R.styleable.view_title_right_src);
        String center_text = array.getString(R.styleable.view_title_center_text);
        array.recycle();

        if (left_src != null) {
            iv_left.setImageDrawable(left_src);
        } else {
            iv_left.setVisibility(GONE);
        }
        if (right_src != null) {
            iv_right.setImageDrawable(right_src);
        } else {
            iv_right.setVisibility(GONE);
        }
        if (!TextUtils.isEmpty(center_text)) {
            tv_center.setText(center_text);
        }
    }
}

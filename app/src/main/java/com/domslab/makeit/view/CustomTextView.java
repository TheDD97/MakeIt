package com.domslab.makeit.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domslab.makeit.R;

public class CustomTextView extends androidx.appcompat.widget.AppCompatTextView {
    public CustomTextView(Context context) {
        super(context);
        this.setTextSize(15);
        this.setTypeface(getResources().getFont(R.font.vollkorn_variablefont_wght));
        setPadding(20,0,20,0);
    }


}

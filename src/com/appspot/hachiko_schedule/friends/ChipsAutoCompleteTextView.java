package com.appspot.hachiko_schedule.friends;


import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import com.appspot.hachiko_schedule.R;

import java.util.HashSet;
import java.util.Set;

// https://github.com/kpbird/chips-edittext-libraryを参考に実装
public class ChipsAutoCompleteTextView
        extends MultiAutoCompleteTextView implements OnItemClickListener {
    private Set<OnItemClickListener> onItemClickListeners = new HashSet<OnItemClickListener>();

    public ChipsAutoCompleteTextView(Context context) {
        super(context);
        init(context);
    }

    public ChipsAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChipsAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context){
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        setOnItemClickListener(this);
    }

    public void addOnItemClickListener(OnItemClickListener listener) {
        onItemClickListeners.add(listener);
    }

    public void setupChips(){
        String text = getText().toString();
        if (!text.contains(",")) {
            return;
        }
        String chips[] = text.trim().split(",");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        int x =0;
        for(String chipText : chips){
            spannableStringBuilder.setSpan(
                    new BackgroundColorSpan(
                            getContext().getResources().getColor(R.color.friend_name_gray)),
                    x, x + chipText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            x = x + chipText.length() + 1;
        }
        setText(spannableStringBuilder);
        setSelection(getText().length());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setupChips();
        for (OnItemClickListener listener: onItemClickListeners) {
            listener.onItemClick(parent, view, position, id);
        }
    }
}

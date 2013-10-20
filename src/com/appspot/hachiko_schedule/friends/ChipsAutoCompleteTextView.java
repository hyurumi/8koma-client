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
import com.appspot.hachiko_schedule.util.HachikoLogger;
import com.google.common.base.Joiner;

import java.util.*;

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

    public void setupChips() {
        setupChips(null);
    }

    private void setupChips(String nameToRemove){
        String text = getText().toString();
        if (!text.contains(",")) {
            return;
        }
        HachikoLogger.debug(":" + nameToRemove);
        List<String> chips = new ArrayList<String>();
        for (String item: text.trim().split(",")) {
            item = item.trim();
            if (nameToRemove != null && nameToRemove.equals(item)) {
                continue;
            }
            chips.add(item);
        }

        SpannableStringBuilder spannableStringBuilder
                = new SpannableStringBuilder(Joiner.on(", ").join(chips));
        if (chips.size() == 0) {
            setText("");
            return;
        }
        spannableStringBuilder.append(", ");
        int x =0;
        for(String chipText : chips){
            spannableStringBuilder.setSpan(
                    new BackgroundColorSpan(
                            getContext().getResources().getColor(R.color.friend_name_gray)),
                    x, x + chipText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            x = x + chipText.length() + 2;
        }
        setText(spannableStringBuilder);
        setSelection(getText().length());
    }

    public void removeName(String name) {
        setupChips(name.trim());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setupChips();
        for (OnItemClickListener listener: onItemClickListeners) {
            listener.onItemClick(parent, view, position, id);
        }
    }
}

package com.appspot.hachiko_schedule.friends;


import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.ui.TexttipSpan;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// https://github.com/kpbird/chips-edittext-libraryを参考に実装
public class ChipsAutoCompleteTextView
        extends MultiAutoCompleteTextView implements OnItemClickListener {
    public interface OnNameDeletedListener {
        public void onNameDeleted(String name);
    }

    private Set<OnItemClickListener> onItemClickListeners = new HashSet<OnItemClickListener>();
    private OnNameDeletedListener onNameDeletedListener;

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
        addTextChangedListener(new NameDeletedWatcher());
    }

    public void setOnNameDeletedListener(OnNameDeletedListener listener) {
        onNameDeletedListener = listener;
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
                    new TexttipSpan(getContext(), R.layout.friend_name_tip,
                            R.id.friend_name_tip_name, R.id.friend_name_tip_image,
                            R.drawable.ic_contact_picture, chipText),
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

    private class NameDeletedWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (after == 0) {
                String str = s.subSequence(start, start + count).toString().trim();
                if (str.length() > 0 && !",".equals(str) && onNameDeletedListener != null) {
                    onNameDeletedListener.onNameDeleted(str);
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

package com.appspot.hachiko_schedule.friends;


import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ListView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendOrGroup;
import com.appspot.hachiko_schedule.ui.TexttipSpan;

import java.util.ArrayList;
import java.util.List;

// https://github.com/kpbird/chips-edittext-libraryを参考に実装
public class ChipsFriendNameEditText extends EditText {
    public interface OnNameDeletedListener {
        public void onNameDeleted(FriendOrGroup item);
    }

    private OnNameDeletedListener onNameDeletedListener;
    private List<FriendOrGroup> selectedItems = new ArrayList<FriendOrGroup>();
    private String extraText = "";
    private ListView listView;
    private FriendsFragment.FriendListAdapter adapter;

    public ChipsFriendNameEditText(Context context) {
        super(context);
        init(context);
    }

    public ChipsFriendNameEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChipsFriendNameEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context){
        addTextChangedListener(new NameDeletedWatcher());
    }

    public void setOnNameDeletedListener(OnNameDeletedListener listener) {
        onNameDeletedListener = listener;
    }

    public void registerListViewAndAdapter(
            ListView listView, FriendsFragment.FriendListAdapter adapter) {
        this.listView = listView;
        this.adapter = adapter;
    }

    private void refreshChips(){
        if (selectedItems.size() == 0) {
            setText(extraText);
            return;
        }
        String delimiter = ", ";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int i = 0; i < selectedItems.size(); i++) {
            spannableStringBuilder.append(selectedItems.get(i).getDisplayName());
            if (i != selectedItems.size() - 1) {
                spannableStringBuilder.append(delimiter);
            }
        }
        spannableStringBuilder.append(delimiter);
        int x =0;
        for(FriendOrGroup item : selectedItems){
            String chipText = item.getDisplayName();
            spannableStringBuilder.setSpan(
                    new TexttipSpan(getContext(), R.layout.friend_name_tip,
                            R.id.friend_name_tip_name, R.id.friend_name_tip_image,
                            item.getPhotoUri(), chipText),
                    x, x + chipText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            x = x + chipText.length() + delimiter.length();
        }
        spannableStringBuilder.append(extraText);
        setText(spannableStringBuilder);
        setSelection(getText().length());
    }

    public void removeItem(FriendOrGroup item) {
        selectedItems.remove(item);
        refreshChips();
    }

    public void toggleSelection(FriendOrGroup item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
            extraText = "";
        }
        refreshChips();
    }

    public List<FriendOrGroup> getSelectedItems() {
        return selectedItems;
    }

    private class NameDeletedWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (after == 0) {
                String str = s.subSequence(start, start + count).toString().trim();
                if (str.length() > 0 && !",".equals(str) && onNameDeletedListener != null) {
                    FriendOrGroup item = getSelectedItemByName(str);
                    selectedItems.remove(item);
                    onNameDeletedListener.onNameDeleted(item);
                }
            }
        }

        private FriendOrGroup getSelectedItemByName(String name) {
            for (FriendOrGroup item: selectedItems) {
                if (name.equals(item.getDisplayName())) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().split(",").length > 1) {
                String[] sliced = s.toString().split(",");
                extraText = sliced[sliced.length - 1];
            } else {
                extraText = s.toString();
            }
            adapter.getFilter().filter(extraText.trim());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

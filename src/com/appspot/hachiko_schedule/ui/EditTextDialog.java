package com.appspot.hachiko_schedule.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * {@link EditText} を含むダイアログ
 */
public class EditTextDialog {
    public interface PositiveButtonListener {
        /**
         * ダイアログをdismissさせるかどうか
         */
        public boolean onPositiveButtonClicked(DialogInterface dialog, String text);
    }

    public static void showDialog(
            Context context, String title, String hint, String positiveButtonLabel,
            final PositiveButtonListener listener) {
        final EditText editText = new EditText(context);
        editText.setHint(hint);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(editText)
                .setPositiveButton(positiveButtonLabel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (listener.onPositiveButtonClicked(
                                dialog, editText.getText().toString())) {
                            dialog.dismiss();
                        }
                    }
                })
                .create();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.toString().length() > 0);
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }
}

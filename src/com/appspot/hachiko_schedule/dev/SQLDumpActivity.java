package com.appspot.hachiko_schedule.dev;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.db.HachikoDBOpenHelper;
import com.appspot.hachiko_schedule.util.HachikoLogger;

/**
 * デバッグ用にSQLを表示する
 */
public class SQLDumpActivity extends Activity {
    private static final String[] TABLE_NAMES
            = new String[] {"users", "non_friend_names", "plans", "candiate_dates"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_show_sql_activity);
        Spinner table = (Spinner) findViewById(R.id.debug_db_names);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, TABLE_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        table.setAdapter(adapter);
        table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String table = spinner.getSelectedItem().toString();
                HachikoDBOpenHelper dbHelper = new HachikoDBOpenHelper(SQLDumpActivity.this, null);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor c = db.query(table, null, null, null, null, null, null);
                String text = dbContent(c);
                ((TextView) findViewById(R.id.debug_db_content)).setText(text);
                HachikoLogger.debug(text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private String dbContent(Cursor c) {
        if (!c.moveToFirst()) {
            return "empty";
        }
        int numOfColumns = c.getColumnCount();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numOfColumns; i++) {
            builder.append(c.getColumnName(i)).append("| ");
        }
        builder.append("\n");
        builder.append("-----\n");
        do {
            for (int i = 0; i < numOfColumns; i++) {
                builder.append(c.getString(i)).append("| ");
            }
            builder.append("\n");
        } while (c.moveToNext());
        return builder.toString();
    }
}

package com.appspot.hachiko_schedule.db;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * 'create table' SQLクエリまわりのヘルパ.
 */
public class SQLiteCreateTableBuilder {
    private StringBuilder stringBuilder = new StringBuilder();
    private List<String> entries = new ArrayList<String>();

    public SQLiteCreateTableBuilder(String tableName) {
        stringBuilder.append("CREATE TABLE " + tableName + " (");
    }

    /**
     * Add column with name and type.
     */
    public SQLiteCreateTableBuilder addColumn(String name, String type) {
        entries.add(name + " " + type);
        return this;
    }

    /**
     * Add column with name, type and constraint
     */
    public SQLiteCreateTableBuilder addColumn(String name, String type, String... constraints) {
        entries.add(name + " " + type + " " + Joiner.on(" ").join(constraints));
        return this;
    }

    /**
     * @return built SQL
     */
    @Override
    public String toString() {
        return stringBuilder.append(Joiner.on(",").join(entries)).append(");").toString();
    }
}

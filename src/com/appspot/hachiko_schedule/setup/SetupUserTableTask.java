package com.appspot.hachiko_schedule.setup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appspot.hachiko_schedule.HachikoApp;
import com.appspot.hachiko_schedule.apis.FriendAPI;
import com.appspot.hachiko_schedule.apis.HachiJsonArrayRequest;
import com.appspot.hachiko_schedule.data.FriendItem;
import com.appspot.hachiko_schedule.db.UserTableHelper;
import com.appspot.hachiko_schedule.friends.ContactManager;
import com.appspot.hachiko_schedule.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SetupUserTableTask extends AsyncTask<Void, Void, String> {

    private final Context context;
    public SetupUserTableTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        List<FriendItem> friends = ContactManager.getInstance(context).getListOfContactEntries();
        setupLocalTable(friends);
        JSONArray requestParams = constructApiParams(friends);
        requestFriendsHachikoIds(requestParams);
        return null;
    }

    private void setupLocalTable(List<FriendItem> friends) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        SQLiteDatabase db = userTableHelper.getWritableUserDB();
        for (FriendItem friend: friends) {
            Uri photoUri = friend.getPhotoUri();
            userTableHelper.insertUserToDb(db, friend.getDisplayName(),
                    friend.getLocalContactId(), photoUri == null ? null : photoUri.toString(),
                    friend.getEmailAddress());
        }
        db.close();
    }

    private JSONArray constructApiParams(List<FriendItem> friends) {
        JSONArray apiParams = new JSONArray();
        for (FriendItem friend: friends) {
            JSONObject object = new JSONObject();
            try {
                object.put("gmail", friend.getEmailAddress());
                object.put("contactId", friend.getLocalContactId());
                apiParams.put(object);
            } catch (JSONException e) {

            }
        }
        return apiParams;
    }

    private void requestFriendsHachikoIds(JSONArray requestParams) {
        Request request = new HachiJsonArrayRequest(
                context, FriendAPI.ADD_FRIENDS.getMethod(), FriendAPI.ADD_FRIENDS.getUrl(),
                requestParams,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        updateHachikoIdWithResponse(jsonArray);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        HachikoLogger.error("response error on friends request", volleyError);
                    }
                }
        );
        HachikoApp.defaultRequestQueue().add(request);
    }

    private void updateHachikoIdWithResponse(JSONArray responseJson) {
        UserTableHelper userTableHelper = new UserTableHelper(context);
        SQLiteDatabase db = userTableHelper.getWritableUserDB();
        for (int i = 0; i < responseJson.length(); i++) {
            try {
                JSONObject object = responseJson.getJSONObject(i);
                long contactId = object.getLong("contactId");
                long hachikoId = object.getLong("hachikoId");
                boolean isHachikoUser = object.getBoolean("registered");
                userTableHelper.updateHachikoId(db, hachikoId, contactId, isHachikoUser);
            } catch (JSONException e) {
                HachikoLogger.error("Unexpected json exception at index" + i
                        + " raw response is " + responseJson);
                break;
            }
        }
        HachikoLogger.debug("response to user request:" + responseJson);
        db.close();
    }
}
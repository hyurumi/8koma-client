package tk.hachikoma.setup;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import tk.hachikoma.HachikoApp;
import tk.hachikoma.apis.HachikoAPI;
import tk.hachikoma.apis.base_requests.HachiJsonArrayRequest;
import tk.hachikoma.data.FriendItem;
import tk.hachikoma.db.UserTableHelper;
import tk.hachikoma.friends.ContactManager;
import tk.hachikoma.prefs.HachikoPreferences;
import tk.hachikoma.util.HachikoLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetupUserTableActivity extends SetupBaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (HachikoPreferences.getDefault(this)
                .getBoolean(HachikoPreferences.KEY_IS_LOCAL_USER_TABLE_SETUP, false)) {
            transitToNextActivity();
        } else {
            uploadFriends();
        }
    }

    protected String uploadFriends() {
        List<FriendItem> friends = ContactManager.getInstance(this).getListOfContactEntries();
        setupLocalTable(friends);
        JSONArray requestParams = constructApiParams(friends);
        requestFriendsHachikoIds(requestParams);
        return null;
    }

    private void setupLocalTable(List<FriendItem> friends) {
        UserTableHelper userTableHelper = new UserTableHelper(this);
        SQLiteDatabase db = userTableHelper.getWritableUserDB();
        for (FriendItem friend: friends) {
            Uri photoUri = friend.getPhotoUri();
            userTableHelper.insertUserToDb(db, friend.getDisplayName(), friend.getPhoneticName(),
                    friend.getLocalContactId(), photoUri == null ? null : photoUri.toString(),
                    friend.getEmailAddress());
        }
        db.close();
    }

    private JSONArray constructApiParams(List<FriendItem> friends) {
        JSONArray apiParams = new JSONArray();
        Set<String> knownEmailAddress = new HashSet<String>();
        for (FriendItem friend: friends) {
            JSONObject object = new JSONObject();
            try {
                if (knownEmailAddress.contains(friend.getEmailAddress())) {
                    continue;
                }
                knownEmailAddress.add(friend.getEmailAddress());
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
                this, HachikoAPI.Friend.ADD_FRIENDS.getMethod(), HachikoAPI.Friend.ADD_FRIENDS.getUrl(),
                requestParams,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        updateHachikoIdWithResponse(jsonArray);
                        HachikoPreferences.getDefaultEditor(SetupUserTableActivity.this).putBoolean(
                                HachikoPreferences.KEY_IS_LOCAL_USER_TABLE_SETUP, true).commit();
                        transitToNextActivity();
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
        UserTableHelper userTableHelper = new UserTableHelper(this);
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

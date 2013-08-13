package com.facebook.widget;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.appspot.hachiko_schedule.R;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SDKの{@link FriendPickerFragment}を参考に，単純化したもの
 */
public class HachikoFbFriendPickerFragment extends SimplifiedPickerFragment<GraphUser> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String MY_FRIENDS = "me/friends";

    public HachikoFbFriendPickerFragment() {
        super(GraphUser.class, R.layout.fragment_fb_friend_list, null);
        setSelectionStrategy(createSelectionStrategy());
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        setSelectionStrategy(createSelectionStrategy());
    }

    @Override
    PickerFragmentAdapter<GraphUser> createAdapter() {
        PickerFragmentAdapter<GraphUser> adapter
                = new PickerFragmentAdapter<GraphUser>(getActivity()) {

                    @Override
                    protected int getGraphObjectRowLayoutId(GraphUser graphObject) {
                        return com.facebook.android.R.layout.com_facebook_picker_list_row;
                    }

                    @Override
                    protected int getDefaultPicture() {
                        return com.facebook.android.R.drawable.com_facebook_profile_default_icon;
                    }
                };
        adapter.setShowCheckbox(true);
        adapter.setShowPicture(true);
        adapter.setSortFields(Arrays.asList(new String[]{NAME}));
        adapter.setGroupByField(NAME);
        return adapter;
    }

    @Override
    SimplifiedPickerFragment.LoadingStrategy createLoadingStrategy() {
        return new ImmediateLoadingStrategy();
    }

    @Override
    SimplifiedPickerFragment.SelectionStrategy createSelectionStrategy() {
        return new SimplifiedPickerFragment.MultiSelectionStrategy();
    }

    @Override
    Request getRequestForLoadData(Session session) {
        if (adapter == null) {
            throw new FacebookException("Can't issue requests until Fragment has been created.");
        }

        return createRequest(session);
    }

    private Request createRequest(Session session) {
        Request request = Request.newGraphPathRequest(session, MY_FRIENDS, null);

        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[]{
                ID,
                NAME
        };
        fields.addAll(Arrays.asList(requiredFields));

        String pictureField = adapter.getPictureFieldSpecifier();
        if (pictureField != null) {
            fields.add(pictureField);
        }

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }

    private class ImmediateLoadingStrategy extends LoadingStrategy {
        @Override
        protected void onLoadFinished(GraphObjectPagingLoader<GraphUser> loader,
                                      SimpleGraphObjectCursor<GraphUser> data) {
            super.onLoadFinished(loader, data);

            // We could be called in this state if we are clearing data or if we are being re-attached
            // in the middle of a query.
            if (data == null || loader.isLoading()) {
                return;
            }

            if (data.areMoreObjectsAvailable()) {
                // We got results, but more are available.
                followNextLink();
            } else {
                // We finished loading results.
                hideActivityCircle();

                // If this was from the cache, schedule a delayed refresh query (unless we got no results
                // at all, in which case refresh immediately.
                if (data.isFromCache()) {
                    loader.refreshOriginalRequest(data.getCount() == 0 ? CACHED_RESULT_REFRESH_DELAY : 0);
                }
            }
        }

        private void followNextLink() {
            // This may look redundant, but this causes the circle to be alpha-dimmed if we have results.
            displayActivityCircle();

            loader.followNextLink();
        }
    }
}

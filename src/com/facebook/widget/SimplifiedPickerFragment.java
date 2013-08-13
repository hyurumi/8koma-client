package com.facebook.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.facebook.*;
import com.facebook.android.R;
import com.facebook.model.GraphObject;
import com.facebook.internal.SessionTracker;

import java.util.*;

/**
 * Facebook SDK内の{@link PickerFragment}から，タイトルバーなどをそぎ落としたもの
 */
public abstract class SimplifiedPickerFragment<T extends GraphObject> extends Fragment {
    private static final String SELECTION_BUNDLE_KEY = "com.facebook.android.PickerFragment.Selection";
    private static final String ACTIVITY_CIRCLE_SHOW_KEY = "com.facebook.android.PickerFragment.ActivityCircleShown";
    private static final int PROFILE_PICTURE_PREFETCH_BUFFER = 5;

    private final int layout;
    private OnErrorListener onErrorListener;
    private OnDataChangedListener onDataChangedListener;
    private OnSelectionChangedListener onSelectionChangedListener;
    private OnDoneButtonClickedListener onDoneButtonClickedListener;
    private GraphObjectFilter<T> filter;
    private ListView listView;
    GraphObjectAdapter<T> adapter;
    private final Class<T> graphObjectClass;
    private LoadingStrategy loadingStrategy;
    private SelectionStrategy selectionStrategy;
    private ProgressBar activityCircle;
    private SessionTracker sessionTracker;

    SimplifiedPickerFragment(Class<T> graphObjectClass, int layout, Bundle args) {
        this.graphObjectClass = graphObjectClass;
        this.layout = layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = createAdapter();
        adapter.setFilter(new GraphObjectAdapter.Filter<T>() {
            @Override
            public boolean includeItem(T graphObject) {
                return filterIncludesItem(graphObject);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(layout, container, false);

        listView = (ListView) view.findViewById(R.id.com_facebook_picker_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onListItemClick((ListView) parent, v, position);
            }
        });
        listView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // We don't actually do anything differently on long-clicks, but setting the listener
                // enables the selector transition that we have for visual consistency with the
                // Facebook app's pickers.
                return false;
            }
        });
        listView.setOnScrollListener(onScrollListener);
        listView.setAdapter(adapter);

        activityCircle = (ProgressBar) view.findViewById(R.id.com_facebook_picker_activity_circle);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sessionTracker = new SessionTracker(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (!session.isOpened()) {
                    // When a session is closed, we want to clear out our data so it is not visible to subsequent users
                    clearResults();
                }
            }
        });

        loadingStrategy = createLoadingStrategy();
        loadingStrategy.attach(adapter);

        selectionStrategy = createSelectionStrategy();
        selectionStrategy.readSelectionFromBundle(savedInstanceState, SELECTION_BUNDLE_KEY);

        if (activityCircle != null && savedInstanceState != null) {
            boolean shown = savedInstanceState.getBoolean(ACTIVITY_CIRCLE_SHOW_KEY, false);
            if (shown) {
                displayActivityCircle();
            } else {
                // Should be hidden already, but just to be sure.
                hideActivityCircle();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listView.setOnScrollListener(null);
        listView.setAdapter(null);

        loadingStrategy.detach();
        sessionTracker.stopTracking();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        selectionStrategy.saveSelectionToBundle(outState, SELECTION_BUNDLE_KEY);
        if (activityCircle != null) {
            outState.putBoolean(ACTIVITY_CIRCLE_SHOW_KEY, activityCircle.getVisibility() == View.VISIBLE);
        }
    }

    /**
     * Gets the current OnDataChangedListener for this fragment, which will be called whenever
     * the underlying data being displaying in the picker has changed.
     *
     * @return the OnDataChangedListener, or null if there is none
     */
    public OnDataChangedListener getOnDataChangedListener() {
        return onDataChangedListener;
    }

    /**
     * Sets the current OnDataChangedListener for this fragment, which will be called whenever
     * the underlying data being displaying in the picker has changed.
     *
     * @param onDataChangedListener the OnDataChangedListener, or null if there is none
     */
    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        this.onDataChangedListener = onDataChangedListener;
    }

    /**
     * Gets the current OnSelectionChangedListener for this fragment, which will be called
     * whenever the user selects or unselects a graph object in the list.
     *
     * @return the OnSelectionChangedListener, or null if there is none
     */
    public OnSelectionChangedListener getOnSelectionChangedListener() {
        return onSelectionChangedListener;
    }

    /**
     * Sets the current OnSelectionChangedListener for this fragment, which will be called
     * whenever the user selects or unselects a graph object in the list.
     *
     * @param onSelectionChangedListener the OnSelectionChangedListener, or null if there is none
     */
    public void setOnSelectionChangedListener(
            OnSelectionChangedListener onSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener;
    }

    /**
     * Gets the current OnDoneButtonClickedListener for this fragment, which will be called
     * when the user clicks the Done button.
     *
     * @return the OnDoneButtonClickedListener, or null if there is none
     */
    public OnDoneButtonClickedListener getOnDoneButtonClickedListener() {
        return onDoneButtonClickedListener;
    }

    /**
     * Sets the current OnDoneButtonClickedListener for this fragment, which will be called
     * when the user clicks the Done button. This will only be possible if the title bar is
     * being shown in this fragment.
     *
     * @param onDoneButtonClickedListener the OnDoneButtonClickedListener, or null if there is none
     */
    public void setOnDoneButtonClickedListener(OnDoneButtonClickedListener onDoneButtonClickedListener) {
        this.onDoneButtonClickedListener = onDoneButtonClickedListener;
    }

    /**
     * Gets the current OnErrorListener for this fragment, which will be called in the event
     * of network or other errors encountered while populating the graph objects in the list.
     *
     * @return the OnErrorListener, or null if there is none
     */
    public OnErrorListener getOnErrorListener() {
        return onErrorListener;
    }

    /**
     * Sets the current OnErrorListener for this fragment, which will be called in the event
     * of network or other errors encountered while populating the graph objects in the list.
     *
     * @param onErrorListener the OnErrorListener, or null if there is none
     */
    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    /**
     * Gets the current filter for this fragment, which will be called for each graph object
     * returned from the service to determine if it should be displayed in the list.
     * If no filter is specified, all retrieved graph objects will be displayed.
     *
     * @return the GraphObjectFilter, or null if there is none
     */
    public GraphObjectFilter<T> getFilter() {
        return filter;
    }

    /**
     * Sets the current filter for this fragment, which will be called for each graph object
     * returned from the service to determine if it should be displayed in the list.
     * If no filter is specified, all retrieved graph objects will be displayed.
     *
     * @param filter the GraphObjectFilter, or null if there is none
     */
    public void setFilter(GraphObjectFilter<T> filter) {
        this.filter = filter;
    }

    /**
     * Gets the Session to use for any Facebook requests this fragment will make.
     *
     * @return the Session that will be used for any Facebook requests, or null if there is none
     */
    public Session getSession() {
        return sessionTracker.getSession();
    }

    /**
     * Sets the Session to use for any Facebook requests this fragment will make. If the
     * parameter is null, the fragment will use the current active session, if any.
     *
     * @param session the Session to use for Facebook requests, or null to use the active session
     */
    public void setSession(Session session) {
        sessionTracker.setSession(session);
    }

    /**
     * Causes the picker to load data from the service and display it to the user.
     *
     * @param forceReload if true, data will be loaded even if there is already data being displayed (or loading);
     *                    if false, data will not be re-loaded if it is already displayed (or loading)
     */
    public void loadData(boolean forceReload) {
        if (!forceReload && loadingStrategy.isDataPresentOrLoading()) {
            return;
        }
        loadDataSkippingRoundTripIfCached();
    }

    boolean filterIncludesItem(T graphObject) {
        if (filter != null) {
            return filter.includeItem(graphObject);
        }
        return true;
    }

    List<T> getSelectedGraphObjects() {
        return adapter.getGraphObjectsById(selectionStrategy.getSelectedIds());
    }

    abstract Request getRequestForLoadData(Session session);

    abstract PickerFragmentAdapter<T> createAdapter();

    abstract LoadingStrategy createLoadingStrategy();

    abstract SelectionStrategy createSelectionStrategy();

    void onLoadingData() {
    }

    void displayActivityCircle() {
        if (activityCircle != null) {
            layoutActivityCircle();
            activityCircle.setVisibility(View.VISIBLE);
        }
    }

    void layoutActivityCircle() {
        // If we've got no data, make the activity circle full-opacity. Otherwise we'll dim it to avoid
        //  cluttering the UI.
        float alpha = (!adapter.isEmpty()) ? .25f : 1.0f;
        setAlpha(activityCircle, alpha);
    }

    void hideActivityCircle() {
        if (activityCircle != null) {
            // We use an animation to dim the activity circle; need to clear this or it will remain visible.
            activityCircle.clearAnimation();
            activityCircle.setVisibility(View.INVISIBLE);
        }
    }

    void setSelectionStrategy(SelectionStrategy selectionStrategy) {
        if (selectionStrategy != this.selectionStrategy) {
            this.selectionStrategy = selectionStrategy;
            if (adapter != null) {
                // Adapter should cause a re-render.
                adapter.notifyDataSetChanged();
            }
        }
    }

    private static void setAlpha(View view, float alpha) {
        // Set the alpha appropriately (setAlpha is API >= 11, this technique works on all API levels).
        AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    private void onListItemClick(ListView listView, View v, int position) {
        @SuppressWarnings("unchecked")
        T graphObject = (T) listView.getItemAtPosition(position);
        String id = adapter.getIdOfGraphObject(graphObject);
        selectionStrategy.toggleSelection(id);
        adapter.notifyDataSetChanged();

        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(SimplifiedPickerFragment.this);
        }
    }

    private void loadDataSkippingRoundTripIfCached() {
        clearResults();

        Request request = getRequestForLoadData(getSession());
        if (request != null) {
            onLoadingData();
            loadingStrategy.startLoading(request);
        }
    }

    private void clearResults() {
        if (adapter != null) {
            boolean wasSelection = !selectionStrategy.isEmpty();
            boolean wasData = !adapter.isEmpty();

            loadingStrategy.clearResults();
            selectionStrategy.clear();
            adapter.notifyDataSetChanged();

            // Tell anyone who cares the data and selection has changed, if they have.
            if (wasData && onDataChangedListener != null) {
                onDataChangedListener.onDataChanged(SimplifiedPickerFragment.this);
            }
            if (wasSelection && onSelectionChangedListener != null) {
                onSelectionChangedListener.onSelectionChanged(SimplifiedPickerFragment.this);
            }
        }
    }

    void updateAdapter(SimpleGraphObjectCursor<T> data) {
        if (adapter != null) {
            // As we fetch additional results and add them to the table, we do not
            // want the items displayed jumping around seemingly at random, frustrating the user's
            // attempts at scrolling, etc. Since results may be added anywhere in
            // the table, we choose to try to keep the first visible row in a fixed
            // position (from the user's perspective). We try to keep it positioned at
            // the same offset from the top of the screen so adding new items seems
            // smoother, as opposed to having it "snap" to a multiple of row height

            // We use the second row, to give context above and below it and avoid
            // cases where the first row is only barely visible, thus providing little context.
            // The exception is where the very first row is visible, in which case we use that.
            View view = listView.getChildAt(1);
            int anchorPosition = listView.getFirstVisiblePosition();
            if (anchorPosition > 0) {
                anchorPosition++;
            }
            GraphObjectAdapter.SectionAndItem<T> anchorItem = adapter.getSectionAndItem(anchorPosition);
            final int top = (view != null &&
                    anchorItem.getType() != GraphObjectAdapter.SectionAndItem.Type.ACTIVITY_CIRCLE) ?
                    view.getTop() : 0;

            // Now actually add the results.
            boolean dataChanged = adapter.changeCursor(data);

            if (view != null && anchorItem != null) {
                // Put the item back in the same spot it was.
                final int newPositionOfItem = adapter.getPosition(anchorItem.sectionKey, anchorItem.graphObject);
                if (newPositionOfItem != -1) {
                    listView.setSelectionFromTop(newPositionOfItem, top);
                }
            }

            if (dataChanged && onDataChangedListener != null) {
                onDataChangedListener.onDataChanged(SimplifiedPickerFragment.this);
            }
        }
    }

    private void reprioritizeDownloads() {
        int lastVisibleItem = listView.getLastVisiblePosition();
        if (lastVisibleItem >= 0) {
            int firstVisibleItem = listView.getFirstVisiblePosition();
            adapter.prioritizeViewRange(firstVisibleItem, lastVisibleItem, PROFILE_PICTURE_PREFETCH_BUFFER);
        }
    }

    private ListView.OnScrollListener onScrollListener = new ListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            reprioritizeDownloads();
        }
    };

    /**
     * Callback interface that will be called when a network or other error is encountered
     * while retrieving graph objects.
     */
    public interface OnErrorListener {
        /**
         * Called when a network or other error is encountered.
         *
         * @param error a FacebookException representing the error that was encountered.
         */
        void onError(SimplifiedPickerFragment<?> fragment, FacebookException error);
    }

    /**
     * Callback interface that will be called when the underlying data being displayed in the
     * picker has been updated.
     */
    public interface OnDataChangedListener {
        /**
         * Called when the set of data being displayed in the picker has changed.
         */
        void onDataChanged(SimplifiedPickerFragment<?> fragment);
    }

    /**
     * Callback interface that will be called when the user selects or unselects graph objects
     * in the picker.
     */
    public interface OnSelectionChangedListener {
        /**
         * Called when the user selects or unselects graph objects in the picker.
         */
        void onSelectionChanged(SimplifiedPickerFragment<?> fragment);
    }

    /**
     * Callback interface that will be called when the user clicks the Done button on the
     * title bar.
     */
    public interface OnDoneButtonClickedListener {
        /**
         * Called when the user clicks the Done button.
         */
        void onDoneButtonClicked(SimplifiedPickerFragment<?> fragment);
    }

    /**
     * Callback interface that will be called to determine if a graph object should be displayed.
     *
     * @param <T>
     */
    public interface GraphObjectFilter<T> {
        /**
         * Called to determine if a graph object should be displayed.
         *
         * @param graphObject the graph object
         * @return true to display the graph object, false to hide it
         */
        boolean includeItem(T graphObject);
    }

    abstract class LoadingStrategy {
        protected final static int CACHED_RESULT_REFRESH_DELAY = 2 * 1000;

        protected GraphObjectPagingLoader<T> loader;
        protected GraphObjectAdapter<T> adapter;

        public void attach(GraphObjectAdapter<T> adapter) {
            loader = (GraphObjectPagingLoader<T>) getLoaderManager().initLoader(0, null,
                    new LoaderManager.LoaderCallbacks<SimpleGraphObjectCursor<T>>() {
                        @Override
                        public Loader<SimpleGraphObjectCursor<T>> onCreateLoader(int id, Bundle args) {
                            return LoadingStrategy.this.onCreateLoader();
                        }

                        @Override
                        public void onLoadFinished(Loader<SimpleGraphObjectCursor<T>> loader,
                                                   SimpleGraphObjectCursor<T> data) {
                            if (loader != LoadingStrategy.this.loader) {
                                throw new FacebookException("Received callback for unknown loader.");
                            }
                            LoadingStrategy.this.onLoadFinished((GraphObjectPagingLoader<T>) loader, data);
                        }

                        @Override
                        public void onLoaderReset(Loader<SimpleGraphObjectCursor<T>> loader) {
                            if (loader != LoadingStrategy.this.loader) {
                                throw new FacebookException("Received callback for unknown loader.");
                            }
                            LoadingStrategy.this.onLoadReset((GraphObjectPagingLoader<T>) loader);
                        }
                    });

            loader.setOnErrorListener(new GraphObjectPagingLoader.OnErrorListener() {
                @Override
                public void onError(FacebookException error, GraphObjectPagingLoader<?> loader) {
                    hideActivityCircle();
                    if (onErrorListener != null) {
                        onErrorListener.onError(SimplifiedPickerFragment.this, error);
                    }
                }
            });

            this.adapter = adapter;
            // Tell the adapter about any data we might already have.
            this.adapter.changeCursor(loader.getCursor());
            this.adapter.setOnErrorListener(new GraphObjectAdapter.OnErrorListener() {
                @Override
                public void onError(GraphObjectAdapter<?> adapter, FacebookException error) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(SimplifiedPickerFragment.this, error);
                    }
                }
            });
        }

        public void detach() {
            adapter.setDataNeededListener(null);
            adapter.setOnErrorListener(null);
            loader.setOnErrorListener(null);

            loader = null;
            adapter = null;
        }

        public void clearResults() {
            if (loader != null) {
                loader.clearResults();
            }
        }

        public void startLoading(Request request) {
            if (loader != null) {
                loader.startLoading(request, true);
                onStartLoading(loader, request);
            }
        }

        public boolean isDataPresentOrLoading() {
            return !adapter.isEmpty() || loader.isLoading();
        }

        protected GraphObjectPagingLoader<T> onCreateLoader() {
            return new GraphObjectPagingLoader<T>(getActivity(), graphObjectClass);
        }

        protected void onStartLoading(GraphObjectPagingLoader<T> loader, Request request) {
            displayActivityCircle();
        }

        protected void onLoadReset(GraphObjectPagingLoader<T> loader) {
            adapter.changeCursor(null);
        }

        protected void onLoadFinished(GraphObjectPagingLoader<T> loader, SimpleGraphObjectCursor<T> data) {
            updateAdapter(data);
        }
    }

    abstract class SelectionStrategy {
        abstract boolean isSelected(String id);
        abstract void toggleSelection(String id);
        abstract Collection<String> getSelectedIds();
        abstract void clear();
        abstract boolean isEmpty();
        abstract boolean shouldShowCheckBoxIfUnselected();
        abstract void saveSelectionToBundle(Bundle outBundle, String key);
        abstract void readSelectionFromBundle(Bundle inBundle, String key);
    }

    class SingleSelectionStrategy extends SelectionStrategy {
        private String selectedId;

        public Collection<String> getSelectedIds() {
            return Arrays.asList(new String[]{selectedId});
        }

        @Override
        boolean isSelected(String id) {
            return selectedId != null && id != null && selectedId.equals(id);
        }

        @Override
        void toggleSelection(String id) {
            if (selectedId != null && selectedId.equals(id)) {
                selectedId = null;
            } else {
                selectedId = id;
            }
        }

        @Override
        void saveSelectionToBundle(Bundle outBundle, String key) {
            if (!TextUtils.isEmpty(selectedId)) {
                outBundle.putString(key, selectedId);
            }
        }

        @Override
        void readSelectionFromBundle(Bundle inBundle, String key) {
            if (inBundle != null) {
                selectedId = inBundle.getString(key);
            }
        }

        @Override
        public void clear() {
            selectedId = null;
        }

        @Override
        boolean isEmpty() {
            return selectedId == null;
        }

        @Override
        boolean shouldShowCheckBoxIfUnselected() {
            return false;
        }
    }

    class MultiSelectionStrategy extends SelectionStrategy {
        private Set<String> selectedIds = new HashSet<String>();

        public Collection<String> getSelectedIds() {
            return selectedIds;
        }

        @Override
        boolean isSelected(String id) {
            return id != null && selectedIds.contains(id);
        }

        @Override
        void toggleSelection(String id) {
            if (id != null) {
                if (selectedIds.contains(id)) {
                    selectedIds.remove(id);
                } else {
                    selectedIds.add(id);
                }
            }
        }

        @Override
        void saveSelectionToBundle(Bundle outBundle, String key) {
            if (!selectedIds.isEmpty()) {
                String ids = TextUtils.join(",", selectedIds);
                outBundle.putString(key, ids);
            }
        }

        @Override
        void readSelectionFromBundle(Bundle inBundle, String key) {
            if (inBundle != null) {
                String ids = inBundle.getString(key);
                if (ids != null) {
                    String[] splitIds = TextUtils.split(ids, ",");
                    selectedIds.clear();
                    Collections.addAll(selectedIds, splitIds);
                }
            }
        }

        @Override
        public void clear() {
            selectedIds.clear();
        }

        @Override
        boolean isEmpty() {
            return selectedIds.isEmpty();
        }

        @Override
        boolean shouldShowCheckBoxIfUnselected() {
            return true;
        }
    }

    abstract class PickerFragmentAdapter<U extends GraphObject> extends GraphObjectAdapter<T> {
        public PickerFragmentAdapter(Context context) {
            super(context);
        }

        @Override
        boolean isGraphObjectSelected(String graphObjectId) {
            return selectionStrategy.isSelected(graphObjectId);
        }

        @Override
        void updateCheckboxState(CheckBox checkBox, boolean graphObjectSelected) {
            checkBox.setChecked(graphObjectSelected);
            int visible = (graphObjectSelected || selectionStrategy
                    .shouldShowCheckBoxIfUnselected()) ? View.VISIBLE : View.GONE;
            checkBox.setVisibility(visible);
        }
    }
}

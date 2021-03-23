package com.quidvis.moneydrop.fragment;

import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.NotificationAdapter;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.custom.CustomCompatFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Notification;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends CustomCompatFragment {

    // TODO: Rename parameter arguments, choose names that match

    private FragmentActivity activity;
    private DbHelper dbHelper;
    private NotificationAdapter notificationAdapter;
    private final ArrayList<Notification> notifications = new ArrayList<>();

    private final static String STATE_KEY = NotificationFragment.class.getName();
    private Bundle state = null;
    private JSONObject data;
    private static boolean started = false;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoanOffersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();
        dbHelper = new DbHelper(activity);

        tvNoContent = view.findViewById(R.id.no_content);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getNotifications(null);
        });

        recyclerView = view.findViewById(R.id.notification_list);

        notificationAdapter = new NotificationAdapter(recyclerView, activity, notifications);
        recyclerView.setAdapter(notificationAdapter);
        notificationAdapter.setOnLoadMoreListener(() -> {
            notificationAdapter.setLoading(true);
            getNotifications(state.getString("nextPage"));
        });

        ((MainActivity) activity).setCustomTitle(getResources().getString(R.string.notification));
        ((MainActivity) activity).setCustomSubtitle(getResources().getString(R.string.no_record));

        start();
    }

    private void start() {

        if (started) return;

        started = true;

        state = getState();

        if (state != null && state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (data != null) {

            try {
                setNotifications(data.getJSONArray("notifications"), false);
            } catch (JSONException e) {
                getNotifications(Objects.requireNonNull(state).getString("nextPage"));
                e.printStackTrace();
            }

        } else {
            getNotifications(Objects.requireNonNull(state).getString("nextPage"));
        }
    }

    public void setLoading(boolean status) {
        setLoading(status, false);
    }

    public void setLoading(boolean status, boolean hasContent) {
        if (status) {

            tvNoContent.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();

        } else {

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            tvNoContent.setVisibility(hasContent ? View.GONE : View.VISIBLE);
            recyclerView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        }
    }

    private void setNotifications(JSONArray notifications, boolean addUp) {

        int size = notifications.length();

        if (!addUp) this.notifications.clear();

        if (addUp && size <= 0) {

            if (notificationAdapter.isLoading()) {
                setLoading(false, true);
                notificationAdapter.setLoading(false);
                notificationAdapter.setPermitLoadMore(false);
                this.notifications.add(null);
                notificationAdapter.notifyItemInserted(this.notifications.size() - 1);
            }

            return;
        }

        if (notificationAdapter.isLoading()) notificationAdapter.setLoading(false);
        notificationAdapter.setPermitLoadMore(true);

        for (int i = 0; i < size; i++) {
            try {

                JSONObject notification = notifications.getJSONObject(i);
                Notification loan = new Notification(activity, notification);
                this.notifications.add(loan);
                if (!addUp) continue;
                data.getJSONArray("notifications").put(notification);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setLoading(false, size > 0);
        size = this.notifications.size();
        ((MainActivity) activity).setCustomSubtitle((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : activity.getResources().getString(R.string.no_record));
        notificationAdapter.notifyDataSetChanged();
    }

    private void getNotifications(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            notificationAdapter.setLoading(false);
            notificationAdapter.setPermitLoadMore(false);
            notifications.add(null);
            notificationAdapter.notifyItemInserted(notifications.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? nextPage : URLContract.NOTIFICATIONS_URL,
                Request.Method.GET, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", dbHelper.getUser().getToken());
                params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {

                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                if (isRefreshing()) setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setNotifications(object.getJSONArray("notifications"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(activity, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }

                if (data == null) setLoading(false);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };

        httpRequest.send();

    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        swipeRefreshLayout.setRefreshing(refreshing);
        if (refreshing) {
            data = null;
            state.putBundle("data", null);
            state.putString("nextPage", null);
            notifications.clear();
            if (!notificationAdapter.isPermitLoadMore()) notificationAdapter.setPermitLoadMore(true);
        }
    }

    public Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    public Bundle getCurrentState() {
        if (state == null) state = getState();
        if (data != null) state.putString("data", data.toString());
        return state;
    }

    public void saveState() {
        Utility.saveState(STATE_KEY, getCurrentState());
    }

    @Override
    public void onPause() {
        super.onPause();
        started = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        started = false;
        saveState();
    }
}
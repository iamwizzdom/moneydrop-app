package com.quidvis.moneydrop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.adapter.ReviewAdapter;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.Review;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserReviewsActivity extends CustomCompatActivity {

    public static final String USER_OBJECT_KEY = "userObject";
    public static final int REVIEW_REQUEST_KEY = 132;
    public final static String STATE_KEY = UserReviewsActivity.class.getName();
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private User user, reviewUser;
    private ReviewAdapter reviewAdapter;
    private final ArrayList<Review> reviews = new ArrayList<>();
    private Bundle state;
    private JSONObject data;

    private boolean refreshing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvItemCount, tvNoContent;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;

    private CircleImageView mvPic;
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reviews);

        Intent intent = getIntent();

        String userString = intent.getStringExtra(USER_OBJECT_KEY);

        if (userString == null) {
            Utility.toastMessage(this, "No user passed");
            finish();
            return;
        }

        try {
            JSONObject userObject = new JSONObject(userString);
            reviewUser = new User(this, userObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid user passed");
            finish();
            return;
        }

        format.setMaximumFractionDigits(0);

        mvPic = findViewById(R.id.user_pic);
        tvUsername = findViewById(R.id.username);

        setUserView();

        tvItemCount = findViewById(R.id.item_count);
        tvItemCount.setText(R.string.no_record);
        tvNoContent = findViewById(R.id.no_content);
        shimmerFrameLayout = findViewById(R.id.shimmer_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setRefreshing(true);
            getReviews(null);
        });

        recyclerView = findViewById(R.id.review_list);

        DbHelper dbHelper = new DbHelper(this);
        user = dbHelper.getUser();

        reviewAdapter = new ReviewAdapter(recyclerView, this, reviews);
        recyclerView.setAdapter(reviewAdapter);
        reviewAdapter.setOnLoadMoreListener(() -> getReviews(state.getString("nextPage")));

        state = getState();

        if (state != null && state.size() > 0) {
            try {
                data = new JSONObject(Objects.requireNonNull(state.getString("data")));
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        getReviews(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            Utility.toastMessage(this, "Failed to capture new review data.");
            return;
        }

        if (requestCode == REVIEW_REQUEST_KEY) {

            String reviewString = data.getStringExtra(UserSingleReviewActivity.REVIEW_OBJECT_KEY);
            int position = data.getIntExtra(UserSingleReviewActivity.REVIEW_POSITION_KEY, 0);

            if (reviewString == null) {
                reviews.remove(position);
                notifyView();
                return;
            }

            try {
                Review review = new Review(this, new JSONObject(reviewString));
                reviews.set(position, review);
                notifyView();
            } catch (JSONException e) {
                e.printStackTrace();
                Utility.toastMessage(this, "Invalid loan passed");
                finish();
            }
        }
    }

    public void viewReviewUser(View view) {
        if (reviewUser.isMe()) return;
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_OBJECT_KEY, reviewUser.getUserObject().toString());
        startActivity(intent);
    }

    public void setUserView() {

        tvUsername.setText(String.format("%s %s", reviewUser.getFirstname(), reviewUser.getLastname()));

        Glide.with(this)
                .load(reviewUser.getPictureUrl())
                .placeholder(reviewUser.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(reviewUser.getDefaultPicture())
                .apply(new RequestOptions().override(150, 150))
                .into(mvPic);
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

    private void setReviews(JSONArray reviews, boolean addUp) {

        int size = reviews.length();

        for (int i = 0; i < size; i++) {
            try {

                JSONObject reviewObject = reviews.getJSONObject(i);
                Review review = new Review(this, reviewObject);
                this.reviews.add(review);
                if (!addUp) continue;
                data.getJSONArray("reviews").put(reviewObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        notifyView();
    }

    private void notifyView() {
        int size = this.reviews.size();
        setLoading(false, size > 0);
        tvItemCount.setText((size > 0) ? String.format("%s %s", size, (size > 1 ? "records" : "record")) : getResources().getText(R.string.no_record));
        reviewAdapter.notifyDataSetChanged();
    }

    private void getReviews(String nextPage) {

        if (nextPage != null && nextPage.equals("#")) {
            setLoading(false, true);
            reviewAdapter.setLoading(false);
            reviewAdapter.setPermitLoadMore(false);
            reviews.add(null);
            reviewAdapter.notifyItemInserted(reviews.size() - 1);
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this,
                nextPage != null ? nextPage : String.format(URLContract.USER_REVIEWS_URL, reviewUser.getUuid()),
                Request.Method.GET, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", user.getToken());
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

                reviewAdapter.setLoading(true);
                reviewAdapter.setPermitLoadMore(false);
                if (data == null) setLoading(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                reviewAdapter.setLoading(false);
                reviewAdapter.setPermitLoadMore(true);
                if (onError && data == null) setLoading(false);

                if (isRefreshing()) setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);
                    setReviews(object.getJSONArray("reviews"), data != null);
                    state.putString("nextPage", object.getJSONObject("pagination").getString("nextPage"));
                    if (data == null) data = object;

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (data == null) setLoading(false);
                    Utility.toastMessage(UserReviewsActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(UserReviewsActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(UserReviewsActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
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
            reviews.clear();
            if (!reviewAdapter.isPermitLoadMore()) reviewAdapter.setPermitLoadMore(true);
        }
    }

    public Bundle getState() {
        return Utility.getState(STATE_KEY);
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}
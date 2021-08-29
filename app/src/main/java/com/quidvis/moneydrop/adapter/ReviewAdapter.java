package com.quidvis.moneydrop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanApplicantsActivity;
import com.quidvis.moneydrop.activity.UserReviewsActivity;
import com.quidvis.moneydrop.activity.UserSingleReviewActivity;
import com.quidvis.moneydrop.interfaces.OnLoadMoreListener;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.model.Review;
import com.quidvis.moneydrop.model.User;

import java.util.List;
import java.util.Objects;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;

import static com.quidvis.moneydrop.constant.Constant.DEFAULT_RECORD_PER_VIEW;

/**
 * Created by Wisdom Emenike.
 * Date: 6/15/2017
 * Time: 12:33 AM
 */

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading = false, permitLoadMore = true;
    private final int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private final RecyclerView recyclerView;
    private final RecyclerView.OnScrollListener mOnScrollListener;

    private final Activity activity;
    private final List<Review> reviews;

    //Constructor
    public ReviewAdapter(RecyclerView recyclerView, Activity activity, List<Review> reviews) {

        this.activity = activity;
        this.reviews = reviews;
        this.recyclerView = recyclerView;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = Objects.requireNonNull(linearLayoutManager).getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (isPermitLoadMore() && !isLoading() && getItemCount() >= DEFAULT_RECORD_PER_VIEW
                        && lastVisibleItem >= (totalItemCount - visibleThreshold)) {
                    if (mOnLoadMoreListener != null) mOnLoadMoreListener.onLoadMore();
                }

            }
        };

        this.recyclerView.addOnScrollListener(mOnScrollListener);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_NO_MORE_RECORD = 2;
        return reviews.get(position) == null ? (isLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_NO_MORE_RECORD) : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {

            return new ParentViewHolder(
                    LayoutInflater.from(activity).inflate(R.layout.review_layout, parent, false)
            );

        } else if (viewType == VIEW_TYPE_LOADING) {

            return new LoadingViewHolder(
                    LayoutInflater.from(activity).inflate(R.layout.loader, parent, false)
            );

        }

        return new NoMoreRecordViewHolder(
                LayoutInflater.from(activity).inflate(R.layout.no_more_record, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ParentViewHolder) {

            Review review = this.reviews.get(position);
            ParentViewHolder parentViewHolder = (ParentViewHolder) holder;

            User reviewer = review.getReviewer();
            parentViewHolder.tvReview.setText(review.getReview());
            parentViewHolder.tvDate.setText(review.getDate());

            Glide.with(activity)
                    .load(reviewer.getPicture())
                    .placeholder(reviewer.getDefaultPicture())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(reviewer.getDefaultPicture())
                    .apply(new RequestOptions().override(150, 150))
                    .into(parentViewHolder.mvPic);

            int size = getItemCount();

            if ((position == 0 && size >= 1) || position > 0 && position < (size - 1))
                parentViewHolder.container.setBackgroundResource(R.drawable.layout_underline);

            parentViewHolder.container.setOnClickListener(v -> {
                Intent intent = new Intent(activity, UserSingleReviewActivity.class);
                intent.putExtra(UserSingleReviewActivity.REVIEW_POSITION_KEY, position);
                intent.putExtra(UserSingleReviewActivity.REVIEW_OBJECT_KEY, review.getReviewObject().toString());
                activity.startActivityForResult(intent, UserReviewsActivity.REVIEW_REQUEST_KEY);
            });

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof NoMoreRecordViewHolder) {
            NoMoreRecordViewHolder noMoreRecordViewHolder = (NoMoreRecordViewHolder) holder;
            noMoreRecordViewHolder.tvNoMoreRecord.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public boolean isPermitLoadMore() {
        return permitLoadMore;
    }

    public void setPermitLoadMore(boolean permitLoadMore) {
        this.permitLoadMore = permitLoadMore;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(final boolean loaded) {

        if (loaded) {

            recyclerView.clearOnScrollListeners();
            reviews.add(null);
            this.notifyItemInserted((getItemCount() - 1));

        } else if (isLoading()) {

            int currentSize = getItemCount();
            if (currentSize > 0) {
                reviews.remove((currentSize - 1));
                this.notifyItemRemoved(currentSize);
            }
            recyclerView.addOnScrollListener(mOnScrollListener);
        }
        isLoading = loaded;
    }

    private static class NoMoreRecordViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvNoMoreRecord;

        public NoMoreRecordViewHolder(View view) {
            super(view);
            tvNoMoreRecord = view.findViewById(R.id.no_more_record);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }

    private static class ParentViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout container;
        public final ImageView mvPic;
        public final TextView tvReview, tvDate;

        public ParentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            mvPic = view.findViewById(R.id.profile_pic);
            tvReview = view.findViewById(R.id.review);
            tvDate = view.findViewById(R.id.review_date);
        }
    }
}

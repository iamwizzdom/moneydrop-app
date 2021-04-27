package com.quidvis.moneydrop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.ImagePreviewActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.OnLoadMoreListener;
import com.quidvis.moneydrop.model.Notification;
import com.quidvis.moneydrop.model.Transaction;
import com.quidvis.moneydrop.utility.NotificationIntent;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.constant.Constant.DEFAULT_RECORD_PER_VIEW;
import static com.quidvis.moneydrop.utility.Utility.getTheme;

/**
 * Created by Wisdom Emenike.
 * Date: 6/15/2017
 * Time: 12:33 AM
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading = false, permitLoadMore = true;
    private final int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private final RecyclerView recyclerView;
    private final RecyclerView.OnScrollListener mOnScrollListener;
    private final Activity activity;
    private final List<Notification> notifications;

    //Constructor
    public NotificationAdapter(RecyclerView recyclerView, Activity activity, List<Notification> notifications) {

        this.activity = activity;
        this.notifications = notifications;
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
        return notifications.get(position) == null ? (isLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_NO_MORE_RECORD) : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {

            return new ParentViewHolder(
                    LayoutInflater.from(activity).inflate(R.layout.notification_layout, parent, false)
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

            Notification notification = this.notifications.get(position);
            ParentViewHolder parentViewHolder = (ParentViewHolder) holder;

            parentViewHolder.tvNotice.setText(notification.getMessage());
            parentViewHolder.tvNoticeTime.setText(notification.getDateTime());

            if (!TextUtils.isEmpty(notification.getImage())) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 2.3f;
                parentViewHolder.noticeContainer.setLayoutParams(params);
                parentViewHolder.noticeImageHolder.setVisibility(View.VISIBLE);
                Glide.with(activity)
                        .load((URLContract.BASE_URL + "/" + notification.getImage()))
                        .placeholder(R.drawable.unisex)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.unisex)
                        .into(parentViewHolder.noticeImage);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 2.7f;
                parentViewHolder.noticeContainer.setLayoutParams(params);
                parentViewHolder.noticeImageHolder.setVisibility(View.GONE);
            }

            int size = getItemCount();

            if ((position == 0 && size > 1) || position > 0 && position < (size - 1))
                parentViewHolder.container.setBackgroundResource(R.drawable.layout_underline);

            if (notification.getActivity().equals("transactionReceipt")) {
                try {
                    JSONObject transObject = new JSONObject(notification.getPayload());
                    Transaction transaction = new Transaction(activity, transObject);
                    ArrayMap<String, Integer> theme = getTheme(transaction.getStatus(), transaction.getType().toLowerCase().equals("top-up"));
                    parentViewHolder.noticeIcon.setImageDrawable(ContextCompat.getDrawable(activity, Objects.requireNonNull(theme.get("icon"))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                parentViewHolder.noticeIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_light_bulb));
            }

            parentViewHolder.container.setOnClickListener(v -> {
                Intent intent = NotificationIntent.getIntent(activity, notification.getActivity(), notification.getPayload());
                activity.startActivity(intent);
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
        return notifications != null ? notifications.size() : 0;
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
            notifications.add(null);
            this.notifyItemInserted((getItemCount() - 1));

        } else if (isLoading()) {

            int currentSize = getItemCount();
            if (currentSize > 0) {
                notifications.remove((currentSize - 1));
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

        public final ImageView noticeIcon;
        public final RelativeLayout noticeImageHolder;
        public final LinearLayout container, noticeContainer;
        public final CircleImageView noticeImage;
        public final TextView tvNotice, tvNoticeTime;

        public ParentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            noticeIcon = view.findViewById(R.id.notification_icon);
            noticeContainer = view.findViewById(R.id.notice_container);
            noticeImageHolder = view.findViewById(R.id.notice_image_holder);
            noticeImage = view.findViewById(R.id.notice_image);
            tvNotice = view.findViewById(R.id.notice);
            tvNoticeTime = view.findViewById(R.id.notice_time);
        }
    }
}

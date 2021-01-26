package com.quidvis.moneydrop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanApplicationDetailsActivity;
import com.quidvis.moneydrop.interfaces.OnLoadMoreListener;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.LoanApplication;
import com.quidvis.moneydrop.utility.Utility;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import static com.quidvis.moneydrop.constant.Constant.DEFAULT_RECORD_PER_VIEW;
import static com.quidvis.moneydrop.utility.Utility.getTheme;

/**
 * Created by Wisdom Emenike.
 * Date: 6/15/2017
 * Time: 12:33 AM
 */

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading = false, permitLoadMore = true;
    private final int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private final RecyclerView recyclerView;
    private final RecyclerView.OnScrollListener mOnScrollListener;
    private final Activity activity;
    private final List<LoanApplication> applications;
    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));

    //Constructor
    public HistoryAdapter(RecyclerView recyclerView, Activity activity, List<LoanApplication> applications) {

        this.activity = activity;
        this.applications = applications;
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
        return applications.get(position) == null ? (isLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_NO_MORE_RECORD) : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {

            return new ParentViewHolder(
                    LayoutInflater.from(activity).inflate(R.layout.history_layout, parent, false)
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

            LoanApplication application = this.applications.get(position);
            ParentViewHolder parentViewHolder = (ParentViewHolder) holder;

            Loan loan = application.getLoan();
            String type = String.format("Loan %s", loan.getLoanType());

            if (loan.isMine()) type += " (Me)";

            parentViewHolder.tvType.setText(type);
            parentViewHolder.tvDate.setText(application.getDate());
            parentViewHolder.tvAmount.setText(format.format(application.getLoan().getAmount()));
            parentViewHolder.tvStatus.setText(Utility.ucFirst(application.getStatus()));

            ArrayMap<String, Integer> theme = getTheme(application.getStatus(), true);

            parentViewHolder.mvIcon.setImageDrawable(ContextCompat.getDrawable(activity, Objects.requireNonNull(theme.get("icon"))));
            parentViewHolder.tvAmount.setTextColor(activity.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
            parentViewHolder.tvStatus.setTextAppearance(activity, Objects.requireNonNull(theme.get("badge")));
            parentViewHolder.tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

            int size = getItemCount();

            if ((position == 0 && size > 1) || position > 0 && position < (size - 1))
                parentViewHolder.container.setBackgroundResource(R.drawable.layout_underline);

            parentViewHolder.container.setOnClickListener(v -> {
                Intent intent = new Intent(activity, LoanApplicationDetailsActivity.class);
                intent.putExtra(LoanApplicationDetailsActivity.LOAN_APPLICATION_OBJECT_KEY, application.getApplicationObject().toString());
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
        return applications != null ? applications.size() : 0;
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
            applications.add(null);
            this.notifyItemInserted((getItemCount() - 1));

        } else if (isLoading()) {

            int currentSize = getItemCount();
            if (currentSize > 0) {
                applications.remove((currentSize - 1));
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
        public final ImageView mvIcon;
        public final TextView tvType, tvDate, tvAmount, tvStatus;

        public ParentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            mvIcon = view.findViewById(R.id.loan_icon);
            tvType = view.findViewById(R.id.loan_type);
            tvDate = view.findViewById(R.id.loan_date);
            tvAmount = view.findViewById(R.id.loan_amount);
            tvStatus = view.findViewById(R.id.loan_status);
        }
    }
}

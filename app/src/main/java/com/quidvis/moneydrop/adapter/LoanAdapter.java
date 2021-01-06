package com.quidvis.moneydrop.adapter;

import android.app.Activity;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.ProfileActivity;
import com.quidvis.moneydrop.interfaces.OnLoadMoreListener;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.Utility;

import java.text.NumberFormat;
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

public class LoanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading = false, permitLoadMore = true;
    private final int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private final RecyclerView recyclerView;
    private final RecyclerView.OnScrollListener mOnScrollListener;

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private final Activity activity;
    private final List<Loan> loanList;

    //Constructor
    public LoanAdapter(RecyclerView recyclerView, Activity activity, List<Loan> loanList) {

        this.activity = activity;
        this.loanList = loanList;
        this.recyclerView = recyclerView;
        format.setMaximumFractionDigits(0);

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = Objects.requireNonNull(linearLayoutManager).getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (isPermitLoadMore() && !isLoading() && totalItemCount >= DEFAULT_RECORD_PER_VIEW
                        && lastVisibleItem >= (totalItemCount - visibleThreshold))
                    if (mOnLoadMoreListener != null) mOnLoadMoreListener.onLoadMore();

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
        return loanList.get(position) == null ? (isLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_NO_MORE_RECORD) : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {

            return new ParentViewHolder(
                    LayoutInflater.from(activity).inflate(R.layout.loan_layout, parent, false)
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

            Loan loan = this.loanList.get(position);
            ParentViewHolder parentViewHolder = (ParentViewHolder) holder;

            parentViewHolder.tvType.setText(loan.getType());
            parentViewHolder.tvDate.setText(loan.getDate());
            parentViewHolder.tvAmount.setText(format.format(loan.getAmount()));
            parentViewHolder.tvStatus.setText(Utility.ucFirst(loan.getStatus()));

            ArrayMap<String, Integer> theme = getTheme(loan.getStatus());

            Glide.with(activity)
                    .load(loan.getPictureUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(loan.getDefaultPicture())
                    .apply(new RequestOptions().override(150, 150))
                    .into(parentViewHolder.mvPic);

            parentViewHolder.tvAmount.setTextColor(activity.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
            parentViewHolder.tvStatus.setTextAppearance(activity, Objects.requireNonNull(theme.get("badge")));
            parentViewHolder.tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

            int size = getItemCount();

            if ((position == 0 && size > 1) || position > 0 && position < (size - 1))
                parentViewHolder.container.setBackgroundResource(R.drawable.layout_underline);

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
        return loanList != null ? loanList.size() : 0;
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
            loanList.add(null);
            this.notifyItemInserted((getItemCount() - 1));

        } else if (isLoading()) {

            int currentSize = getItemCount();
            if (currentSize > 0) {
                loanList.remove((currentSize - 1));
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
        public final CircleImageView mvPic;
        public final TextView tvType, tvDate, tvAmount, tvStatus;

        public ParentViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            mvPic = view.findViewById(R.id.profile_pic);
            tvType = view.findViewById(R.id.loan_type);
            tvDate = view.findViewById(R.id.loan_date);
            tvAmount = view.findViewById(R.id.loan_amount);
            tvStatus = view.findViewById(R.id.loan_status);
        }
    }
}

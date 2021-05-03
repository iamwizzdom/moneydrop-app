package com.quidvis.moneydrop.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.LoanDetailsActivity;
import com.quidvis.moneydrop.fragment.LoanOffersFragment;
import com.quidvis.moneydrop.fragment.LoanRequestsFragment;
import com.quidvis.moneydrop.fragment.custom.CustomFragment;
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
    private final LinearLayoutManager linearLayoutManager;
    private final RecyclerView.OnScrollListener mOnScrollListener;

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en", "ng"));
    private final CustomFragment fragment;
    private final Context context;
    private final List<Loan> loans;
    private int mPosition = -1;

    //Constructor
    public LoanAdapter(RecyclerView recyclerView, CustomFragment fragment, List<Loan> loans) {

        this.fragment = fragment;
        this.context = fragment.getContext();
        this.loans = loans;
        this.recyclerView = recyclerView;
        format.setMaximumFractionDigits(0);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

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
        return loans.get(position) == null ? (isLoading() ? VIEW_TYPE_LOADING : VIEW_TYPE_NO_MORE_RECORD) : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {

            return new ParentViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.loan_layout, parent, false)
            );

        } else if (viewType == VIEW_TYPE_LOADING) {

            return new LoadingViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.loader, parent, false)
            );

        }

        return new NoMoreRecordViewHolder(
                LayoutInflater.from(context).inflate(R.layout.no_more_record, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ParentViewHolder) {

            Loan loan = this.loans.get(position);
            ParentViewHolder parentViewHolder = (ParentViewHolder) holder;

            String type = String.format("Loan %s", loan.getLoanType());;

            if (loan.isMine()) type += " (Me)";

            parentViewHolder.tvType.setText(type);
            parentViewHolder.tvDate.setText(loan.getDate());
            parentViewHolder.tvAmount.setText(format.format(loan.getAmount()));
            parentViewHolder.tvStatus.setText(Utility.castEmpty(loan.getStatus(), "Unknown"));

            ArrayMap<String, Integer> theme = getTheme(loan.getStatus());

            User user = loan.getUser();
            Glide.with(fragment)
                    .load(user.getPictureUrl())
                    .placeholder(user.getDefaultPicture())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(user.getDefaultPicture())
                    .apply(new RequestOptions().override(150, 150))
                    .into(parentViewHolder.mvPic);

            parentViewHolder.tvAmount.setTextColor(fragment.getResources().getColor(Objects.requireNonNull(theme.get("color"))));
            parentViewHolder.tvStatus.setTextAppearance(context, Objects.requireNonNull(theme.get("badge")));
            parentViewHolder.tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));

            int size = getItemCount();

            if ((position == 0 && size > 1) || position > 0 && position < (size - 1))
                parentViewHolder.container.setBackgroundResource(R.drawable.layout_underline);

            parentViewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, LoanDetailsActivity.class);
                intent.putExtra(LoanDetailsActivity.LOAN_POSITION_KEY, position);
                intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, loan.getLoanObject().toString());
                fragment.startActivityForResult(intent, loan.isLoanOffer() ? LoanOffersFragment.LOAN_OFFER_DETAILS_KEY : LoanRequestsFragment.LOAN_REQUEST_DETAILS_KEY);
            });

            if ((loan.isPending() || loan.isAwaiting()) && loan.isMine()) {
                parentViewHolder.itemView.setOnLongClickListener(v -> {
                    setPosition(parentViewHolder.getAbsoluteAdapterPosition());
                    return false;
                });
            } else {
                parentViewHolder.itemView.setLongClickable(false);
            }

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof NoMoreRecordViewHolder) {
            NoMoreRecordViewHolder noMoreRecordViewHolder = (NoMoreRecordViewHolder) holder;
            noMoreRecordViewHolder.tvNoMoreRecord.setVisibility(View.VISIBLE);
        }
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public Loan getItem(int position) {
        return loans.get(position);
    }

    public void removeItem(int position) {
        if (loans.remove(position) != null) {

            CustomFragment fragment = getFragment();

            if (fragment instanceof LoanOffersFragment) {
                LoanOffersFragment offersFragment = ((LoanOffersFragment) fragment);
                offersFragment.removeData(position);
                fragment.saveState();
            }

            if (fragment instanceof LoanOffersFragment) {
                LoanOffersFragment offersFragment = ((LoanOffersFragment) fragment);
                offersFragment.removeData(position);
                fragment.saveState();
            }

            notifyDataSetChanged();
        }
    }

    public void setItem(int position, Loan loan) {
        if (loans.set(position, loan) != null) notifyDataSetChanged();
    }

    public void fadeItem(int position, boolean fade) {
        position = ((recyclerView.getChildCount() - (linearLayoutManager.findLastVisibleItemPosition() - position)) - 1);
        View view = recyclerView.getChildAt(position);
        if (view != null) view.setAlpha(fade ? .5f : 1f);
    }

    private CustomFragment getFragment() {
        return fragment;
    }

    @Override
    public int getItemCount() {
        return loans != null ? loans.size() : 0;
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
            loans.add(null);
            this.notifyItemInserted((getItemCount() - 1));

        } else if (isLoading()) {

            int currentSize = getItemCount();
            if (currentSize > 0) {
                loans.remove((currentSize - 1));
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

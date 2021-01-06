package com.quidvis.moneydrop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.fragment.MainFragment;
import com.quidvis.moneydrop.model.Loan;
import com.quidvis.moneydrop.utility.Utility;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Wisdom Emenike.
 * Date: 6/15/2017
 * Time: 12:33 AM
 */

public class DashboardLoanListAdapter extends BaseAdapter {

    private final NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
    private final Activity activity;
    private final List<Loan> loanList;
    private final MainFragment mainFragment;

    //Constructor
    public DashboardLoanListAdapter(Activity activity, MainFragment mainFragment, List<Loan> loanList) {
        this.activity = activity;
        this.mainFragment = mainFragment;
        this.loanList = loanList;
    }

    @Override
    public int getCount() {
        return loanList.size();
    }

    @Override
    public Object getItem(int position) {
        return loanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View v = View.inflate(activity, R.layout.transaction_layout, null);

        ImageView transactionIcon = v.findViewById(R.id.transaction_icon);
        TextView transactionType = v.findViewById(R.id.transaction_type);
        TextView transactionDate = v.findViewById(R.id.transaction_date);
        TextView transactionAmount = v.findViewById(R.id.transaction_amount);
        TextView transactionStatus = v.findViewById(R.id.transaction_status);

        //set text for TextView
        Loan loan = this.loanList.get(position);

        transactionType.setText(loan.getType());
        transactionDate.setText(loan.getDate());
        transactionAmount.setText(format.format(loan.getAmount()));
        transactionStatus.setText(Utility.ucFirst(loan.getStatus()));
        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mainFragment.setLoadingLoans(false);
        mainFragment.setLoadingTransactions(false);
    }
}

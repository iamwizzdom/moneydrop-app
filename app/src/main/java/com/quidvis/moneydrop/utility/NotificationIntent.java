package com.quidvis.moneydrop.utility;

import android.content.Context;
import android.content.Intent;

import com.quidvis.moneydrop.activity.LoanApplicantsActivity;
import com.quidvis.moneydrop.activity.LoanApplicationDetailsActivity;
import com.quidvis.moneydrop.activity.LoanDetailsActivity;
import com.quidvis.moneydrop.activity.LoanRepaymentTransactionsActivity;
import com.quidvis.moneydrop.activity.MainActivity;
import com.quidvis.moneydrop.activity.TransactionReceiptActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationIntent {

    public static Intent getIntent(Context context, String activityName, String payload) {
        Intent intent = new Intent(context, getActivity(activityName));
        switch (activityName) {
            case "loanApplicant":
                intent.putExtra(LoanApplicantsActivity.LOAN_KEY, payload);
                break;
            case "wallet":
            case "history":
                intent.putExtra(MainActivity.START_FRAGMENT, activityName);
                break;
            case "transactionReceipt":
                intent.putExtra(TransactionReceiptActivity.TRANSACTION_KEY, payload);
                break;
            case "loanDetails":
                intent.putExtra(LoanDetailsActivity.LOAN_OBJECT_KEY, payload);
                break;
            case "loanApplicationDetails":
                intent.putExtra(LoanApplicationDetailsActivity.LOAN_APPLICATION_OBJECT_KEY, payload);
                break;
            case "loanRepaymentTransaction":
                try {
                    JSONObject object = new JSONObject(payload);
                    intent.putExtra(LoanRepaymentTransactionsActivity.APPLICATION_REFERENCE, object.getString("uuid"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return intent;
    }

    private static Class<?> getActivity(String activityName) {
        switch (activityName) {
            case "mainActivity":
                return MainActivity.class;
            case "loanApplicant":
                return LoanApplicantsActivity.class;
            case "transactionReceipt":
                return TransactionReceiptActivity.class;
            case "loanDetails":
                return LoanDetailsActivity.class;
            case "loanApplicationDetails":
                return LoanApplicationDetailsActivity.class;
            case "loanRepaymentTransaction":
                return LoanRepaymentTransactionsActivity.class;
            default:
                break;
        }
        return null;
    }
}

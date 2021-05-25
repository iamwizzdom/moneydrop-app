package com.quidvis.moneydrop.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.model.Card;
import com.quidvis.moneydrop.model.Transaction;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Objects;

import static com.quidvis.moneydrop.constant.Constant.FILE_PATH;

public class TransactionReceiptActivity extends CustomCompatActivity {

    public static final String TRANSACTION_KEY = "transObject";
    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_receipt);

        Intent intent = getIntent();

        String transString = intent.getStringExtra(TRANSACTION_KEY);

        if (transString == null) {
            Utility.toastMessage(this, "No transaction passed");
            finish();
            return;
        }

        try {
            JSONObject transObject = new JSONObject(transString);
            transaction = new Transaction(this, transObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Invalid transaction passed");
            finish();
            return;
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
        format.setMaximumFractionDigits(2);

        ImageView ivIcon = findViewById(R.id.transaction_icon);
        TextView tvType = findViewById(R.id.transaction_type);
        TextView tvDirection = findViewById(R.id.transaction_direction);
        TextView tvReference = findViewById(R.id.transaction_reference);
        TextView tvAmount = findViewById(R.id.transaction_amount);
        TextView tvCharges = findViewById(R.id.charges);
        TextView tvDate = findViewById(R.id.transaction_date);
        TextView tvCurrency = findViewById(R.id.transaction_currency);
        TextView tvStatus = findViewById(R.id.transaction_status);
        TextView tvNarration = findViewById(R.id.narration);

        ArrayMap<String, Integer> theme = Utility.getTheme(transaction.getStatus());

        Card card = transaction.getCard();
        int icon = card != null ? CardsActivity.getCardIcon(card.getBrand()) : R.drawable.ic_launcher;
        ivIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), icon, null));
        tvType.setText(transaction.getType());
        tvDirection.setText(transaction.getDirection());
        tvReference.setText(transaction.getReference());
        tvAmount.setText(format.format(transaction.getAmount()));
        tvCharges.setText(format.format(transaction.getFees()));
        tvCurrency.setText(transaction.getCurrency());
        tvDate.setText(transaction.getDateTime());
        tvStatus.setText(transaction.getStatus());
        tvStatus.setTextAppearance(Objects.requireNonNull(theme.get("badge")));
        tvStatus.setBackgroundResource(Objects.requireNonNull(theme.get("background")));
        tvNarration.setText(Utility.castEmpty(transaction.getNarration(), "No narrative"));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                layoutToImage(null);
            }
        }
    }

    public void layoutToImage(View view) {

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
            return;
        }

        // get view group using reference
        LinearLayout rootLayout = findViewById(R.id.receipt_layout);
        // convert view group to bitmap
        rootLayout.setDrawingCacheEnabled(true);
        rootLayout.buildDrawingCache();
        Bitmap bm = rootLayout.getDrawingCache();
        Bitmap waterMark = Utility.drawableToBitmap(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher, null));
        bm = Utility.addWatermark(bm, waterMark, 15);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        try {
            Utility.saveByteArray(bytes.toByteArray(), FILE_PATH,
                    transaction.getReference() + ".png",
                    true, true, this, findViewById(R.id.receipt_parent_layout));
        } catch (IOException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Failed to save receipt");
        }

    }

    public void onBackPressed(View view) {
        onBackPressed();
    }

}
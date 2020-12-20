package com.quidvis.moneydrop.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.preference.Session;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;
import com.quidvis.moneydrop.utility.view.EditCard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private final static String STATE_KEY = MainActivity.class.getName();
    private TextView titleTv, subtitleTv;
    private BottomNavigationView navView;
    private NavController navController;
    private DbHelper dbHelper;

    private CircleImageView profilePic;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        int itemId = item.getItemId();
        try {
            navController.navigate(itemId, null, getNavOptions());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profilePic = findViewById(R.id.profile_pic);
        titleTv = findViewById(R.id.title);
        subtitleTv = findViewById(R.id.subtitle);

        dbHelper = new DbHelper(this);

        setUserPic();

        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_loan).build();
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController = getNavController();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navView.setSelectedItemId(Objects.requireNonNull(navController.getCurrentDestination()).getId());
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        NavigationUI.setupWithNavController(navView, navController);

    }

    public void loadFragment(View view) {
        navController.navigate(view.getId(), null, getNavOptions());
    }

    @Override
    public void onBackPressed() {
        if (navController.popBackStack()) {
            navView.setSelectedItemId(Objects.requireNonNull(navController.getCurrentDestination()).getId());
        }
        super.onBackPressed();
    }

    private void setUserPic() {

        User user = dbHelper.getUser();

        Glide.with(MainActivity.this)
                .load(user.getPictureUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(user.getDefaultPicture())
                .into(profilePic);

        profilePic.setOnClickListener(view -> showBottomSheetDialog());
    }

    public void setCustomTitle(String title) {
        titleTv.setText(title);
    }

    public void setCustomSubtitle(String title) {
        subtitleTv.setText(title);
    }

    private NavController getNavController() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
        }
        return ((NavHostFragment) fragment).getNavController();
    }

    protected NavOptions getNavOptions() {

        return new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_up)
                .setExitAnim(R.anim.slide_down)
//                .setPopEnterAnim(R.anim.slide_up)
//                .setPopExitAnim(R.anim.slide_down)
                .build();
    }

    public static void logout(Activity activity) {
        logout(activity, "Logged out");
    }

    public static void logout(Activity activity, String title, String message) {
        logout(activity, title, message, null, null);
    }

    public static void logout(Activity activity, String message) {
        logout(activity, null, message, null, null);
    }

    public static void logout(Activity activity, String message, Session session) {
        logout(activity, null, message, null, session);
    }

    public static void logout(Activity activity, String message, DbHelper dbHelper) {
        logout(activity, null, message, dbHelper, null);
    }

    public static void logout(Activity activity, String title, String message, DbHelper dbHelper, Session session) {

        if (dbHelper == null) dbHelper = new DbHelper(activity);
        if (session == null) session = new Session(activity);

        session.setLoggedIn(false);
        dbHelper.deleteUser();
        dbHelper.deleteAllCards();

        Intent intent = new Intent(activity, LoginActivity.class);
        if (title != null) intent.putExtra(LoginActivity.TITLE, title);
        if (message != null) intent.putExtra(LoginActivity.MESSAGE, message);
        activity.startActivity(intent);
        activity.finish();
    }

    public void viewAllLoanRequest(View view) {
        startActivity(new Intent(this, UserLoanActivity.class));
    }

    public void viewAllTransaction(View view) {
        startActivity(new Intent(this, TransactionsActivity.class));
    }

    public void showBottomSheetDialog() {

        ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_user, null));
        sheetLayoutModel.setText(getResources().getString(R.string.user_account));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_transactions, null));
        sheetLayoutModel.setText(getResources().getString(R.string.transactions));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            viewAllTransaction(v);
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wallet, null));
        sheetLayoutModel.setText(getResources().getString(R.string.wallet));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
//            logout(this);
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_credit_card, null));
        sheetLayoutModel.setText(getResources().getString(R.string.cards));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            startActivity(new Intent(MainActivity.this, CardsActivity.class));
        });

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_logout, null));
        sheetLayoutModel.setText(getResources().getString(R.string.logout));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            logout(this);
        });

        layoutModels.add(sheetLayoutModel);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);
        bottomSheet.show();
    }

    public void showTopUpDialog(View view) {

        ArrayList<BottomSheetLayoutModel> layoutModels = new ArrayList<>();

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_credit_card, null));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            sheet.dismiss();
            MainActivity.this.showTopUpAmountDialog();
        });
        sheetLayoutModel.setText("Credit Card");

        layoutModels.add(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bank_transfer, null));
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            Utility.toastMessage(MainActivity.this, "This feature is coming soon.");
        });
        sheetLayoutModel.setText("Bank Transfer");

        layoutModels.add(sheetLayoutModel);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, layoutModels);
        bottomSheet.setTitle("Top Up via");
        bottomSheet.show();
    }

    public void showTopUpAmountDialog() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.topup_amount_bottom_sheet_layout, null);
        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this, bottomSheetView);
        bottomSheet.show();
    }

    public Bundle getState(String key) {
        Bundle state = Utility.getState(STATE_KEY);
        state = state.getBundle(key);
        return state != null ? state : new Bundle();
    }

    public void saveState(String key, Bundle state) {
        Bundle prevState = Utility.getState(STATE_KEY);
        prevState.putBundle(key, state);
        Utility.saveState(STATE_KEY, prevState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.clearState(STATE_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserPic();
    }
}
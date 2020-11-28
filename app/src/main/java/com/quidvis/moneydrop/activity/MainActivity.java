package com.quidvis.moneydrop.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Objects;

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
        navController.navigate(itemId, null, getNavOptions());
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

        String imageUrl = (URLContract.URL_SCHEME + URLContract.HOST_URL + "/" + user.getPicture());
        Glide.with(MainActivity.this)
                .load(imageUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(
                        user.getGender() == User.GENDER_MALE ? R.drawable.male : (
                                user.getGender() == User.GENDER_FEMALE ? R.drawable.female : R.drawable.unisex
                        )
                ).into(profilePic);

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

        Intent intent = new Intent(activity, LoginActivity.class);
        if (title != null) intent.putExtra(LoginActivity.TITLE, title);
        if (message != null) intent.putExtra(LoginActivity.MESSAGE, message);
        activity.startActivity(intent);
        activity.finish();
    }

    public void viewAllLoanRequest(View view) {
        startActivity(new Intent(this, LoanRequestActivity.class));
    }

    public void viewAllTransaction(View view) {
        startActivity(new Intent(this, TransactionsActivity.class));
    }

    public void showBottomSheetDialog() {

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dashboard_bottom_sheet_layout, null);
        LinearLayout vProfile = bottomSheetView.findViewById(R.id.profile);
        LinearLayout vTransactions = bottomSheetView.findViewById(R.id.transactions);
        LinearLayout vLogout = bottomSheetView.findViewById(R.id.logout);
        vProfile.setOnClickListener(view -> {
            bottomSheet.dismiss();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
        vTransactions.setOnClickListener(view -> {
            bottomSheet.dismiss();
            viewAllTransaction(view);
        });
        vLogout.setOnClickListener(view -> {
            bottomSheet.dismiss();
            logout(this);
        });
        bottomSheet.setView(bottomSheetView);
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
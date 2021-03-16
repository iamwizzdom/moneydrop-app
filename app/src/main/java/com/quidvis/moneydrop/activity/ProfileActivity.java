package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.ProfileOptionFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.utility.Utility.startRevealActivity;

public class ProfileActivity extends CustomCompatActivity {

    private NavController navController;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar uploadProgressBar;
    private RatingBar ratingBar;
    private DbHelper dbHelper;
    private User user;
    private ImageView imagePicker;
    private static File camImage;
    private CircleImageView profilePic;
    private Bundle startDestinationBundle;
    private boolean refreshing = false;

    public static final String USER_OBJECT_KEY = "userObject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();

        if (intent != null) {
            String userObject = intent.getStringExtra(USER_OBJECT_KEY);
            if (userObject != null) {
                try {
                    user = new User(this, new JSONObject(userObject));
                    startDestinationBundle = new Bundle();
                    startDestinationBundle.putString(USER_OBJECT_KEY, user.getUserObject().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        dbHelper = new DbHelper(this);

        if (user == null) user = dbHelper.getUser();

        navController = getNavController();
        navController.setGraph(R.navigation.profile_navigation, startDestinationBundle);

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        uploadProgressBar = findViewById(R.id.upload_photo_progress_bar);
        imagePicker = findViewById(R.id.image_picker);

        boolean isMe = user.isMe();

        if (isMe) imagePicker.setOnClickListener(v -> selectImage());
        else imagePicker.setVisibility(View.GONE);

        profilePic = findViewById(R.id.profile_pic);
        ratingBar = findViewById(R.id.ratingBar);

        profilePic.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(ImagePreviewActivity.IMAGE_URL, user.getPictureUrl());
            startRevealActivity(this, v, ImagePreviewActivity.class, bundle);
        });

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) rateUser(rating);
        });

        refreshLayout.setEnabled(isMe);
        if (isMe) refreshLayout.setOnRefreshListener(this::getUserData);

        setUser();
    }

    public void setUser() {
        setUser(this.user);
    }

    public void setUser(User user) {

        if (user == null) user = this.user = dbHelper.getUser();

        TextView tvName = findViewById(R.id.account_name);
        TextView tvEmail = findViewById(R.id.account_email);

        Glide.with(ProfileActivity.this)
                .load(user.getPictureUrl())
                .placeholder(user.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(user.getDefaultPicture())
                .into(profilePic);

        tvName.setText(String.format("%s %s", user.getFirstname(), user.getLastname()));
        tvEmail.setText(user.getEmail());
        ratingBar.setRating(Double.valueOf(user.getRating()).floatValue());
    }

    private void selectImage() {
        imagePicker.setVisibility(View.GONE);
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {

                try {

                    PackageManager packageManager = getPackageManager();
                    boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);

                    if (hasCamera) {
                        // start the image capture Intent
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(this,
                                        BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
                        startActivityForResult(intent, 1);

                    } else
                        Utility.toastMessage(ProfileActivity.this, "This device seems not to have a camera");

                } catch (Exception ex) {

                    Utility.toastMessage(ProfileActivity.this, "There was an error with the camera.");
                }

            } else if (item == 1) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            } else {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(dialogInterface -> imagePicker.setVisibility(View.VISIBLE));
        builder.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        camImage = File.createTempFile("profile-photo", ".jpg", storageDir);
        return camImage;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Utility.toastMessage(ProfileActivity.this, requestCode == 1 ? "Image capture failed." : "Image selection failed.");
            if (requestCode == 1 && camImage != null) //noinspection ResultOfMethodCallIgnored
                camImage.delete();
            return;
        }

        Uri uri = null;

        if (requestCode == 1) uri = Uri.fromFile(camImage);
        else if (requestCode == 2) uri = data.getData();

        try {

            if (uri == null) throw new IOException("Failed");

            Bitmap bitmap = Utility.getBitmapFormUri(this, uri);
            File file = requestCode == 1 ? camImage : Utility.getFileFromMediaUri(this, uri);
            int degree = Utility.getBitmapDegree(file.getAbsolutePath());
            bitmap = Utility.rotateBitmapByDegree(bitmap, degree);
            String imageString = imageToBase64(bitmap);

            if (imageString == null || imageString.isEmpty()) throw new IOException("Failed");

            if (requestCode == 1 && camImage != null) //noinspection ResultOfMethodCallIgnored
                camImage.delete();

            updatePhoto(imageString);

        } catch (IOException e) {
            e.printStackTrace();
            Utility.toastMessage(this, "Image upload failed, please try again.");
        }
    }

    private String imageToBase64(Bitmap bitmap) {
        return Base64.encodeToString(Utility.toByteArray(bitmap), Base64.DEFAULT);
    }

    private void progressToggle(boolean status) {
        uploadProgressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    private NavController getNavController() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_profile_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            throw new IllegalStateException("Activity " + this + " does not have a NavHostFragment");
        }
        return ((NavHostFragment) fragment).getNavController();
    }

    protected NavOptions getNavOptions() {

        return new NavOptions.Builder()
                .setEnterAnim(R.anim.from_right)
                .setExitAnim(R.anim.to_left)
                .setPopEnterAnim(R.anim.from_left)
                .setPopExitAnim(R.anim.to_right)
                .build();
    }

    public void loadEditFragment(View view) {
        if (!user.isMe()) return;
        Bundle bundle = new Bundle();
        int id = view.getId();
        if (id == R.id.account_name) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Name");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_NAME);
        } else if (id == R.id.phone_number) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Phone");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_PHONE);
        } else if (id == R.id.email) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Email");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_EMAIL);
        } else if (id == R.id.gender) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Gender");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_GENDER);
        } else if (id == R.id.address) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Address");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_ADDRESS);
        } else if (id == R.id.dob) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update DOB");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_DOB);
        } else if (id == R.id.bvn) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update BVN");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_BVN);
        } else if (id == R.id.change_password) {
            bundle.putString(ProfileOptionFragment.EDIT_TITLE, "Update Password");
            bundle.putString(ProfileOptionFragment.EDIT_OPTION, ProfileOptionFragment.EDIT_PASSWORD);
        } else if (id == R.id.backBtn) {
            setUser(null);
            navController.popBackStack();
            return;
        }
        navController.navigate(R.id.nav_profile_edit, bundle, getNavOptions());
    }

    public void viewReviews(View view) {
        Intent intent = new Intent(this, UserReviewsActivity.class);
        intent.putExtra(UserReviewsActivity.USER_OBJECT_KEY, user.getUserObject().toString());
        startActivity(intent);
    }

    private void getUserData() {

        HttpRequest httpRequest = new HttpRequest(this, URLContract.PROFILE_INFO_REQUEST_URL,
                Request.Method.GET, new HttpRequestParams() {

            @Override
            public Map<String, String> getParams() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", user.getToken());
                params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                return params;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {

                setRefreshing(true);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
                setRefreshing(false);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) {

                        JSONObject userData = object.getJSONObject("response");

                        if (userData.has("user")) {

                            JSONObject userObject = userData.getJSONObject("user");

                            user.setFirstname(userObject.getString("firstname"));
                            user.setMiddlename(userObject.getString("middlename"));
                            user.setLastname(userObject.getString("lastname"));
                            user.setPhone(userObject.getString("phone"));
                            user.setEmail(userObject.getString("email"));
                            user.setBvn(userObject.getString("bvn"));
                            user.setPicture(userObject.getString("picture"));
                            user.setDob(userObject.getString("dob"));
                            user.setGender(Integer.parseInt(Utility.castNull(userObject.getString("gender"), "0")));
                            user.setAddress(userObject.getString("address"));
                            user.setCountry(userObject.getString("country"));
                            user.setState(userObject.getString("state"));
                            user.setStatus(userObject.getInt("status"));
                            JSONObject verified = userObject.getJSONObject("verified");
                            user.setVerifiedEmail(verified.getBoolean("email"));
                            user.setVerifiedPhone(verified.getBoolean("phone"));

                            if (user.update()) {
                                ProfileActivity.this.setUser();
                                List<Fragment> fragments = Utility.getActivityNavFragments(ProfileActivity.this);
                                for (Fragment fragment: fragments) {

                                    if (fragment instanceof ProfileOptionFragment) {
                                        ((ProfileOptionFragment) fragment).setUser(user);
                                    }

                                }
                                Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                            } else {
                                Utility.toastMessage(ProfileActivity.this, "Failed to update user info locally. Please try again later.");
                            }

                        } else Utility.toastMessage(ProfileActivity.this, "Failed to retrieve user info");

                    } else Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, statusCode == 503 ? error :
                            "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send();

    }

    private void updatePhoto(String imageString) {

        HttpRequest httpRequest = new HttpRequest(this,
                String.format(URLContract.PROFILE_UPDATE_REQUEST_URL, "picture"), Request.Method.POST,
                new HttpRequestParams() {
                    @Override
                    public Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("picture", imageString);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Auth-Token", dbHelper.getUser().getToken());
                        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                        return params;
                    }

                    @Override
                    public byte[] getBody() {
                        try {
                            return imageString.getBytes(StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }) {
            @Override
            protected void onRequestStarted() {
                progressToggle(true);
                imagePicker.setVisibility(View.GONE);
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                progressToggle(false);
                imagePicker.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject userData = object.getJSONObject("response");

                    if (userData.has("user")) {

                        JSONObject userObject = userData.getJSONObject("user");

                        user.setPicture(userObject.getString("picture"));

                        if (user.update()) {
                            setUser(user);
                            Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                        } else {
                            Utility.toastMessage(ProfileActivity.this, "Failed to update user picture locally. Please try again later.");
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(ProfileActivity.this);

                    dialog.setTitle(object.getString("title"));
                    if (object.has("errors")) {
                        JSONObject errors = object.getJSONObject("errors");
                        if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                        else dialog.setMessage(object.getString("message"));
                    } else dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok");
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

                progressToggle(false);
                imagePicker.setVisibility(View.VISIBLE);
            }
        };

        httpRequest.send();
    }

    public void rateUser(float rating) {

        if (user.isMe()) {
            ratingBar.setRating(Double.valueOf(user.getRating()).floatValue());
            Utility.toastMessage(this, "You can't rate yourself");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, URLContract.PROFILE_RATE_REQUEST_URL, Request.Method.POST,
                new HttpRequestParams() {
                    @Override
                    public Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("id", user.getUuid());
                        params.put("rate", String.valueOf(rating));
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Auth-Token", dbHelper.getUser().getToken());
                        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                        return params;
                    }

                    @Override
                    public byte[] getBody() {
                        return null;
                    }
                }) {
            @Override
            protected void onRequestStarted() {
            }

            @Override
            protected void onRequestCompleted(boolean onError) {
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    ratingBar.setRating(Double.valueOf(user.getRating()).floatValue());
                    Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
                }

            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
                ratingBar.setRating(Double.valueOf(user.getRating()).floatValue());
            }

            @Override
            protected void onRequestCancelled() {

            }
        };

        httpRequest.send();
    }

    public void showDialogMessage(String title, String message) {
        Utility.alertDialog(ProfileActivity.this, title, message, "Ok", Dialog::dismiss);
    }

    public void onBackPressed(View view) {
        onBackPressed();
    }
}

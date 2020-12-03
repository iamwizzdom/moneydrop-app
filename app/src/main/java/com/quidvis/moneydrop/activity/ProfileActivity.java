package com.quidvis.moneydrop.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.fragment.ProfileOptionFragment;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.utility.Utility.startRevealActivity;

public class ProfileActivity extends AppCompatActivity {

    private NavController navController;
    private ProgressBar uploadProgressBar;
    private DbHelper dbHelper;
    private User user;
    private ImageView imagePicker;
    private static File camImage;
    private CircleImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DbHelper(this);

        user = dbHelper.getUser();

        navController = getNavController();
        uploadProgressBar = findViewById(R.id.upload_photo_progress_bar);
        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(v -> selectImage());

        profilePic = findViewById(R.id.profile_pic);

        profilePic.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(ImagePreviewActivity.IMAGE_URL, user.getPictureUrl());
            startRevealActivity(this, v, ImagePreviewActivity.class, bundle);
        });

        setUser();
    }

    public void setUser() {

        TextView tvName = findViewById(R.id.account_name);
        TextView tvEmail = findViewById(R.id.account_email);

        Glide.with(ProfileActivity.this)
                .load(user.getPictureUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(user.getDefaultPicture())
                .into(profilePic);

        tvName.setText(String.format("%s %s", user.getFirstname(), user.getLastname()));
        tvEmail.setText(user.getEmail());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Utility.toastMessage(ProfileActivity.this, requestCode == 1 ? "Image capture failed." : "Image selection failed.");
            return;
        }

        if (requestCode == 1) {

            //Here you have the bitmap of the image from camera
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 7;
            Bitmap bitmap = BitmapFactory.decodeFile(camImage.getAbsolutePath(), options);

            Matrix matrix = new Matrix();
            matrix.postRotate(0);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            String imageString = imageToBase64(bitmap);
            camImage.delete();

            if (imageString != null && !imageString.isEmpty()) {
                updatePhoto(imageString);
            }

        } else if (requestCode == 2) {

            try {

                Uri uri = data.getData();
                if (uri == null) throw new IOException("Failed");
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                Matrix matrix = new Matrix();
                matrix.postRotate(0);
                bitmap = Utility.getResizedBitmap(bitmap, 7);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                String imageString = imageToBase64(bitmap);

                if (imageString != null && !imageString.isEmpty()) {
                    updatePhoto(imageString);
                }

            } catch (IOException e) {
                e.printStackTrace();
                showDialogMessage("Update failed", "Sorry an unexpected error occurred, please try again later.");
            }
        }
    }

    private String imageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
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

    protected NavOptions getNavOptions(boolean fromRight) {

        return new NavOptions.Builder()
                .setEnterAnim(fromRight ? R.anim.from_right : R.anim.from_left)
                .setExitAnim(fromRight ? R.anim.to_left : R.anim.to_right)
                .build();
    }

    public void loadEditFragment(View view) {
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
            setUser();
            navController.popBackStack();
            navController.navigate(R.id.nav_profile_option, null, getNavOptions(false));
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> navController.popBackStack(), 500);
            return;
        }
        navController.navigate(R.id.nav_profile_edit, bundle, getNavOptions(true));
    }

    private void updatePhoto(String imageString) {

        HttpRequest httpRequest = new HttpRequest(this, String.format("%s/%s",
                URLContract.PROFILE_UPDATE_REQUEST_URL, "picture"), Request.Method.POST,
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
                        params.put("Authorization", String.format("Bearer %s", dbHelper.getUser().getToken()));
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
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    JSONObject userData = object.getJSONObject("response");

                    if (userData.has("user")) {

                        JSONObject userObject = userData.getJSONObject("user");

                        User user = dbHelper.getUser();
                        user.setPicture(userObject.getString("picture"));

                        if (user.update()) {
                            setUser();
                            Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                        } else {
                            Utility.toastMessage(ProfileActivity.this, "Failed to update user picture locally. Please try again later.");
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
                }

                progressToggle(false);
                imagePicker.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(ProfileActivity.this);

                    dialog.setTitle(object.getString("title"));
                    JSONObject errors = object.getJSONObject("error");
                    dialog.setMessage(object.has("error") && errors.length() > 0 ? Utility.serializeObject(errors) : object.getString("message"));
                    dialog.setPositiveButton("Ok");
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(ProfileActivity.this, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                progressToggle(false);
                imagePicker.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onRequestCancelled() {

                progressToggle(false);
                imagePicker.setVisibility(View.VISIBLE);
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

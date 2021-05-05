package com.quidvis.moneydrop.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.barteksc.pdfviewer.PDFView;
import com.hbb20.CountryCodePicker;
import com.quidvis.moneydrop.BuildConfig;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.custom.CustomCompatActivity;
import com.quidvis.moneydrop.constant.Constant;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.database.DbHelper;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.model.BankStatement;
import com.quidvis.moneydrop.model.Country;
import com.quidvis.moneydrop.model.State;
import com.quidvis.moneydrop.model.User;
import com.quidvis.moneydrop.network.VolleyMultipartRequest;
import com.quidvis.moneydrop.network.VolleySingleton;
import com.quidvis.moneydrop.network.WebFileReader;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.CustomBottomAlertDialog;
import com.quidvis.moneydrop.utility.CustomBottomSheet;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;
import com.quidvis.moneydrop.utility.model.BottomSheetLayoutModel;
import com.quidvis.moneydrop.utility.view.DialogSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.quidvis.moneydrop.utility.Utility.startRevealActivity;

public class ProfileActivity extends CustomCompatActivity implements DatePickerDialog.OnDateSetListener {

    private SwipeRefreshLayout refreshLayout;
    private CircleImageView profilePic;
    private ProgressBar uploadProgressBar;
    private ImageView imagePicker, pdfPlaceholder;
    private RatingBar ratingBar;
    private TextView tvUsername, tvPhone, tvEmail,
            tvGender, tvDob, tvCountry, tvState, tvAddress, tvFileName,
            tvGenderError, tvDobError, tvCountryError, tvStateError, tvBankStatementError;
    private CircularProgressButton submitBtn;
    private ArrayList<Country> countries;
    private ArrayList<State> states;

    private PDFView pdfViewer;

    private DbHelper dbHelper;
    private User user;
    private Uri uri;
    private static File camImage;
    private byte[] inputData;
    private String fileName = "", mimeType = "";

    private final Calendar calendar = Calendar.getInstance();
    private final DateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", new java.util.Locale("en", "ng"));

    private final ArrayMap<String, Object> inputs = new ArrayMap<>();

    public static final String USER_OBJECT_KEY = "userObject";

    public final static int IMAGE_PICK_CAMERA = 123;
    public final static int IMAGE_PICK_GALLERY = 102;
    public final static int VERIFY_EMAIL_KEY = 132;
    public final static int VERIFY_PHONE_KEY = 142;
    public final static int SELECT_BANK_STATEMENT = 162;

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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        dbHelper = new DbHelper(this);

        if (user == null) user = dbHelper.getUser();

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        uploadProgressBar = findViewById(R.id.upload_photo_progress_bar);
        imagePicker = findViewById(R.id.image_picker);
        profilePic = findViewById(R.id.profile_pic);
        ratingBar = findViewById(R.id.ratingBar);
        LinearLayout phone_number = findViewById(R.id.phone_number);
        LinearLayout email = findViewById(R.id.email);
        LinearLayout dob = findViewById(R.id.dob);
        LinearLayout address = findViewById(R.id.address);
        LinearLayout state = findViewById(R.id.state);
        LinearLayout settings = findViewById(R.id.settings);

        tvUsername = findViewById(R.id.username_text);
        tvPhone = findViewById(R.id.phone_number_text);
        tvEmail = findViewById(R.id.email_text);
        tvGender = findViewById(R.id.gender_text);
        tvDob = findViewById(R.id.dob_text);
        tvCountry = findViewById(R.id.country_text);
        tvState = findViewById(R.id.state_text);
        tvAddress = findViewById(R.id.address_text);

        boolean isMe = user.isMe();

        if (isMe) imagePicker.setOnClickListener(v -> selectImage());
        else {
            imagePicker.setVisibility(View.GONE);
            phone_number.setVisibility(View.GONE);
            email.setVisibility(View.GONE);
            dob.setVisibility(View.GONE);
            address.setVisibility(View.GONE);
            state.setBackground(null);
            settings.setVisibility(View.GONE);
        }

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
        TextView tvAcctEmail = findViewById(R.id.account_email);

        Glide.with(ProfileActivity.this)
                .load(user.getPictureUrl())
                .placeholder(user.getDefaultPicture())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(user.getDefaultPicture())
                .into(profilePic);

        String name = String.format("%s %s", user.getFirstname(), user.getLastname());
        tvName.setText(name);
        tvAcctEmail.setText(user.getEmail());
        ratingBar.setRating(Double.valueOf(user.getRating()).floatValue());

        tvUsername.setText(name);
        tvPhone.setText(user.getPhone());
        tvEmail.setText(user.getEmail());
        tvGender.setText(Utility.convertGender(user.getGender(), "Unknown"));
        tvDob.setText(user.getDob());
        tvCountry.setText(Utility.castEmpty(user.getCountry() != null ? user.getCountry().getName() : null, "Unknown"));
        tvState.setText(Utility.castEmpty(user.getState() != null ? user.getState().getName() : null, "Unknown"));
        tvAddress.setText(Utility.castEmpty(user.getAddress(), "Unknown"));
    }

    private void selectImage() {
        imagePicker.setVisibility(View.GONE);

        CustomBottomSheet bottomSheet = CustomBottomSheet.newInstance(this);

        BottomSheetLayoutModel sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_take_photo, this);
        sheetLayoutModel.setText("Take Photo");
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            try {

                PackageManager packageManager = getPackageManager();
                boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);

                if (hasCamera) {
                    // start the image capture Intent
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(this,
                                    BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
                    startActivityForResult(intent, IMAGE_PICK_CAMERA);

                } else {
                    Utility.toastMessage(ProfileActivity.this, "This device seems not to have a camera");
                }

            } catch (Exception ex) {

                Utility.toastMessage(ProfileActivity.this, "There was an error with the camera.");
            }
            sheet.dismiss();
        });
        bottomSheet.addLayoutModel(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_gallery, this);
        sheetLayoutModel.setText("Choose from Gallery");
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_GALLERY);
            sheet.dismiss();
        });
        bottomSheet.addLayoutModel(sheetLayoutModel);

        sheetLayoutModel = new BottomSheetLayoutModel();
        sheetLayoutModel.setIconLeft(R.drawable.ic_delete_photo, this);
        sheetLayoutModel.setText("Remove Photo");
        sheetLayoutModel.setOnClickListener((sheet, v) -> {
            if (TextUtils.isEmpty(Utility.castEmpty(user.getPicture()))) {
                Utility.toastMessage(ProfileActivity.this, "You don't have a photo to remove.");
                sheet.dismiss();
                return;
            }
            CustomBottomAlertDialog alertDialog = new CustomBottomAlertDialog(ProfileActivity.this);
            alertDialog.setIcon(R.drawable.ic_remove);
            alertDialog.setMessage("Are you sure you want to remove your photo?");
            alertDialog.setNegativeButton("No, cancel");
            alertDialog.setPositiveButton("Yes, proceed", vw -> removePhoto());
            sheet.dismiss();
            alertDialog.display();
        });
        bottomSheet.addLayoutModel(sheetLayoutModel);

        bottomSheet.setOnDismissListener(() -> imagePicker.setVisibility(View.VISIBLE));

        bottomSheet.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        camImage = File.createTempFile("profile-photo", ".jpg", storageDir);
        return camImage;
    }

    public boolean isRefreshing() {
        return refreshLayout.isRefreshing();
    }

    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK_CAMERA:
                    Utility.toastMessage(ProfileActivity.this, "Image capture failed.");
                    if (camImage != null) camImage.delete();
                    break;
                case IMAGE_PICK_GALLERY:
                    Utility.toastMessage(ProfileActivity.this, "Image selection failed.");
                    break;
                case SELECT_BANK_STATEMENT:
                    Utility.toastMessage(ProfileActivity.this, "File selection failed.");
                    break;
                default:
                    Utility.toastMessage(ProfileActivity.this, "Failed to capture verification data.");
                    break;
            }
            return;
        }

        if (requestCode == IMAGE_PICK_CAMERA || requestCode == IMAGE_PICK_GALLERY) {

            if (requestCode == IMAGE_PICK_CAMERA) uri = Uri.fromFile(camImage);
            else uri = data.getData();

            try {

                if (uri == null) throw new IOException("Failed");

                Bitmap bitmap = Utility.getBitmapFormUri(this, uri);
                File file = requestCode == IMAGE_PICK_CAMERA ? camImage : Utility.getFileFromMediaUri(this, uri);
                int degree = Utility.getBitmapDegree(file.getAbsolutePath());
                bitmap = Utility.rotateBitmapByDegree(bitmap, degree);
                String imageString = imageToBase64(bitmap);

                if (imageString == null || imageString.isEmpty()) throw new IOException("Failed");

                if (requestCode == IMAGE_PICK_CAMERA && camImage != null) //noinspection ResultOfMethodCallIgnored
                    camImage.delete();

                updatePhoto(imageString);

            } catch (IOException e) {
                e.printStackTrace();
                Utility.toastMessage(this, "Image upload failed, please try again.");
            }

        } else if (requestCode == VERIFY_EMAIL_KEY || requestCode == VERIFY_PHONE_KEY) {

            boolean verified = data.getBooleanExtra(VerificationActivity.VERIFIED, false);

            if (verified) {

                Map<String, String> params = getUpdateParams();

                if (requestCode == VERIFY_EMAIL_KEY) user.setEmail(params.get("email"));
                else user.setPhone(params.get("phone"));

                if (user.update()) {
                    setUser();
                    Utility.toastMessage(ProfileActivity.this, String.format("%s updated successfully.", requestCode == VERIFY_EMAIL_KEY ? "Email address" : "Phone number"));
                } else Utility.toastMessage(ProfileActivity.this, "Failed to update user info locally. Please try again later.");

            } else Utility.toastMessage(ProfileActivity.this, String.format("%s verification failed.", requestCode == VERIFY_EMAIL_KEY ? "Email" : "Phone"));

        } else if (requestCode == SELECT_BANK_STATEMENT) {

            tvBankStatementError.setVisibility(View.GONE);

            uri = data.getData();
            assert uri != null;
            String uriString = uri.toString();

            if (uriString.startsWith("content://")) {
                ContentResolver cr = ProfileActivity.this.getContentResolver();
                try (Cursor cursor = cr.query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        mimeType = cr.getType(uri);
                    }
                }
            } else if (uriString.startsWith("file://")) {
                File myFile = new File(uriString);
                fileName = myFile.getName();
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uriString);
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }

            tvFileName.setText(fileName);

            try {
                InputStream iStream = ProfileActivity.this.getContentResolver().openInputStream(uri);
                inputData = Utility.toByteArray(iStream);
                pdfPlaceholder.setVisibility(View.GONE);
                pdfViewer.setVisibility(View.VISIBLE);
                renderPDF(inputData);
            } catch (Exception e) {
                inputData = null;
                e.printStackTrace();
            }

        }
    }

    private String imageToBase64(Bitmap bitmap) {
        return Base64.encodeToString(Utility.toByteArray(bitmap), Base64.DEFAULT);
    }

    private void progressToggle(boolean status) {
        uploadProgressBar.setVisibility(status ? View.VISIBLE : View.GONE);
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

                            user.setValues(userData.getJSONObject("user"));

                            if (user.update()) {
                                setUser();
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
                String.format(URLContract.PROFILE_UPDATE_REQUEST_URL, "picture"), Request.Method.PUT,
                new HttpRequestParams() {
                    @Override
                    public Map<String, String> getParams() {
                        return null;
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

    private void removePhoto() {

        HttpRequest httpRequest = new HttpRequest(this,
                String.format(URLContract.PROFILE_UPDATE_REQUEST_URL, "picture-remove"), Request.Method.PUT,
                new HttpRequestParams() {
                    @Override
                    public Map<String, String> getParams() {
                        return null;
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
                            Utility.toastMessage(ProfileActivity.this, "Failed to remove user picture locally. Please try again later.");
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

    public void editInfo(View view) {

        if (!user.isMe()) return;

        CustomBottomSheet bottomSheet = new CustomBottomSheet(this);
        String title;
        int layout;

        int id = view.getId();

        if (id == R.id.username) {

            title = "Update Name";
            layout = R.layout.profile_edit_name;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                EditText etFirstname = view1.findViewById(R.id.etFirstname);
                EditText etMiddlename = view1.findViewById(R.id.etMiddlename);
                EditText etLastname = view1.findViewById(R.id.etLastname);
                submitBtn = view1.findViewById(R.id.submit);
                etFirstname.setText(user.getFirstname());
                etMiddlename.setText(Utility.castEmpty(user.getMiddlename()));
                etLastname.setText(user.getLastname());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("firstname", etFirstname);
                inputs.put("middlename", etMiddlename);
                inputs.put("lastname", etLastname);
            });

        } else if (id == R.id.phone_number) {

            title = "Update Phone";
            layout = R.layout.profile_edit_phone;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                CountryCodePicker ccp = view1.findViewById(R.id.ccp);
                EditText etPhone = view1.findViewById(R.id.etPhone), tvOldPhone = new EditText(this);
                submitBtn = view1.findViewById(R.id.submit);
                ccp.registerCarrierNumberEditText(etPhone);
                ccp.setFullNumber(user.getPhone());
                tvOldPhone.setText(user.getPhone());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("phone", ccp);
                inputs.put("old_phone", tvOldPhone);
            });

        } else if (id == R.id.email) {

            title = "Update Email";
            layout = R.layout.profile_edit_email;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                EditText etEmail = view1.findViewById(R.id.etEmail), tvOldEmail = new EditText(this);
                submitBtn = view1.findViewById(R.id.submit);
                etEmail.setText(user.getEmail());
                tvOldEmail.setText(user.getEmail());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("email", etEmail);
                inputs.put("old_email", tvOldEmail);
            });

        } else if (id == R.id.edit_gender) {

            title = "Update Gender";
            layout = R.layout.profile_edit_gender;
            bottomSheet.setOnViewInflatedListener(view1 -> {

                LinearLayout maleGender = view1.findViewById(R.id.male_gender);
                LinearLayout femaleGender = view1.findViewById(R.id.female_gender);
                ImageView maleChecker = view1.findViewById(R.id.male_checker);
                ImageView femaleChecker = view1.findViewById(R.id.female_checker);
                submitBtn = view1.findViewById(R.id.submit);
                tvGenderError = view1.findViewById(R.id.tvGenderError);

                maleChecker.setVisibility(user.getGender() == Constant.MALE ? View.VISIBLE : View.GONE);
                femaleChecker.setVisibility(user.getGender() == Constant.FEMALE ? View.VISIBLE : View.GONE);

                EditText etGender = new EditText(this);
                etGender.setText(String.valueOf(user.getGender()));
                maleGender.setOnClickListener(v -> {
                    maleChecker.setVisibility(View.VISIBLE);
                    femaleChecker.setVisibility(View.GONE);
                    etGender.setText(String.valueOf(Constant.MALE));
                    tvGenderError.setVisibility(View.GONE);
                });
                femaleGender.setOnClickListener(v -> {
                    maleChecker.setVisibility(View.GONE);
                    femaleChecker.setVisibility(View.VISIBLE);
                    etGender.setText(String.valueOf(Constant.FEMALE));
                    tvGenderError.setVisibility(View.GONE);
                });
                inputs.put("gender", etGender);
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
            });

        } else if (id == R.id.dob) {

            title = "Update Date of Birth";
            layout = R.layout.profile_edit_dob;
            bottomSheet.setOnViewInflatedListener(view1 -> {

                EditText etDob = view1.findViewById(R.id.etDob);
                submitBtn = view1.findViewById(R.id.submit);
                tvDobError = view1.findViewById(R.id.tvDobError);
                etDob.setText(user.getDob());
                try {
                    calendar.setTime(Objects.requireNonNull(formatter.parse(user.getDob())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                etDob.setOnClickListener(v -> setDate());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("dob", etDob);
            });

        } else if (id == R.id.country) {

            title = "Update Country";
            layout = R.layout.profile_edit_country;
            bottomSheet.setOnViewInflatedListener(view1 -> {

                DialogSpinner dsCountries = view1.findViewById(R.id.country_spinner);
                tvCountryError = view1.findViewById(R.id.tvCountryError);
                submitBtn = view1.findViewById(R.id.submit);
                countries = dbHelper.getCountries();
                int size = countries.size(); int selectedCountry = 0;
                int userCountry = 0;
                if (user.getCountry() != null) userCountry = user.getCountry().getUid();

                String[] countryList = new String[size];
                for (int i = 0; i < size; i++) {
                    Country country = countries.get(i);
                    countryList[i] = country.getName();
                    if (country.getUid() == userCountry) selectedCountry = i;
                }

                dsCountries.setEntries(countryList);
                if (selectedCountry != 0) dsCountries.setSelectedItemPosition(selectedCountry);

                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("country", dsCountries);
            });

        } else if (id == R.id.state) {

            title = "Update State";
            layout = R.layout.profile_edit_state;
            bottomSheet.setOnViewInflatedListener(view1 -> {

                DialogSpinner dsState = view1.findViewById(R.id.state_spinner);
                tvStateError = view1.findViewById(R.id.tvStateError);
                submitBtn = view1.findViewById(R.id.submit);
                if (states == null) {
                    if (user.getCountry() == null) {
                        Utility.toastMessage(ProfileActivity.this, "Select a country first.");
                        bottomSheet.dismiss();
                        return;
                    }
                    states = dbHelper.getStates(user.getCountry());
                }

                int size = states.size(); int selectedState = 0;
                int userState = 0;
                if (user.getState() != null) userState = user.getState().getUid();

                String[] stateList = new String[size];
                for (int i = 0; i < size; i++) {
                    State state = states.get(i);
                    stateList[i] = state.getName();
                    if (state.getUid() == userState) selectedState = i;
                }

                dsState.setEntries(stateList);
                if (selectedState != 0) dsState.setSelectedItemPosition(selectedState);

                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("state", dsState);
            });

        } else if (id == R.id.address) {

            title = "Update Address";
            layout = R.layout.profile_edit_address;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                EditText etAddress = view1.findViewById(R.id.etAddress);
                submitBtn = view1.findViewById(R.id.submit);
                etAddress.setText(user.getAddress());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("address", etAddress);
            });

        } else if (id == R.id.bvn) {

            title = "Set BVN";
            layout = R.layout.profile_edit_bvn;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                EditText etBVN = view1.findViewById(R.id.etBVN);
                submitBtn = view1.findViewById(R.id.submit);
                etBVN.setText(user.getBvn());
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("bvn", etBVN);
            });

        } else if (id == R.id.bank_statement) {

            title = "Set Bank Statement";
            layout = R.layout.profile_edit_bank_statement;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                pdfPlaceholder = view1.findViewById(R.id.pdf_placeholder);
                pdfViewer = view1.findViewById(R.id.pdf_viewer);
                tvFileName = view1.findViewById(R.id.file_name);
                submitBtn = view1.findViewById(R.id.submit);
                TextView fileSelector = view1.findViewById(R.id.file_selector);
                tvBankStatementError = view1.findViewById(R.id.tvBankStatementError);
                BankStatement bankStatement = user.getBankStatement();
                if (bankStatement != null && !TextUtils.isEmpty(Utility.castEmpty(bankStatement.getFile()))) {
                    tvFileName.setText(bankStatement.getFileName());
                    WebFileReader fileReader = new WebFileReader(this, bankStatement.getFileUrl(), WebFileReader.Method.GET) {
                        @Override
                        protected void onReadStarted() {
                            Utility.toastMessage(ProfileActivity.this, "Reading file from server, please wait.");
                        }

                        @Override
                        protected void onReadCancelled() {

                            Utility.toastMessage(ProfileActivity.this, "Reading cancelled.");
                        }

                        @Override
                        protected void onReadCompleted(boolean onError) {

                            if (!onError) {
                                pdfPlaceholder.setVisibility(View.GONE);
                                pdfViewer.setVisibility(View.VISIBLE);
                                Utility.toastMessage(ProfileActivity.this, "Reading completed, now rendering.");
                            }
                        }

                        @Override
                        protected void onReadSuccess(String fileName, byte[] fileByte, int statusCode, Map<String, List<String>> headers) {
                            renderPDF(fileByte);
                        }

                        @Override
                        public void onReadError(String error, int statusCode, Map<String, List<String>> headers) {

                            Utility.toastMessage(ProfileActivity.this, error);
                        }
                    };
                    fileReader.send();
                }
                fileSelector.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    ProfileActivity.this.startActivityForResult(intent, SELECT_BANK_STATEMENT);
                });
                submitBtn.setOnClickListener(v -> updateBankStatement(bottomSheet, id));
            });

        } else if (id == R.id.change_password) {

            title = "Change Password";
            layout = R.layout.profile_edit_password;
            bottomSheet.setOnViewInflatedListener(view1 -> {
                EditText etCurrentPassword = view1.findViewById(R.id.etCurrentPassword);
                EditText etPassword = view1.findViewById(R.id.etPassword);
                EditText etConfirmPassword = view1.findViewById(R.id.etConfirmPassword);
                submitBtn = view1.findViewById(R.id.submit);
                submitBtn.setOnClickListener(v -> update(bottomSheet, id));
                inputs.put("current_password", etCurrentPassword);
                inputs.put("password", etPassword);
                inputs.put("password_confirmation", etConfirmPassword);
            });

        } else return;

        bottomSheet.setTitle(title);
        bottomSheet.setViewResource(layout);
        bottomSheet.show();
    }

    private Map<String, String> getUpdateParams() {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            String key = entry.getKey();
            Object input = entry.getValue();
            if (input instanceof EditText)
                params.put(key, ((EditText) input).getText().toString());
            else if (input instanceof CountryCodePicker)
                params.put(key, ((CountryCodePicker) input).getFullNumberWithPlus());
            else if (input instanceof DialogSpinner) {
                if (key.equals("country")) {
                    int country = this.countries.get(((DialogSpinner) input).getSelectedItemPosition()).getUid();
                    params.put(key, String.valueOf(country));
                } else {
                    int state = this.states.get(((DialogSpinner) input).getSelectedItemPosition()).getUid();
                    params.put(key, String.valueOf(state));
                }
            }
        }
        return params;
    }

    private String getUpdateUriType(int id) {
        if (id == R.id.username) return "name";
        else if (id == R.id.phone_number) return "phone";
        else if (id == R.id.email) return "email";
        else if (id == R.id.edit_gender) return "gender";
        else if (id == R.id.dob) return "dob";
        else if (id == R.id.country) return "country";
        else if (id == R.id.state) return "state";
        else if (id == R.id.address) return "address";
        else if (id == R.id.bvn) return "bvn";
        else if (id == R.id.bank_statement) return "bank-statement";
        else if (id == R.id.change_password) return "password";
        else return null;
    }

    private void updateBankStatement(CustomBottomSheet bottomSheet, int updateID) {

        tvBankStatementError.setVisibility(View.GONE);

        if (uri == null || inputData == null) {
            Utility.toastMessage(ProfileActivity.this, "Please select your bank statement");
            return;
        }

        if (!Validator.isNetworkConnected(ProfileActivity.this)) {
            Utility.toastMessage(ProfileActivity.this, "No network connection, check your connection and try again.");
            return;
        }

        submitBtn.startAnimation();

        String uriType = getUpdateUriType(updateID), url = String.format( URLContract.PROFILE_UPDATE_REQUEST_URL, uriType);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, response -> {

            submitBtn.revertAnimation();

            try {

                String responseData = response != null ? new String(response.data) : "";
                responseData = responseData.trim();

                JSONObject object = new JSONObject(responseData);

                if (object.getBoolean("status")) {

                    bottomSheet.dismiss();

                    JSONObject userData = object.getJSONObject("response");

                    if (userData.has("user")) {

                        JSONObject userObject = userData.getJSONObject("user");
                        user.setValues(userObject);

                        if (user.update()) {
                            setUser();
                            Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                        } else {
                            Utility.toastMessage(ProfileActivity.this, "Failed to update user info locally. Please try again later.");
                        }

                    } else {
                        Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                    }

                } else {
                    Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
            }

        }, error -> {

            submitBtn.revertAnimation();

            try {

                NetworkResponse response = error.networkResponse;

                String responseData = response != null ? new String(response.data) : "";
                responseData = responseData.trim();

                JSONObject object = new JSONObject(responseData);

                if (object.has("errors")) {

                    JSONObject errors = object.getJSONObject("errors");

                    if (errors.length() > 0) {
                        for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            String value = errors.getString(key);
                            if (TextUtils.isEmpty(value)) continue;
                            if (tvBankStatementError != null && key.equals("bank_statement")) {
                                tvBankStatementError.setText(value);
                                tvBankStatementError.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } else {
                    tvBankStatementError.setText(object.getString("message"));
                    tvBankStatementError.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Utility.toastMessage(ProfileActivity.this, "Something unexpected happened. Please try that again.");
            }

        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Auth-Token", user.getToken());
                params.put("Authorization", String.format("Basic %s", Base64.encodeToString(Constant.SERVER_CREDENTIAL.getBytes(), Base64.NO_WRAP)));
                return params;
            }

            @Override
            public Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("bank_statement", new DataPart(fileName, inputData, mimeType));
                return params;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(Constant.RETRY_IN_60_SEC, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        multipartRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance().addToRequestQueue(multipartRequest);
    }

    private void update(CustomBottomSheet bottomSheet, int updateID) {

        String uriType = getUpdateUriType(updateID), url = String.format( URLContract.PROFILE_UPDATE_REQUEST_URL, uriType);

        assert uriType != null;

        if (uriType.equals("email")) url = URLContract.VERIFY_EMAIL_REQUEST_URL;
        else if (uriType.equals("phone")) url = URLContract.VERIFY_PHONE_REQUEST_URL;

        Map<String, String> params = getUpdateParams();

        HttpRequest httpRequest = new HttpRequest(this, url,
                (uriType.equals("phone") || uriType.equals("email")) ? Request.Method.POST : Request.Method.PUT,
                new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                if (!uriType.equals("email") && !uriType.equals("phone")) params.put("Auth-Token", user.getToken());
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

                for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                    Object input = entry.getValue();
                    if (input instanceof DialogSpinner) {
                        Utility.disableDialogSpinner((DialogSpinner) input);
                    } else {
                        EditText editText = getEditText(input);
                        if (editText != null) {
                            Utility.clearFocus(editText, ProfileActivity.this);
                            Utility.disableEditText(editText);
                        }
                    }
                }

                if (tvDobError != null) tvDobError.setVisibility(View.GONE);
                else if (tvGenderError != null) tvGenderError.setVisibility(View.GONE);
                else if (tvCountryError != null) tvCountryError.setVisibility(View.GONE);
                else if (tvStateError != null) tvStateError.setVisibility(View.GONE);

                submitBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                    Object input = entry.getValue();
                    if (input instanceof DialogSpinner) {
                        Utility.enableDialogSpinner((DialogSpinner) input);
                    } else {
                        EditText editText = getEditText(input);
                        if (editText != null) Utility.enableEditText(editText);
                    }
                }
                submitBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(response);

                    if (object.getBoolean("status")) bottomSheet.dismiss();

                    if (uriType.equals("email") || uriType.equals("phone")) {

                        if (object.getBoolean("status")) {

                            Intent intent = new Intent(ProfileActivity.this, VerificationActivity.class);
                            intent.putExtra(VerificationActivity.VERIFICATION_DATA, params.get(uriType));
                            intent.putExtra(VerificationActivity.VERIFICATION_TYPE, uriType);
                            intent.putExtra(VerificationActivity.COUNT_DOWN_TIME, object.getInt("expire"));
                            intent.putExtra(VerificationActivity.OLD_DATA, uriType.equals("email") ? user.getEmail() : user.getPhone());
                            startActivityForResult(intent, uriType.equals("email") ? VERIFY_EMAIL_KEY : VERIFY_PHONE_KEY);
                            Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                        } else {
                            AwesomeAlertDialog dialog = new AwesomeAlertDialog(ProfileActivity.this);
                            dialog.setTitle(object.getString("title"));
                            dialog.setMessage(object.getString("message"));
                            dialog.setPositiveButton("Ok", Dialog::dismiss);
                            dialog.display();
                        }

                    } else {

                        JSONObject userData = object.getJSONObject("response");

                        if (userData.has("user")) {

                            JSONObject userObject = userData.getJSONObject("user");
                            user.setValues(userObject);

                            if (user.update()) {
                                setUser();
                                Utility.toastMessage(ProfileActivity.this, object.getString("message"));
                            } else {
                                Utility.toastMessage(ProfileActivity.this, "Failed to update user info locally. Please try again later.");
                            }

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

                    if ((uriType.equals("email") || uriType.equals("phone")) && statusCode == HttpURLConnection.HTTP_CONFLICT) {

                        Utility.toastMessage(ProfileActivity.this, object.getString("message"));

                    } else {

                        dialog.setTitle(object.getString("title"));
                        dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok");
                        dialog.display();

                        if (object.has("errors")) {

                            JSONObject errors = object.getJSONObject("errors");

                            if (errors.length() > 0) {
                                for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                                    String key = it.next();
                                    String value = errors.getString(key);
                                    if (TextUtils.isEmpty(value)) continue;
                                    EditText editText = getEditText(inputs.get(key));
                                    if (tvDobError != null && key.equals("dob")) {
                                        tvDobError.setText(value);
                                        tvDobError.setVisibility(View.VISIBLE);
                                    } else if (tvGenderError != null && key.equals("gender")) {
                                        tvGenderError.setText(value);
                                        tvGenderError.setVisibility(View.VISIBLE);
                                    } else if (tvCountryError != null && key.equals("country")) {
                                        tvCountryError.setText(value);
                                        tvCountryError.setVisibility(View.VISIBLE);
                                    } else if (tvStateError != null && key.equals("state")) {
                                        tvStateError.setText(value);
                                        tvStateError.setVisibility(View.VISIBLE);
                                    } else if (editText != null) editText.setError(value);
                                }
                            }
                        }
                    }
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

    private void renderPDF(byte[] resultByte) {
        pdfViewer.fromBytes(resultByte)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .onRender((pages, pageWidth, pageHeight) -> {
                    pdfViewer.fitToWidth(Math.round(pageWidth / pages));
                    pdfViewer.setMinZoom(pageWidth / pages);
                    pdfViewer.setMidZoom(Math.round((pageWidth + (pageWidth / 4))));
                    pdfViewer.setMaxZoom(Math.round((pageWidth + (pageWidth / 3))));
                    pdfViewer.enableDoubletap(true);
                })
                .load();
    }

    private void setDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                ProfileActivity.this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(year, month, dayOfMonth);
        Objects.requireNonNull(getEditText(inputs.get("dob"))).setText(formatter.format(calendar.getTime()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearFocus();
    }

    private void clearFocus() {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            EditText editText = getEditText(entry.getValue());
            if (editText != null && editText.hasFocus()) Utility.clearFocus(editText, ProfileActivity.this);
        }
    }

    private EditText getEditText(Object object) {
        if (object instanceof CountryCodePicker) object = Utility.getClassField(object, "editText_registeredCarrierNumber");
        if (object instanceof EditText) return ((EditText) object);
        return null;
    }
}

package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Request;
import com.chaos.view.PinView;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.RegistrationActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.utility.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerificationOTPFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerificationOTPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationOTPFragment extends Fragment {

    private Activity activity;
    private int countDownTime = 0;
    private String email;
    private TextView tvResend;
    private View.OnClickListener resendCodeListener;
    private CountDownTimer countDownTimer;
    private PinView pvOTP;
    private CircularProgressButton verifyBtn;

    static final String COUNT_DOWN_TIME = "countDownTime", EMAIL = "email";

    private static final String FORMAT = "Resend code in %s:%s";

    public VerificationOTPFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VerificationOTPFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VerificationOTPFragment newInstance() {
        return new VerificationOTPFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            email = bundle.getString(EMAIL);
            countDownTime = bundle.getInt(COUNT_DOWN_TIME, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        tvResend = view.findViewById(R.id.resend_code);
        TextView verificationNote = view.findViewById(R.id.verification_note);
        String text = getResources().getString(R.string.verification_note) + " at " + email;
        verificationNote.setText(text);

        pvOTP = view.findViewById(R.id.verification_otp);

        verifyBtn = view.findViewById(R.id.verifyBtn);

        resendCodeListener = v -> requestVerification();

        countDown();

        verifyBtn.setOnClickListener(v -> verifyOTP());

        pvOTP.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                verifyOTP();
                return true;
            }
            return false;
        });
    }

    private void countDown() {

        countDownTimer = new CountDownTimer(countDownTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String time = String.format(FORMAT, minutes > 9 ? minutes : "0" + minutes, seconds > 9 ? seconds : "0" + seconds);
                tvResend.setText(time);
                tvResend.setTextColor(getResources().getColor(R.color.titleColorGray));
                tvResend.setOnClickListener(null);
            }

            @Override
            public void onFinish() {
                tvResend.setText(R.string.resend_code);
                tvResend.setTextColor(getResources().getColor(R.color.colorAccent));
                tvResend.setOnClickListener(resendCodeListener);
            }
        };

        countDownTimer.start();
    }


    private void requestVerification() {

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(activity, "Please enter a valid email address");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(URLContract.VERIFY_EMAIL_REQUEST_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                if (countDownTimer != null) countDownTimer.cancel();
                tvResend.setText(getResources().getString(R.string.resending));
                tvResend.setOnClickListener(null);
                tvResend.setTextColor(getResources().getColor(R.color.titleColor));
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    Utility.toastMessage(activity, object.getString("message"));
                    email = object.getString("email");
                    countDownTime = object.getInt("expire");
                    countDown();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(activity);

                    dialog.setTitle(object.getString("title"));

                    JSONObject errors = object.getJSONObject("error");

                    if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                    else dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok");
                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(activity);
    }


    private void verifyOTP() {

        final String otp = Objects.requireNonNull(pvOTP.getText()).toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(activity, "Please enter a valid email address");
            return;
        }

        if (otp.length() < 4) {
            Utility.toastMessage(activity, "Please enter a complete OTP");
            return;
        }

        HttpRequest httpRequest = new HttpRequest(URLContract.VERIFY_EMAIL_URL, Request.Method.POST, new HttpRequestParams() {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("code", otp);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        }) {
            @Override
            protected void onRequestStarted() {
                Utility.disableEditText(pvOTP);
                Utility.clearFocus(pvOTP, activity);
                verifyBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);
                    Utility.toastMessage(activity, object.getString("message"));
                    if (countDownTimer != null) countDownTimer.cancel();
                    tvResend.setVisibility(View.GONE);
                    tvResend.setOnClickListener(null);

                    final Intent intent = new Intent(activity, RegistrationActivity.class);
                    intent.putExtra(RegistrationActivity.EMAIL, email);
                    startActivity(intent);
                    activity.finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }
                verifyBtn.revertAnimation();
                Utility.enableEditText(pvOTP);
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                try {

                    JSONObject object = new JSONObject(error);
                    AwesomeAlertDialog dialog = new AwesomeAlertDialog(activity);

                    dialog.setTitle(object.getString("title"));

                    JSONObject errors = object.getJSONObject("error");

                    if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                    else dialog.setMessage(object.getString("message"));

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Ok", dialog1 -> {
                            dialog1.dismiss();
                            final Intent intent = new Intent(activity, RegistrationActivity.class);
                            intent.putExtra(RegistrationActivity.EMAIL, email);
                            startActivity(intent);
                            activity.finish();
                        });
                    } else dialog.setPositiveButton("Ok");

                    dialog.display();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }

                verifyBtn.revertAnimation();
                Utility.enableEditText(pvOTP);
            }

            @Override
            protected void onRequestCancelled() {

            }
        };
        httpRequest.send(activity);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    @Override
    public void onStart() {
        super.onStart();
        Utility.requestFocus(pvOTP, activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.clearFocus(pvOTP, activity);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

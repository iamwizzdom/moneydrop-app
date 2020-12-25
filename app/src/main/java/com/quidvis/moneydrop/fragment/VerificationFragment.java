package com.quidvis.moneydrop.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.android.volley.Request;
import com.quidvis.moneydrop.R;
import com.quidvis.moneydrop.activity.RegistrationActivity;
import com.quidvis.moneydrop.activity.VerificationActivity;
import com.quidvis.moneydrop.constant.URLContract;
import com.quidvis.moneydrop.interfaces.HttpRequestParams;
import com.quidvis.moneydrop.utility.AwesomeAlertDialog;
import com.quidvis.moneydrop.network.HttpRequest;
import com.quidvis.moneydrop.utility.Utility;
import com.quidvis.moneydrop.utility.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationFragment extends Fragment {

    private Activity activity;
    private AwesomeAlertDialog dialog;
    private EditText etEmail;
    private CircularProgressButton verifyBtn;

    public VerificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VerificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VerificationFragment newInstance() {
        return new VerificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        dialog = new AwesomeAlertDialog(activity);

        etEmail = view.findViewById(R.id.etEmail);

        verifyBtn = view.findViewById(R.id.verifyBtn);

        etEmail.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                requestVerification();
                return true;
            }
            return false;
        });

        verifyBtn.setOnClickListener(v -> requestVerification());
    }

    private void requestVerification() {


        final String email = etEmail.getText().toString();

        if (!Validator.isValidEmail(email)) {
            Utility.toastMessage(activity, "Please enter a valid email address");
            return;
        }

        HttpRequest httpRequest = new HttpRequest((AppCompatActivity) activity, URLContract.VERIFY_EMAIL_REQUEST_URL, Request.Method.POST, new HttpRequestParams() {
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
                Utility.disableEditText(etEmail);
                Utility.clearFocus(etEmail, activity);
                verifyBtn.startAnimation();
            }

            @Override
            protected void onRequestCompleted(boolean onError) {

                verifyBtn.revertAnimation();
            }

            @Override
            protected void onRequestSuccess(String response, int statusCode, Map<String, String> headers) {
                try {

                    JSONObject object = new JSONObject(response);

                    dialog.setTitle(object.getString("title"));
                    dialog.setMessage(object.getString("message"));
                    dialog.setPositiveButton("Ok", Dialog::dismiss);
                    dialog.display();
                    etEmail.setText("");
                    Bundle bundle = new Bundle();
                    bundle.putString(VerificationOTPFragment.EMAIL, object.getString("email"));
                    bundle.putInt(VerificationOTPFragment.COUNT_DOWN_TIME, object.getInt("expire"));
                    ((VerificationActivity) activity).loadFragment(VerificationActivity.TAG_OTP, bundle);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.enableEditText(etEmail);
                    Utility.toastMessage(activity, "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestError(String error, int statusCode, Map<String, String> headers) {

                Utility.enableEditText(etEmail);
                try {

                    JSONObject object = new JSONObject(error);

                    if (statusCode == HttpURLConnection.HTTP_CONFLICT) {

                        Utility.toastMessage(activity, object.getString("message"));
                        final Intent intent = new Intent(activity, RegistrationActivity.class);
                        intent.putExtra(RegistrationActivity.EMAIL, email);
                        startActivity(intent);
                        activity.finish();

                    } else {

                        dialog.setTitle(object.getString("title"));
                        JSONObject errors = object.getJSONObject("error");
                        if (errors.length() > 0) dialog.setMessage(Utility.serializeObject(errors));
                        else dialog.setMessage(object.getString("message"));
                        dialog.setPositiveButton("Ok");
                        dialog.display();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.toastMessage(activity, statusCode == 503 ? error :
                                    "Something unexpected happened. Please try that again.");
                }
            }

            @Override
            protected void onRequestCancelled() {

            }

        };
        httpRequest.send();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Utility.requestFocus(etEmail, activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.clearFocus(etEmail, activity);
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

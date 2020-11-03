package com.quidvis.moneydrop.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chaos.view.PinView;
import com.quidvis.moneydrop.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;

import static com.quidvis.moneydrop.constant.Constant.FEMALE;
import static com.quidvis.moneydrop.constant.Constant.MALE;


/**
 * Created by Wisdom Emenike.
 * Date: 9/25/2018
 * Time: 11:11 AM
 */

@SuppressWarnings("ALL")
public class Utility {

    private static boolean permitNet = true;

    /**
     *
     * @param string
     * @return
     */
    public static String ucFirst(String string) {
        string = string.trim();
        if (string.length() < 2) return string;
        return (string.substring(0, 1).toUpperCase() +
                string.substring(1, string.length()));
    }

    /**
     *
     * @param string
     * @return
     */
    public static String lcFirst(String string) {
        string = string.trim();
        if (string.length() < 2) return string;
        return (string.substring(0, 1).toLowerCase() +
                string.substring(1, string.length()));
    }

    /**
     *
     * @param string
     * @return
     */
    public static String ucWord(String string) {
        String s = string;
        s = s.trim();
        String[] _string = s.split(" ");
        StringBuilder str = new StringBuilder();
        for (String a_string : _string) {
            str.append((str.length() == 0) ? "" : " ").append(Utility.ucFirst(a_string));
        }
        return str.toString();
    }

    /**
     *
     * @param string
     * @return
     */
    public static String stripSpaces(String string) {
        return string.replaceAll(" ", "");
    }

    /**
     *
     * @param jsonArray
     * @return
     */
    public static JSONArray arrayShuffle(JSONArray jsonArray) {
        int length = jsonArray.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                int rand = (int)(Math.random() * (length - 1) + 1);
                try {
                    String value = jsonArray.getString(i);
                    String randValue = jsonArray.getString(rand);
                    jsonArray.put(i, randValue);
                    jsonArray.put(rand, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonArray;
    }

    /**
     *
     * @param jsonArray
     * @return
     */
    public static int[] arrayShuffle(int[] intArray) {
        int length = intArray.length;
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                int rand = (int)(Math.random() * (length - 1) + 1);
                int value = intArray[i];
                int randValue = intArray[rand];
                intArray[i] = randValue;
                intArray[rand] = value;
            }
        }
        return intArray;
    }

    /**
     *
     * @param array
     * @param variable
     * @return
     */
    public static boolean inArray(JSONArray array, String variable) {
        int size = array.length(), i;
        if (size > 0) {
            for (i = 0; i < size; i++) {
                try {
                    if (Objects.equals(array.get(i).toString(), variable))
                        return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     *
     * @param phone
     * @return
     */
    public static String filterPhone(String phone) {
        return phone.replaceAll("[^+0-9]", "");
    }

    /**
     *
     * @param phone
     * @param prefix
     * @return
     */
    public static String formatPhone(String phone, String prefix) {
        phone = filterPhone(phone);
        if (phone.substring(0, 1).equals("+")) {
            return phone;
        } else if (phone.substring(0, prefix.length()).equals(prefix)) {
            return "+" + phone;
        }
        return "+" + prefix + phone.substring(1, phone.length());
    }

    /**
     *
     * @param phone
     * @return
     */
    public static String filterName(String phone) {
        return phone.replaceAll("[^_0-9A-Z a-z]", "");
    }

    /**
     *
     * @param object
     * @return
     */
    public static String serializeObject(JSONObject object) {
        String message = ""; int count = 0;
        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
            String key = it.next();
            try {
                String value = object.getString(key);
                if (TextUtils.isEmpty(value)) continue;
                message += (!TextUtils.isEmpty(message) ? "\n" : "") + (++count + ". " + value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    /**
     *
     * @param editText
     * @param activity
     */
    public static void clearFocus(EditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        editText.clearFocus();
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     *
     * @param editText
     * @param activity
     */
    public static void requestFocus(EditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    public static void enableEditText(@NonNull final EditText editText) {
        enableEditText(editText, Color.BLACK);
    }

    public static void enableEditText(@NonNull final EditText editText, int color) {
        editText.setEnabled(true);
        editText.setTextColor(color);
    }

    public static void disableEditText(@NonNull EditText editText) {
        disableEditText(editText, Color.GRAY);
    }

    public static void disableEditText(@NonNull EditText editText, int color) {
        editText.setEnabled(false);
        editText.setTextColor(color);
    }

    /**
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     *
     * @param image
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = 600;
            height = (int) (width / bitmapRatio);
        } else {
            height = 600;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     *
     * @param src
     * @return
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return getResizedBitmap(BitmapFactory.decodeStream(input));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class InputStreamData {
        private boolean successful;
        private String fileName, error;
        private byte[] fileByte;

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public byte[] getFileByte() {
            return fileByte;
        }

        public void setFileByte(byte[] fileByte) {
            this.fileByte = fileByte;
        }
    }

    /**
     *
     * @param bytes
     * @return
     */
    public static Bitmap toBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes == null ? 0 : bytes.length);
    }

    /**
     *
     * @param bitmap
     * @return
     */
    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);
        return stream.toByteArray();
    }

    /**
     *
     * @param bitmap
     * @return
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return toByteArray(inputStream, (1024 * 2));
    }

    /**
     *
     * @param bitmap
     * @return
     */
    public static byte[] toByteArray(InputStream inputStream, int maxSize) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[maxSize];
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            os.write(buffer, 0, n);
        }
        return os.toByteArray();
    }


    /**
     *
     * @param gender
     * @return
     */
    public static String convertGender(int gender) {
        return convertGender(gender, "Unknown gender");
    }

    /**
     *
     * @param gender
     * @param defaultValue
     * @return
     */
    public static String convertGender(int gender, String defaultValue) {
        return (gender == MALE ? "Male" : (gender == FEMALE ? "Female" : defaultValue));
    }

    /**
     *
     * @param string
     * @return
     */
    public static String isNull(String string) {
        return isNull(string, "");
    }

    /**
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static String isNull(String value, String defaultValue) {
        return (value == null || value.equalsIgnoreCase("null") ? defaultValue : value);
    }

    /**
     *
     * @param string
     * @return
     */
    public static String isEmpty(String string) {
        return isEmpty(string, "");
    }

    /**
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static String isEmpty(String value, String defaultValue) {
        return (TextUtils.isEmpty(value = isNull(value)) ? defaultValue : value);
    }


    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}

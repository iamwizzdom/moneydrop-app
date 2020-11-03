package com.quidvis.moneydrop.utility;

import com.quidvis.moneydrop.BuildConfig;

/**
 * Created by Wisdom Emenike.
 * Date: 9/16/2019
 * Time: 7:00 PM
 */
public class Version {
    public static boolean isOldVersion(String version) {
        String[] currentVersionArray = BuildConfig.VERSION_NAME.split("[.]");
        String[] serverVersionArray = version.split("[.]");
        for (int i = 0; i < serverVersionArray.length; i++) {
            if (currentVersionArray.length > i) {
                String token = serverVersionArray[i];
                if (Integer.valueOf(token) > Integer.valueOf(currentVersionArray[i]))
                    return true;
            } else return true;
        }
        return false;
    }
}

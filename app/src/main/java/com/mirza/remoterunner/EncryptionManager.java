package com.mirza.remoterunner;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptionManager {
    private static final String PREF_FILENAME = "secret_prefs";

    public static String encrypt(Context context, String plainText) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                masterKeyAlias, PREF_FILENAME, context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("secret", plainText);
        editor.apply();
        String encryptedText = sharedPreferences.getString("secret", null);
        editor.remove("secret");
        editor.apply();

        return encryptedText;
    }

    public static String decrypt(Context context, String encryptedText) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                masterKeyAlias,
                PREF_FILENAME,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("secret", encryptedText);
        editor.apply();
        String decryptedText = sharedPreferences.getString("secret", null);
        editor.remove("secret");
        editor.apply();
        return decryptedText;
    }
}

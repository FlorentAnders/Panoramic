/*
 * Copyright (C) 2017 Florent Anders
 * You are not allowed to use this code without contacting
 * service@madebyfa.de and asking for the permission to use it.
 */

package com.fa.donation;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.fa.mastodon.util.PublicValues;
import com.keylesspalace.tusky.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private TextView descriptionText;

    private Button buttonBasic;
    private Button buttonStandard;
    private Button buttonMedium;
    private Button buttonLarge;
    private Button buttonXL;

    private static final String PLAY_STORE_APP_ID = "com.android.vending";

    private BillingProcessor bp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        bp = new BillingProcessor(this, PublicValues.LICENSE_KEY, this);

        bp.loadOwnedPurchasesFromGoogle();

        descriptionText = (TextView) findViewById(R.id.textView);

        // Set on click listeners to fire Play when interested in donating
        buttonStandard = (Button) findViewById(R.id.buttonStandard);
        buttonStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this, "donation_standard");
            }
        });
        buttonMedium = (Button) findViewById(R.id.buttonMedium);
        buttonMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this, "donation_medium");
            }
        });
        buttonLarge = (Button) findViewById(R.id.buttonLarge);
        buttonLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this, "donation_large");
            }
        });
        buttonXL = (Button) findViewById(R.id.buttonXL);
        buttonXL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this, "donation_xl");
            }
        });
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if(productId.equals("panoramic_plus")){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("haspaidplus", true).apply();
        } else if(productId.equals("donation_basic")){
            bp.consumePurchase("donation_basic");
        } else if(productId.equals("donation_standard")){
            bp.consumePurchase("donation_standard");
        } else if(productId.equals("donation_medium")){
            bp.consumePurchase("donation_medium");
        } else if(productId.equals("donation_large")){
            bp.consumePurchase("donation_large");
        } else if(productId.equals("donation_xl")){
            bp.consumePurchase("donation_xl");
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if(bp.listOwnedProducts().contains("panoramic_plus")){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("haspaidplus", true).apply();
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    public static boolean checkSignatures(String signature, Activity activity){
        // Checks whether the donation app's signature matches the app's signature
        PackageManager manager = activity.getPackageManager();
        if (manager.checkSignatures(activity.getPackageName(), signature) == PackageManager.SIGNATURE_MATCH) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean verifyInstaller(String signature, final Activity activity) {
        // Verifies whether the app was installed from com.android.vending aka the Play Store
        final String installer = activity.getPackageManager().getInstallerPackageName(signature);
        if (installer  != null && installer.equals(PLAY_STORE_APP_ID)){
            return true;
        } else {
            return false;
        }
    }

    public static boolean getUsernameIncluded(String userName, Activity activity){
        // If the package wasn't installed from Google Play, the Android Device ID might match
        // one of those. We'll check whether this is the case or not.
        return false;
    }

    public static String getUsername(Context activity) {
        // Returns a String that gets the Android Device ID
        return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static Boolean isPlus(Activity activity) {
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("haspaidplus", false)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Query the signature for this application to detect whether it matches the
     * signature of the real developer. If it doesn't the app must have been
     * resigned, which indicates it may been tampered with.
     *
     * @param context
     * @return true if the app's signature matches the expected signature.
     */
    public static boolean validateAppSignature(Context context) throws PackageManager.NameNotFoundException {

        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), PackageManager.GET_SIGNATURES);
        //note sample just checks the first signature
        for (Signature signature : packageInfo.signatures) {
            // SHA1 the signature
            String sha1 = getSHA1(signature.toByteArray());
            // check is matches hardcoded value
            Toast.makeText(context, sha1, Toast.LENGTH_LONG).show();
            return context.getPackageName().equals(sha1);
        }

        return false;
    }

    //computed the sha1 hash of the signature
    public static String getSHA1(byte[] sig) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        digest.update(sig);
        byte[] hashtext = digest.digest();
        return bytesToHex(hashtext);
    }

    //util method to convert byte array to hex string
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

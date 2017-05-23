/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.fa.mastodon.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.fa.donation.DonateActivity;
import com.fa.mastodon.util.PublicValues;
import com.keylesspalace.tusky.R;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

public class PreferencesFragment extends PreferenceFragment implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        bp = new BillingProcessor(getActivity(), PublicValues.LICENSE_KEY, this);

        bp.loadOwnedPurchasesFromGoogle();

        findPreference("remads").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                bp.purchase(getActivity(), "panoramic_plus");
                return false;
            }
        });

        findPreference("navigationcolor").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(!DonateActivity.isPlus(getActivity())){
                    SnackbarManager.show(
                            Snackbar.with(getActivity().getApplicationContext()) // context
                                    .type(SnackbarType.MULTI_LINE)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                    .color(Color.WHITE) // change the background color
                                    .actionColor(Color.BLACK)
                                    .textColor(Color.BLACK)
                                    .text(getString(R.string.plus_intro)) // text to display
                                    .actionLabel(getString(R.string.plus_get)) // action button label
                                    .actionListener(new ActionClickListener() {
                                        @Override
                                        public void onActionClicked(Snackbar snackbar) {
                                            bp.purchase(getActivity(), "panoramic_plus");
                                        }
                                    })
                            // action button's ActionClickListener
                            , getActivity());
                }
                return false;
            }
        });
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if(productId.equals("panoramic_plus")){
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("haspaidplus", true).apply();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if(bp.listOwnedProducts().contains("panoramic_plus")){
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("haspaidplus", true).apply();
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
}

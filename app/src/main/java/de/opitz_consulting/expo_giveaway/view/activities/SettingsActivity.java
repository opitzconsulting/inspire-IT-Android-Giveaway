/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.opitz_consulting.expo_giveaway.view.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import de.opitz_consulting.expo_giveaway.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    //Preferencefragment holding the view
    public static class MyPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
    {

        EditTextPreference beaconPref;
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            beaconPref = (EditTextPreference) findPreference(getString(R.string.pref_beacon));

            beaconPref.setOnPreferenceChangeListener(this);
        }


        // Preference.OnPreferenceChangeListener implementation
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            //just validate textfields
            if (!(preference instanceof EditTextPreference)) {
                return true;
            }

            //handle different inputfields
            String key = preference.getKey();

            //BeaconId field
            if(key.equals(getString(R.string.pref_beacon))){
                return validateBeacon(Integer.parseInt(newValue.toString()));
            }else{
                //allow changes from fields that are not handled here
                return true;
            }
        }

        //validates a beacon id (must be between 0 and 65535)
        private boolean validateBeacon(int beaconId) {
            if (beaconId <= 65535 && beaconId >= 0) {
                return true;
            } else {
                Toast.makeText(getActivity(), getString(R.string.invalidBeaconId), Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }
}

package de.opitz_consulting.expo_giveaway.view.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;


import java.util.Collection;

import de.opitz_consulting.expo_giveaway.R;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier{

    private final static int REQUEST_ENABLE_BT = 1001;

    private TextView counter;
    private TextView counterText;
    private TextView distance;
    private TextView beacon;
    private TextView status;
    private TextView waitTime;

    private AlertDialog infoDialog;
    private SharedPreferences sharedPref;

    private BeaconManager beaconManager;
    private Region region;
    private long timeToWait;
    private long lastScanTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBluetoothOn();

        //set preferences to default at first start
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        sharedPref  = PreferenceManager.getDefaultSharedPreferences(this);

        counter = (TextView) findViewById(R.id.tv_number);
        counterText = (TextView) findViewById(R.id.tv_counter);
        distance = (TextView) findViewById(R.id.tv_distance);
        beacon = (TextView) findViewById(R.id.tv_beacon);
        status = (TextView) findViewById(R.id.tv_status);
        waitTime = (TextView) findViewById(R.id.tv_waittime);

        initBeaconManager();
    }

    private void initBeaconManager() {
        //initialize BeaconManager with IBeaconParser and Scan Duration
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                //Layout for iBeacons
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        //Set background scan duration to 1.1s
        beaconManager.setBackgroundScanPeriod(1100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBluetoothOn();

        loadSettings();
        reloadCounter();

        //reset beaconmanager to reload region
        if(beaconManager.isBound(this)){
            beaconManager.unbind(this);
        }
        beaconManager.bind(this);
        beaconManager.setBackgroundMode(false);
    }

    private void loadSettings(){
        //load texts
        counterText.setText(sharedPref.getString(getString(R.string.pref_name), ""));

        //load region to scan
        int beaconId = Integer.parseInt(sharedPref.getString(getString(R.string.pref_beacon), getString(R.string.pref_beacon_default)));
        region = new Region("beacon", null, null, Identifier.fromInt(beaconId));

        // load and set background interval time
        long backgroundInterval = Long.parseLong(sharedPref.getString(getString(R.string.pref_background_interval), getString(R.string.pref_background_interval_default)))*1000;
        long foregroundInterval = Long.parseLong(sharedPref.getString(getString(R.string.pref_foreground_interval), getString(R.string.pref_foreground_interval_default))) * 1000;

        beaconManager.setForegroundBetweenScanPeriod(foregroundInterval);
        beaconManager.setBackgroundBetweenScanPeriod(backgroundInterval);
        beacon.setText(String.valueOf(beaconId));
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(infoDialog != null && infoDialog.isShowing()){
            infoDialog.dismiss();
        }
        // if background_scan is disabled the beacon manager gets unbound
        // else it will be set to background mode
        boolean backgroundScan = sharedPref.getBoolean(getString(R.string.pref_background_scan), false);
        if(!backgroundScan){
            beaconManager.unbind(this);
        }
        else{
            beaconManager.setBackgroundMode(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        else if(id == R.id.about){
            displayLicensesDialog();
        }
        else if(id == R.id.help){
            displayHelpDialog();
        }
        else if(id == R.id.disclaimer){
            displayDisclaimerDialog();
        }


        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(beacons.size() != 1) {
            handleBeaconNotFound();
        }else{
            Beacon beacon = beacons.iterator().next();
            double maxDistance = Double.parseDouble(sharedPref.getString(getString(R.string.pref_min_distance),
                    getString(R.string.pref_min_distance_default)));
            String statusText;

            if(beacon.getDistance() <= maxDistance){
                handleBeaconInRange();
                statusText = getString(R.string.inRange);
            }else{
                statusText = getString(R.string.notInRange);
            }
            updateBeaconInView(getDistanceValue(beacon), statusText);
        }
    }


    private void handleBeaconNotFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(R.string.notFound);
            }
        });
    }

    private void handleBeaconInRange() {
        //Beacon can be registered
        if(lastScanTime == 0 || timeToWait <= 0){
            //reset waittime and update last scan
            timeToWait = 1000 * Integer.parseInt(sharedPref.getString(getString(R.string.pref_waittime), "5"));
            lastScanTime = System.currentTimeMillis();
            incrementCount();

        }else{//Beacon can not be registered
            long now = System.currentTimeMillis();
            long diff = now - lastScanTime;
            timeToWait -= diff;
            lastScanTime = now;
        }
    }

    private void updateBeaconInView(final String distanceValue, final String statusText){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadCounter();
                String timeText;
                distance.setText(distanceValue);
                status.setText(statusText);
                if(timeToWait <=0){
                    timeText = String.format(getString(R.string.seconds), 0);
                }else{
                    timeText = String.format(getString(R.string.seconds), timeToWait/1000);
                }
                    waitTime.setText(timeText);
            }
        });
    }

    private void reloadCounter() {
        int count = sharedPref.getInt(getString(R.string.pref_count), 0);
        counter.setText(String.valueOf(count));
    }


    private String getDistanceValue(Beacon beacon){
        double dist = beacon.getDistance();
        dist = Math.round(dist*100d)/100d;
        return String.valueOf(dist)+"m";
    }

    private void incrementCount() {
        int current = sharedPref.getInt(getString(R.string.pref_count), 0);
        sharedPref.edit().putInt(getString(R.string.pref_count), ++current).apply();
    }


    public void resetButtonClicked(View view){
        sharedPref.edit().putInt(getString(R.string.pref_count), 0).apply();
        reloadCounter();
    }


    private void checkBluetoothOn(){
        // check whether bluetooth is available and turned on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bluetoothNotAvalaible),
                    Toast.LENGTH_SHORT).show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, getString(R.string.activateBluetooth),Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                checkPermissions();
            }
        }
    }

    private void checkPermissions() {
        // check whether permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void displayLicensesDialog() {
        WebView view = (WebView) View.inflate(this, R.layout.dialog_info, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");

        infoDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(getString(R.string.licenses))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    private void displayHelpDialog() {
        WebView view = (WebView) View.inflate(this, R.layout.dialog_info, null);
        view.loadUrl("file:///android_asset/help.html");
        infoDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(getString(R.string.menu_help))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    private void displayDisclaimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.disclaimer)
                .setMessage(R.string.disclaimer_txt)
                .setPositiveButton("OK", null)
                .show();

    }
}
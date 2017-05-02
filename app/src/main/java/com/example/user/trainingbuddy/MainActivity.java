package com.example.user.trainingbuddy;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lpresearch.lpsensorlib.LpmsB2;
import com.lpresearch.lpsensorlib.LpmsBData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LpSensorLibMainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    boolean FlagCancelled = false;

    final Context context = this;
    TextView textSensorStatus, textSensorInfo, textSensorSettings, textSensorData;
    Button btnDiscoverSensor, btnToggleConnectSensor, btnRecord, btnTimeStamp;
    LpService mService;
    boolean mBound = false;

    // UI Update
    private Handler mUiUpdateHandler;
    final Handler handler = new Handler();
    int UI_UPDATE_RATE = 20; //ms
    int uiSensorDataUpdateCount = 0;
    int uiUpdateBatteryDataCount = 0;

    // Service Related
    MyResultReceiver resultReceiver;
    long serviceUptime = 0;

    // Sensor related
    int sensorConnectionStatus = LpmsB2.SENSOR_STATUS_DISCONNECTED;
    String serialNumber;
    String deviceName;
    String firmwareInfo;
    String sensorData;
    String currentLpms;
    String username;
    int nameCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("training.buddy", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = prefs.edit();

        if(prefs.getInt("COUNTER", 0)>0) {
            edt.putInt("COUNTER", 0);
        }

        nameCounter = prefs.getInt("COUNTER", 0);

        Intent fromGallery = getIntent();
        username = fromGallery.getStringExtra("NAME");
        // Text View
        textSensorStatus = (TextView) findViewById(R.id.txt_status);
        textSensorInfo = (TextView) findViewById(R.id.txt_info);
        textSensorSettings = (TextView) findViewById(R.id.txt_settings);
        textSensorData = (TextView) findViewById(R.id.txt_data);

        // Button
        btnDiscoverSensor = (Button) findViewById(R.id.btn_discover);
        btnToggleConnectSensor = (Button) findViewById(R.id.btn_connect_sensor);
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnTimeStamp = (Button) findViewById(R.id.btn_resetTimestamp);

        // Other inits
        mUiUpdateHandler = new Handler();
        resultReceiver = new MyResultReceiver(null);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferences();
        if (!btAdapter.isEnabled()) {
            isBtEnabled = false;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        startOrBindToService();
        mUiUpdateHandler.postDelayed(updateUI, UI_UPDATE_RATE);
        prepareDiscoveredDevicesList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindFromService();
        mUiUpdateHandler.removeCallbacks(updateUI);
        savePreferences();
    }

    private void savePreferences()
    {
        // Save states
        SharedPreferences settings = getSharedPreferences(TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putString("currentLpms", currentLpms);
        editor.putInt("discoveredLpmsListSize", discoveredLpmsList.size()); /* sKey is an array */
        editor.putBoolean("isBtEnabled", isBtEnabled);

        for(int i=0;i<discoveredLpmsList.size();i++)
        {
            editor.putString("lpms_" + i, discoveredLpmsList.get(i));
        }
        editor.commit();

    }

    private void getPreferences()
    {
        // get preferences
        SharedPreferences settings = getSharedPreferences(TAG, MODE_PRIVATE);
        currentLpms = settings.getString("currentLpms", null);          // getting String
        discoveredLpmsList.clear();
        int size = settings.getInt("discoveredLpmsListSize", 0);

        for(int i=0;i<size;i++)
        {
            discoveredLpmsList.add(settings.getString("lpms_" + i, null));
        }
        isBtEnabled = settings.getBoolean("isBtEnabled", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_quit:
                stopService();
                return true;
            case R.id.action_set_imu_id:
                settingsSetImuId();
                return true;
            case R.id.action_set_stream_freq:
                settingsSetStreamFreq();
                return true;
            case R.id.action_set_gyro_range:
                settingsSetGyroRange();
                return true;
            case R.id.action_set_acc_range:
                settingsSetAccRange();
                return true;
            case R.id.action_set_mag_range:
                settingsSetMagRange();
                return true;
            case R.id.action_set_transmit_data:
                settingsSetTransmitData();
                return true;
            case R.id.action_set_filter_mode:
                settingsSetFilterMode();
                return true;
            case R.id.action_set_orientation_offset:
                settingsSetOrientationOffset();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LpService.LocalBinder binder = (LpService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    ///////////////////////////////
    // Button Callback
    //////////////////////////////
    public void onButtonDiscoverSensorClick(View view) {
        Log.e("000","001");
        if (mService.getSensorConnectionStatus() == LpmsB2.SENSOR_STATUS_CONNECTING)
        {
            showToast("Discovery cancelled, sensor connection establishing");
            return;
        }
        if (mService.getSensorConnectionStatus() == LpmsB2.SENSOR_STATUS_CONNECTED)
        {
            showToast("Discovery cancelled, sensor connection established");
            return;
        }
        if (btAdapter.isDiscovering())
        {
            showToast("Discovery in progress");
            btnDiscoverSensor.setText("Discovering...");
            return;
        }
        Log.e("000","001");
        startBtDiscovery();
    }

    public void onButtonToggleConnectSensorClick(View view) {
        Log.d(TAG, "connectBtn " );
        int connectionStatus = mService.getSensorConnectionStatus();
        if (connectionStatus == LpmsB2.SENSOR_STATUS_CONNECTING || connectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED) {
            new AlertDialog.Builder(this)
                    .setMessage("Disconnect from sensor?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mService.disconnectSensor();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        else if (connectionStatus == LpmsB2.SENSOR_STATUS_DISCONNECTED) {
            if (currentLpms != null && !currentLpms.isEmpty() )
                mService.connectSensor(currentLpms);
            else
                showToast("Please select a sensor to connect");
        }
        else{
            Log.d(TAG, "??" );

        }
    }

    /*public void onButtonGetSensorDataClick(View view) {
        if (!mBound) {
            showToast("Service not started");
            return;
        }
        mService.getSensorDataCmd();
    }*/

    public void onButtonSetTimestampClick(View view) {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        mService.resetTimestamp();
    }


    ///////////////////////////////
    // Service Callbacks
    //////////////////////////////
    @SuppressLint("ParcelCreator")
    class MyResultReceiver extends ResultReceiver {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == LpService.MSG_SENSOR_CONNECTED) {
                String msg = resultData.getString(LpService.KEY_MESG);
                showToastOnUI(msg);
            } else if (resultCode == LpService.MSG_SENSOR_DISCONNECTED) {
                String msg = resultData.getString(LpService.KEY_MESG);
                showToastOnUI(msg);
            } else if (resultCode == LpService.MSG_SENSOR_CONNECTION_ERROR) {
                String msg = resultData.getString(LpService.KEY_MESG);
                showToastOnUI(msg);
            } else {
                showToast("yay");
            }
        }

        void showToastOnUI(String msg) {
            final String lMsg = msg;
            Handler h = new Handler(getBaseContext().getMainLooper());
            // Although you need to pass an appropriate context
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), lMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    ///////////////////////////////
    // Privates
    //////////////////////////////

    // Method to start the service
    public void startOrBindToService() {
        if (mService.isRunning()) {
            bindToService();
        } else {
            Intent intent = new Intent(this, LpService.class);
            intent.putExtra("receiver", resultReceiver);
            startService(intent);
            bindToService();
        }
    }

    // Method to stop the service
    public void stopService() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mService.disconnectSensor();
                        stopService(new Intent(getBaseContext(), LpService.class));
                        if (isBtEnabled == false)
                            btAdapter.disable();
                        isBtEnabled = true;
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void bindToService() {
        // Bind to LocalService
        Intent intent = new Intent(this, LpService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromService() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    // Settings Menu
    private void settingsSetImuId()
    {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Imu ID: ");
        builder.setCancelable(false);

        final NumberPicker picker = new NumberPicker(context);
        int maxValue=20;
        if (maxValue < mService.getImuId())
            maxValue = mService.getImuId();
        picker.setMinValue(1);
        picker.setMaxValue(maxValue);
        picker.setValue(mService.getImuId());

        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // get user input and set it to result
                        // edit text
                        try {
                            int ts = picker.getValue();
                            Log.d(TAG, "ts: " + Integer.toString(ts));
                            mService.setImuId(ts);
                        } catch(NumberFormatException nfe) {
                            System.out.println("Could not parse " + nfe);
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.setView(picker);
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void settingsSetStreamFreq()
    {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Streaming Frequency: ");
        builder.setCancelable(false);

        final int [] freqArray = new int[]{5, 10, 25, 50, 100, 200, 400};
        CharSequence items[] = new CharSequence[] {"5Hz", "10Hz", "25Hz", "50Hz", "100Hz", "200Hz", "400Hz"};
        int currentFreq = mService.getStreamFrequency();
        int currentChoice;
        for (currentChoice=0; currentChoice < freqArray.length; ++currentChoice) {
            if (currentFreq == freqArray[currentChoice]) {
                break;
            }
        }

        builder.setSingleChoiceItems(items, currentChoice, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        int freq = freqArray[lw.getCheckedItemPosition()];
                        mService.setStreamFrequency(freq);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void settingsSetGyroRange() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Gyro Range: ");
        builder.setCancelable(false);

        final int [] gyrArray = new int[]{125, 245, 500, 1000, 2000};
        CharSequence items[] = new CharSequence[] {"125dps", "245dps", "500dps", "1000dps", "2000dps"};
        int currentGyrRange = mService.getGyroRange();
        int currentChoice;
        for (currentChoice=0; currentChoice < gyrArray.length; ++currentChoice) {
            if (currentGyrRange == gyrArray[currentChoice]) {
                break;
            }
        }

        builder.setSingleChoiceItems(items, currentChoice, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        int range = gyrArray[lw.getCheckedItemPosition()];
                        mService.setGyroRange(range);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void settingsSetAccRange() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Accelerometer Range: ");
        builder.setCancelable(false);

        final int [] accArray = new int[]{2,4,8,16};
        CharSequence items[] = new CharSequence[] {"2G", "4G", "8G", "16G"};
        int currentAccRange = mService.getAccRange();
        int currentChoice;
        for (currentChoice=0; currentChoice < accArray.length; ++currentChoice) {
            if (currentAccRange == accArray[currentChoice]) {
                break;
            }
        }

        builder.setSingleChoiceItems(items, currentChoice, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        int range = accArray[lw.getCheckedItemPosition()];
                        mService.setAccRange(range);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void settingsSetMagRange() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Magnetometer Range: ");
        builder.setCancelable(false);

        final int [] magArray = new int[]{4,8,12,16};
        CharSequence items[] = new CharSequence[] {"2Gauss", "8Gauss", "12Gauss", "16Gauss"};
        int currentMagRange = mService.getMagRange();
        int currentChoice;
        for (currentChoice=0; currentChoice < magArray.length; ++currentChoice) {
            if (currentMagRange == magArray[currentChoice]) {
                break;
            }
        }

        builder.setSingleChoiceItems(items, currentChoice, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        int range = magArray[lw.getCheckedItemPosition()];
                        mService.setMagRange(range);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }
    private void settingsSetTransmitData() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Transmission Data: ");
        builder.setCancelable(false);
        List<CharSequence> list = new ArrayList<CharSequence>();
        list.add("Accelerometer Data");
        list.add("Gyro Data");
        list.add("Magnetometer Data");
        list.add("Angular Velocity");
        list.add("Quaternion Data");
        list.add("Euler Angles");
        list.add("Linear Acceleration");
        list.add("Pressure Data");
        list.add("Altitude Data");
        list.add("Temperature Data");

        CharSequence items[] = list.toArray(new CharSequence[list.size()]);
        int count = items.length;
        boolean[] is_checked = new boolean[count]; // set is_checked boolean false;
        is_checked[0] = mService.isAccDataEnabled();
        is_checked[1] = mService.isGyroDataEnabled();
        is_checked[2] = mService.isMagDataEnabled();
        is_checked[3] = mService.isAngularVelDataEnabled();
        is_checked[4] = mService.isQuaternionDataEnabled();
        is_checked[5] = mService.isEulerDataEnabled();
        is_checked[6] = mService.isLinAccDataEnabled();
        is_checked[7] = mService.isPressureDataEnabled();
        is_checked[8] = mService.isAltitudeDataEnabled();
        is_checked[9] = mService.isTemperatureDataEnabled();

        builder.setMultiChoiceItems(items, is_checked, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView list = ((AlertDialog) dialog).getListView();
                        int transmissionData = 0;
                        if (list.isItemChecked(0))
                            transmissionData |= LpmsB2.LPMS_ACC_RAW_OUTPUT_ENABLED;
                        if (list.isItemChecked(1))
                            transmissionData |= LpmsB2.LPMS_GYR_RAW_OUTPUT_ENABLED;
                        if (list.isItemChecked(2))
                            transmissionData |= LpmsB2.LPMS_MAG_RAW_OUTPUT_ENABLED;
                        if (list.isItemChecked(3))
                            transmissionData |= LpmsB2.LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED;
                        if (list.isItemChecked(4))
                            transmissionData |= LpmsB2.LPMS_QUAT_OUTPUT_ENABLED;
                        if (list.isItemChecked(5))
                            transmissionData |= LpmsB2.LPMS_EULER_OUTPUT_ENABLED;
                        if (list.isItemChecked(6))
                            transmissionData |= LpmsB2.LPMS_LINACC_OUTPUT_ENABLED;
                        if (list.isItemChecked(7))
                            transmissionData |= LpmsB2.LPMS_PRESSURE_OUTPUT_ENABLED;
                        if (list.isItemChecked(8))
                            transmissionData |= LpmsB2.LPMS_ALTITUDE_OUTPUT_ENABLED;
                        if (list.isItemChecked(9))
                            transmissionData |= LpmsB2.LPMS_TEMPERATURE_OUTPUT_ENABLED;

                        mService.setTransmissionData(transmissionData);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();

    }

    private void settingsSetFilterMode() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Filter Mode: ");
        builder.setCancelable(false);

        CharSequence items[] = new CharSequence[] {"Gyr Only", "Gyr+Acc (Kalman)", "Gyr+Acc+Mag (Kalman)",
                "Gyr+Acc (DCM)", "Gyr+Acc+Mag (DCM)"};
        int currentFilterMode = mService.getFilterMode();

        builder.setSingleChoiceItems(items, currentFilterMode, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        mService.setFilterMode(lw.getCheckedItemPosition());
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void settingsSetOrientationOffset() {
        if (!mBound) {
            showToast("Service not started");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Orientation Offset: ");
        builder.setCancelable(false);

        CharSequence items[] = new CharSequence[] {"Object Reset", "Heading Reset", "Alignment Reset", "Reset Offset"};

        builder.setSingleChoiceItems(items, 0, null);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ListView lw = ((AlertDialog)dialog).getListView();
                        switch (lw.getCheckedItemPosition()) {
                            case 0:
                                mService.setOrientationOffset(LpmsB2.LPMS_OFFSET_MODE_OBJECT);
                                break;
                            case 1:
                                mService.setOrientationOffset(LpmsB2.LPMS_OFFSET_MODE_HEADING);
                                break;
                            case 2:
                                mService.setOrientationOffset(LpmsB2.LPMS_OFFSET_MODE_ALIGNMENT);
                                break;
                            case 3:
                                mService.resetOrientationOffset();
                                break;
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    // Update UI
    private Runnable updateUI = new Runnable() {
        LpmsBData d = new LpmsBData();
        int lastSensorConnectionStatus = -1;
        public void run() {
            if (mBound) {
                serviceUptime = (long) mService.getUptime();
                d = mService.getSensorData();
                sensorConnectionStatus = mService.getSensorConnectionStatus();

                // Service uptime
                //textServiceStatus.setText(getString(R.string.ui_server_status, formatHHMMSS(serviceUptime)));

                // Sensor Info and buttons
                // Refresh only on connection status change
                if (sensorConnectionStatus != lastSensorConnectionStatus) {
                    if (lastSensorConnectionStatus != LpmsB2.SENSOR_STATUS_CONNECTED && sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED) {
                        serialNumber = mService.getSerialNumber();
                        deviceName = mService.getDeviceName();
                        firmwareInfo = mService.getFirmwareInfo();
                        textSensorInfo.setText(getString(R.string.ui_sensor_info, serialNumber, deviceName, firmwareInfo));
                        textSensorInfo.setTextColor(Color.YELLOW);
                    }
                    switch (sensorConnectionStatus) {
                        case LpmsB2.SENSOR_STATUS_CONNECTED:
                            textSensorStatus.setText(getString(R.string.ui_sensor_status, "Connected"));
                            textSensorStatus.setTextColor(Color.GREEN);
                            btnToggleConnectSensor.setText(getString(R.string.btn_disconnect_sensor));
                            btnDiscoverSensor.setEnabled(false);
                            break;
                        case LpmsB2.SENSOR_STATUS_CONNECTING:
                            textSensorStatus.setText(getString(R.string.ui_sensor_status, "Connecting"));
                            textSensorStatus.setTextColor(Color.YELLOW);
                            btnToggleConnectSensor.setText(getString(R.string.btn_disconnect_sensor));
                            btnDiscoverSensor.setEnabled(false);
                            break;
                        case LpmsB2.SENSOR_STATUS_DISCONNECTED:
                            textSensorStatus.setText(getString(R.string.ui_sensor_status, "Disconnected"));
                            textSensorStatus.setTextColor(Color.RED);
                            btnToggleConnectSensor.setText(getString(R.string.btn_connect_sensor));
                            btnDiscoverSensor.setEnabled(true);
                            spinner.setClickable(true);
                            spinner.setEnabled(true);
                            break;
                        default:
                            break;

                    }
                    lastSensorConnectionStatus = sensorConnectionStatus;
                }


                if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED  && d!=null ) {
                    // Sensor data

                    if (uiSensorDataUpdateCount > 3) {
                        textSensorSettings.setText(getString(R.string.ui_sensor_settings,
                                mService.getImuId(),
                                mService.getStreamFrequency(),
                                mService.getGyroRange(),
                                mService.getAccRange(),
                                mService.getMagRange(),
                                ""+mService.getFilterMode()
                        ));
                        textSensorSettings.setTextColor(Color.CYAN);
                        /*
                        textSensorData.setText(getString(R.string.ui_sensor_data,
                                d.timestamp,
                                d.acc[0], d.acc[1], d.acc[2]
                        ));*/

                        textSensorData.setText(getString(R.string.ui_sensor_data,
                                d.timestamp,
                                d.acc[0], d.acc[1], d.acc[2],
                                d.gyr[0], d.gyr[1], d.gyr[2],
                                d.mag[0], d.mag[1], d.mag[2],
                                d.angVel[0], d.angVel[1], d.angVel[2],
                                d.quat[0], d.quat[1], d.quat[2], d.quat[3],
                                d.euler[0], d.euler[1], d.euler[2],
                                d.linAcc[0], d.linAcc[1], d.linAcc[2],
                                d.pressure, d.altitude, d.temperature,
                                d.batteryLevel, d.chargingStatus
                        ));
                        uiSensorDataUpdateCount = 0;
                    }

                    if (uiUpdateBatteryDataCount > 1000/UI_UPDATE_RATE) { // 5sec
                        mService.getBattery();
                        mService.getChargingStatus();
                        uiUpdateBatteryDataCount = 0;
                    }

                    //cubeRenderer.updateRotation(d.quat);
                    uiSensorDataUpdateCount++;
                    uiUpdateBatteryDataCount++;
                }
            }
            mUiUpdateHandler.postDelayed(this, UI_UPDATE_RATE);
        }
    };

    private Runnable refreshSensorSettingsUI = new Runnable() {
        public void run() {
            if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED )
            {
                textSensorSettings.setText(getString(R.string.ui_sensor_settings,
                        mService.getImuId(),
                        mService.getStreamFrequency(),
                        mService.getGyroRange(),
                        mService.getAccRange(),
                        mService.getMagRange(),
                        ""+mService.getFilterMode()
                ));
                textSensorSettings.setTextColor(Color.CYAN);
            }
        }

    };

    private void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public String formatHHMMSS(long secondsCount) {
        // Calculate the seconds to display:
        int seconds = (int) (secondsCount % 60);
        secondsCount -= seconds;
        // Calculate the minutes:
        long minutesCount = secondsCount / 60;
        long minutes = minutesCount % 60;
        minutesCount -= minutes;
        // Calculate the hours:
        long hoursCount = minutesCount / 60;
        // Build the String
        return String.format("%02d:%02d:%02d", hoursCount, minutes, seconds);
    }

    // Bluetooth
    ArrayList<String> discoveredLpmsList = new ArrayList<String>();
    ArrayAdapter lpmsListAdapter;
    Spinner spinner;
    BluetoothAdapter btAdapter;
    boolean isBtEnabled=true;

    public void startBtDiscovery() {
        Toast.makeText(this, "Starting discovery..", Toast.LENGTH_SHORT).show();
        synchronized (discoveredLpmsList) {
            discoveredLpmsList.clear();
        }
        lpmsListAdapter.notifyDataSetChanged();
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        btAdapter.startDiscovery();
        /*
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        synchronized (discoveredLpmsList) {
            discoveredLpmsList.clear();
        }
        for(BluetoothDevice bt : pairedDevices) {
            if (bt.getName().contains("LPMS")) {
                discoveredLpmsList.add(bt.getAddress());
            }
        }
        */
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // BT Discovery
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                btnDiscoverSensor.setText("Discovering..");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btnDiscoverSensor.setText("Discover Sensor");
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device == null) return;

                synchronized (discoveredLpmsList) {
                    if ((device.getName() != null) && (device.getName().length() > 0)) {
                        if (device.getName().contains("LPMS")) {
                            for (ListIterator<String> it = discoveredLpmsList.listIterator(); it.hasNext(); ) {
                                if (device.getAddress().equals(it.next())) return;
                            }
                            discoveredLpmsList.add(device.getAddress());
                            lpmsListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    public void prepareDiscoveredDevicesList() {
        spinner = (Spinner) findViewById(R.id.spn_sensorlist);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        lpmsListAdapter = new ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, discoveredLpmsList);
        spinner.setAdapter(lpmsListAdapter);
        lpmsListAdapter.notifyDataSetChanged();

        if (currentLpms != null) {
            int spinnerPosition = lpmsListAdapter.getPosition(currentLpms);
            spinner.setSelection(spinnerPosition);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item!=null) {
                    currentLpms = item.toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void onButtonRecordDataClick(View view) {

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        new recordSensor().execute("");
        dispatchTakeVideoIntent();




    }

    private class recordSensor extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            sensorData = "";
            do {
                if (FlagCancelled == true)
                {
                    FlagCancelled = false;
                    break;
                }
                LpmsBData d = new LpmsBData();
                    d = mService.getSensorData();
                    sensorConnectionStatus = mService.getSensorConnectionStatus();
                if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED  && d!=null ) {
                    // Sensor data

                        sensorData += getString(R.string.ui_sensor_data,
                                d.timestamp,
                                d.acc[0], d.acc[1], d.acc[2],
                                d.gyr[0], d.gyr[1], d.gyr[2],
                                d.mag[0], d.mag[1], d.mag[2],
                                d.angVel[0], d.angVel[1], d.angVel[2],
                                d.quat[0], d.quat[1], d.quat[2], d.quat[3],
                                d.euler[0], d.euler[1], d.euler[2],
                                d.linAcc[0], d.linAcc[1], d.linAcc[2],
                                d.pressure, d.altitude, d.temperature,
                                d.batteryLevel, d.chargingStatus
                        ) + "\n";
                    }


            } while(true);


            return sensorData;
        }

        @Override
        protected void onPostExecute(String result){
            TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
            String fileName = "SensorData_" + username + nameCounter + ".txt";
            app.writeFile(fileName, sensorData);
            nameCounter++;
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

        @Override
        protected void onCancelled(String result){
            TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
            String fileName = "SensorData_" + username + nameCounter + ".txt";
            app.writeFile(fileName, sensorData);
            nameCounter++;
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            String extStorage = Environment.getExternalStorageDirectory().toString() + "/Download";
            String fileName = "VideoData_" + username + nameCounter + ".mp4";
            File video = new File(extStorage, fileName);
            Uri uriSavedVideo = Uri.fromFile(video);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent takeVideo) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            //Uri videoUri = takeVideo.getData();
            setFlagCancelled();
            TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
            String extStorage = Environment.getExternalStorageDirectory().toString() + "/Download";
            String fileName = "VideoData_" + username + nameCounter + ".mp4";
            app.loadVideoInfoList();
            app.addVideoInfo(username, extStorage + "/" + fileName);
            app.saveVideoInfoList();
        }
    }

    protected void setFlagCancelled() {
        FlagCancelled = true;
    }
}


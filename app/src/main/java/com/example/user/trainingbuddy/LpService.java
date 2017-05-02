package com.example.user.trainingbuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lpresearch.lpsensorlib.LpmsB2;
import com.lpresearch.lpsensorlib.LpmsBData;

import java.util.Timer;
import java.util.TimerTask;

public class LpService extends Service {
    private static final String TAG = "LpService";

    //////////////////////////////////////////////////////////////////////
    // Globals
    //////////////////////////////////////////////////////////////////////
    // Main UI communication protocol
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_LOGGING_STARTED = 3;
    static final int MSG_LOGGING_STOPPED = 4;
    static final int MSG_LOGGING_ERROR = 5;
    static final int MSG_SENSOR_CONNECTED = 6;
    static final int MSG_SENSOR_DISCONNECTED = 7;
    static final int MSG_SENSOR_CONNECTION_ERROR = 8;
    static final String KEY_MESG = "MESG";
    ResultReceiver resultReceiver;

    // Thread related
    Timer timer;
    private int UPDATE_RATE = 10; // ms
    private static boolean isRunning = false;

    // Binder given to clients
    public class LocalBinder extends Binder {
        LpService getService() {
            return LpService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    // Notification
    private NotificationManager nm;
    int notificationId = 2;

    // Service Status
    boolean serviceQuit = false;
    long startTime = System.currentTimeMillis();

    // LpmsB
    LpmsB2 lpmsB;
    LpmsBData d;

    int sensorDataQueueSize = 0;

    //////////////////////////////////////////////////////////////////////

    public LpService() {
        Log.d(TAG, "LpLpService()");
    }

    @Override
    public void onCreate() {
        // Init
        lpmsB = new LpmsB2();
        d = new LpmsBData();

        // Sensor read Timer

        timer = new Timer();
        timer.scheduleAtFixedRate(getLpmsBDataTask, 0, UPDATE_RATE);
        /*
        serviceQuit = false;
        Thread t = new Thread(new getLpmsBDataThread());
        t.start();
        */
        showNotification();

        // Service Status
        startTime = System.currentTimeMillis();
        isRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            resultReceiver = intent.getParcelableExtra("receiver");
        } catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serviceQuit = true;
        // Clean up
        // Lpms
        disconnectSensor();

        // Timer
        timer.cancel();

        // Notification.
        nm.cancel(notificationId);

        // Service status
        isRunning = false;
    }

    ///////////////////////////////
    // method for clients
    ///////////////////////////////
    public double getUptime() {
        if (isRunning)
            return (double) (System.currentTimeMillis() - startTime) / 1000.0;
        return 0.0;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public void connectSensor(String address) {
        Log.d(TAG, "connect: " +address);

        if (lpmsB.getConnectionStatus() == LpmsB2.SENSOR_STATUS_CONNECTED) {
            sendMessageToUI(KEY_MESG, "Already connected to " + address, MSG_SENSOR_CONNECTION_ERROR);
            return;
        }

        final String add = address;

        new Thread(new Runnable() {
            public void run() {
                if (lpmsB.connect(add))
                {
                    //sendMessageToUI(KEY_MESG, "Connected to " + add, MSG_SENSOR_CONNECTED);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    sendMessageToUI(KEY_MESG, "Error connecting to " + add, MSG_SENSOR_CONNECTION_ERROR);
                }
            }
        }).start();
    }

    public void disconnectSensor() {
        if (lpmsB.getConnectionStatus() == LpmsB2.SENSOR_STATUS_DISCONNECTED) {
            sendMessageToUI(KEY_MESG, "Sensor 1 not connected", MSG_SENSOR_DISCONNECTED);
            return;
        }
        lpmsB.disconnect();
    }

    public int getSensorConnectionStatus() {
        return lpmsB.getConnectionStatus();
    }

    // Sensor settings
    public void setImuId(int id) {
        lpmsB.setImuId(id);
    }

    public int getImuId() {
        return lpmsB.getImuId();
    }

    public void setStreamFrequency(int freq) {
        lpmsB.setStreamFrequency(freq);
    }

    public int getStreamFrequency() {
        return lpmsB.getStreamFrequency();
    }

    public void setGyroRange(int range) {
        lpmsB.setGyroRange(range);
    }

    public int getGyroRange() {
       return lpmsB.getGyroRange();
    }

    public void setAccRange(int range) {
        lpmsB.setAccRange(range);
    }

    public int getAccRange() {
        return lpmsB.getAccRange();
    }

    public void setMagRange(int range) {
        lpmsB.setMagRange(range);
    }

    public int getMagRange() {
        return lpmsB.getMagRange();
    }

    // Data settings
    public void setTransmissionData(int v) {
        lpmsB.setTransmissionData(v);
    }

    public void enable16BitData() {
        lpmsB.enable16BitData();
    }

    public void enable32BitData() {
        lpmsB.enable32BitData();
    }

    public boolean is16BitDataEnabled() {
        return lpmsB.is16BitDataEnabled();
    }

    public void enableAccData(boolean b) {
        lpmsB.enableAccData(b);
    }

    public boolean isAccDataEnabled() {
        return lpmsB.isAccDataEnabled();
    }

    public void enableGyroData(boolean b) {
        lpmsB.enableGyroData(b);
    }

    public boolean isGyroDataEnabled() {
        return lpmsB.isGyroDataEnabled();
    }

    public void enableMagData(boolean b) {
        lpmsB.enableMagData(b);
    }

    public boolean isMagDataEnabled() {
        return lpmsB.isMagDataEnabled();
    }

    public void enableAngularVelData(boolean b) {
        lpmsB.enableAngularVelData(b);
    }

    public boolean isAngularVelDataEnabled() {
        return lpmsB.isAngularVelDataEnable();
    }

    public void enableQuaternionData(boolean b) {
        lpmsB.enableQuaternionData(b);
    }

    public boolean isQuaternionDataEnabled() {
        return lpmsB.isQuaternionDataEnabled();
    }


    public void enableEulerData(boolean b) {
        lpmsB.enableEulerData(b);
    }

    public boolean isEulerDataEnabled() {
        return lpmsB.isEulerDataEnabled();
    }

    public void enableLinAccData(boolean b) {
        lpmsB.enableLinAccData(b);
    }

    public boolean isLinAccDataEnabled() {
        return lpmsB.isLinAccDataEnabled();
    }

    public void enablePressureData(boolean b) {
        lpmsB.enablePressureData(b);
    }

    public boolean isPressureDataEnabled() {
        return lpmsB.isPressureDataEnabled();
    }

    public void enableAltitudeData(boolean b) {
        lpmsB.enableAltitudeData(b);
    }

    public boolean isAltitudeDataEnabled() {
        return lpmsB.isAltitudeDataEnabled();
    }

    public void enableTemperatureData(boolean b) {
        lpmsB.enableTemperatureData(b);
    }

    public boolean isTemperatureDataEnabled() {
        return lpmsB.isTemperatureDataEnabled();
    }

    public void setFilterMode(int mode) {
        lpmsB.setFilterMode(mode);
    }

    public int getFilterMode() {
        return lpmsB.getFilterMode();
    }


    public LpmsBData getSensorData() {
        return d;
    }

    public void getSensorDataCmd() {
        d = lpmsB.getLpmsBData();
    }

    public int getSensorDataQueueSize() {
        return sensorDataQueueSize;
    }

    public String getSerialNumber()
    {
        return lpmsB.getSerialNumber();
    }

    public String getDeviceName()
    {
        return lpmsB.getDeviceName();
    }

    public String getFirmwareInfo()
    {
        return lpmsB.getFirmwareInfo();
    }

    public void resetFactorySettings()
    {
        lpmsB.resetFactorySettings();
    }

    public void setCommandMode()
    {
        lpmsB.setCommandMode();
    }

    public void setStreamingMode()
    {
        lpmsB.setStreamingMode();
    }

    public void resetTimestamp()
    {
        lpmsB.resetTimestamp();
    }

    public void setTimestamp(int ts)
    {
        lpmsB.setTimestamp(ts);
    }

    public void setOrientationOffset(int offset) { lpmsB.setOrientationOffset(offset);}

    public void resetOrientationOffset() { lpmsB.resetOrientationOffset();}


    public void getBattery()
    {
        lpmsB.getBatteryPercentage();
        lpmsB.getBatteryVoltage();
    }

    public void getChargingStatus()
    {
        lpmsB.getChargingStatus();
    }


    ///////////////////////////////
    // Privates
    //////////////////////////////
    TimerTask getLpmsBDataTask = new TimerTask() {
        @Override
        public void run() {
            if (lpmsB.getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED){// && lpmsB.isStreamingMode()) {
                // bSensorConnected = true;
                sensorDataQueueSize = lpmsB.hasNewData();
                while (sensorDataQueueSize > 0) {
                        d = lpmsB.getLpmsBData();
                    sensorDataQueueSize--;
                }
            }
        }
    };

    public class getLpmsBDataThread implements Runnable {

        public void run() {
            while (!serviceQuit) {
                if (lpmsB.getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {// && lpmsB.isStreamingMode()) {
                    // bSensorConnected = true;
                    sensorDataQueueSize = lpmsB.hasNewData();
                    while (sensorDataQueueSize > 0) {
                        d = lpmsB.getLpmsBData();
                        sensorDataQueueSize--;
                    }
                }
            }
        }
    }

    private void showNotification() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(getText(R.string.service_title))
                        .setContentText(getText(R.string.service_text));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        nm.notify(notificationId, notification);
    }

    private void sendMessageToUI(String key, String value, int what) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        try {
            resultReceiver.send(what, bundle);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
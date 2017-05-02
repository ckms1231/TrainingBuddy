package com.lpresearch.lpsensorlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

//TODO:
// move lpservice check connection to here
// check conenction streaming state before enabling streaming
public class LpmsB2
        extends Thread {
    //////////////////////////////////////////////
    // Stream frequency enable bits
    //////////////////////////////////////////////
    public static final int LPMS_STREAM_FREQ_5HZ = 5;
    public static final int LPMS_STREAM_FREQ_10HZ = 10;
    public static final int LPMS_STREAM_FREQ_25HZ = 25;
    public static final int LPMS_STREAM_FREQ_50HZ = 50;
    public static final int LPMS_STREAM_FREQ_100HZ = 100;
    public static final int LPMS_STREAM_FREQ_200HZ = 200;
    public static final int LPMS_STREAM_FREQ_400HZ = 400;
    public static final int LPMS_FILTER_GYR = 0;
    public static final int LPMS_FILTER_GYR_ACC = 1;
    public static final int LPMS_FILTER_GYR_ACC_MAG = 2;
    public static final int LPMS_FILTER_MADGWICK_GYR_ACC = 3;
    public static final int LPMS_FILTER_MADGWICK_GYR_ACC_MAG = 4;
    // Gyro Range
    public static final int LPMS_GYR_RANGE_125DPS  = 125;
    public static final int LPMS_GYR_RANGE_245DPS  = 245;
    public static final int LPMS_GYR_RANGE_250DPS  = 250;
    public static final int LPMS_GYR_RANGE_500DPS  = 500;
    public static final int LPMS_GYR_RANGE_1000DPS  = 1000;
    public static final int LPMS_GYR_RANGE_2000DPS  = 2000;
    // Acc Range
    public static final int LPMS_ACC_RANGE_2G = 2;
    public static final int LPMS_ACC_RANGE_4G = 4;
    public static final int LPMS_ACC_RANGE_8G = 8;
    public static final int LPMS_ACC_RANGE_16G = 16;
    // Mag Range
    public static final int LPMS_MAG_RANGE_4GAUSS = 4;
    public static final int LPMS_MAG_RANGE_8GAUSS = 8;
    public static final int LPMS_MAG_RANGE_12GAUSS =12;
    public static final int LPMS_MAG_RANGE_16GAUSS =16;
    public static final int PARAMETER_SET_DELAY =10;
    // Connection status
    public static final int SENSOR_STATUS_CONNECTED = 1;
    public static final int SENSOR_STATUS_CONNECTING = 2;
    public static final int SENSOR_STATUS_DISCONNECTED = 3;
    // Offset mode
    public static final int  LPMS_OFFSET_MODE_OBJECT = 0;
    public static final int  LPMS_OFFSET_MODE_HEADING = 1;
    public static final int  LPMS_OFFSET_MODE_ALIGNMENT = 2;



    final String TAG = "LpmsB2";
    final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int PACKET_ADDRESS0 	= 0;
    final int PACKET_ADDRESS1 	= 1;
    final int PACKET_FUNCTION0 	= 2;
    final int PACKET_FUNCTION1 	= 3;
    final int PACKET_LENGTH0 	= 4;
    final int PACKET_LENGTH1 	= 5;
    final int PACKET_RAW_DATA 	= 6;
    final int PACKET_LRC_CHECK0 = 7;
    final int PACKET_LRC_CHECK1 = 8;
    final int PACKET_END 		= 9;

   	//////////////////////////////////////////////
    // Command Registers
   	//////////////////////////////////////////////
    final int REPLY_ACK 			= 0;
    final int REPLY_NACK 			= 1;
    final int UPDATE_FIRMWARE 		= 2;
    final int UPDATE_IAP 			= 3;
    final int GET_CONFIG 			= 4;
    final int GET_STATUS 			= 5;
    final int GOTO_COMMAND_MODE 	= 6;
    final int GOTO_STREAM_MODE 		= 7;
    final int GET_SENSOR_DATA 		= 9;
    final int SET_TRANSMIT_DATA 	= 10;
    final int SET_STREAM_FREQ 		= 11;
	 // Register value save and reset
	 final int WRITE_REGISTERS    	= 15;
	 final int RESTORE_FACTORY_VALUE = 16;
	 // Reference setting and offset reset
	 final int RESET_REFERENCE  	= 17;
	 final int SET_ORIENTATION_OFFSET = 18;
	 final int RESET_ORIENTATION_OFFSET= 82;
	//IMU ID setting
	final int SET_IMU_ID 			= 20;
	final int GET_IMU_ID 			= 21;
	// Gyroscope settings
	final int START_GYR_CALIBRA 	= 22;
	final int ENABLE_GYR_AUTOCAL   	= 23;
	final int ENABLE_GYR_THRES 		= 24;
	final int SET_GYR_RANGE    		= 25;
	final int GET_GYR_RANGE    		= 26;
	// Accelerometer settings
	final int SET_ACC_BIAS     		= 27;
	final int GET_ACC_BIAS     		= 28;
	final int SET_ACC_ALIGN_MATRIX 	= 29;
	final int GET_ACC_ALIGN_MATRIX 	= 30;
	final int SET_ACC_RANGE    		= 31;
	final int GET_ACC_RANGE    		= 32;
	final int SET_GYR_ALIGN_BIAS   	= 48;
	final int GET_GYR_ALIGN_BIAS   	= 49;
	final int SET_GYR_ALIGN_MATRIX 	= 50;
	final int GET_GYR_ALIGN_MATRIX 	= 51;
	// Magnetometer settings
	final int SET_MAG_RANGE    		= 33;
	final int GET_MAG_RANGE    		= 34;
	final int SET_HARD_IRON_OFFSET 	= 35;
	final int GET_HARD_IRON_OFFSET 	= 36;
	final int SET_SOFT_IRON_MATRIX 	= 37;
	final int GET_SOFT_IRON_MATRIX 	= 38;
	final int SET_FIELD_ESTIMATE   	= 39;
	final int GET_FIELD_ESTIMATE   	= 40;
	final int SET_MAG_ALIGNMENT_MATRIX  = 76;
	final int SET_MAG_ALIGNMENT_BIAS = 77;
	final int SET_MAG_REFRENCE 		= 78;
	final int GET_MAG_ALIGNMENT_MATRIX = 79;
	final int GET_MAG_ALIGNMENT_BIAS = 80;
	final int GET_MAG_REFERENCE		= 81;
	// Filter settings
	final int SET_FILTER_MODE  		= 41;
	final int GET_FILTER_MODE  		= 42;
	final int SET_FILTER_PRESET		= 43;
	final int GET_FILTER_PRESET		= 44;
	final int SET_LIN_ACC_COMP_MODE	= 67;
	final int GET_LIN_ACC_COMP_MODE	= 68;

    //////////////////////////////////////////////
    // Status register contents
    //////////////////////////////////////////////
	final int SET_CENTRI_COMP_MODE 	= 69;
	final int GET_CENTRI_COMP_MODE 	= 70;
	final int SET_RAW_DATA_LP  		= 60;
	final int GET_RAW_DATA_LP  		= 61;
    final int SET_TIME_STAMP		= 66;
    final int SET_LPBUS_DATA_MODE 	= 75;
    final int GET_FIRMWARE_VERSION 	= 47;
    final int GET_BATTERY_LEVEL		= 87;
    final int GET_BATTERY_VOLTAGE   = 88;
    final int GET_CHARGING_STATUS   = 89;
    final int GET_SERIAL_NUMBER     = 90;
    final int GET_DEVICE_NAME       = 91;
    final int GET_FIRMWARE_INFO     = 92;
    final int START_SYNC            = 96;
    final int STOP_SYNC             = 97;
    final int GET_PING              = 98;
    final int GET_TEMPERATURE       = 99;

   	//////////////////////////////////////////////
    // Configuration register contents
   	//////////////////////////////////////////////
    public static final int LPMS_GYR_AUTOCAL_ENABLED = 0x00000001 << 30;
    public static final int LPMS_LPBUS_DATA_MODE_16BIT_ENABLED = 0x00000001 << 22;
    public static final int LPMS_LINACC_OUTPUT_ENABLED = 0x00000001 << 21;
    public static final int LPMS_DYNAMIC_COVAR_ENABLED = 0x00000001 << 20;
    public static final int LPMS_ALTITUDE_OUTPUT_ENABLED = 0x00000001 << 19;
    public static final int LPMS_QUAT_OUTPUT_ENABLED = 0x00000001 << 18;
    public static final int LPMS_EULER_OUTPUT_ENABLED = 0x00000001 << 17;
    public static final int LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED = 0x00000001 << 16;
    public static final int LPMS_GYR_CALIBRA_ENABLED = 0x00000001 << 15;
    public static final int LPMS_HEAVEMOTION_OUTPUT_ENABLED = 0x00000001 << 14;
    public static final int LPMS_TEMPERATURE_OUTPUT_ENABLED = 0x00000001 << 13;
    public static final int LPMS_GYR_RAW_OUTPUT_ENABLED = 0x00000001 << 12;
    public static final int LPMS_ACC_RAW_OUTPUT_ENABLED = 0x00000001 << 11;
    public static final int LPMS_MAG_RAW_OUTPUT_ENABLED = 0x00000001 << 10;
    public static final int LPMS_PRESSURE_OUTPUT_ENABLED = 0x00000001 << 9;
    final int LPMS_STREAM_FREQ_5HZ_ENABLED      = 0x00000000;
    final int LPMS_STREAM_FREQ_10HZ_ENABLED     = 0x00000001;
    final int LPMS_STREAM_FREQ_25HZ_ENABLED     = 0x00000002;
    final int LPMS_STREAM_FREQ_50HZ_ENABLED     = 0x00000003;
    final int LPMS_STREAM_FREQ_100HZ_ENABLED    = 0x00000004;
    final int LPMS_STREAM_FREQ_200HZ_ENABLED    = 0x00000005;
    final int LPMS_STREAM_FREQ_400HZ_ENABLED    = 0x00000006;
    final int LPMS_STREAM_FREQ_MASK             = 0x00000007;

    final int MAX_BUFFER = 512;
    final int DATA_QUEUE_SIZE = 64;
    // status
    int connectionStatus = SENSOR_STATUS_DISCONNECTED;

    // Protocol parsing related
    int rxState = PACKET_END;
    byte[] rxBuffer = new byte[MAX_BUFFER];
    byte[] txBuffer = new byte[MAX_BUFFER];
    byte[] rawTxData = new byte[MAX_BUFFER];
    byte[] rawRxBuffer = new byte[MAX_BUFFER];
    int currentAddress = 0;
    int currentFunction = 0;
    int currentLength = 0;
    int rxIndex = 0;
    byte b = 0;
    int lrcCheck = 0;
    int nBytes = 0;
    boolean waitForAck = false;
    boolean waitForData = false;
    byte inBytes[] = new byte[2];
    // Connection related
    BluetoothDevice mDevice;
    String mAddress;
    InputStream mInStream;
    OutputStream mOutStream;
    BluetoothSocket mSocket;
    BluetoothAdapter mAdapter;

    // Settings related
    int imuId = 0;
    int gyrRange = 0;
    int accRange = 0;
    int magRange = 0;
    int streamingFrequency = 0;
    int filterMode = 0;
    boolean isStreamMode = false;

    int configurationRegister = 0;
    private boolean configurationRegisterReady = false;
    private String serialNumber = "";
    private boolean serialNumberReady = false;
    private String deviceName = "";
    private boolean deviceNameReady = false;
    private String firmwareInfo="";
    private boolean firmwareInfoReady = false;
    private String firmwareVersion;

    boolean accEnable = false;
    boolean gyrEnable = false;
    boolean magEnable = false;
    boolean angularVelEnable = false;
    boolean quaternionEnable = false;
    boolean eulerAngleEnable = false;
    boolean linAccEnable = false;
    boolean pressureEnable = false;
    boolean altitudeEnable = false;
    boolean temperatureEnable = false;
    boolean heaveEnable = false;
    boolean sixteenBitDataEnable = false;
    boolean resetTimestampFlag = false;

    boolean newDataFlag = false;
    LinkedBlockingDeque<LpmsBData> dataQueue = new LinkedBlockingDeque<LpmsBData>();
    LpmsBData mLpmsBData = new LpmsBData();
    int frameCounter = 0;
    // Data Logging


    public LpmsB2()
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /////////////////////////////////////////////////////////////////////
    // Public methods
    /////////////////////////////////////////////////////////////////////
    /**
     * @param address is a valid Bluetooth MAC address
     * @return false or true
     * if true , the sensor will be at command mode.And after that, you should
     *  use setAcquisitionParameters() to set some  sensor param.
     */
    public boolean connect(String address) {
        //Log.d(TAG, "[LpmsBThread] Another connection establishing: " +address);
        if (connectionStatus != SENSOR_STATUS_DISCONNECTED) {
            //Log.d(TAG, "[LpmsBThread] Another connection establishing: " + mAddress);
            return false;
        }

        if (mAdapter == null) {
            //Log.d(TAG, "[LpmsBThread] Didn't find Bluetooth adapter");
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        mAddress = address;
        connectionStatus = SENSOR_STATUS_CONNECTING;
        //Log.d(TAG, "[LpmsBThread] Connecting to: " + mAddress);

        mAdapter.cancelDiscovery();

        //Log.d(TAG, "[LpmsBThread] Getting device");

        try {
            mDevice = mAdapter.getRemoteDevice(mAddress);

        } catch (IllegalArgumentException e) {
            //Log.d(TAG, "[LpmsBThread] Invalid Bluetooth address", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        mSocket = null;
        //Log.d(TAG, "[LpmsBThread] Creating socket");

        try {
            mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
        } catch (Exception e) {
            //Log.d(TAG, "[LpmsBThread] Socket create() failed", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        //Log.d(TAG, "[LpmsBThread] Trying to connect..");
        try {
            mSocket.connect();
        } catch (IOException e) {
            //Log.d(TAG, "[LpmsBThread] Couldn't connect to device", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        //Log.d(TAG, "[LpmsBThread] Connected!");

        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();

        } catch (IOException e) {
        	//Log.d(TAG, "[LpmsBThread] Streams not created", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }


        Thread t = new Thread(new ClientReadThread());
        t.start();

        setCommandMode();
        _getSerialNumber();
        _getDeviceName();
        _getFirmwareInfo();
        _getSensorSettings();
        setStreamingMode();
        frameCounter = 0;
        connectionStatus = SENSOR_STATUS_CONNECTED;
        return true;
    }

    /**
     *  disconnect cancel discovery and socket close.
     * @return
     */
    public boolean disconnect() {
    	boolean res = false;
    	if (connectionStatus != SENSOR_STATUS_DISCONNECTED)
    		res = true;
    	mAdapter.cancelDiscovery();
        try {
            mSocket.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        // TODO: reset data
        // clean up
        // dataQueue.clear();
        //stop/Logging();
        //Log.d(TAG, "[LpmsBThread] Connection closed test");
        connectionStatus = SENSOR_STATUS_DISCONNECTED;
        return res;
    }

    public String getAddress() {
        return mAddress;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mDevice;
    }
    /**
     * use command mode to communicate with sensor.
     */
    public void setCommandMode()
    {
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_COMMAND_MODE);
        //Log.d(TAG, "Send GOTO_COMMAND_MODE");
        _waitForAckLoop();
        isStreamMode = false;
    }


    /**
     * change sensor to Streaming mode.
     * it will send data too much ,and you have better to change sensor mode to set sensor or get some data you need.
     */
    public void setStreamingMode()
    {
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_STREAM_MODE);
        //Log.d(TAG, "Send GOTO_STREAM_MODE");
        _waitForAckLoop();
        isStreamMode = true;
    }

    public void setImuId(final int id)
    {
        if (!assertConnected())
            return;

        new Thread(new Thread() {
            public void run() {
                boolean b = isStreamMode;
                setCommandMode();
                waitForAck = true;
                lpbusSetInt32(SET_IMU_ID, id);
                _waitForAckLoop();
                _getSensorSettings();
                _saveParameters();
                if (b)
                    setStreamingMode();
            }
        }).start();
    }

    public int getImuId()
    {
        return imuId;
    }

    public int getGyroRange()
    {
        return gyrRange;
    }

    public void setGyroRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_GYR_RANGE_125DPS ||
                range == LPMS_GYR_RANGE_245DPS ||
                range == LPMS_GYR_RANGE_500DPS ||
                range == LPMS_GYR_RANGE_1000DPS ||
                range == LPMS_GYR_RANGE_2000DPS) {

            new Thread(new Thread() {
                public void run() {
                    boolean b = isStreamMode;
                    setCommandMode();
                    waitForAck=true;
                    lpbusSetInt32(SET_GYR_RANGE, range);
                    _waitForAckLoop();
                    _getSensorSettings();
                    _saveParameters();
                    if(b)
                        setStreamingMode();
                }
            }).start();
        }
    }

    public int getAccRange()
    {
        return accRange;
    }

    public void setAccRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_ACC_RANGE_2G ||
                range == LPMS_ACC_RANGE_4G ||
                range == LPMS_ACC_RANGE_8G ||
                range == LPMS_ACC_RANGE_16G ) {

            new Thread(new Thread() {
                public void run() {
                    boolean b = isStreamMode;
                    setCommandMode();
                    waitForAck = true;
                    lpbusSetInt32(SET_ACC_RANGE, range);
                    _waitForAckLoop();
                    _getSensorSettings();
                    _saveParameters();
                    if (b)
                        setStreamingMode();
                }
            }).start();
        }
    }


    public int getMagRange()
    {
        return magRange;
    }

    public void setMagRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_MAG_RANGE_4GAUSS ||
                range == LPMS_MAG_RANGE_8GAUSS ||
                range == LPMS_MAG_RANGE_12GAUSS ||
                range == LPMS_MAG_RANGE_16GAUSS ) {

            new Thread(new Thread() {
                public void run() {
                    boolean b = isStreamMode;
                    setCommandMode();
                    waitForAck = true;
                    lpbusSetInt32(SET_MAG_RANGE, range);
                    _waitForAckLoop();
                    _getSensorSettings();
                    _saveParameters();
                    if (b)
                        setStreamingMode();
                }
            }).start();
        }
    }


    public int getFilterMode()
    {
        return filterMode;
    }

    public void setFilterMode(final int mode)
    {
        if (!assertConnected())
            return;
        if (mode == LPMS_FILTER_GYR ||
                mode == LPMS_FILTER_GYR_ACC ||
                mode == LPMS_FILTER_GYR_ACC_MAG ||
                mode == LPMS_FILTER_MADGWICK_GYR_ACC ||
                mode == LPMS_FILTER_MADGWICK_GYR_ACC_MAG) {

            new Thread(new Thread() {
                public void run() {
                    boolean b = isStreamMode;
                    setCommandMode();
                    waitForAck = true;
                    lpbusSetInt32(SET_FILTER_MODE, mode);
                    _waitForAckLoop();
                    _getSensorSettings();
                    _saveParameters();
                    if (b)
                        setStreamingMode();
                }
            }).start();
        }
    }

    public int getStreamFrequency()
    {
        return streamingFrequency;
    }

    public void setStreamFrequency(final int freq)
    {
        if (!assertConnected())
            return;
        if (freq == LPMS_STREAM_FREQ_5HZ||
                freq == LPMS_STREAM_FREQ_10HZ ||
                freq == LPMS_STREAM_FREQ_25HZ ||
                freq == LPMS_STREAM_FREQ_50HZ ||
                freq == LPMS_STREAM_FREQ_100HZ ||
                freq == LPMS_STREAM_FREQ_200HZ ||
                freq == LPMS_STREAM_FREQ_400HZ ) {

            new Thread(new Thread() {
                public void run() {
                    boolean b = isStreamMode;
                    setCommandMode();
                    waitForAck = true;
                    lpbusSetInt32(SET_STREAM_FREQ, freq);
                    _waitForAckLoop();
                    _getSensorSettings();
                    _saveParameters();
                    if (b)
                        setStreamingMode();
                }
            }).start();
        }
    }


    public void setTransmissionData(final int v)
    {
        if (!assertConnected())
            return;

        new Thread(new Thread() {
            public void run(){
                boolean b = isStreamMode;
                setCommandMode();
                waitForAck = true;
                lpbusSetInt32(SET_TRANSMIT_DATA, v);
                _waitForAckLoop();
                _getSensorSettings();
                _saveParameters();
                if (b)
                    setStreamingMode();
            }
        }).start();

    }

    public void enableGyroData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_GYR_RAW_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_GYR_RAW_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isGyroDataEnabled() {
        return gyrEnable;
    }

    public void enableAccData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_ACC_RAW_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_ACC_RAW_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isAccDataEnabled() {
        return accEnable;
    }

    public void enableMagData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_MAG_RAW_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_MAG_RAW_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isMagDataEnabled() {
        return magEnable;
    }

    public void enableAngularVelData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isAngularVelDataEnable() {
        return angularVelEnable;
    }

    public void enableQuaternionData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_QUAT_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_QUAT_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isQuaternionDataEnabled() {
        return quaternionEnable;
    }

    public void enableEulerData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_EULER_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_EULER_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isEulerDataEnabled() {
        return eulerAngleEnable;
    }

    public void enableLinAccData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_LINACC_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_LINACC_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isLinAccDataEnabled() {
        return linAccEnable;
    }

    public void enablePressureData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_PRESSURE_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_PRESSURE_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isPressureDataEnabled(){
        return pressureEnable;
    }

    public void enableAltitudeData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_ALTITUDE_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_ALTITUDE_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isAltitudeDataEnabled() {
        return altitudeEnable;
    }


    public void enableTemperatureData(boolean b)
    {
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= LPMS_TEMPERATURE_OUTPUT_ENABLED;
        else
            configurationRegister &= ~LPMS_TEMPERATURE_OUTPUT_ENABLED;
        _setTransmissionData();
    }

    public boolean isTemperatureDataEnabled() {
        return temperatureEnable;
    }

    public void enable16BitData()
    {
        if (!assertConnected())
            return;
        configurationRegister |= LPMS_LPBUS_DATA_MODE_16BIT_ENABLED;
        _setTransmissionData();
    }

    public void enable32BitData()
    {
        if (!assertConnected())
            return;
        configurationRegister &= ~LPMS_LPBUS_DATA_MODE_16BIT_ENABLED;
        _setTransmissionData();
    }

    public boolean is16BitDataEnabled()
    {
        if (sixteenBitDataEnable)
            return true;
        else
            return false;
    }
    /**
     * @return
     *		Return the data length of the sensor.
     */
    public int hasNewData() {
        int n;
        synchronized (dataQueue) {
            n = dataQueue.size();
        }
        return n;
    }

    /**
     * @return LpmsBData
     * you can use this to read out data .
     */
    public LpmsBData getLpmsBData() {
        LpmsBData d=null;
        if (!assertConnected())
            return d;
        if (!isStreamMode) {

            synchronized (dataQueue) {
                while (dataQueue.size() > 0) {
                    d = dataQueue.getLast();
                    dataQueue.removeLast();
                }
            }
            waitForData = true;
            lpbusSetNone(GET_SENSOR_DATA);
            //Log.d(TAG, "Send GET_SENSOR_DATA");
            _waitForDataLoop();
        }
        synchronized (dataQueue) {
            if (dataQueue.size() > 0) {
                d = dataQueue.getLast();
                dataQueue.removeLast();
            }
        }

        return d;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }


    public String getDeviceName()
    {
        return deviceName;
    }


    public boolean isStreamingMode()
    {
        return isStreamMode;
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public String getFirmwareInfo()
    {
        return firmwareInfo;
    }

    public void startSyncSensor()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(START_SYNC);
        waitForAck = true;
        _waitForAckLoop();
    }

    public void stopSyncSensor()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(STOP_SYNC);
        waitForAck = true;
        _waitForAckLoop();
    }

    private void testPing()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_PING);
        //Log.d(TAG, "Send GET_PING");
    }

    public void resetFactorySettings()
    {
        if (!assertConnected())
            return;

        boolean b = isStreamMode;
        setCommandMode();
        waitForAck = true;
        lpbusSetNone(RESTORE_FACTORY_VALUE);
        _waitForAckLoop();
        _getSensorSettings();
        _saveParameters();
        if (b)
            setStreamingMode();
    }

    public void setOrientationOffset(int offset){

        if (!assertConnected())
            return;
        if (offset == LPMS_OFFSET_MODE_ALIGNMENT ||
                offset == LPMS_OFFSET_MODE_HEADING ||
                offset == LPMS_OFFSET_MODE_OBJECT) {
            lpbusSetInt32(SET_ORIENTATION_OFFSET, offset);
        }
    }

    public void resetOrientationOffset() {
        if (!assertConnected())
            return;
        lpbusSetNone(RESET_ORIENTATION_OFFSET);
    }

    public void resetTimestamp() {
        if (!assertConnected())
            return;
        lpbusSetInt32(SET_TIME_STAMP, 0);
    }

    public void setTimestamp(int ts) {

        if (!assertConnected())
            return;
        lpbusSetInt32(SET_TIME_STAMP, ts);
    }

    public void getBatteryPercentage() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_BATTERY_LEVEL);
        //Log.d(TAG, "Send  GET_BATTERY_LEVEL");
    }

    public void getBatteryVoltage() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_BATTERY_VOLTAGE);
        //Log.d(TAG, "Send  GET_BATTERY_VOLTAGE");
    }

    public void getChargingStatus() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_CHARGING_STATUS);
        //Log.d(TAG, "Send  GET_CHARGING_STATUS");
    }

    /////////////////////////////////////////////////////////////////////
    // LP Bus Related
    /////////////////////////////////////////////////////////////////////
    void lpbusSetInt32(int command, int v) {
        for (int i = 0; i < 4; ++i) {
            rawTxData[i] = (byte) (v & 0xff);
            v = v >> 8;
        }

        sendData(command, 4);
    }

    void lpbusSetNone(int command) {
        sendData(command, 0);
    }

    void lpbusSetData(int command, int length, byte[] dataBuffer) {
        for (int i = 0; i < length; ++i) {
            rawTxData[i] = dataBuffer[i];
        }

        sendData(command, length);
    }

    void parseSensorData() {
        int o = 0;
        float r2d = 57.2958f;

        mLpmsBData.imuId = imuId;
        mLpmsBData.timestamp = (float) convertRxbytesToInt(o, rxBuffer)*0.0025f;
        o += 4;
        if ( gyrEnable )
        {
            mLpmsBData.gyr[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( accEnable )
        {
            mLpmsBData.acc[0] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
            mLpmsBData.acc[1] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
            mLpmsBData.acc[2] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
        }

        if ( magEnable )
        {
            mLpmsBData.mag[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( angularVelEnable )
        {
            mLpmsBData.angVel[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.angVel[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.angVel[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( quaternionEnable )
        {
            mLpmsBData.quat[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[3] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( eulerAngleEnable )
        {
            mLpmsBData.euler[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.euler[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.euler[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( linAccEnable )
        {
            mLpmsBData.linAcc[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.linAcc[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.linAcc[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( pressureEnable )
        {
            mLpmsBData.pressure = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( altitudeEnable )
        {
            mLpmsBData.altitude = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( temperatureEnable )
        {
            mLpmsBData.temperature = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( heaveEnable )
        {
            mLpmsBData.heave = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        synchronized (dataQueue) {
            if (dataQueue.size() < DATA_QUEUE_SIZE)
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            else {
                dataQueue.removeLast();
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            }
        }

        newDataFlag = true;
    }

    void parseSensorData16Bit() {
        int o = 0;
        float r2d = 57.2958f;

        mLpmsBData.imuId = imuId;
        mLpmsBData.timestamp = (float) convertRxbytesToInt(0, rxBuffer)*0.0025f;

        o += 4;
        mLpmsBData.frameNumber = frameCounter;
        frameCounter++;

        if ( gyrEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.gyr[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f * r2d;
                o += 2;
            }
            //Log.d(TAG, mLpmsBData.gyr[0]+" "+mLpmsBData.gyr[1]+" "+mLpmsBData.gyr[2]);
        }

        if ( accEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.acc[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f;
                o += 2;
            }
        }

        if ( magEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.mag[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 100.0f;
                o += 2;
            }
        }

        if ( angularVelEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.angVel[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f* r2d;
                o += 2;
            }
        }

        if ( quaternionEnable )
        {
            for (int i = 0; i < 4; ++i) {
                mLpmsBData.quat[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f;
                o += 2;
            }
        }

        if ( eulerAngleEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.euler[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f * r2d;
                o += 2;
            }
        }

        if ( linAccEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.linAcc[i] = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 1000.0f;
                o += 2;
            }
        }

        if ( pressureEnable )
        {
            mLpmsBData.pressure = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 100.0f;
            o += 2;
        }

        if ( altitudeEnable )
        {
            mLpmsBData.altitude = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 10.0f;
            o += 2;
        }

        if ( temperatureEnable )
        {
            mLpmsBData.temperature = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 100.0f;
            o += 2;
        }

        if ( heaveEnable )
        {
            mLpmsBData.heave = (float) ((short) (((rxBuffer[o + 1]) << 8) | (rxBuffer[o + 0] & 0xff))) / 100.0f;
            o += 2;
        }

        synchronized (dataQueue) {
            if (dataQueue.size() < DATA_QUEUE_SIZE)
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            else {
                dataQueue.removeLast();
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            }
        }

        newDataFlag = true;
    }

    void parseFunction() {
        switch (currentFunction) {
            case REPLY_ACK:
                //Log.d(TAG, "Received ACK");
                waitForAck = false;
                break;

            case REPLY_NACK:
                //Log.d(TAG, "Received NACK");
                waitForAck = false;
                break;

            case GET_CONFIG:
                configurationRegister = convertRxbytesToInt(0, rxBuffer);
                //Log.d(TAG, "GET_CONFIG: " + configurationRegister);
                configurationRegisterReady = true;
                waitForData = false;
                break;

            case GET_STATUS:
                waitForData = false;
                break;

            case GET_SENSOR_DATA:
                if ( (configurationRegister & LPMS_LPBUS_DATA_MODE_16BIT_ENABLED) != 0) {
                    parseSensorData16Bit();
                } else {
                    parseSensorData();
                }
                waitForData = false;
                break;

            case GET_IMU_ID:
                imuId = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_GYR_RANGE:
                gyrRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_ACC_RANGE:
                accRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_MAG_RANGE:
                magRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_FILTER_MODE:
                filterMode = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_BATTERY_LEVEL:
                mLpmsBData.batteryLevel = convertRxbytesToFloat(0, rxBuffer);
                //Log.d(TAG, "GET_BATTERY_LEVEL " +  mLpmsBData.batteryLevel);
                waitForData = false;
                break;

            case GET_CHARGING_STATUS:
                mLpmsBData.chargingStatus = convertRxbytesToInt(0, rxBuffer);
                //Log.d(TAG, "GET_CHARGING_STATUS ");
                waitForData = false;
                break;

            case GET_BATTERY_VOLTAGE:
                mLpmsBData.batteryVoltage = convertRxbytesToFloat(0, rxBuffer);
                //Log.d(TAG, "GET_BATTERY_VOLTAGE " + mLpmsBData.batteryVoltage);
                waitForData = false;
                break;

            case GET_SERIAL_NUMBER:
                serialNumber = convertRxbytesToString(24, rxBuffer);
                //Log.d(TAG, serialNumber);
                serialNumberReady = true;
                waitForData = false;
                break;

            case GET_DEVICE_NAME:
                deviceName = convertRxbytesToString(16, rxBuffer);
                //Log.d(TAG, deviceName);
                deviceNameReady = true;
                waitForData = false;
                break;

            case GET_FIRMWARE_INFO:
                firmwareInfo = convertRxbytesToString(16, rxBuffer);
                //Log.d(TAG, firmwareInfo);
                firmwareInfoReady = true;
                waitForData = false;
                break;

            case GET_FIRMWARE_VERSION:
                int vmajor = convertRxbytesToInt(8, rxBuffer);
                int vminor = convertRxbytesToInt(4, rxBuffer);
                int vbuild = convertRxbytesToInt(0, rxBuffer);
                firmwareVersion  = Integer.toString(vmajor)+"."+Integer.toString(vminor)+"."+ Integer.toString(vbuild);
                //Log.d(TAG,  "FW Ver: " +  firmwareVersion);
                waitForData = false;
                break;

            case GET_PING:
                float mT;
                mT = (float) convertRxbytesToInt(0, rxBuffer)*0.0025f;
                //Log.d(TAG, "GET_PING: " +  mT );
                waitForData = false;
                break;

            case START_SYNC:
                //Log.d(TAG, "START_SYNC");
                waitForAck = false;
                break;

            case STOP_SYNC:
                //Log.d(TAG, "STOP_SYNC");
                waitForAck = false;
                break;

            case SET_TRANSMIT_DATA:
                waitForData = false;
                break;

            case GET_TEMPERATURE:
                mLpmsBData.temperature = convertRxbytesToFloat(0, rxBuffer);
                //Log.d(TAG, "GET_TEMPERATURE");
                waitForData = false;
                break;
        }
        
        //waitForAck = false;
       // waitForData = false;
    }

    void parse() {
        int lrcReceived = 0;
        //String s="";
        for (int i = 0; i < nBytes; i++) {
            b = rawRxBuffer[i];
        	//s += Integer.toHexString(b& 0xFF) + " " ;
            switch (rxState) {
                case PACKET_END:
                    if (b == 0x3a) {
                        rxState = PACKET_ADDRESS0;
                    }
                    break;

                case PACKET_ADDRESS0:
                    inBytes[0] = b;
                    rxState = PACKET_ADDRESS1;
                    break;

                case PACKET_ADDRESS1:
                    inBytes[1] = b;
                    currentAddress = convertRxbytesToInt16(0, inBytes);
                    imuId = currentAddress;
                    rxState = PACKET_FUNCTION0;
                    break;

                case PACKET_FUNCTION0:
                    inBytes[0] = b;
                    rxState = PACKET_FUNCTION1;
                    break;

                case PACKET_FUNCTION1:
                    inBytes[1] = b;
                    currentFunction = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_LENGTH0;
                    break;

                case PACKET_LENGTH0:
                    inBytes[0] = b;
                    rxState = PACKET_LENGTH1;
                    break;

                case PACKET_LENGTH1:
                    inBytes[1] = b;
                    currentLength = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_RAW_DATA;
                    rxIndex = 0;
                    break;

                case PACKET_RAW_DATA:
                    if (rxIndex == currentLength) {
                        lrcCheck = (currentAddress & 0xffff) + (currentFunction & 0xffff) + (currentLength & 0xffff);
                        for (int j = 0; j < currentLength; j++) {
                            if (j < MAX_BUFFER) {
                                lrcCheck += (int) rxBuffer[j] & 0xff;
                            } else break;

                        }

                        inBytes[0] = b;
                        rxState = PACKET_LRC_CHECK1;
                    } else {
                        if (rxIndex < MAX_BUFFER) {

                            rxBuffer[rxIndex] = b;
                            rxIndex++;
                        } else break;
                    }
                    break;

                case PACKET_LRC_CHECK1:
                    inBytes[1] = b;

                    lrcReceived = convertRxbytesToInt16(0, inBytes);
                    lrcCheck = lrcCheck & 0xffff;
                    
                    if (lrcReceived == lrcCheck) 
                    {
                        parseFunction();                   
                    }

                    rxState = PACKET_END;
                    break;

                default:
                    rxState = PACKET_END;
                    break;
            }
        }
       // Log.d(TAG, "[LpmsBThread] Received: " + s);
    }

    void sendData(int function, int length) {
        int txLrcCheck;

        txBuffer[0] = 0x3a;
        convertInt16ToTxbytes(imuId, 1, txBuffer);
        convertInt16ToTxbytes(function, 3, txBuffer);
        convertInt16ToTxbytes(length, 5, txBuffer);
        
        for (int i = 0; i < length; ++i) {
            txBuffer[7 + i] = rawTxData[i];
        }
        
        txLrcCheck = (imuId & 0xffff) + (function & 0xffff) + (length & 0xffff);

        for (int j = 0; j < length; j++) {
            txLrcCheck += (int) rawTxData[j] & 0xff;
        }

        convertInt16ToTxbytes(txLrcCheck, 7 + length, txBuffer);
        txBuffer[9 + length] = 0x0d;
        txBuffer[10 + length] = 0x0a;

        String s="";
        
        for (int i = 0; i < 11 + length; i++) {
        	s += Integer.toHexString(txBuffer[i] & 0xFF) + " " ;
        }
        //Log.d(TAG, "[LpmsBThread] Sending: " + s);
        
        try {
           // Log.d(TAG, "[LpmsBThread] Sending data");
            mOutStream.write(txBuffer, 0, length + 11);
        } catch (Exception e) {
            //Log.d(TAG, "[LpmsBThread] Error while sending data");
            e.printStackTrace();
           // throw new RuntimeException(e);
        }
    }

    void sendAck() {
        sendData(REPLY_ACK, 0);
    }

    void sendNack() {
        sendData(REPLY_NACK, 0);
    }

    float convertRxbytesToFloat(int offset, byte buffer[]) {
    	int l;                              
    	
        l = buffer[offset + 0];                                  
        l &= 0xff;                                         
        l |= ((long) buffer[offset + 1] << 8);                   
        l &= 0xffff;                                       
        l |= ((long) buffer[offset + 2] << 16);                  
        l &= 0xffffff;                                     
        l |= ((long) buffer[offset + 3] << 24);
        
        return Float.intBitsToFloat(l);
    }

    int convertRxbytesToInt(int offset, byte buffer[]) {
    	int v;    
        v = (int) ((buffer[offset] & 0xFF)   
                | ((buffer[offset+1] & 0xFF)<<8)   
                | ((buffer[offset+2] & 0xFF)<<16)   
                | ((buffer[offset+3] & 0xFF)<<24));  
        return v;
        
    }

    int convertRxbytesToInt16(int offset, byte buffer[]) {
        int v;
        //TODO
        v= (int) ((buffer[offset]&0xFF)   
                | ((buffer[offset+1]<<8) & 0xFF00)); 
        
        return v;
    }

    String convertRxbytesToString(int length, byte buffer[]) {
        byte[] t = new byte[length];
        for (int i = 0; i < length; i++) {
            t[i] = buffer[i];
        }

        String decodedString = new String(t).trim();
        return decodedString;
    }

    void convertIntToTxbytes(int v, int offset, byte buffer[]) {
        byte[] t = ByteBuffer.allocate(4).putInt(v).array();

        for (int i = 0; i < 4; i++) {
            buffer[3 - i + offset] = t[i];
        }
    }

    void convertInt16ToTxbytes(int v, int offset, byte buffer[]) {
        byte[] t = ByteBuffer.allocate(2).putShort((short) v).array();

        for (int i = 0; i < 2; i++) {
            buffer[1 - i + offset] = t[i];
        }
    }

    void convertFloatToTxbytes(float f, int offset, byte buffer[]) {
        int v = Float.floatToIntBits(f);
        byte[] t = ByteBuffer.allocate(4).putInt(v).array();

        for (int i = 0; i < 4; i++) {
            buffer[3 - i + offset] = t[i];
        }
    }

    /*
    public boolean startLogging() {
        if (connectionStatus == SENSOR_STATUS_DISCONNECTED) {
            dl.setStatusMesg("No sensor connected");
            return false;
        }
        if (dl.startLogging()) {
            resetTimestamp();
            return true;
        }
        return false;
    }

    public boolean stopLogging() {
        return dl.stopLogging();
    }

    public boolean isLogging() {
        return dl.isLogging();
    }

    public String getLoggerStatusMesg() {
        return dl.getStatusMesg();
    }

    public String getOutputFilename() {
       return dl.getOutputFilename();
   }
*/
    private void _waitForAckLoop()
    {
        int timeout = 0;
        while (timeout++ < 30  && waitForAck)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
               // throw new RuntimeException(e);
            }
        }
    }

    private void _waitForDataLoop()
    {
        int timeout = 0;
        while (timeout++ < 30  && waitForData)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
               // throw new RuntimeException(e);
            }
        }
    }

    private void _getSensorSettings()
    {
        _getConfig();
        _getGyroRange();
        _getAccRange();
        _getMagRange();
        _getFilterMode();
        printConfig();

    }

    private void _getConfig()
    {
        configurationRegisterReady = false;
        lpbusSetNone(GET_CONFIG);
        //Log.d(TAG, "Send GET_CONFIG");
        int timeout = 0;
        while (timeout++ < 30  && !configurationRegisterReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
                // new RuntimeException(e);
            }
        }
        // Parse configuration
        parseConfig(configurationRegister);
    }


    private void parseConfig(int config )
    {
        //Log.d(TAG, "config: " + config);

        // Stream frequency
        if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_5HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_5HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_10HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_10HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_25HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_25HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_50HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_50HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_100HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_100HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_200HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_200HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_400HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_400HZ;

        if ( (config & LPMS_GYR_RAW_OUTPUT_ENABLED) != 0) {
            gyrEnable = true;
            //Log.d(TAG, "GYRO ENABLED");
        }else
        {
            gyrEnable = false;
            //Log.d(TAG, "GYRO DISABLED");
        }
        if ( (config  &LPMS_ACC_RAW_OUTPUT_ENABLED) != 0) {
            accEnable = true;
            //Log.d(TAG, "ACC ENABLED");
        }else
        {
            accEnable = false;
            //Log.d(TAG, "ACC DISABLED");
        }
        if ( (config & LPMS_MAG_RAW_OUTPUT_ENABLED) != 0) {
            magEnable = true;
            //Log.d(TAG, "MAG ENABLED");
        }
        else
        {
            magEnable = false;
            //Log.d(TAG, "MAG DISABLED");
        }
        if ( (config & LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED) != 0) {
            angularVelEnable = true;
            //Log.d(TAG, "AngVel ENABLED");
        }
        else
        {
            angularVelEnable = false;
            //Log.d(TAG, "AngVel DISABLED");
        }
        if ( (config & LPMS_QUAT_OUTPUT_ENABLED) != 0) {
            quaternionEnable = true;
            //Log.d(TAG, "QUAT ENABLED");
        }
        else
        {
            quaternionEnable = false;
            //Log.d(TAG, "QUAT DISABLED");
        }
        if ( (config & LPMS_EULER_OUTPUT_ENABLED) != 0) {
            eulerAngleEnable = true;
            //Log.d(TAG, "EULER ENABLED");
        }
        else
        {
            eulerAngleEnable = false;
            //Log.d(TAG, "EULER DISABLED");
        }
        if ( (config & LPMS_LINACC_OUTPUT_ENABLED) != 0) {
            linAccEnable =true;
            //Log.d(TAG, "LINACC ENABLED");
        }
        else
        {
            linAccEnable = false;
            //Log.d(TAG, "LINACC DISABLED");
        }
        if ( (config & LPMS_PRESSURE_OUTPUT_ENABLED) != 0) {
            pressureEnable = true;
            //Log.d(TAG, "PRESSURE ENABLED");
        }
        else
        {
            pressureEnable = false;
            //Log.d(TAG, "PRESSURE DISABLED");
        }

        if ( (config & LPMS_TEMPERATURE_OUTPUT_ENABLED) != 0) {
            temperatureEnable = true;
            //Log.d(TAG, "PRESSURE ENABLED");
        }
        else
        {
            temperatureEnable = false;
            //Log.d(TAG, "PRESSURE DISABLED");
        }

        if ( (config & LPMS_ALTITUDE_OUTPUT_ENABLED) != 0) {
            altitudeEnable = true;
            //Log.d(TAG, "PRESSURE ENABLED");
        }
        else
        {
            altitudeEnable = false;
            //Log.d(TAG, "PRESSURE DISABLED");
        }
        if ( (config & LPMS_HEAVEMOTION_OUTPUT_ENABLED) != 0) {
            heaveEnable = true;
            //Log.d(TAG, "heave ENABLED");
        }
        else
        {
            heaveEnable = false;
            //Log.d(TAG, "heave DISABLED");
        }

        if ( (config & LPMS_LPBUS_DATA_MODE_16BIT_ENABLED) != 0) {
            sixteenBitDataEnable = true;
            //Log.d(TAG, "16 bit ENABLED");
        }
        else
        {
            sixteenBitDataEnable = false;
            //Log.d(TAG, "16 bit DISABLED");
        }

        //Log.d(TAG, "Stream freq: " + streamingFrequency);
    }

    private void _getGyroRange()
    {
        waitForData = true;
        lpbusSetNone(GET_GYR_RANGE);
        //Log.d(TAG, "Send GET_GYR_RANGE");
        _waitForDataLoop();
        //Log.d(TAG, "Gyro range: " + gyrRange + "dps");
    }

    private void _getAccRange()
    {
        waitForData = true;
        lpbusSetNone(GET_ACC_RANGE);
        //Log.d(TAG, "Send GET_ACC_RANGE");
        _waitForDataLoop();
        //Log.d(TAG, "Acc range: " + accRange + "g");
    }

    private void _getMagRange()
    {
        waitForData = true;
        lpbusSetNone(GET_MAG_RANGE);
        //Log.d(TAG, "Send GET_MAG_RANGE");
        _waitForDataLoop();
        //Log.d(TAG, "Mag range: " + magRange + "mT");
    }

    private void _getFilterMode()
    {
        waitForData = true;
        lpbusSetNone(GET_FILTER_MODE);
        //Log.d(TAG, "Send GET_FILTER_MODE");
        _waitForDataLoop();
        //Log.d(TAG, "Filter mode: " + filterMode);
    }

    private void _getSerialNumber()
    {
        serialNumberReady = false;
        serialNumber = "";
        lpbusSetNone(GET_SERIAL_NUMBER);
        int timeout = 0;
        while (timeout++ < 30 && !serialNumberReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
    private void _getDeviceName()
    {
        deviceNameReady = false;
        deviceName = "";
        lpbusSetNone(GET_DEVICE_NAME);
        int timeout = 0;
        while (timeout++ < 30 && !deviceNameReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

    private void _getFirmwareInfo()
    {
        firmwareInfoReady = false;
        firmwareInfo = "";
        lpbusSetNone(GET_FIRMWARE_INFO);
        int timeout = 0;
        while (timeout++ < 30 && !firmwareInfoReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }
    }

    private void _saveParameters()
    {
        waitForAck = true;
        lpbusSetNone(WRITE_REGISTERS);
        _waitForAckLoop();
    }

    private void _setTransmissionData()
    {
        new Thread(new Thread() {
            public void run() {
                boolean b = isStreamMode;
                setCommandMode();
                waitForAck = true;
                lpbusSetInt32(SET_TRANSMIT_DATA, configurationRegister);
                _waitForAckLoop();
                _getSensorSettings();
                _saveParameters();
                if (b)
                    setStreamingMode();
            }
        }).start();
    }

    private boolean assertConnected()
    {
        if ( connectionStatus == SENSOR_STATUS_CONNECTED )
            return true;
        return false;
    }


    private void printConfig()
    {
        //Log.d(TAG, "config: " + config);
        Log.d(TAG, "SN: " + serialNumber);
        Log.d(TAG, "FW: " + firmwareInfo);
        Log.d(TAG, "DN: " + deviceName);
        Log.d(TAG, "ImuId: " + imuId);
        Log.d(TAG, "StreamFreq: " + streamingFrequency );
        Log.d(TAG, "Gyro: " + gyrRange);
        Log.d(TAG, "Acc: " + accRange);
        Log.d(TAG, "Mag: " + magRange);

        if (gyrEnable) {
            Log.d(TAG, "GYRO ENABLED");
        }else {
            Log.d(TAG, "GYRO DISABLED");
        }
        if ( accEnable) {
            Log.d(TAG, "ACC ENABLED");
        }else {
            Log.d(TAG, "ACC DISABLED");
        }
        if ( magEnable ) {
            Log.d(TAG, "MAG ENABLED");
        } else {
            Log.d(TAG, "MAG DISABLED");
        }
        if (angularVelEnable) {
            Log.d(TAG, "AngVel ENABLED");
        } else {
            Log.d(TAG, "AngVel DISABLED");
        }
        if (quaternionEnable) {
            Log.d(TAG, "QUAT ENABLED");
        } else {
            Log.d(TAG, "QUAT DISABLED");
        }
        if ( eulerAngleEnable) {
            Log.d(TAG, "EULER ENABLED");
        } else {
            Log.d(TAG, "EULER DISABLED");
        }
        if (linAccEnable) {
            Log.d(TAG, "LINACC ENABLED");
        } else {
            Log.d(TAG, "LINACC DISABLED");
        }
        if ( pressureEnable) {
            Log.d(TAG, "PRESSURE ENABLED");
        } else {
            Log.d(TAG, "PRESSURE DISABLED");
        }
        if ( altitudeEnable) {
            Log.d(TAG, "ALTITUDE ENABLED");
        } else {
            Log.d(TAG, "ALTITUDE DISABLED");
        }
        if ( temperatureEnable) {
            Log.d(TAG, "TEMPERATURE ENABLED");
        } else {
            Log.d(TAG, "TEMPERATURE DISABLED");
        }

        if ( heaveEnable) {
            Log.d(TAG, "heave ENABLED");
        } else {
            Log.d(TAG, "heave DISABLED");
        }

        if (sixteenBitDataEnable) {
           Log.d(TAG, "16 bit ENABLED");
        } else {
            Log.d(TAG, "16 bit DISABLED");
        }

    }


    public class ClientReadThread implements Runnable {

        public void run() {
            while (mSocket.isConnected()) {
                try {
                    nBytes = mInStream.read(rawRxBuffer);
                } catch (Exception e) {
                	break;
                }

                parse();

            }

        }
    }
}
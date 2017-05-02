package com.lpresearch.lpsensorlib;

public class LpmsBData {
	public int imuId = 0;
	public float timestamp = 0.0f;
	public int frameNumber;
	public float[] gyr = new float[3];
	public float[] acc = new float[3];
	public float[] mag = new float[3];
	public float[] quat = new float[4];
	public float[] euler = new float[3];
	public float[] linAcc = new float[3];
	public float[] angVel = new float[3];
	public float pressure;
	public float altitude;
	public float batteryLevel;
	public float batteryVoltage;
	public int chargingStatus;
	public float temperature;
	public float heave;

	public LpmsBData() {
		reset();
	}
	
	public LpmsBData(LpmsBData d) {
		imuId = d.imuId;
		timestamp = d.timestamp;
		frameNumber = d.frameNumber;
		pressure = d.pressure;
		altitude = d.altitude;
		batteryLevel = d.batteryLevel;
		batteryVoltage = d.batteryVoltage;
		temperature = d.temperature;
		chargingStatus = d.chargingStatus;
		heave = d.heave;

		for (int i=0; i<3; i++) gyr[i] = d.gyr[i];
		for (int i=0; i<3; i++) acc[i] = d.acc[i];
		for (int i=0; i<3; i++) mag[i] = d.mag[i];
		for (int i=0; i<3; i++) angVel[i] = d.angVel[i];
		for (int i=0; i<4; i++) quat[i] = d.quat[i];
		for (int i=0; i<3; i++) euler[i] = d.euler[i];
		for (int i=0; i<3; i++) linAcc[i] = d.linAcc[i];
	}


	public void reset()
	{
		imuId = 0;
		timestamp = 0;
		frameNumber = 0;
		pressure = 0;
		altitude = 0;
		batteryLevel = 0;
		batteryVoltage = 0;
		temperature = 0;
		chargingStatus = 0;
		heave = 0;

		for (int i=0; i<3; i++) gyr[i] = 0;
		for (int i=0; i<3; i++) acc[i] = 0;
		for (int i=0; i<3; i++) mag[i] = 0;
		for (int i=0; i<3; i++) angVel[i] = 0;
		for (int i=0; i<4; i++) quat[i] = 0;
		for (int i=0; i<3; i++) euler[i] = 0;
		for (int i=0; i<3; i++) linAcc[i] = 0;

	}
}
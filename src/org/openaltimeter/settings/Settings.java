package org.openaltimeter.settings;

import org.openaltimeter.TypeConverter;

public class Settings {
	
	public enum BatteryType { NIMH, LIPO, NONE };
	public enum SettingsFormat { V2_FORMAT };
	
	//TODO: this is a fudge
	public static int SETTINGS_SIZE = 17;

	public int logIntervalMS;
	public float heightUnits;
	public BatteryType batteryType;
    // if the battery type is NIMH, then this is the threshold, for LIPO it's the per cell threshold
	public float lowVoltageThreshold;
	public float batteryMonitorCalibration;
	public boolean logServo;
    
    // creates the settings object from a stream of bytes, as output by the
    // OA board
    public Settings(byte[] bytes, SettingsFormat format) {
    	switch (format) {
	    	case V2_FORMAT:
	    		parseV2Bytes(bytes);
	    		break;
    	}
    }
    
    public Settings() {	}
    
    public boolean equals(Settings other) {
		return ((logIntervalMS == other.logIntervalMS) &&
				(heightUnits == other.heightUnits) &&
				(batteryType == other.batteryType) &&
				(lowVoltageThreshold == other.lowVoltageThreshold) &&
				(batteryMonitorCalibration == other.batteryMonitorCalibration) &&
				(logServo == other.logServo));
    }

	private void parseV2Bytes(byte[] bytes) {
    	logIntervalMS = TypeConverter.bytesToSignedInt(bytes[0], bytes[1], (byte)0x00, (byte)0x00);
    	heightUnits = TypeConverter.bytesToFloat(bytes[2], bytes[3], bytes[4], bytes[5]);
    	int batteryTypeByte = bytes[6];
    	switch (batteryTypeByte) {
    	case 0:
    		batteryType = BatteryType.NIMH;
    		break;
    	case 1:
    		batteryType = BatteryType.LIPO;
    		break;
    	case 2:
       		batteryType = BatteryType.NONE;
       		break;
    	}
        lowVoltageThreshold = TypeConverter.bytesToFloat(bytes[8], bytes[9], bytes[10], bytes[11]);
        batteryMonitorCalibration = TypeConverter.bytesToFloat(bytes[12], bytes[13], bytes[14], bytes[15]);
        logServo = !(bytes[16] == 0);
      	    		
    }
    
    // produces a byte array suitable for sending to the OA board
    public byte[] toByteArray() {
    	byte[] bytes = new byte[SETTINGS_SIZE];
    	byte[] liBytes = TypeConverter.unsignedShortToBytes(logIntervalMS);
    	bytes[0] = liBytes[0];
    	bytes[1] = liBytes[1];
    	byte[] huBytes = TypeConverter.floatToBytes(heightUnits);
       	bytes[2] = huBytes[0];
    	bytes[3] = huBytes[1];
       	bytes[4] = huBytes[2];
    	bytes[5] = huBytes[3];
    	switch (batteryType) {
    	case NIMH:
    		bytes[6] = 0;
    		break;
    	case LIPO:
    		bytes[6] = 1;
    		break;
    	case NONE:
    		bytes[6] = 2;
       		break;
    	}	
    	bytes[7] = 0;
       	byte[] lvBytes = TypeConverter.floatToBytes(lowVoltageThreshold);
       	bytes[8] = lvBytes[0];
    	bytes[9] = lvBytes[1];
       	bytes[10] = lvBytes[2];
    	bytes[11] = lvBytes[3];
       	byte[] bmBytes = TypeConverter.floatToBytes(batteryMonitorCalibration);
       	bytes[12] = bmBytes[0];
    	bytes[13] = bmBytes[1];
       	bytes[14] = bmBytes[2];
    	bytes[15] = bmBytes[3];
    	bytes[16] = (byte)(logServo ? 1 : 0);
    	return bytes;
    }
}
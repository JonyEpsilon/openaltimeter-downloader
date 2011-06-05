package org.openaltimeter.settings;

import org.openaltimeter.TypeConverter;

public class Settings {

	public enum BatteryType {
		NIMH, LIPO, NONE
	};

	public enum Action {
		DO_NOTHING, OUTPUT_MAX_HEIGHT, OUTPUT_MAX_LAUNCH_HEIGHT, 
		OUTPUT_LAUNCH_WINDOW_END_HEIGHT, OUTPUT_BATTERY_VOLTAGE
	};

	// TODO: this is a fudge
	public static int SETTINGS_SIZE = 21;

	public int logIntervalMS;
	public float heightUnits;
	public BatteryType batteryType;
	// if the battery type is NIMH, then this is the threshold, for LIPO it's
	// the per cell threshold
	public float lowVoltageThreshold;
	public float batteryMonitorCalibration;
	public boolean logServo;
	public Action midPositionAction;
	public Action onPositionAction;

	// creates the settings object from a stream of bytes, as output by the
	// OA board
	public Settings(byte[] bytes) {
		parseBytes(bytes);
	}

	public Settings() {
	}

	public boolean equals(Settings other) {
		return ((logIntervalMS == other.logIntervalMS) && (heightUnits == other.heightUnits)
				&& (batteryType == other.batteryType) && (lowVoltageThreshold == other.lowVoltageThreshold)
				&& (batteryMonitorCalibration == other.batteryMonitorCalibration) && (logServo == other.logServo)
				&& (midPositionAction == other.midPositionAction) && (onPositionAction == other.onPositionAction));
	}

	private void parseBytes(byte[] bytes) {
		logIntervalMS = TypeConverter.bytesToSignedInt(bytes[0], bytes[1], (byte) 0x00, (byte) 0x00);
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
		int midPositionActionByte = bytes[17];
		midPositionAction = parseActionByte(midPositionActionByte);
		int onPositionActionByte = bytes[19];
		onPositionAction = parseActionByte(onPositionActionByte);
	}

	public Action parseActionByte(int actionByte) {
		Action action = Action.DO_NOTHING;
		switch (actionByte) {
		case 0:
			action = Action.DO_NOTHING;
			break;
		case 1:
			action = Action.OUTPUT_MAX_HEIGHT;
			break;
		case 2:
			action = Action.OUTPUT_MAX_LAUNCH_HEIGHT;
			break;
		case 3:
			action = Action.OUTPUT_LAUNCH_WINDOW_END_HEIGHT;
			break;
		case 4:
			action = Action.OUTPUT_BATTERY_VOLTAGE;
			break;
		}
		return action;
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
		bytes[16] = (byte) (logServo ? 1 : 0);
		bytes[17] = actionToByte(midPositionAction);
		bytes[18] = 0;
		bytes[19] = actionToByte(onPositionAction);
		bytes[20] = 0;
		return bytes;
	}
	
	public byte actionToByte(Action a) {
		byte b = 0;
		switch (a) {
		case DO_NOTHING:
			b = 0;
			break;
		case OUTPUT_MAX_HEIGHT:
			b = 1;
			break;
		case OUTPUT_MAX_LAUNCH_HEIGHT:
			b = 2;
			break;
		case OUTPUT_LAUNCH_WINDOW_END_HEIGHT:
			b = 3;
			break;
		case OUTPUT_BATTERY_VOLTAGE:
			b = 4;
			break;
		}
		return b;
	}
}
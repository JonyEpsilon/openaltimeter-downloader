/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010-2011  Jony Hudson, Jan Steidl
    http://openaltimeter.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openaltimeter.data;

import org.openaltimeter.TypeConverter;

public class LogEntry {
	
	// note that pressure must be long, and servo int as Java doesn't support
	// unsigned integer types.
	public long pressure;
	public double temperature;
	public double battery;
	public double altitude;
	public int servo;
	
	public static final int DATASTORE_LOG_ENTRY_SIZE = 5;

	public LogEntry() {}
	
	public static LogEntry logEntryFromBytes(byte[] b, int os)
	{
		return logEntryFromV1ByteFormat(b, os);
	}
	
	// The V1 format applies some linear transformations to the data
	// before storing it so that it can be stored in smaller data types.
	// This function reverses those transformations. See the firmware
	// source code for full documentation of this optimisation.
	private static LogEntry logEntryFromV1ByteFormat(byte[] b, int os)
	{
		LogEntry le = new LogEntry();
		int pressureRaw = TypeConverter.bytesToSignedShort(b[os + 0], b[os + 1]);
		int temperatureRaw = TypeConverter.byteToUnsignedByte(b[os + 2]);
		int batteryRaw = TypeConverter.byteToUnsignedByte(b[os + 3]);
		int servoRaw = TypeConverter.byteToUnsignedByte(b[os + 4]);
		// look out for empty entry
		if ( (pressureRaw == -1) && (temperatureRaw == 255) && (batteryRaw == 255) )
		{
			le.pressure = -1;
			le.temperature = -1;
			le.battery = -1;
			le.servo = -1;
		} else {
			le.pressure = (int)pressureRaw + 101325;
			le.temperature = ((temperatureRaw * 2.5) - 150.0) / 10.0;
			le.battery = 2.0 + (0.05 * (double)batteryRaw);
			if (servoRaw == 0) le.servo = 0;
			else le.servo = ((int)servoRaw * 8) + 500;
		}
		return le;
	}
	
	public void fromRawData(String line) {
		String[] splitLine = line.split("[: ]");
		// try not to be fooled by blank lines etc
		if (splitLine.length >= 9) {
			pressure = Integer.parseInt(splitLine[2]);
			temperature = Double.parseDouble(splitLine[5]);
			battery = Double.parseDouble(splitLine[8]);	
			if (splitLine.length >= 12) 
				servo = Integer.parseInt(splitLine[11]);
			else
				servo = 0;
		} else {
			pressure = -1;
			temperature = -1;
			battery = 0.0;
			servo = 0;
		}
	}
	
	public String rawDataToString()
	{
		return "P: " + pressure + " T: " + temperature + " B: " + battery + " S: " + servo;
	}
	
	public String rawDataToUploadString() {
		return "P: " + pressure + " T: " + 
			((int)(temperature * 100)) + " B: " 
			+ ((int)(battery * 100)) + " S: " + servo;
	}

}

/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010  Jony Hudson, Jan Steidl
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

public class LogEntry {
	
	public long pressure;
	public double temperature;
	public double battery;
	public double altitudeFt;
	public double altitudeM;
	public byte servo;
	
	private static final long MAX_PRESSURE = 131071;
	
	public LogEntry() {}
	
	// this method makes a LogEntry from a set of raw bytes. The bytes can be
	// part of a larger array - the bytes starting at offset will be used.
	public LogEntry(byte[] data, int os)
	{
		// representation of data in 32 bits of Long:
		// bits 1 .. 17 => pressure, 18 .. 25 => 26 .. 32 => temperature
		long bits = bytesToLong(data[os + 0], data[os + 1], data[os + 2], data[os + 3]);
		
		pressure = (bits >> 15) < MAX_PRESSURE ? bits >> 15 : -1;
		// temperature has 10 degrees offset, to represent range -10 to +54 degrees 
		temperature = (bits & 0x7FL) / 2.0 - 10;
		// battery has 2V offset, to represent range 2 to 14,8V
		battery = ((bits >> 7) & 0xFFL) / 20.0 + 2;
		
		servo = data[os + 4];
	}
	
	public String rawDataToString()
	{
		return "P: " + pressure + " T: " + temperature + " B: " + battery + " S: " + servo;
	}

	// Transformation of bytes to Long representing compressed data 
	private long bytesToLong(byte b0, byte b1, byte b2, byte b3)
	{
		long retVal = 0;

        long firstByte = b0 < 0 ? b0 + 256 : b0;
        long secondByte = b1 < 0 ? b1 + 256 : b1;
        long thirdByte = b2 < 0 ? b2 + 256 : b2;
        long fourthByte = b3 < 0 ? b3 + 256 : b3;
        
		retVal = ((firstByte << 24) | (secondByte << 16) | (thirdByte << 8) | fourthByte) & 0xFFFFFFFFL;
		
		return retVal;
	}

	public void fromRawData(String line) {
		String[] splitLine = line.split("[: ]");
		// try not to be fooled by blank lines etc
		if (splitLine.length >= 9) {
			pressure = Integer.parseInt(splitLine[2]);
			temperature = Double.parseDouble(splitLine[5]);
			battery = Double.parseDouble(splitLine[8]);	
			if (splitLine.length >= 12) 
				servo = Byte.parseByte(splitLine[11]);
			else
				servo = 0;
		} else {
			pressure = -1;
			temperature = -1;
			battery = 0.0;
			servo = 0;
		}
	}

}

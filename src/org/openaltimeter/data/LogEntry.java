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
	
	public int pressure;
	public double temperature;
	public double battery;
	public double altitudeFt;
	public double altitudeM;
	
	public LogEntry() {}
	
	// this method makes a LogEntry from a set of raw bytes. The bytes can be
	// part of a larger array - the bytes starting at offset will be used.
	public LogEntry(byte[] data, int os)
	{
		pressure = bytesToInt(data[os + 0], data[os + 1], data[os + 2], data[os + 3]);
		temperature = (double)bytesToInt(data[os + 4], data[os + 5], data[os + 6], data[os + 7]) / 10.0;
		battery = bytesToFloat(data[os + 8], data[os + 9], data[os + 10], data[os + 11]);

	}
	
	public String rawDataToString()
	{
		return "P: " + pressure + " T: " + temperature + " B: " + battery;
	}
	
	// I'm sure there must be a way to do this built in, but I can't find it.
	// This assumes little-endian byte order, suitable for AVR-GCC
	private int bytesToInt(byte b0, byte b1, byte b2, byte b3)
	{
		int i = 0;
		i += ((int)b3 & 0x000000FF) << 24;
		i += ((int)b2 & 0x000000FF) << 16;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return i;
	}
	
	private float bytesToFloat(byte b0, byte b1, byte b2, byte b3)
	{
		int i = 0;
		i += ((int)b3 & 0x000000FF) << 24;
		i += ((int)b2 & 0x000000FF) << 16;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return Float.intBitsToFloat(i);
	}

	public void fromRawData(String line) {
		String[] splitLine = line.split("[: ]");
		// try not to be fooled by blank lines etc
		if (splitLine.length >= 9) {
			pressure = Integer.parseInt(splitLine[2]);
			temperature = Double.parseDouble(splitLine[5]);
			battery = Double.parseDouble(splitLine[8]);	
		} else {
			pressure = -1;
			temperature = -1;
			battery = 0.0;
		}
	}

}

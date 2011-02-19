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
	
	public enum DataFormat {
		BETA_FORMAT,
		JAN_BETA_FORMAT,
		V1_FORMAT
	}
	
	public LogEntry() {}
	
	public static LogEntry logEntryFromBytes(byte[] b, int os, DataFormat format)
	{
		LogEntry le = new LogEntry();
		switch(format) {
			case BETA_FORMAT:
				le =  logEntryFromBetaByteFormat(b, os);
				break;
			case JAN_BETA_FORMAT:
				le = logEntryFromJanBetaByteFormat(b, os);
				break;
			case V1_FORMAT:
				le = logEntryFromV1ByteFormat(b, os);
				break;
		}
		return le;
	}
	
	private static LogEntry logEntryFromBetaByteFormat(byte[] b, int os)
	{
		LogEntry le = new LogEntry();
		le.pressure = bytesToInt(b[os + 0], b[os + 1], b[os + 2], b[os + 3]);
		le.temperature = (double)bytesToInt(b[os + 4], b[os + 5], b[os + 6], b[os + 7]) / 10.0;
		le.battery = bytesToFloat(b[os + 8], b[os + 9], b[os + 10], b[os + 11]);
		le.servo = 0;
		
		return le;
	}
	
	// This method parses the compressed data format the Jan used for a while.
	// It's here for compatibility purposes.
	private static LogEntry logEntryFromJanBetaByteFormat(byte[] b, int os)
	{
		LogEntry le = new LogEntry();
		// representation of data in 32 bits of Long:
		// bits 1 .. 17 => pressure, 18 .. 25 => 26 .. 32 => temperature
		long bits = bytesToCompressedLong(b[os + 0], b[os + 1], b[os + 2], b[os + 3]);
		
		le.pressure = (bits >> 15) < MAX_PRESSURE ? bits >> 15 : -1;
		// temperature has 10 degrees offset, to represent range -10 to +54 degrees 
		le.temperature = (bits & 0x7FL) / 2.0 - 10;
		// battery has 2V offset, to represent range 2 to 14,8V
		le.battery = ((bits >> 7) & 0xFFL) / 20.0 + 2;
		
		le.servo = b[os + 4];
		
		return le;
	}
	
	private static LogEntry logEntryFromV1ByteFormat(byte[] b, int os)
	{
		return new LogEntry();
	}

	// Transformation of bytes to Long representing compressed data 
	private static long bytesToCompressedLong(byte b0, byte b1, byte b2, byte b3)
	{
		long retVal = 0;

        long firstByte = b0 < 0 ? b0 + 256 : b0;
        long secondByte = b1 < 0 ? b1 + 256 : b1;
        long thirdByte = b2 < 0 ? b2 + 256 : b2;
        long fourthByte = b3 < 0 ? b3 + 256 : b3;
        
		retVal = ((firstByte << 24) | (secondByte << 16) | (thirdByte << 8) | fourthByte) & 0xFFFFFFFFL;
		
		return retVal;
	}
	
	// I'm sure there must be a way to do this built in, but I can't find it.
	// This assumes little-endian byte order, suitable for AVR-GCC
	private static int bytesToInt(byte b0, byte b1, byte b2, byte b3)
	{
		int i = 0;
		i += ((int)b3 & 0x000000FF) << 24;
		i += ((int)b2 & 0x000000FF) << 16;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return i;
	}
	
	private static float bytesToFloat(byte b0, byte b1, byte b2, byte b3)
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
	
	public String rawDataToString()
	{
		return "P: " + pressure + " T: " + temperature + " B: " + battery + " S: " + servo;
	}

}

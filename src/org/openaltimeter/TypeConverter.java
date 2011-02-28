/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010  Jony Hudson
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
package org.openaltimeter;

public class TypeConverter {
	
	// I'm sure there must be a way to do this built in, but I can't find it.
	// This assumes little-endian byte order, suitable for AVR-GCC
	public static int bytesToSignedInt(byte b0, byte b1, byte b2, byte b3)
	{
		int i = 0;
		i += ((int)b3 & 0x000000FF) << 24;
		i += ((int)b2 & 0x000000FF) << 16;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return i;
	}

	public static short bytesToSignedShort(byte b0, byte b1)
	{
		short i = 0;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return i;
	}

	public static float bytesToFloat(byte b0, byte b1, byte b2, byte b3)
	{
		int i = 0;
		i += ((int)b3 & 0x000000FF) << 24;
		i += ((int)b2 & 0x000000FF) << 16;
		i += ((int)b1 & 0x000000FF) << 8;
		i += ((int)b0 & 0x000000FF);
		return Float.intBitsToFloat(i);
	}

	public static int byteToUnsignedByte(byte b)
	{
		if (b >= 0) return b;
		else return (int)b + 256 ;
	}

}

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
package org.openaltimeter.comms;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.Vector;

import org.openaltimeter.desktopapp.Controller;

public class SerialLink {

	private SerialPort sp;
	public InputStream in;
	private OutputStream out;

	public static List<String> getSerialPorts() {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
		List<String> commPortNames = new Vector<String>();
		while (commPorts.hasMoreElements())	commPortNames.add(commPorts.nextElement().getName());
		return commPortNames;
	}

	public void connect(String port, int rate) throws NoSuchPortException,
			PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(port);
		sp = (SerialPort) portID.open("openaltimeter comm port", rate);
		sp.setSerialPortParams(rate, SerialPort.DATABITS_8,	SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		// hook up to the SerialEvents - we don't activate this event listener unless we're doing a buffered read
		sp.addEventListener(new SerialPortEventListener() {
			@Override
			public void serialEvent(SerialPortEvent arg0) {
				try {
					while (in.available() > 0) buffer[bufferPos++] = (byte)in.read();
				} catch (IOException e) {
					// we can't throw this exception, so the best we can do is log it and carry on.
					Controller.log("Error reading serial stream. Try downloading again.", "error");
				}
			}
		});
//		sp.setInputBufferSize(Altimeter.FLASH_MEMORY_SIZE + 1024);
		in = sp.getInputStream();
		out = sp.getOutputStream();
	}

	public void disconnect() {
		sp.close();
	}

	public void sendReset() {
		sp.setDTR(false);
		// we're resetting the board, so it makes sense to clear any junk out of
		// the input buffer here.
		clearInput();
		try {Thread.sleep(100);} catch (InterruptedException e) {}
		sp.setDTR(true);
	}

	// we'll often want to remove junk from the input buffer before exchanging
	// data with the logger. This method does just that.
	public void clearInput() {
		try { in.skip(in.available()); } catch (IOException e) {}
	}
	
	public void write(char c) throws IOException
	{
		out.write(c);
	}
	
	public void write(String s) throws IOException
	{
		char[] chars = s.toCharArray();
		for (char c : chars) out.write(c);
	}
	
	public String readString(int bufferSize) throws IOException
	{
		byte[] inputBuffer = new byte[bufferSize];
		in.read(inputBuffer, 0, bufferSize);
		return new String(inputBuffer, Charset.forName("ASCII")).trim();
	}
	
	private byte[] buffer;
	private int bufferPos;
	
	// this method and the following three implement a very simple buffered read.
	// It shouldn't be necessary to do this, as java has buffered streams, but I
	// simply couldn't get them to work well with rxtx.
	public void startBufferedRead(int bufferSize)
	{
		buffer = new byte[bufferSize];
		bufferPos = 0;
		sp.notifyOnDataAvailable(true);
	}
	
	public void stopBufferedRead()
	{
		sp.notifyOnDataAvailable(false);
	}
	
	public int available()
	{
		return bufferPos;
	}
	
	public byte[] getBuffer()
	{
		return buffer;
	}
}

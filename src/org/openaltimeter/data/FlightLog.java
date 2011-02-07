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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class FlightLog {
 
	public ArrayList<LogEntry> logData = new ArrayList<LogEntry>();
	public ArrayList<Annotation> annotations = new ArrayList<Annotation>();
	
	private static final int BASE_PRESSURE_SAMPLES = 20;
	
	public void add(LogEntry entry) {
		logData.add(entry);
	}
	
	// method for calculating base pressure for data starting at startIndex
	public double calculateBasePressure(int startIndex)
	{
		int count = 0;
		double basePressure = 0;
		for (int i = startIndex; (i < startIndex + BASE_PRESSURE_SAMPLES) && (i < logData.size()); i++)
		{
			LogEntry le = logData.get(i);
			if (le.pressure != -1) {
				count++;
				basePressure += le.pressure;
			}
			else
				break;
		}
		
		return basePressure / count;
	}
	
	// this method takes the raw log data and converts it into altitude data
	public void calculateAltitudes()
	{
		double basePressure = 0;
		
		for (int i = 0; i < logData.size(); i++)
		{
			LogEntry le = logData.get(i);
			
			if (le.pressure != -1) {
				if (basePressure == 0) 
					basePressure = calculateBasePressure(i);
				
				le.altitudeM = AltitudeConverter.altitudeMFromPressure(le.pressure, basePressure);
				le.altitudeFt = AltitudeConverter.feetFromM(le.altitudeM);
			}
			else 
				basePressure = 0;
		}
		// TEMP: add some annotations to test the serializer
		RegionAnnotation ra = new RegionAnnotation();
		ra.endTime = 10;
		ra.startTime = 5;
		ra.text = "Hello, region.";
		PointAnnotation pa = new PointAnnotation();
		pa.text = "Hello, point.";
		pa.time = 3;
		annotations.add(ra);
		annotations.add(pa);
	}

	public double[] getAltitudeFt() {
		int numPoints = logData.size();
		double[] data = new double[numPoints];
		for (int i = 0; i < numPoints; i++) data[i] = logData.get(i).altitudeFt;
		return data;
	}
	
	public double[] getBattery()
	{
		int numPoints = logData.size();
		double[] data = new double[numPoints];
		for (int i = 0; i < numPoints; i++) data[i] = logData.get(i).battery;
		return data;
	}

	public double[] getTemperature() {
		int numPoints = logData.size();
		double[] data = new double[numPoints];
		for (int i = 0; i < numPoints; i++) data[i] = logData.get(i).temperature;
		
		return data;
	}

	public String rawDataToString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < logData.size(); i++) sb.append(logData.get(i).rawDataToString() + "\r\n");
		return sb.toString();
	}
	
	public void fromRawData(String rawData) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(rawData));
		String line;
		while ((line = br.readLine()) != null) {
			LogEntry le = new LogEntry();
			le.fromRawData(line);
			logData.add(le);
		}
		calculateAltitudes();
	}
}

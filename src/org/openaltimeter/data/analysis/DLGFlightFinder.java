package org.openaltimeter.data.analysis;

import java.util.ArrayList;
import java.util.List;

import org.openaltimeter.data.FlightLog;

// This class scans flight logs and finds the DLG launches. All work is done in meters.
// The algorithm may look kind of horrible - this is because it's a direct port of the algorithm
// that runs on the OA. The OA algorithm is written to run in real time, with a flash memory
// store holding the history.
public class DLGFlightFinder {

	public List<DLGFlight> findDLGLaunches(FlightLog fl) {
		ArrayList<DLGFlight> flights = new ArrayList<DLGFlight>();
		double[] altData = fl.getAltitude();
		boolean launched = false;
		for (int i = 0; i < altData.length; i++)
			launched = updateHeightMonitor(altData, i, fl.logInterval, flights);
		if (launched) {
			// add the last flight
			DLGFlight flight = new DLGFlight();
			flight.launchHeight = maxLaunchHeight;
			flight.launchIndex = maxLaunchHeightIndex;
			flight.launchWindowEndHeight = launchWindowEndHeight;
			flight.maxHeight = maxHeight;
			flight.maxIndex = maxHeightIndex;
			flight.startHeight = launchHeight;
			flight.startIndex = launchIndex;
			flights.add(flight);
		}
		return flights;
	}

	// -- launch detector
	// these parameters tune the launch detector
	// this is the rate of climb that is considered a launch. It's measured in
	// m/s.
	static final double LAUNCH_CLIMB_THRESHOLD = 3.0;
	// this is how long the climb intervals have to exceed the launch climb rate
	// to trigger the launch detector, measured in ms
	static final int LAUNCH_CLIMB_TIME = 1500;
	// this is how many samples to seek back after the launch was detected to
	// find the minimum height
	static final int LAUNCH_SEEKBACK_SAMPLES = 20;
	// this is how long the launch window is, in ms. The launch height will be
	// measured in this window.
	static final int LAUNCH_WINDOW_TIME = 5000;
	// the height at which the launch detector re-arms. Measured in meters.
	static final double LAUNCH_DETECTOR_REARM_HEIGHT = 8.0;

	double currentHeight = 0;
	double maxHeight = 0; // The overall maximum height of the flight.

	boolean updateHeightMonitor(double[] altData, int index, double logInterval, ArrayList<DLGFlight> flights) {
		currentHeight = altData[index];
		if (currentHeight > maxHeight) {
			maxHeight = currentHeight;
			maxHeightIndex = index;
		}
		return updateDLGHeightMonitor(altData, index, logInterval, flights);
	}

	// DLG specific height functions and variables. This is broken out from the
	// main height monitor to make the firmware
	// easier to customise.
	int launchCount = 0; // A launch is defined as N successive periods with
							// more than a certain climb rate.
							// This keeps track of how long we've been climbing.
	int launchWindowCount = 0; // Used to track launch height separate from max
								// height.
	double lastHeight = 0; // Used for measuring climb rates.
	boolean launched = false; // This indicates whether we're in flight or not.
								// Launch detector is disabled in flight.
	double maxLaunchHeight = 0;
	double launchWindowEndHeight = 0; // It's useful to know what height was
										// attained a few seconds after launch
										// to optimise push over.
	double launchHeight = 0;
	int launchIndex = 0;
	int maxHeightIndex = 0;
	int maxLaunchHeightIndex = 0;

	boolean updateDLGHeightMonitor(double[] altData, int index, double logInterval, ArrayList<DLGFlight> flights) {
		// We monitor the height data looking for a "launch". This is a number
		// of samples that climb consistently at greater than a given rate.
		if (!launched) {
			if (currentHeight - lastHeight > (LAUNCH_CLIMB_THRESHOLD * logInterval))
				launchCount++;
			else
				launchCount = 0;
			lastHeight = currentHeight;
			if (launchCount >= (LAUNCH_CLIMB_TIME / (logInterval * 1000))) {
				// we've just detected a launch - disable the launch detector
				launched = true;
				launchCount = 0;
				// store the previous launch details
				if (maxLaunchHeight != 0) {
					DLGFlight fl = new DLGFlight();
					fl.launchHeight = maxLaunchHeight;
					fl.launchIndex = maxLaunchHeightIndex;
					fl.launchWindowEndHeight = launchWindowEndHeight;
					fl.maxHeight = maxHeight;
					fl.maxIndex = maxHeightIndex;
					fl.startHeight = launchHeight;
					fl.startIndex = launchIndex;
					flights.add(fl);
				}
				launchIndex = 0;
				maxHeightIndex = 0;
				maxLaunchHeightIndex = 0;
				// When we detect a launch we do a few things: we reset the
				// launch height to the lowest height
				// in the few seconds before the launch;
				// we start a countdown which defines the "launch window"; we
				// reset the maximum heights.
				// -- reset launch height
				double newLaunchHeight = (double)Double.MAX_VALUE;
				for (int i = LAUNCH_SEEKBACK_SAMPLES; i > 0; i--) {
					int seekbackIndex = index - i;
					// have we reached the start of the array?
					if (seekbackIndex >= 0) {
						// we stop if we encounter a file boundary.
						// unlikely to happen, but worth checking for.
						if (altData[seekbackIndex] == FlightLog.PRESSURE_EMPTY_DATA)
							break;
						if (altData[seekbackIndex] < newLaunchHeight)
							newLaunchHeight = altData[seekbackIndex];
					}
				}
				// -- save the launch height
				launchHeight = newLaunchHeight;
				// -- save the launch index
				launchIndex = Math.max(index - LAUNCH_SEEKBACK_SAMPLES, 0);
				// -- time the launch window
				launchWindowCount = (int) (LAUNCH_WINDOW_TIME / (logInterval * 1000)) + 1;
				// -- reset max heights
				maxHeight = 0;
				maxLaunchHeight = 0;
				launchWindowEndHeight = 0;
			}
		} else {
			// The launch detector is disabled after a launch, so that it can't
			// retrigger in flight. It is reset by either the logger's altitude
			// coming below a certain threshold, or a height output function
			// being commanded by the user (on the basis that this should always
			// happen on the ground - the latter is implemented to stop the
			// logger getting stuck should the ground-level pressure change
			// dramatically during
			// a flight.)
			// Here we check for the former.
			if (currentHeight < LAUNCH_DETECTOR_REARM_HEIGHT)
				launched = false;
		}
		// if we're in the launch window we need to track the maximum altitude.
		if (launchWindowCount > 0) {
			if (currentHeight > maxLaunchHeight) {
				maxLaunchHeight = currentHeight;
				maxLaunchHeightIndex = index;
			}
			// if this is the end of the launch window then we record the height
			if (launchWindowCount == 1)
				launchWindowEndHeight = currentHeight;
			launchWindowCount--;
		}
		
		// so we no if there's a launch that we've missed
		return launched;
	}
}

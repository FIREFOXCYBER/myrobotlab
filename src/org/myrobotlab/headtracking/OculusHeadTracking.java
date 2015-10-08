package org.myrobotlab.headtracking;


import java.io.Serializable;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.OculusRift;
import org.myrobotlab.service.data.OculusData;
import org.slf4j.Logger;

import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.TrackingState;

/**
 * OculusHeadTracking -This is a helper thread that will poll the oculus
 * head tracking information and it will publish the roll/pitch/yaw information.
 *
 */
public class OculusHeadTracking implements Runnable, Serializable  {

	public final static Logger log = LoggerFactory.getLogger(OculusHeadTracking.class);
	private static final long serialVersionUID = -4067064437788846187L;
	protected final Hmd hmd;
	protected final HmdDesc hmdDesc;
	boolean running = false;
	transient public OculusRift oculus;
	transient Thread trackerThread = null;
	private int pollIntervalMS = 20;
	
	public OculusHeadTracking(Hmd hmd, HmdDesc hmdDesc) {
		// Grab a handle to the initialized hmd.
		this.hmd = hmd;
		this.hmdDesc = hmdDesc;
	}

	@Override
	public void run() {
		
		running = true;
		while (running) {
			TrackingState trackingState = hmd.getTrackingState(0);
			double w = Math.toDegrees(trackingState.HeadPose.Pose.Orientation.w);
	  		// rotations about x axis  (pitch)
	  		double pitch = Math.toDegrees(trackingState.HeadPose.Pose.Orientation.x);
	  		// rotation about y axis (yaw)
	  		double yaw = Math.toDegrees(trackingState.HeadPose.Pose.Orientation.y);
	  		// rotation about z axis (roll)
	  		double roll = Math.toDegrees(trackingState.HeadPose.Pose.Orientation.z);
			// log.info("Roll: " + z*RAD_TO_DEGREES);
			// log.info("Pitch:"+ x*RAD_TO_DEGREES);
			// log.info("Yaw:"+ y*RAD_TO_DEGREES );
	  		OculusData headTrackingData = new OculusData(roll, pitch, yaw);
	  		oculus.invoke("publishOculusData", headTrackingData);
	  		try {
	  			// There need to be polling interval here.
				Thread.sleep(pollIntervalMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				// break out ...
				break;
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public OculusRift getOculus() {
		return oculus;
	}

	public void setOculus(OculusRift oculus) {
		this.oculus = oculus;
	}

	public void start() {
		log.info("starting head tracking");
		if (trackerThread != null) {
			log.info("Head tracker thread already started.");
			return;
		}
		trackerThread = new Thread(this, String.format("%s_oculusHeadTracking", oculus.getName()));
		trackerThread.start();
 	}

	public void stop() {
		log.debug("stopping head tracking");
        running = false;
        trackerThread = null;
	}

	// default 20 ms
	public int getPollIntervalMS() {
		return pollIntervalMS;
	}

	public void setPollIntervalMS(int pollIntervalMS) {
		this.pollIntervalMS = pollIntervalMS;
	}

	

}

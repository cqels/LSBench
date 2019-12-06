package sib.objects;

import java.io.Serializable;

public class GPS extends SocialObject implements Serializable{
	long 	trackedTime; 
	String	trackedLocation;
	double 	longt;
	double 	latt; 
	int 	userId; 	// Id of the user has been tracked
	
	public long getTrackedTime() {
		return trackedTime;
	}
	public void setTrackedTime(long trackedTime) {
		this.trackedTime = trackedTime;
	}
	public String getTrackedLocation() {
		return trackedLocation;
	}
	public void setTrackedLocation(String trackedLocation) {
		this.trackedLocation = trackedLocation;
	}
	public double getLongt() {
		return longt;
	}
	public void setLongt(double longt) {
		this.longt = longt;
	}
	public double getLatt() {
		return latt;
	}
	public void setLatt(double latt) {
		this.latt = latt;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}

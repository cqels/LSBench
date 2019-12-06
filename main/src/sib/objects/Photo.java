package sib.objects;

import java.util.ArrayList;

public class Photo extends SocialObject {
	long photoId; 
	long albumId; 
	int locationIdx; 
	int creatorId;		// Id of user's account
	String locationName; 
	double latt; 
	double longt; 
	long takenTime; 
	int[] tags;
	int[] interestedUserAccs;
	
	IP ipAddress; 
	String userAgent;				// Send from where e.g., iPhone, Samsung, HTC
	
	byte 	browserIdx; 

	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public byte getBrowserIdx() {
		return browserIdx;
	}
	public void setBrowserIdx(byte browserIdx) {
		this.browserIdx = browserIdx;
	}
	public IP getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(IP ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public long getPhotoId() {
		return photoId;
	}
	public void setPhotoId(long photoId) {
		this.photoId = photoId;
	}
	public long getAlbumId() {
		return albumId;
	}
	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}
	public int getLocationIdx() {
		return locationIdx;
	}
	public void setLocationIdx(int locationIdx) {
		this.locationIdx = locationIdx;
	}
	public long getTakenTime() {
		return takenTime;
	}
	public void setTakenTime(long takenTime) {
		this.takenTime = takenTime;
	}
	public int[] getTags() {
		return tags;
	}
	public void setTags(int[] tags) {
		this.tags = tags;
	}
	public double getLatt() {
		return latt;
	}
	public void setLatt(double latt) {
		this.latt = latt;
	}
	public double getLongt() {
		return longt;
	}
	public void setLongt(double longt) {
		this.longt = longt;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public int[] getInterestedUserAccs() {
		return interestedUserAccs;
	}
	public void setInterestedUserAccs(int[] interestedUserAccs) {
		this.interestedUserAccs = interestedUserAccs;
	}	
}

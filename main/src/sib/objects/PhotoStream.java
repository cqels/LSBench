package sib.objects;

import java.io.Serializable;

public class PhotoStream implements Serializable{
	boolean isPhoto = true; 
	long photoId;
	
	long albumId; 
	int locationIdx; 
	int creatorId;		// Id of user's account
	String locationName; 
	double latt; 
	double longt; 
	//long takenTime;    // Replaced by createdDate
	int[] tags;
	int[] interestedUserAccs;
	
	IP ipAddress; 
	String userAgent;				// Send from where e.g., iPhone, Samsung, HTC
	
	byte 	browserIdx; 
	
	String title; 
	long createdDate;
	
	public PhotoStream(){}
	public PhotoStream(Photo photo){
		isPhoto = true; 
		this.photoId = photo.getPhotoId(); 
		this.albumId = photo.getAlbumId(); 
		this.locationIdx = photo.getLocationIdx();
		this.creatorId = photo.getCreatorId();		// Id of user's account
		this.locationName = photo.getLocationName();
		this.latt = photo.getLatt(); 
		this.longt = photo.getLongt();
		this.createdDate = photo.getTakenTime();
		this.tags = photo.getTags();
		this.interestedUserAccs = photo.getInterestedUserAccs();
		this.ipAddress = photo.getIpAddress();
		this.userAgent = photo.getUserAgent();	
		this.browserIdx = photo.getBrowserIdx(); 
	}
	public PhotoStream(PhotoAlbum album){
		isPhoto = false; 
		this.albumId = album.getAlbumId(); 
		this.creatorId = album.getCreatorId();
		this.title = album.getTitle(); 
		this.createdDate = album.getCreatedDate();
		this.locationIdx = album.getLocationIdx();
	}
	public Photo getPhoto(){
		Photo photo = new Photo();
		photo.setPhotoId(photoId); 
		photo.setAlbumId(albumId); 
		photo.setLocationIdx(locationIdx);
		photo.setCreatorId(creatorId);		// Id of user's account
		photo.setLocationName(locationName);
		photo.setLatt(latt); 
		photo.setLongt(longt);
		photo.setTakenTime(createdDate);
		photo.setTags(tags);
		photo.setInterestedUserAccs(interestedUserAccs);
		photo.setIpAddress(ipAddress);
		photo.setUserAgent(userAgent);	
		photo.setBrowserIdx(browserIdx); 
		
		return photo; 
	}
	
	public PhotoAlbum getPhotoAlbum(){
		PhotoAlbum album = new PhotoAlbum(); 
		album.setAlbumId(albumId); 
		album.setCreatorId(creatorId);
		album.setTitle(title); 
		album.setCreatedDate(createdDate);
		album.setLocationIdx(locationIdx);
		return album; 
	}
	
	public long getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
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
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
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
	public int[] getTags() {
		return tags;
	}
	public void setTags(int[] tags) {
		this.tags = tags;
	}
	public int[] getInterestedUserAccs() {
		return interestedUserAccs;
	}
	public void setInterestedUserAccs(int[] interestedUserAccs) {
		this.interestedUserAccs = interestedUserAccs;
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
	public byte getBrowserIdx() {
		return browserIdx;
	}
	public void setBrowserIdx(byte browserIdx) {
		this.browserIdx = browserIdx;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isPhoto() {
		return isPhoto;
	}
	public void setPhoto(boolean isPhoto) {
		this.isPhoto = isPhoto;
	}

}

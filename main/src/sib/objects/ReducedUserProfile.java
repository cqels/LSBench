package sib.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class ReducedUserProfile extends SocialObject implements Serializable {
	int 				accountId;
	long	 			createdDate; 
	public short 		numFriends;

	public short 		numFriendsAdded;
	public short 		numPassFriends[];
	public short 		numPassFriendsAdded[];
	
	Friend 				friendList[];
	HashSet<Integer>	friendIds; 		// Use a hashset for checking the existence
	
	int					dicElementIds[];	// Id of an element in a dictionary, e.g., locationId
										// interestId
	
	//For user's agent information
	boolean				isHaveSmartPhone; 		// Use for providing the user agent information
	byte 				agentIdx; 				// Index of user agent in the dictionary, e.g., 0 for iPhone, 1 for HTC
	byte				browserIdx;				// Index of web browser, e.g., 0 for Internet Explorer
	
	//For IP address
	boolean 			isFrequentChange;		// About 1% of users frequently change their location
	IP					ipAddress;				// IP address
	
	
	// Store redundant info
	int 				locationIdx;
	int 				forumWallId; 
	int 				forumStatusId;
	HashSet<Integer> 	setOfInterests; 
	short				popularPlaceIds[]; 
	byte				numPopularPlace; 


	/*
	private void writeObject(ObjectOutputStream s) 
				throws IOException {
		s.defaultWriteObject(); 
	}
	private void readObject(ObjectInputStream s)
		throws IOException, ClassNotFoundException {
	}
	*/
	public void clear(){
		Arrays.fill(friendList,null);
		friendIds.clear();
		numPassFriends = null; 
		numPassFriendsAdded = null; 
		dicElementIds = null; 
		setOfInterests.clear();
		popularPlaceIds = null; 
	}
	public ReducedUserProfile(){
		
	}
	public ReducedUserProfile(UserProfile user, int pass, int numCorrDimensions){
		this.setAccountId(user.getAccountId());
		this.setCreatedDate(user.getCreatedDate());
		this.setNumFriends(user.getNumFriends());
		this.setNumFriendsAdded((short)0);
		/*
		switch (pass) {
			case 0: 
				this.setDicElementId(user.getLocationIdx());
				break;
			case 1: 
				break;
			case 2:
				break;
			default: 
				this.setDicElementId(-1);
				break;
		}
		*/
		
		dicElementIds = new int[numCorrDimensions];
		
		//this.setDicElementId(user.getLocationIdx(),0);
		this.setDicElementId(user.getLocationZId(),0);
		
		this.setDicElementId(user.getFirstInterestIdx(),1);
		
		this.setDicElementId(user.getRandomIdx(),2);
		
		this.allocateFriendListMemory();
		
		// for user's agent information
		this.setHaveSmartPhone(user.isHaveSmartPhone);
		this.setAgentIdx(user.getAgentIdx());
		this.setBrowserIdx(user.getBrowserIdx());
		this.setFrequentChange(user.isFrequentChange);
		this.setIpAddress(user.getIpAddress());
		
		this.setNumPassFriends(user.getNumPassFriends());
		
		// DucPM: Need to check whether this info needs to be stored here
		this.setLocationIdx(user.getLocationIdx());
		this.setForumStatusId(user.getForumStatusId());
		this.setForumWallId(user.getForumWallId());
		this.setSetOfInterests(user.getSetOfInterests());
		this.setPopularPlaceIds(user.getPopularPlaceIds());
		this.setNumPopularPlace(user.getNumPopularPlace());
		
		this.numPassFriendsAdded = new short[numCorrDimensions];

	}
	
	public int getDicElementId(int index) {
		return dicElementIds[index];
	}

	public void setDicElementId(int dicElementId, int index) {
		this.dicElementIds[index] = dicElementId;
	}

	public void setPassFriendsAdded(int pass, short numPassFriendAdded) {
		numPassFriendsAdded[pass] = numPassFriendAdded;
	}
	public short getPassFriendsAdded(int pass) {
		return numPassFriendsAdded[pass];
	}	
	
	
	public short getLastInterestFriendIdx(){
		return (short)(numPassFriendsAdded[1] - 1);
	}
	public short getStartInterestFriendIdx(){
		return (short)(numPassFriendsAdded[0]);
	}
	public short getLastLocationFriendIdx(){
		return (short)(numPassFriendsAdded[0] - 1);
	}
	
	public short getNumFriendsAdded() {
		return numFriendsAdded;
	}
	
	public void resetUser(){
		accountId = -1;
		numFriends = 0; 
		numFriendsAdded = 0;
		 
	}
	
	public void addNewFriend(Friend friend) {
		try {
			friendList[numFriendsAdded] = friend;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Total number of friends " + numFriends);
			System.out.println("Number of friends added " + numFriendsAdded);
			System.out.println("Number of pass friends " + numPassFriends[0]);
			System.out.println("Number of pass friends " + numPassFriends[1]);
			System.out.println("Number of pass friends " + numPassFriends[2]);
			
			e.printStackTrace();
			System.exit(-1);
			
		}
		friendIds.add(friend.getFriendAcc());
		numFriendsAdded++;
	}
	
	public boolean isExistFriend(int friendId){
		return friendIds.contains(friendId);
	}
	
	public void setNumFriendsAdded(short numFriendsAdded) {
		this.numFriendsAdded = numFriendsAdded;
	}
	

	/*
	public void print(){
		System.out.println("Account Id: " + accountId);
		System.out.println("Friends added: " + numFriendsAdded + " / " + numFriends);
	}
	public void printDetail(){
		System.out.println("Account Id: " + accountId);
		System.out.print("Total number of friends: " + numFriends);
		System.out.print(numFriendsAdded + " user friends added: ");
		for (int i = 0; i < numFriendsAdded; i ++){
			System.out.print(" " + friendList[i].getFriendAcc());
		}
	}
	*/
	
	public void allocateFriendListMemory(){
		friendList = new Friend[numFriends];
		friendIds = new HashSet<Integer>(numFriends);
	}

	public Friend[] getFriendList() {
		return friendList;
	}

	public void setFriendList(Friend[] friendList) {
		this.friendList = friendList;
	}

	/*
	public short getNumFriends() {
		return numFriends;
	}
	*/
	public short getNumFriends(int pass) {
		return numPassFriends[pass];
	}
	public void setNumFriends(short numFriends) {
		this.numFriends = numFriends;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	public boolean isHaveSmartPhone() {
		return isHaveSmartPhone;
	}

	public void setHaveSmartPhone(boolean isHaveSmartPhone) {
		this.isHaveSmartPhone = isHaveSmartPhone;
	}

	public byte getAgentIdx() {
		return agentIdx;
	}

	public void setAgentIdx(byte agentIdx) {
		this.agentIdx = agentIdx;
	}

	public byte getBrowserIdx() {
		return browserIdx;
	}

	public void setBrowserIdx(byte browserIdx) {
		this.browserIdx = browserIdx;
	}

	public boolean isFrequentChange() {
		return isFrequentChange;
	}

	public void setFrequentChange(boolean isFrequentChange) {
		this.isFrequentChange = isFrequentChange;
	}

	public IP getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(IP ipAddress) {
		this.ipAddress = ipAddress;
	}
	public short[] getNumPassFriends() {
		return numPassFriends;
	}
	public void setNumPassFriends(short[] numPassFriends) {
		this.numPassFriends = numPassFriends;
	}
	public int getLocationIdx() {
		return locationIdx;
	}
	public void setLocationIdx(int locationIdx) {
		this.locationIdx = locationIdx;
	}
	public int getForumWallId() {
		return forumWallId;
	}
	public void setForumWallId(int forumWallId) {
		this.forumWallId = forumWallId;
	}
	public int getForumStatusId() {
		return forumStatusId;
	}
	public void setForumStatusId(int forumStatusId) {
		this.forumStatusId = forumStatusId;
	}
	public HashSet<Integer> getSetOfInterests() {
		return setOfInterests;
	}
	public void setSetOfInterests(HashSet<Integer> setOfInterests) {
		this.setOfInterests = setOfInterests;
	}
	public byte getNumPopularPlace() {
		return numPopularPlace;
	}
	public void setNumPopularPlace(byte numPopularPlace) {
		this.numPopularPlace = numPopularPlace;
	}
	public short getPopularId(int index){
		return popularPlaceIds[index];
	}
	public short[] getPopularPlaceIds() {
		return popularPlaceIds;
	}
	public void setPopularPlaceIds(short[] popularPlaceIds) {
		this.popularPlaceIds = popularPlaceIds;
	}
}
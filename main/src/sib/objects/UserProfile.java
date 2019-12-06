package sib.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class UserProfile extends SocialObject implements Serializable {
	int 				accountId;
	//String 				firstName; 
	//String 				lastName; 
	//String 				location;
	int 				locationIdx; 
	int 				locationZId;
	

	int 				randomIdx; 
	//String 				organization; 
	int 				organizationIdx;
	//String 				institution;	
	int 				forumWallId; 
	int 				forumStatusId;
	long	 			createdDate; 
	public short 		numFriends;
	public short 		numInterests; 

	//public short 		numLocationFriends;
	//public short 		numInterestFriends;
	//public short		numRandomFriends;  
	public short 		numPassFriends[];		// Max number of friends can be 
												// generated after kth passes
	
	public short		lastLocationFriendIdx; 
	public short		startInterestFriendIdx; 
	public short 		lastInterestFriendIdx; 
	
	public short 		numFriendsAdded;
	Friend 				friendList[];
	HashSet<Integer>	friendIds; 		// Use a hashset for checking the existence
	
	HashSet<Integer> 	setOfInterests; 

	//For user's agent information
	boolean				isHaveSmartPhone; 		// Use for providing the user agent information
	byte 				agentIdx; 				// Index of user agent in the dictionary, e.g., 0 for iPhone, 1 for HTC
	byte				browserIdx;				// Index of web browser, e.g., 0 for Internet Explorer
	
	//For IP address
	boolean 			isFrequentChange;		// About 1% of users frequently change their location
	IP					ipAddress;				// IP address
	
	//For popular places
	short				popularPlaceIds[]; 
	byte				numPopularPlace; 

	public short getPopularId(int index){
		return popularPlaceIds[index];
	}
	public short[] getPopularPlaceIds() {
		return popularPlaceIds;
	}
	public void setPopularPlaceIds(short[] popularPlaceIds) {
		this.popularPlaceIds = popularPlaceIds;
	}
	public byte getNumPopularPlace() {
		return numPopularPlace;
	}
	public void setNumPopularPlace(byte numPopularPlace) {
		this.numPopularPlace = numPopularPlace;
	}
	public IP getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(IP ipAddress) {
		this.ipAddress = ipAddress;
	}
	public boolean isFrequentChange() {
		return isFrequentChange;
	}
	public void setFrequentChange(boolean isFrequentChange) {
		this.isFrequentChange = isFrequentChange;
	}

	public short getNumFriendsAdded() {
		return numFriendsAdded;
	}
	
	public void resetUser(){
		accountId = -1;
		locationIdx = -1; 
		organizationIdx = -1; 
		forumWallId = -1; 
		forumStatusId = -1;
		numFriends = 0; 
		numFriendsAdded = 0;
		isHaveSmartPhone = false; 
		
		setOfInterests = new HashSet<Integer>();  
		 
	}

	
	public void addNewFriend(Friend friend) {
		friendList[numFriendsAdded] = friend;
		friendIds.add(friend.getFriendAcc());
		numFriendsAdded++;
	}
	
	public boolean isExistFriend(int friendId){
		return friendIds.contains(friendId);
	}
	
	public void setNumFriendsAdded(short numFriendsAdded) {
		this.numFriendsAdded = numFriendsAdded;
	}


	public void print(){
		System.out.println("Account Id: " + accountId);
		System.out.println("User location: " + locationIdx);
		System.out.println("Friends added: " + numFriendsAdded + " / " + numFriends);
		System.out.println("Number of location friends " + numPassFriends[0]);
		System.out.println("Number of interest friends " + numPassFriends[1]);
		System.out.println("Number of interest " + numInterests);
	}
	public void printDetail(){
		System.out.println("Account Id: " + accountId);
		System.out.println("User location: " + locationIdx);
		System.out.print("Total number of friends: " + numFriends);
		System.out.print(numFriendsAdded + " user friends added: ");
		for (int i = 0; i < numFriendsAdded; i ++){
			System.out.print(" " + friendList[i].getFriendAcc());
		}
		System.out.println();
		System.out.print(numInterests + "User Interests: ");
		Iterator it = setOfInterests.iterator(); 
		while (it.hasNext()){
			System.out.print(" " + it.next()); 
		}
		
		System.out.println();
	}
	public short getNumInterests() {
		return numInterests;
	}
	public void setNumInterests(short numInterests) {
		this.numInterests = numInterests;
	}
	public short getNumPassFriends(int pass) {
		return numPassFriends[pass];
	}
	public void setNumPassFriends(short numPassFriends, int pass) {
		this.numPassFriends[pass] = numPassFriends;
	}
	public HashSet<Integer> getSetOfInterests() {
		return setOfInterests;
	}
	public int getFirstInterestIdx(){
		// Randomly select one interest
		Iterator iter = setOfInterests.iterator();

		int interestIdx = ((Integer)iter.next()).intValue();
		
		return interestIdx;
	}	
	public void setSetOfInterests(HashSet<Integer> setOfInterests) {
		this.setOfInterests = setOfInterests;
	}
	
	public void allocateFriendListMemory(int numFriendPasses){
		friendList = new Friend[numFriends];
		friendIds = new HashSet<Integer>(numFriends);
		numPassFriends = new short[numFriendPasses];
	}

	public Friend[] getFriendList() {
		return friendList;
	}

	public void setFriendList(Friend[] friendList) {
		this.friendList = friendList;
	}

	public short getNumFriends() {
		return numFriends;
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

	public static String getPrefixed(long userIdx)
	{
		StringBuffer s = new StringBuffer();
		s.append("person");
		s.append(userIdx);
		return s.toString();
	}
	
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
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
	
	public int getLocationIdx() {
		return locationIdx;
	}

	public void setLocationIdx(int locationIdx) {
		this.locationIdx = locationIdx;
	}

	public int getOrganizationIdx() {
		return organizationIdx;
	}

	public void setOrganizationIdx(int organizationIdx) {
		this.organizationIdx = organizationIdx;
	}
	public short getLastLocationFriendIdx() {
		return lastLocationFriendIdx;
	}
	public void setLastLocationFriendIdx(short lastLocationFriendIdx) {
		this.lastLocationFriendIdx = lastLocationFriendIdx;
	}
	public short getLastInterestFriendIdx() {
		return lastInterestFriendIdx;
	}
	public void setLastInterestFriendIdx(short lastInterestFriendIdx) {
		this.lastInterestFriendIdx = lastInterestFriendIdx;
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
	public short getStartInterestFriendIdx() {
		return startInterestFriendIdx;
	}
	public void setStartInterestFriendIdx(short startInterestFriendIdx) {
		this.startInterestFriendIdx = startInterestFriendIdx;
	}
	public byte getBrowserIdx() {
		return browserIdx;
	}
	public void setBrowserIdx(byte browserIdx) {
		this.browserIdx = browserIdx;
	}
	public int getRandomIdx() {
		return randomIdx;
	}
	public void setRandomIdx(int randomIdx) {
		this.randomIdx = randomIdx;
	}
	public short[] getNumPassFriends() {
		return numPassFriends;
	}
	public void setNumPassFriends(short[] numPassFriends) {
		this.numPassFriends = numPassFriends;
	}
	public int getLocationZId() {
		return locationZId;
	}
	public void setLocationZId(int locationZId) {
		this.locationZId = locationZId;
	}
}

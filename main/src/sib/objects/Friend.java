package sib.objects;

import java.io.Serializable;

public class Friend extends SocialObject implements Serializable{
	int friendAcc; 
	long createdTime;			//approved Time 
	long requestTime;			 
	long declinedTime; 
	byte passIdx;
	byte initiator; 			// 0: if user with smaller Id initiate the relationship, 1: if else
	
	
	//For user's agent information
	boolean				isHaveSmartPhone; 		// Use for providing the user agent information
	byte 				agentIdx; 				// Index of user agent in the dictionary, e.g., 0 for iPhone, 1 for HTC
	byte				browserIdx;				// Index of web browser, e.g., 0 for Internet Explorer
	
	boolean 			isFrequentChange;		
	IP	 				sourceIp; 				// Source IP address of the friend
	
	
	public Friend(int friendAcc, long _requestedTime, long _declinedTime, long _createdTime, byte passidx, byte initiator){
		this.friendAcc = friendAcc;
		this.requestTime = _requestedTime;
		this.declinedTime = _declinedTime;
		this.createdTime = _createdTime; 
		this.passIdx = passidx; 
		this.initiator = initiator;
	}
	
	/*
	public Friend(UserProfile user, long _requestedTime, long _declinedTime, long _createdTime, 
					byte passidx, byte initiator){
		this.friendAcc = user.getAccountId();
		this.requestTime = _requestedTime;
		this.declinedTime = _declinedTime;
		this.createdTime = _createdTime; 
		this.passIdx = passidx; 
		this.initiator = initiator;
		
		this.isHaveSmartPhone = user.isHaveSmartPhone;
		this.agentIdx = user.getAgentIdx();
		this.browserIdx = user.getBrowserIdx();
		this.isFrequentChange = user.isFrequentChange; 
		this.setSourceIp(user.getIpAddress());
				
	}
	*/

	public Friend(ReducedUserProfile user, long _requestedTime, long _declinedTime, long _createdTime, 
			byte passidx, byte initiator){
		this.friendAcc = user.getAccountId();
		this.requestTime = _requestedTime;
		this.declinedTime = _declinedTime;
		this.createdTime = _createdTime; 
		this.passIdx = passidx; 
		this.initiator = initiator;
		
		this.isHaveSmartPhone = user.isHaveSmartPhone;
		this.agentIdx = user.getAgentIdx();
		this.browserIdx = user.getBrowserIdx();
		this.isFrequentChange = user.isFrequentChange; 
		this.setSourceIp(user.getIpAddress());
			
	}	
	
	
	public int getFriendAcc() {
		return friendAcc;
	}
	public void setFriendAcc(int friendAcc) {
		this.friendAcc = friendAcc;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}
	public int getPassIdx() {
		return passIdx;
	}
	public void setPassIdx(byte passIdx) {
		this.passIdx = passIdx;
	} 
	public long getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}
	public long getDeclinedTime() {
		return declinedTime;
	}
	public void setDeclinedTime(long declinedTime) {
		this.declinedTime = declinedTime;
	}
	public byte getInitiator() {
		return initiator;
	}
	public void setInitiator(byte initiator) {
		this.initiator = initiator;
	}
	public IP getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(IP sourceIp) {
		this.sourceIp = sourceIp;
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

}

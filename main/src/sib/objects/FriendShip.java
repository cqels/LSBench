package sib.objects;


enum Status{
	initiator, 
	requested,
	approved
}
enum CorrelatedInfo{
	location, 
	interest
}
public class FriendShip extends SocialObject{
	Status status;
	CorrelatedInfo correlatedInfo;
	int userAcc01, userAcc02;
	long createdTime;
	
	public FriendShip(int userAcc1, int userAcc2){
		this.userAcc01 = userAcc1;
		this.userAcc02 = userAcc2;
	}
	public FriendShip(int userAcc1, int userAcc2, CorrelatedInfo correlatedInfo, long createdTime){
		this.userAcc01 = userAcc1;
		this.userAcc02 = userAcc2;
		this.correlatedInfo = correlatedInfo; 
		this.createdTime = createdTime;
	}
	public CorrelatedInfo getCorrelatedInfo() {
		return correlatedInfo;
	}
	public void setCorrelatedInfo(CorrelatedInfo correlatedInfo) {
		this.correlatedInfo = correlatedInfo;
	}	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public int getUserAcc01() {
		return userAcc01;
	}
	public void setUserAcc01(int userAcc01) {
		this.userAcc01 = userAcc01;
	}
	public int getUserAcc02() {
		return userAcc02;
	}
	public void setUserAcc02(int userAcc02) {
		this.userAcc02 = userAcc02;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}
}



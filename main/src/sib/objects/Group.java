package sib.objects;

import java.util.Iterator;
import java.util.Vector;



public class Group extends SocialObject{
	int groupId; 
	int moderatorId; 		//creator Id
	long createdDate;
	
	int forumWallId; 
	int forumStatusId; 
	
	String groupName; 

	String[] tags;
	
	int locationIdx; 			// Each group is for one location which is the creator's location
	int interestIdx; 			// Each group is for only one interest of the creator
	
	GroupMemberShip memberShips[]; 
	int numMemberAdded = 0; 

	public void initAllMemberships(int numMembers){
		memberShips = new GroupMemberShip[numMembers];
	}
	public void addMember(GroupMemberShip member){
		memberShips[numMemberAdded] = member;
		numMemberAdded++;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getModeratorId() {
		return moderatorId;
	}
	public void setModeratorId(int moderatorId) {
		this.moderatorId = moderatorId;
	}
	public long getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	} 
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public GroupMemberShip[] getMemberShips() {
		return memberShips;
	}
	public void setMemberShips(GroupMemberShip[] memberShips) {
		this.memberShips = memberShips;
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
	public int getNumMemberAdded() {
		return numMemberAdded;
	}
	public void setNumMemberAdded(int numMemberAdded) {
		this.numMemberAdded = numMemberAdded;
	}
	public int getLocationIdx() {
		return locationIdx;
	}
	public void setLocationIdx(int locationIdx) {
		this.locationIdx = locationIdx;
	}
	
	public int getInterestIdx() {
		return interestIdx;
	}
	public void setInterestIdx(int interestIdx) {
		this.interestIdx = interestIdx;
	}

}

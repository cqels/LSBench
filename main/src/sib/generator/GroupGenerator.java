package sib.generator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import sib.dictionary.InterestDictionary;
import sib.dictionary.LocationDictionary;
import sib.objects.Group;
import sib.objects.GroupMemberShip;
import sib.objects.ReducedUserProfile;

public class GroupGenerator {
	static int groupId = 0;
	DateGenerator dateGenerator; 
	LocationDictionary locationDic;
	InterestDictionary interestDic;
	static int forumId; 
	Random 	randGroupInterest; 
	
	public GroupGenerator(DateGenerator _dateGenerator, LocationDictionary _locationDic, 
			InterestDictionary _interestDic, int numUsers, long seed){
		this.dateGenerator = _dateGenerator; 
		this.locationDic = _locationDic; 
		this.interestDic = _interestDic; 
		this.forumId = numUsers * 2 + 1;
		randGroupInterest = new Random(seed);
	}
	public Group createGroup(ReducedUserProfile user){
		Group group = new Group(); 
		forumId = forumId + 2;
		groupId++;
		
		group.setGroupId(groupId);
		group.setModeratorId(user.getAccountId());
		group.setCreatedDate(dateGenerator.randomGroupCreatedDate(user));
		group.setForumWallId(forumId);
		group.setForumStatusId(forumId + 1);
		
		//Use the user location for group locationIdx
		group.setLocationIdx(user.getLocationIdx());
		
		//Select one user's interest for the group's interest
		HashSet<Integer> interestSet = user.getSetOfInterests();
		// Randomly select one interest
		Iterator iter = interestSet.iterator();
		int idx = randGroupInterest.nextInt(interestSet.size());
		for (int i = 0; i < idx; i++){
			iter.next();
		}
		  
		int interestIdx = ((Integer)iter.next()).intValue();
		group.setInterestIdx(interestIdx);
		
		//Set tags of this group
		String tags[] = new String[2];
		tags[0] = locationDic.getLocatioName(group.getLocationIdx());
		tags[1] = interestDic.getInterestdsNamesMapping().get(interestIdx);
		
		//Set name of group
		group.setGroupName("Group for " + tags[1] + " in " + tags[0]);
		
		group.setTags(tags);
		
		return group; 
	}
	
	public GroupMemberShip createGroupMember(int userId, long groupCreatedDate, long earliestJoinDate){
		GroupMemberShip memberShip = new GroupMemberShip();
		memberShip.setUserId(userId);
		memberShip.setJoinDate(dateGenerator.randomGroupMemberJoinDate(groupCreatedDate, earliestJoinDate));
		
		return memberShip;
	}
	
}

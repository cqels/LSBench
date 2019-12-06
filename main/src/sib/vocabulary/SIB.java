package sib.vocabulary;

import java.util.HashMap;

public class SIB {
	
	public static String SUT = "http://www.ins.cwi.nl/sib/";
	
	//The Namespace of this vocabulary as String
	public static String NS = "http://www.ins.cwi.nl/sib/vocabulary/";
	
	public static String PREFIX = "sib:";
	public static String FACTPREFIX = "sib_";
	
	public static String base = "http://www.ins.cwi.nl/sib/";
	
	public static String PREFIX_PERSON = "sibp:";
	public static String FACTPREFIX_PERSON = "sibp_";
	
	public static String NS_PERSON = base + "person/";

	public static String PREFIX_USER = "sibu:";
	public static String FACTPREFIX_USER = "sibu_";
	
	public static String NS_USER = base + "user/";
	
	public static String PREFIX_FRIENDSHIP = "sibfr:";
	public static String FACTPREFIX_FRIENDSHIP = "sibfr_";
	
	public static String NS_FRIENDSHIP = base + "friendship/";

	public static String PREFIX_FORUM = "sibfo:";
	public static String FACTPREFIX_FORUM = "sibfo_";
	public static String NS_FORUM = base + "forum/";

	public static String PREFIX_GROUP = "sibg:";
	public static String FACTPREFIX_GROUP = "sibg_";
	public static String NS_GROUP = base + "group/";

	public static String PREFIX_GROUP_MEMBER = "sibgm:";
	public static String FACTPREFIX_GROUP_MEMBER = "sibgm_";
	public static String NS_GROUP_MEMBER = base + "group/membership/";

	public static String PREFIX_POST = "sibpo:";
	public static String FACTPREFIX_POST = "sibpo_";
	
	public static String NS_POST = base + "post/";

	public static String PREFIX_COMMENT = "sibc:";
	public static String FACTPREFIX_COMMENT = "sibc_";
	
	public static String NS_COMMENT = base + "post/comment/";

	public static String PREFIX_PHOTOALBUM = "sibpha:";
	public static String FACTPREFIX_PHOTOALBUM = "sibpha_";
	
	public static String NS_PHOTOALBUM = base + "photoalbum/";

	public static String PREFIX_PHOTO = "sibpho:";
	public static String FACTPREFIX_PHOTO = "sibpho_";
	
	public static String NS_PHOTO = base + "photoalbum/photo/";
	
	public static String PREFIX_GPS = "sibgps:";
	public static String FACTPREFIX_GPS = "sibgps_";
	public static String NS_GPS = base + "gps/";

	
	
	
	private static HashMap<String, String> uriMap = new HashMap<String, String>();
	
	/*
	 * For prefixed versions
	 */
	public static String prefixed(String string) {
		if(uriMap.containsKey(string)) {
			return uriMap.get(string);
		}
		else {
			String newValue = PREFIX + string;
			uriMap.put(string, newValue);
			return newValue;
		}
	}

	public static String factprefixed(String string) {
		if(uriMap.containsKey(string)) {
			return uriMap.get(string);
		}
		else {
			String newValue = FACTPREFIX + string;
			uriMap.put(string, newValue);
			return newValue;
		}
	}
	//Get the URI of this vocabulary
	public static String getURI() { return NS; }
		
	/*

	//Resource type: person
	public static  String Person = (NS+ "Person");
	
	//Resource type: user
	public static  String User = (NS+ "User");

	//Resource type: forum
	public static  String Forum = (NS+ "Forum");

	//Resource type: location
	public static  String Location = (NS+ "Location");

	//Resource type: Organization
	public static  String Organization = (NS+ "Organization");
	
	//Resource type: Post
	public static  String Post = (NS+ "Post");

	//Resource type: Post
	public static  String Comment = (NS+ "Comment");

	//Resource type: Photo
	public static  String Photo = (NS+ "Photo");

	//Resource type: Group
	public static  String Group = (NS+ "group");

	//Property: country
	public static  String location = (NS + "location");
	
	//Property: product
	public static  String organization = (NS + "organization");
	
	//Property: tag
	public static  String tag = (NS + "tag");

	//Property: like
	public static  String like = (NS + "like");

	//Property: added
	public static  String added = (NS + "added");

	//Property: member_of_membership
	public static  String member_of_membership = (NS + "member_of_membership");

	//Property: group-of-membership
	public static  String group_of_membership = (NS + "group_of_membership");
	
	*/

	public static String getLocationURI(int locationRefIdx){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("location");
		s.append(locationRefIdx);
		s.append(">");			
		return s.toString();
	}
	
	public static String getOrganizationURI(int organizationRefIdx){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("organization");
		s.append(organizationRefIdx);
		s.append(">");

		return s.toString();
	}	
	
	/*
	public static String getUserURI(int userIdx){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("user");
		s.append(userIdx);
		s.append(">");
		
		return s.toString();
	}
	*/	
	public static String getUserURI(int userIdx){
		return PREFIX_USER + "u" + userIdx; 
	}

	public static String factgetUserURI(int userIdx){
		return FACTPREFIX_USER + "u" + userIdx; 
	}
	/*
	public static String getPersonURI(int personIdx){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("person");
		s.append(personIdx);
		s.append(">");
			
		return s.toString();
	}
	*/		
	public static String getPersonURI(int personIdx){
		return PREFIX_PERSON + "p" + personIdx; 
	}
	
	public static String factgetPersonURI(int personIdx){
		return FACTPREFIX_PERSON + "p" + personIdx; 
	}
	/*
	public static String getForumURI(int forumIdx){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("forum");
		s.append(forumIdx);
		s.append(">");		

		return s.toString();
	}
	*/
	public static String getForumURI(int forumIdx){
		return PREFIX_FORUM + "fo" + forumIdx;
	}
	public static String factgetForumURI(int forumIdx){
		return FACTPREFIX_FORUM + "fo" + forumIdx;
	}
	
	/*
	public static String getPostURI(int forumId, long postId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("forum");
		s.append("/");
		s.append(forumId);
		s.append("/");
		s.append("post");
		s.append(postId);
		s.append(">");		

		return s.toString();
	}
	*/
	public static String getPostURI(int forumId, long postId){
		return PREFIX_POST + "po" + postId;
	}
	
	public static String factgetPostURI(int forumId, long postId){
		return FACTPREFIX_POST + "po" + postId;
	}
	/*
	public static String getCommentURI(long postId, long commentId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("post");
		s.append("/");
		s.append(postId);
		s.append("/");
		s.append("cmt");
		s.append(commentId);
		s.append(">");		

		return s.toString();
	}
	*/
	public static String getCommentURI(long postId, long commentId){
		return PREFIX_COMMENT + "co" + commentId;
	}

	public static String factgetCommentURI(long postId, long commentId){
		return FACTPREFIX_COMMENT + "co" + commentId;
	}
	/*
	public static String getPhotoAlbumURI(long albumId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("photoalbum");
		s.append("/");
		s.append(albumId);
		s.append(">");		

		return s.toString();
	}
	*/
	
	public static String getPhotoAlbumURI(long albumId){
		return PREFIX_PHOTOALBUM + "pa" + albumId;
	}

	public static String factgetPhotoAlbumURI(long albumId){
		return FACTPREFIX_PHOTOALBUM + "pa" + albumId;
	}

	/*
	public static String getPhotoURI(long albumId, long photoId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("photoalbum");
		s.append("/");
		s.append(albumId);
		s.append("/");
		s.append("photo");
		s.append("/");
		s.append(photoId);
		s.append(">");		

		return s.toString();
	}
	*/
	public static String getPhotoURI(long albumId, long photoId){
		return PREFIX_PHOTO + "pho" + photoId;
	}
	
	public static String getGPSURI(long gpsId){
		return PREFIX_GPS + "gps" + gpsId;
	}

	public static String factgetPhotoURI(long albumId, long photoId){
		return FACTPREFIX_PHOTO + "pho" + photoId;
	}
	
	public static String factgetGPSURI(long gpsId){
		return FACTPREFIX_GPS + "gps" + gpsId;
	}

	/*
	public static String getGroupURI(long groupId){
		StringBuffer s = new StringBuffer();
		s.append("<");
		s.append(SUT);
		s.append("group");
		s.append("/");
		s.append(groupId);
		s.append(">");		

		return s.toString();
	}	
	*/
	
	public static String getGroupURI(long groupId){
		return PREFIX_GROUP + "g" + groupId;
	}
	public static String factgetGroupURI(long groupId){
		return FACTPREFIX_GROUP + "g" + groupId;
	}
	/*
	public static String getGroupMemberShipURI(int groupId, long membershipId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("group");
		s.append("/");
		s.append(groupId);
		s.append("/");
		s.append("membership");
		s.append("/");
		s.append(membershipId);
		s.append(">");		

		return s.toString();
	}	
	*/
	
	public static String getGroupMemberShipURI(int groupId, long membershipId){
		return PREFIX_GROUP_MEMBER + "gm" + membershipId;
	}
	public static String factgetGroupMemberShipURI(int groupId, long membershipId){
		return FACTPREFIX_GROUP_MEMBER + "gm" + membershipId;
	}
	/*
	public static String getFriendshipURI(long friendshipId){
		StringBuffer s = new StringBuffer();
		
		s.append("<");
		s.append(SUT);
		s.append("friendship");
		s.append("/");
		s.append(friendshipId);
		s.append(">");
			
		return s.toString();
	}
	*/
	public static String getFriendshipURI(long friendshipId){
		return PREFIX_FRIENDSHIP + "fr" + friendshipId;
	}

	public static String factgetFriendshipURI(long friendshipId){
		return FACTPREFIX_FRIENDSHIP + "fr" + friendshipId;
	}
		
}

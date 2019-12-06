package sib.serializer;

import sib.objects.Photo;
import sib.objects.Post;
import sib.objects.ReducedUserProfile;
import sib.objects.SocialObject;
import sib.objects.UserExtraInfo;

public class TriG implements Serializer{
	public void serialize(){};
	
	public Long triplesGenerated(){ 
		return (long)100;
	};
	
	public void gatherData(SocialObject socialObject){};
	
	public void gatherData(ReducedUserProfile user, UserExtraInfo extraInfo){};
	
	public void gatherData(Post post, boolean isLikeStream){};
	
	public void gatherData(Photo photo, boolean isLikeStream){};
	
	public void setNumLikeDuplication(int numPostLikeDuplication, int numPhotoLikeDuplication){};
}

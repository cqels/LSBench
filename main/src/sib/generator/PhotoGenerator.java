package sib.generator;

import java.util.Random;
import java.util.Vector;

import sib.dictionary.PopularPlacesDictionary;
import sib.objects.Friend;
import sib.objects.Location;
import sib.objects.Photo;
import sib.objects.PhotoAlbum;
import sib.objects.PopularPlace;
import sib.objects.ReducedUserProfile;
import sib.objects.UserProfile;

public class PhotoGenerator {
	
	DateGenerator		dateGenerator;
	static long			photoAlbumId = 0;
	static long			photoId = 0;
	Vector<Location>	vLocations;
	PopularPlacesDictionary dicPopularPlaces; 
	Random 				rand;
	Random				randLikes;
	int					maxNumUserTags;

	Random				randPopularPlaces;
	Random				randPopularPlacesId; 
	double				probPopularPlaces;
	//int					selectedlocationIdx;			
	
	public PhotoGenerator(DateGenerator _dateGen, Vector<Location> _vLocations, 
						long _seed, int _maxNumUserTags, PopularPlacesDictionary _dicPopularPlaces,
						double _probPopularPlaces){
		this.dateGenerator = _dateGen; 
		this.vLocations = _vLocations; 
		rand = new Random(_seed);
		randLikes = new Random(_seed);
		this.maxNumUserTags = _maxNumUserTags; 
		this.dicPopularPlaces = _dicPopularPlaces; 
		this.randPopularPlaces = new Random(_seed);
		this.randPopularPlacesId = new Random(_seed);
		this.probPopularPlaces = _probPopularPlaces;
	}
	
	public PhotoAlbum generateAlbum(ReducedUserProfile user){
		photoAlbumId++;
		PhotoAlbum album = new PhotoAlbum(); 
		album.setCreatorId(user.getAccountId());
		album.setAlbumId(photoAlbumId);
		album.setCreatedDate(dateGenerator.randomPhotoAlbumCreatedDate(user));
		
		album.setLocationIdx(rand.nextInt(vLocations.size()));
		
		album.setTitle("Album " + vLocations.get(album.getLocationIdx()).getName());
			
		return album;
	}
	public Photo generatePhoto(ReducedUserProfile user, PhotoAlbum album, 
								int idxInAlbum, int maxNumLikes){
		photoId++;
		Photo photo = new Photo();
		
		photo.setAlbumId(album.getAlbumId());
		photo.setCreatorId(album.getCreatorId());
		int locationIdx = album.getLocationIdx();
		byte numPopularPlace = user.getNumPopularPlace();
		photo.setLocationIdx(locationIdx);
		Location location = vLocations.get(locationIdx);
		if (numPopularPlace == 0){
			photo.setLocationName(location.getName());
			photo.setLatt(location.getLatt());
			photo.setLongt(location.getLongt());
		}
		else{
			int popularPlaceId;
			PopularPlace popularPlace;
			if (randPopularPlaces.nextDouble() < probPopularPlaces){
				//Generate photo information from user's popular place
				int popularIndex = randPopularPlacesId.nextInt(numPopularPlace);
				popularPlaceId = user.getPopularId(popularIndex);
				popularPlace = dicPopularPlaces.getPopularPlace(user.getLocationIdx(),popularPlaceId);
				photo.setLocationName(popularPlace.getName());
				photo.setLatt(popularPlace.getLatt());
				photo.setLongt(popularPlace.getLongt());
			}
			
			else{
				// Randomly select one places from Album location idx
				popularPlaceId = dicPopularPlaces.getPopularPlace(locationIdx);
				if (popularPlaceId != -1){
					popularPlace = dicPopularPlaces.getPopularPlace(locationIdx,popularPlaceId);
					photo.setLocationName(popularPlace.getName());
					photo.setLatt(popularPlace.getLatt());
					photo.setLongt(popularPlace.getLongt());
				}
				else{
					photo.setLocationName(location.getName());
					photo.setLatt(location.getLatt());
					photo.setLongt(location.getLongt());
				}
			}
		}
		
		photo.setPhotoId(photoId);
		
		//Assume that the photo are created one by one after 1 second from
		// the creation of the album
		
		photo.setTakenTime(album.getCreatedDate() + 1000*(idxInAlbum+1));	
		
		int numTags = rand.nextInt(maxNumUserTags);
		photo.setTags(getListFriendTags(user, numTags));
		
		int numberOfLikes = randLikes.nextInt(maxNumLikes);
		
		photo.setInterestedUserAccs(getFriendsLiked(user, numberOfLikes));
		
		return photo; 
	}
	
	public int[] getListFriendTags(ReducedUserProfile user, int _numTags){
		int	friendTags[];
		Friend fullFriendList[] = user.getFriendList();
		// Number of location-related and interest-related friends
		int numLocationInterestFriends = user.getLastInterestFriendIdx() + 1;
		if (_numTags >= numLocationInterestFriends){
			friendTags = new int[numLocationInterestFriends];
			for (int i = 0; i < numLocationInterestFriends; i++){
				friendTags[i] = fullFriendList[i].getFriendAcc();
			}
		}
		else{
			friendTags = new int[_numTags];
			int startIdx = rand.nextInt(numLocationInterestFriends - _numTags);
			for (int i = 0; i < _numTags; i++){
				friendTags[i] = fullFriendList[i+startIdx].getFriendAcc();
			}				
		}
		
		return friendTags; 
	}
	
	public int[] getFriendsLiked(ReducedUserProfile user, int numOfLikes){
		Friend fullFriendList[] = user.getFriendList();
		
		int friends[];
		if (numOfLikes >= user.getNumFriendsAdded()){
			friends = new int[user.getNumFriendsAdded()];
			for (int j = 0; j < user.getNumFriendsAdded(); j++){
				friends[j] = fullFriendList[j].getFriendAcc();
			}
		}
		else{
			friends = new int[numOfLikes];
			int startIdx = randLikes.nextInt(user.getNumFriendsAdded() - numOfLikes);
			for (int j = 0; j < numOfLikes; j++){
				friends[j] = fullFriendList[j+startIdx].getFriendAcc();
			}			
		}
		
		return friends; 
		 
	}
	
	public Vector<Location> getvLocations() {
		return vLocations;
	}
	public void setvLocations(Vector<Location> vLocations) {
		this.vLocations = vLocations;
	}
	
}

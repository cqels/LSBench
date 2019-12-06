package sib.generator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import sib.objects.Event;
import sib.objects.GPS;
import sib.objects.ReducedUserProfile;
import sib.objects.UserExtraInfo;
import sib.storage.StreamStoreManager;

public class GPSGenerator {
	Vector<Event>		eventSet;
	Random				randGPS; // gps is generated around one event
	Random				randNumUser; 
	Random				randNumGPS; 
	Random				randNumSharing; 
	Random				randIsProvideGPS; // is a user provide gps info or not
			
	int					totalNumUsers; 
	double				probProvideGPS; 
	int 				maxNumSharingGPSperUser;
	int					maxNumGPSperSharingTime; // FOr each time that user provides the GPS
												// info, how many GPS info that he sends
												// e.g., 10 GPSs, 5 seconds/each
	
	public GPSGenerator(Vector<Event> _eventSet, long seed, int _totalNumUser,
						double _probProvideGPS, int _maxNumSharingGPSperUser,
						int _maxNumGPSperSharingTime){
		this.eventSet = _eventSet;
		this.randGPS = new Random(seed);
		this.randNumUser = new Random(seed);
		this.randNumGPS = new Random(seed);
		this.randNumSharing = new Random(seed);
		this.randIsProvideGPS = new Random(seed);
		this.totalNumUsers = _totalNumUser; 
		this.probProvideGPS = _probProvideGPS; 
		this.maxNumSharingGPSperUser = _maxNumSharingGPSperUser; 
		this.maxNumGPSperSharingTime = _maxNumGPSperSharingTime;
	}
	public GPS generateGPSAroundEvent(int userId, int eventId, int gpsIdx){
		GPS gps = new GPS();
		Event event = eventSet.get(eventId);
		
		gps.setUserId(userId);
		gps.setLatt(event.getLatt() + randGPS.nextDouble() * 0.01);
		gps.setLongt(event.getLongt() + randGPS.nextDouble() * 0.01);
		// Tracked 10s for each gps
		gps.setTrackedTime(event.getEventTime() + gpsIdx * 60000);
		gps.setTrackedLocation(event.getEventPlace());
		
		return gps; 
	}

	public void generateGPSperUser(ReducedUserProfile user, UserExtraInfo extraInfo,
								  StreamStoreManager storeMng){
		long oneday = 1000*60*60*24;
		if (randIsProvideGPS.nextDouble() < probProvideGPS){
			int numSharings = randNumSharing.nextInt(maxNumSharingGPSperUser) + 1;
			int numGPSsPerShare = randNumGPS.nextInt(maxNumGPSperSharingTime) + 1;
			for (int i = 0; i < numSharings; i++){
				for (int j = 0; j < numSharings; j++){
					GPS gps = new GPS();
					gps.setUserId(user.getAccountId());
					gps.setLatt(extraInfo.getLatt() + randGPS.nextDouble() * 0.01);
					gps.setLongt(extraInfo.getLongt() + randGPS.nextDouble() * 0.01);
					// Tracked 10s for each gps
					long trackedTime = (long)(user.getCreatedDate() + i * oneday + j * 5000);
					gps.setTrackedTime(trackedTime);
					gps.setTrackedLocation(extraInfo.getLocation());
					
					storeMng.serialize(gps);
				}
			}
		}
	}

	/*
	 * Assume that each events attract 50->200 users
	 * Each user at an event provides gps information for
	 * 100 times
	 */
	public void generateAllGPSForAllEvents(StreamStoreManager storeMng){
		for (int i = 0; i < eventSet.size(); i++){
			int numAttendedUsers = randNumUser.nextInt(100) + 20;
			//int lastUserId = 0;
			HashSet<Integer> attendedUsers = new HashSet<Integer>(numAttendedUsers);
			while (attendedUsers.size() < numAttendedUsers){
				//int step = randNumUser.nextInt(totalNumUsers - numAttendedUsers- lastUserId + j);
				//lastUserId = lastUserId + step + j;
				int userId = randNumUser.nextInt(totalNumUsers);
				if (!attendedUsers.contains(userId)){
					attendedUsers.add(userId);
				}
			}
			
			Iterator it = attendedUsers.iterator(); 
			while (it.hasNext()){
				Integer userId = (Integer)it.next();
				int numGPSforUser = randNumGPS.nextInt(100);
				
				for (int j = 0; j < numGPSforUser; j++){
					GPS gps = generateGPSAroundEvent(userId.intValue(),i,j);
					storeMng.serialize(gps);
				}
			}
		}
	}
}

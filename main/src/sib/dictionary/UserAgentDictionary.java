package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

import sib.objects.Comment;
import sib.objects.Friend;
import sib.objects.Photo;
import sib.objects.Post;
import sib.objects.ReducedUserProfile;
import sib.objects.UserProfile;

public class UserAgentDictionary {
	String 				agentFileName = "";
	Vector<String> 		vUserAgents;
	RandomAccessFile 	agentFile; 
	Random				randGen;
	double 				probSentFromAgent; 
	Random				randSentFrom;
	
	public UserAgentDictionary(String _agentFileName, long seed, long seed2, double _probSentFromAgent){
		this.agentFileName = _agentFileName; 
		randGen = new Random(seed);
		randSentFrom = new Random(seed2);
		this.probSentFromAgent = _probSentFromAgent; 
	}
	
	public void init(){
		
		try {
			vUserAgents = new Vector<String>();
			agentFile = new RandomAccessFile(agentFileName, "r");
			
			extractAgents(); 
			
			agentFile.close();
			
			System.out.println("Done ... " + vUserAgents.size() + " agents have been extracted ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractAgents(){
		String line; 
		
		try {
			while ((line = agentFile.readLine()) != null){
				vUserAgents.add(line.trim());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPostUserAgent(ReducedUserProfile user, Post post){
		// Sent from user's agent
		if (user.isHaveSmartPhone() && (randSentFrom.nextDouble() > probSentFromAgent)){
			post.setUserAgent(getUserAgent(user.getAgentIdx()));
		}
		else
			post.setUserAgent("");
	}

	public void setCommentUserAgent(Friend friend, Comment comment){
		// Sent from user's agent
		if (friend.isHaveSmartPhone() && (randSentFrom.nextDouble() > probSentFromAgent)){
			comment.setUserAgent(getUserAgent(friend.getAgentIdx()));
		}
		else
			comment.setUserAgent("");
	}
	
	public void setPhotoUserAgent(ReducedUserProfile user, Photo photo){
		// Sent from user's agent
		if (user.isHaveSmartPhone() && (randSentFrom.nextDouble() > probSentFromAgent)){
			photo.setUserAgent(getUserAgent(user.getAgentIdx()));
		}
		else
			photo.setUserAgent("");
	}
	
	public String getUniformRandomAgent(){
		int randIdx = randGen.nextInt(vUserAgents.size());
		
		return vUserAgents.get(randIdx); 
	}
	
	public byte getRandomUserAgentIdx(){
		return (byte)randGen.nextInt(vUserAgents.size());
	}	
	public String getUserAgent(int idx){
		return vUserAgents.get(idx);
	}
}

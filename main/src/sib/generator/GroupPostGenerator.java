package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

import sib.objects.Comment;
import sib.objects.Group;
import sib.objects.GroupMemberShip;
import sib.objects.Post;

public class GroupPostGenerator {
	/*
	private String groupPostFile; 
	
	RandomAccessFile groupPostDictionary;
	
	Random rand;				// This random generator is used for generating 
								// friendIdx, articleIdx
	Random randTextSize;

	Vector<Vector<String>> groupArticles;
	Vector<Vector<String>> groupArticleTags;
	
	DateGenerator dateGen;
	int numOfArticles;
	
	//static int postId = -1;
	//static int commentId = -1;
	Random randReplyTo; 			//For comment

	public GroupPostGenerator(String _groupPostFile, long seed, long seedTextSize, DateGenerator _dateGen) {
		numOfArticles = -1;
		rand = new Random(seed);
		randTextSize = new Random(seedTextSize);
		this.groupPostFile = _groupPostFile;
		
		this.dateGen = _dateGen;
		
		groupArticles = new Vector<Vector<String>>();
		groupArticleTags = new Vector<Vector<String>>();

		//groupArticlesInit();
		randReplyTo = new Random(seed);
	}

	public void groupArticlesInit() {
		try {
			groupPostDictionary = new RandomAccessFile(groupPostFile, "r");
			groupArticlesExtract();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void groupArticlesExtract() {
		String strLine = "";
		String curTopic = "";
		//System.out.println("Scanning articles dictionary");
		int lineNo = 0;
		try {
			while ((strLine = groupPostDictionary.readLine()) != null) {
				lineNo++;
				String infos[] = strLine.split("  ");
				if (curTopic.compareTo(infos[0]) != 0) {
					groupArticles.add(new Vector<String>());
					groupArticleTags.add(new Vector<String>());
					groupArticleTags.lastElement().add(infos[1]);
					groupArticles.lastElement().add(infos[2]);
					curTopic = infos[0];
					numOfArticles++;
				} else {
					groupArticleTags.lastElement().add(infos[1]);
					groupArticles.lastElement().add(infos[2]);
					numOfArticles++;
				}
			}
			
			System.out.println("Done ... " + numOfArticles + " group articles have been extracted ");
		} catch (Exception e) {
			System.out.println("Problem at line " + lineNo);
			e.printStackTrace();
		}
	}
	
	public int getGroupArticleIdx(int categoryIdx) {
		return rand.nextInt(groupArticles.get(categoryIdx).size());
	}
	
	public String getRandomGroupPostText(int minSizeOfText, int maxSizeOfText,
			int categoryIdx, int articleIdx) {
		String content;

		int textSize;
		int startingPos;

		String finalString = "";

		content = groupArticles.get(categoryIdx).get(articleIdx);

		// Generate random fragment from the content
		textSize = randTextSize.nextInt(maxSizeOfText - minSizeOfText)
				+ minSizeOfText;

		if (textSize >= content.length()) {
			return content;
		} else {
			// Get the starting position for the fragment of text
			startingPos = randTextSize.nextInt(content.length() - textSize);
			finalString = content
					.substring(startingPos, startingPos + textSize - 1 );

			return returnWholeWords(finalString);
		}
	}
	
	
	public String getRandomGroupPostComment(int minSizeOfComment, int maxSizeOfComment,
			int categoryIdx, int articleIdx) {
		String content;

		int textSize;
		int startingPos;

		String finalString = "";

		content = groupArticles.get(categoryIdx).get(articleIdx);

		// Generate random fragment from the content
		textSize = randTextSize.nextInt(maxSizeOfComment - minSizeOfComment)
				+ minSizeOfComment;

		if (textSize >= content.length()) {
			return content;
		} else {
			// Get the starting position for the fragment of text
			startingPos = randTextSize.nextInt(content.length() - textSize);
			finalString = content
					.substring(startingPos, startingPos + textSize - 1);
			

			return returnWholeWords(finalString);
		}
	}	
	
	public String returnWholeWords(String inputString){
		int posSpace = inputString.indexOf(" ");
		String returnString = inputString.substring(posSpace).trim();
		posSpace = returnString.lastIndexOf(" ");
		
		if (posSpace != -1){
			returnString = returnString.substring(0, posSpace);
		}

		return returnString;
	}
	
	public String[] getGroupPostTags(int categoryIdx, int articleIdx) {

		String tags = groupArticleTags.get(categoryIdx).get(articleIdx);
		
		return tags.split(" ");
	}
	
	public int[] getMembersLiked(Group group, int numOfLikes){
		GroupMemberShip groupMembers[] = group.getMemberShips();

		int friends[];
		if (numOfLikes >= groupMembers.length){
			friends = new int[groupMembers.length];
			for (int j = 0; j < groupMembers.length; j++){
				friends[j] = groupMembers[j].getUserId();
			}
		}
		else{
			friends = new int[numOfLikes];
			int startIdx = rand.nextInt(groupMembers.length - numOfLikes);
			for (int j = 0; j < numOfLikes; j++){
				friends[j] = groupMembers[j+startIdx].getUserId();
			}			
		}
		
		return friends; 
	}
	
	public Post getRandomGroupPost(Group group, int minSizeOfText,
			int maxSizeOfText, int maxNumberOfLikes) {
		
		RandomTextGenerator.postId++;
		// String tags[] =
		Post post = new Post();
		
		// Get one authorId
		int memberIdx = rand.nextInt(group.getMemberShips().length);
		GroupMemberShip memberShip = group.getMemberShips()[memberIdx];
			
		int articleIdx = getGroupArticleIdx(group.getLocationIdx());
		post.setArticleIdx(articleIdx);
		
		post.setAuthorId(memberShip.getUserId());
		
		post.setContent(getRandomGroupPostText(minSizeOfText, maxSizeOfText,
				group.getLocationIdx(), articleIdx));
		
		post.setCreatedDate(dateGen.randomGroupPostCreatedDate(memberShip.getJoinDate()));

		post.setForumId(group.getForumWallId()); // Temporarily use this value
													// for forumId
		post.setPostId(RandomTextGenerator.postId);
		
		post.setTags(getGroupPostTags(group.getLocationIdx(), articleIdx));
		
		int numberOfLikes = rand.nextInt(maxNumberOfLikes);
		
		post.setInterestedUserAccs(getMembersLiked(group, numberOfLikes));
		
		// Get random comments
		
		return post;
	}
	
	// The content of the commment is generated 
	// from the article containing the post
	public Comment getRandomGroupComment(Post post, Group group, 
			int minSizeOfComment, int maxSizeOfComment, long lastCommentCreatedDate,
			long startCommentId, long lastCommentId){
		
		Comment comment = new Comment();
		
		// Randomly select one member
		int memberIdx = rand.nextInt(group.getMemberShips().length);
		
		GroupMemberShip memberShip = group.getMemberShips()[memberIdx];

		if (memberShip.getJoinDate() > post.getCreatedDate()){
			comment.setAuthorId(-1);
			return comment;
		}
		
		RandomTextGenerator.commentId++;
		comment.setAuthorId(memberShip.getUserId());
		comment.setCommentId(RandomTextGenerator.commentId);
		comment.setPostId(post.getPostId());
		comment.setReply_of(getReplyToId(startCommentId, lastCommentId));
		comment.setForumId(post.getForumId());
		comment.setContent(getRandomGroupPostComment(minSizeOfComment, maxSizeOfComment, 
									group.getLocationIdx(), post.getArticleIdx()));
		
		comment.setCreateDate(dateGen.powerlawCommDateDay(lastCommentCreatedDate));
		
		return comment;
	}
	
	public long getReplyToId(long startId, long lastId){
		int parentId; 
		if (lastId > (startId+1)){
			parentId = randReplyTo.nextInt((int)(lastId - startId));
			if (parentId == 0) return -1; 
			else return (long)(parentId + startId); 
		}
		
		return -1; 
	}
	*/
}

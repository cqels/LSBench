package sib.objects;

import java.io.Serializable;

public class PostStream  implements Serializable{
	boolean isaPost = true; 
	
	long postId; 
	String title; 
	String content; 
	long createdDate; 
	int authorId; 
	int forumId;
	
	int articleIdx;					// Index of articles in the set of same region/interest article  					
	int interestIdx;				// Index of one interest in user's interests
	
	String tags[]; 
	int interestedUserAccs[];		//List of users who are interested in the post  
	
	IP ipAddress; 
	String userAgent;				// Send from where e.g., iPhone, Samsung, HTC
	
	byte browserIdx;					// Set browser Idx 
	
	boolean isInterestPost; 		//Only use for group's post
	
	long commentId; 
	
	long reply_of; 			//Id of the parent post/comment of this comment
	
	
	public PostStream(){}
	public PostStream(Post post){
		isaPost = true; 
		postId = post.getPostId(); 
		title = post.getTitle();
		content = post.getContent();
		createdDate = post.getCreatedDate(); 
		authorId = post.getAuthorId();
		forumId = post.getForumId(); 
		articleIdx = post.getArticleIdx();
		interestIdx = post.getInterestIdx();
		
		tags = post.getTags();
		interestedUserAccs = post.getInterestedUserAccs(); 
		ipAddress = post.getIpAddress();
		userAgent = post.getUserAgent(); 
		browserIdx = post.getBrowserIdx(); 
		isInterestPost = post.isInterestPost();
	}
	
	public PostStream(Comment comment){
		isaPost = false; 
		postId = comment.getPostId(); 
		content = comment.getContent();
		createdDate = comment.getCreateDate(); 
		authorId = comment.getAuthorId();
		forumId = comment.getForumId(); 
		
		ipAddress = comment.getIpAddress();
		userAgent = comment.getUserAgent(); 
		browserIdx = comment.getBrowserIdx();
		
		commentId = comment.getCommentId(); 
		reply_of = comment.getReply_of();
	}
	
	public Comment getComment(){
		Comment comment = new Comment();
		comment.setPostId(postId);
		comment.setContent(content);
		comment.setCreateDate(createdDate);
		comment.setAuthorId(authorId);
		comment.setForumId(forumId);
		comment.setIpAddress(ipAddress);
		comment.setUserAgent(userAgent);
		comment.setBrowserIdx(browserIdx);
		comment.setCommentId(commentId);
		comment.setReply_of(reply_of);
		
		return comment; 
	}
	public Post getPost(){
		Post post = new Post();
		post.setPostId(postId);
		post.setTitle(title);
		post.setContent(content);
		post.setCreatedDate(createdDate);
		post.setAuthorId(authorId);
		post.setForumId(forumId);
		post.setArticleIdx(articleIdx);
		post.setInterestIdx(interestIdx);
		post.setTags(tags);
		post.setIpAddress(ipAddress);
		post.setUserAgent(userAgent);
		post.setBrowserIdx(browserIdx);
		post.setInterestedUserAccs(interestedUserAccs);
		post.setInterestPost(isInterestPost);
		
		return post; 
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public int[] getInterestedUserAccs() {
		return interestedUserAccs;
	}
	public void setInterestedUserAccs(int[] interestedUserAccs) {
		this.interestedUserAccs = interestedUserAccs;
	}
	public boolean isIsaPost() {
		return isaPost;
	}
	public void setIsaPost(boolean isaPost) {
		this.isaPost = isaPost;
	}
	
	public void printPostStream(){
		System.out.println("postId : " + postId );
		System.out.println("title : " + title );
		System.out.println("content : " + content );
		System.out.println("createdDate : " + createdDate );
		System.out.println("authorId : " + authorId );
		System.out.println("forumId : " + forumId );
		System.out.println("articleIdx : " + articleIdx );
		System.out.println("interestIdx : " + interestIdx );
		System.out.println("tags : " + tags.length );
		System.out.println("interestedUserAccs : " + interestedUserAccs.length );
		System.out.println("ipAddress : " + ipAddress.toString() );
		System.out.println("userAgent : " + userAgent );
		System.out.println("browserIdx : " + browserIdx );
		System.out.println("commentId : " + commentId );
		System.out.println("reply_of : " + reply_of );
	}

}

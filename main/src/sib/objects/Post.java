package sib.objects;

public class Post extends SocialObject{
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
		/*
		public Post(int numOfTags, int numOfLikes){
			tags = new ArrayList<String>(numOfTags);
			interestedUserAccs = new ArrayList<Integer>(numOfLikes);
		}
		*/
		public int getInterestIdx() {
			return interestIdx;
		}
		public void setInterestIdx(int interestIdx) {
			this.interestIdx = interestIdx;
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
		
		public long getPostId() {
			return postId;
		}
		public void setPostId(long postId) {
			this.postId = postId;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public long getCreatedDate() {
			return createdDate;
		}
		public void setCreatedDate(long createdDate) {
			this.createdDate = createdDate;
		}
		public int getAuthorId() {
			return authorId;
		}
		public void setAuthorId(int authorId) {
			this.authorId = authorId;
		}
		public int getForumId() {
			return forumId;
		}
		public void setForumId(int forumId) {
			this.forumId = forumId;
		}
		public int getArticleIdx() {
			return articleIdx;
		}
		public void setArticleIdx(int articleIdx) {
			this.articleIdx = articleIdx;
		}
		public String getUserAgent() {
			return userAgent;
		}
		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}
		public IP getIpAddress() {
			return ipAddress;
		}
		public void setIpAddress(IP ipAddress) {
			this.ipAddress = ipAddress;
		}
		public boolean isInterestPost() {
			return isInterestPost;
		}
		public void setInterestPost(boolean isInterestPost) {
			this.isInterestPost = isInterestPost;
		}
		public byte getBrowserIdx() {
			return browserIdx;
		}
		public void setBrowserIdx(byte browserId) {
			this.browserIdx = browserId;
		}		
}

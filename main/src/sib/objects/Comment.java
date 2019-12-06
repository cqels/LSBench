package sib.objects;

public class Comment extends SocialObject {
	long commentId; 
	String content; 
	long postId;
	int authorId; 
	long createDate;
	
	int forumId; 
	long reply_of; 			//Id of the parent post/comment of this comment
	
	IP ipAddress; 
	String userAgent;				// Send from where e.g., iPhone, Samsung, HTC
	byte browserIdx;				// Set browser Idx	
	
	public IP getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(IP ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public byte getBrowserIdx() {
		return browserIdx;
	}
	public void setBrowserIdx(byte browserIdx) {
		this.browserIdx = browserIdx;
	}
	public long getReply_of() {
		return reply_of;
	}
	public void setReply_of(long reply_of) {
		this.reply_of = reply_of;
	}
	public int getForumId() {
		return forumId;
	}
	public void setForumId(int forumId) {
		this.forumId = forumId;
	}
	public long getCommentId() {
		return commentId;
	}
	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getPostId() {
		return postId;
	}
	public void setPostId(long postId) {
		this.postId = postId;
	}
	public int getAuthorId() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public long getCreateDate() {
		return createDate;
	}
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	} 
	
}

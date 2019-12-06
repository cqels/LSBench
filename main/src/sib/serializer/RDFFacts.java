package sib.serializer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import sib.generator.DateGenerator;
import sib.objects.Comment;
import sib.objects.Friend;
import sib.objects.FriendShip;
import sib.objects.GPS;
import sib.objects.Group;
import sib.objects.GroupMemberShip;
import sib.objects.Photo;
import sib.objects.PhotoAlbum;
import sib.objects.Post;
import sib.objects.ReducedUserProfile;
import sib.objects.RelationshipStatus;
import sib.objects.SocialObject;
import sib.objects.UserExtraInfo;
import sib.objects.UserProfile;
import sib.vocabulary.DBP;
import sib.vocabulary.DBPO;
import sib.vocabulary.DBPPROP;
import sib.vocabulary.DC;
import sib.vocabulary.DCTERMS;
import sib.vocabulary.FOAF;
import sib.vocabulary.RDF;
import sib.vocabulary.RDFS;
import sib.vocabulary.SIB;
import sib.vocabulary.SIOC;
import sib.vocabulary.SIOCT;
import sib.vocabulary.XSD;

public class RDFFacts implements Serializer {
	
	private long nrTriples;
	private int numPostLikeDuplication; 
	private int	numPhotoLikeDuplication; 
	private FileWriter[] dataFileWriter;
	private boolean forwardChaining;
	private boolean haveToGeneratePrefixes = true;
	int currentWriter = 0;
	static long membershipId = 0;
	static long friendshipId = 0; 
	static long gpsId = 0; 
	
	HashMap<Integer, String> interestIdsNames;
	Vector<String>	vBrowserNames;
	
	GregorianCalendar date;
	
	public RDFFacts(String file, boolean forwardChaining)
	{
		this(file, forwardChaining, 1);
	}
	
	public RDFFacts(String file, boolean forwardChaining, int nrOfOutputFiles)
	{
		date = new GregorianCalendar();
		int nrOfDigits = ((int)Math.log10(nrOfOutputFiles)) + 1;
		String formatString = "%0" + nrOfDigits + "d";
		try{
			dataFileWriter = new FileWriter[nrOfOutputFiles];
			if(nrOfOutputFiles==1)
				this.dataFileWriter[0] = new FileWriter(file + ".fact");
			else
				for(int i=1;i<=nrOfOutputFiles;i++)
					dataFileWriter[i-1] = new FileWriter(file + String.format(formatString, i) + ".fact");
				
		} catch(IOException e){
			System.err.println("Could not open File for writing.");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		try {
			for(int i=0;i<nrOfOutputFiles;i++)
				dataFileWriter[i].append(getNamespaces());
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
		this.forwardChaining = forwardChaining;
		nrTriples = 0l;
		
	}

        public RDFFacts(String file, boolean forwardChaining, int nrOfOutputFiles, 
        			HashMap<Integer, String> _interestIdsNames, Vector<String> _vBrowsers)
        {
        		date = new GregorianCalendar();
        		this.interestIdsNames = _interestIdsNames;	
        		this.vBrowserNames = _vBrowsers;
                int nrOfDigits = ((int)Math.log10(nrOfOutputFiles)) + 1;
                String formatString = "%0" + nrOfDigits + "d";
                try{
                        dataFileWriter = new FileWriter[nrOfOutputFiles];
                        if(nrOfOutputFiles==1)
                                this.dataFileWriter[0] = new FileWriter(file + ".fact");
                        else
                                for(int i=1;i<=nrOfOutputFiles;i++)
                                        dataFileWriter[i-1] = new FileWriter(file + String.format(formatString, i) + ".fact");

                } catch(IOException e){
                        System.err.println("Could not open File for writing.");
                        System.err.println(e.getMessage());
                        System.exit(-1);
                }

                try {
                        for(int i=0;i<nrOfOutputFiles;i++)
                                dataFileWriter[i].append(getNamespaces());
                } catch(IOException e) {
                        System.err.println(e.getMessage());
                }

                this.forwardChaining = forwardChaining;
                nrTriples = 0l;
        }
	
	@Override
	public Long triplesGenerated() {
		return nrTriples;
	}
	
	@Override
	public void gatherData(SocialObject socialObject){
		if(haveToGeneratePrefixes) {
			generatePrefixes();
			haveToGeneratePrefixes = false;
		}

		try {
				if(socialObject instanceof FriendShip){
					dataFileWriter[currentWriter].append(convertFriendShip((FriendShip)socialObject));
				}
				else if(socialObject instanceof Post){
					dataFileWriter[currentWriter].append(convertPost((Post)socialObject));
				}
				else if(socialObject instanceof Comment){
					dataFileWriter[currentWriter].append(convertComment((Comment)socialObject));
				}
				else if (socialObject instanceof PhotoAlbum){
					dataFileWriter[currentWriter].append(convertPhotoAlbum((PhotoAlbum)socialObject));
				}
				else if (socialObject instanceof Photo){
					dataFileWriter[currentWriter].append(convertPhoto((Photo)socialObject));
				}
				else if (socialObject instanceof Group){
					dataFileWriter[currentWriter].append(convertGroup((Group)socialObject));
				}
				else if (socialObject instanceof GPS){
					dataFileWriter[currentWriter].append(convertGPS((GPS)socialObject));
				}
				currentWriter = (currentWriter + 1) % dataFileWriter.length;
		}
		catch(IOException e){
			System.out.println("Cannot write to output file ");
			e.printStackTrace();
			System.exit(-1);
		}
		
	} 
	
	@Override
	public void gatherData(ReducedUserProfile userProfile, UserExtraInfo extraInfo){
		
		try {
			dataFileWriter[currentWriter].append(convertUserProfile(userProfile, extraInfo));
		} catch (IOException e) {
			System.out.println("Cannot write to output file ");
			e.printStackTrace();
		}
	}
	
	public void gatherData(Post post, boolean isLikeStream){
		
		try {
			dataFileWriter[currentWriter].append(convertPost(post, isLikeStream));
		} catch (IOException e) {
			System.out.println("Cannot write to output file ");
			e.printStackTrace();
		}
	}
	
	public void gatherData(Photo photo, boolean isLikeStream){
		
		try {
			dataFileWriter[currentWriter].append(convertPhoto(photo, isLikeStream));
		} catch (IOException e) {
			System.out.println("Cannot write to output file ");
			e.printStackTrace();
		}
	}
	
	/*
	private String getNamespaces() {
		StringBuffer result = new StringBuffer();
		result.append(createPrefixLine(RDF.PREFIX, RDF.NS));
		result.append(createPrefixLine(RDFS.PREFIX, RDFS.NS));
		result.append(createPrefixLine(FOAF.PREFIX, FOAF.NS));
		result.append(createPrefixLine(XSD.PREFIX, XSD.NS));
		result.append(createPrefixLine(DC.PREFIX, DC.NS));
		result.append(createPrefixLine(DCTERMS.PREFIX, DCTERMS.NS));
		result.append(createPrefixLine(SIOC.PREFIX, SIOC.NS));
		result.append(createPrefixLine(SIOCT.PREFIX, SIOCT.NS));
		result.append(createPrefixLine(DBP.PREFIX, DBP.NS));
		result.append(createPrefixLine(DBPO.PREFIX, DBPO.NS));
		result.append(createPrefixLine(DBPPROP.PREFIX, DBPPROP.NS));
		result.append(createPrefixLine(SIB.PREFIX, SIB.NS));
		
		//Prefix for other entities of sib 
		result.append(createPrefixLine(SIB.PREFIX_PERSON, SIB.NS_PERSON));
		result.append(createPrefixLine(SIB.PREFIX_USER, SIB.NS_USER));
		result.append(createPrefixLine(SIB.PREFIX_FORUM, SIB.NS_FORUM));
		result.append(createPrefixLine(SIB.PREFIX_FRIENDSHIP, SIB.NS_FRIENDSHIP));
		result.append(createPrefixLine(SIB.PREFIX_GROUP, SIB.NS_GROUP));
		result.append(createPrefixLine(SIB.PREFIX_GROUP_MEMBER, SIB.NS_GROUP_MEMBER));
		result.append(createPrefixLine(SIB.PREFIX_POST, SIB.NS_POST));
		result.append(createPrefixLine(SIB.PREFIX_COMMENT, SIB.NS_COMMENT));
		result.append(createPrefixLine(SIB.PREFIX_PHOTO, SIB.NS_PHOTO));
		result.append(createPrefixLine(SIB.PREFIX_PHOTOALBUM, SIB.NS_PHOTOALBUM));
		result.append(createPrefixLine(SIB.PREFIX_GPS, SIB.NS_GPS));
		
		
		return result.toString();
	}
	*/
	private String getNamespaces(){
		return "";
	}
	private String createPrefixLine(String prefix, String namespace) {
		StringBuffer result = new StringBuffer();
		result.append("@prefix ");
		result.append(prefix);
		result.append(" ");
		result.append(createURIref(namespace));
		result.append(" .\n");
		
		return result.toString();
	}

	//Create URIREF from URI
	private String createURIref(String uri)
	{
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(uri);
		result.append(">");
		return result.toString();
	}
	
	private void generatePrefixes() {
		
	}
	
	
	public String convertUserProfile(ReducedUserProfile profile, UserExtraInfo extraInfo){
		StringBuffer result = new StringBuffer();
		//First the uriref for the subject
		
		//a
		result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					"is_a",
					FOAF.factprefixed("Person")					
					));
		
		//foaf:firstName
		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				FOAF.factprefixed("firstName"),
				createLiteral(extraInfo.getFirstName())					
				));		

		//foaf:lastName
		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				FOAF.factprefixed("lastName"),
				createLiteral(extraInfo.getLastName())					
				));		

		//dc:location
		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				FOAF.factprefixed("based_near"),
				DBP.factprefixed(extraInfo.getLocation())					
				));		

		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				FOAF.factprefixed("based_near"),
				createLiteral(extraInfo.getLocation())					
				));		
		
		
		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				DBPPROP.factprefixed("latd"),
				Double.toString(extraInfo.getLatt())					
				));		
		

		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				DBPPROP.factprefixed("longd"),
				Double.toString(extraInfo.getLongt())					
				));	
		

		//dc:organization	
		if (!extraInfo.getOrganization().equals("")){

			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					FOAF.factprefixed("organization"),
					createLiteral(extraInfo.getOrganization())					
					));	
			
		}
		
		//sib:class_year
		if (extraInfo.getClassYear() != -1 ){
			date.setTimeInMillis(extraInfo.getClassYear());
			String dateString = DateGenerator.formatYear(date);
			
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					SIB.factprefixed("class_year"),
					createLiteral(dateString)
					));	

		}
		
		//sib:workAt
		if (!extraInfo.getCompany().equals("")){
			
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					SIB.factprefixed("workAt"),
					createLiteral(extraInfo.getCompany()) 
					));	
			
			
			//sib:workFrom
			date.setTimeInMillis(extraInfo.getWorkFrom());
			String dateString = DateGenerator.formatYear(date);
			
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					SIB.factprefixed("workFrom"),
					createLiteral(dateString)
					));	
		}
		// For user's extra info
		if (!extraInfo.getGender().equals(""))
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					FOAF.factprefixed("gender"),
					createLiteral(extraInfo.getGender()) 
					));	
			
		
		
		if (extraInfo.getDateOfBirth() != -1 ){
			date.setTimeInMillis(extraInfo.getDateOfBirth());
			String dateString = DateGenerator.formatDate(date);
			
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					FOAF.factprefixed("birthday"),
					createLiteral(dateString)					
					));	
			
		}
		
		if (!extraInfo.getEmail().equals("")){
			result.append(createTripleSPOStatic(
					SIB.factgetPersonURI((int)profile.getAccountId()),
					SIOC.factprefixed("email"),
					createLiteral(extraInfo.getEmail()) 
					));	
		}

		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				SIB.factprefixed("browser"),
				createLiteral(vBrowserNames.get(profile.getBrowserIdx())) 
				));	

		result.append(createTripleSPOStatic(
				SIB.factgetPersonURI((int)profile.getAccountId()),
				SIOC.factprefixed("ip_address"),
				createLiteral(profile.getIpAddress().toString()) 
				));	
		

		//a for user
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				"is_a",
				SIB.factprefixed("User") 
				));	

		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				"is_a",
				 SIOC.factprefixed("user") 
				));	
		
		
		//sioc:account_of
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				SIOC.factprefixed("account_of"),
				SIB.factgetPersonURI((int)profile.getAccountId())
				));			
		
		//sioc:moderator_of
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				SIOC.factprefixed("moderator_of"),
				SIB.factgetForumURI(profile.getForumWallId())
				));			

		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				SIOC.factprefixed("moderator_of"),
				SIB.factgetForumURI(profile.getForumStatusId())
				));		

		//sioc:subscriber_of
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				SIOC.factprefixed("subscriber_of"),
				SIB.factgetForumURI(profile.getForumWallId())
				));			

		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				SIOC.factprefixed("subscriber_of"),
				SIB.factgetForumURI(profile.getForumStatusId())
				));		
		
		
		//sib:status
		String status = ""; 
		if (extraInfo.getStatus() != RelationshipStatus.NOSTATUS){
			
			switch (extraInfo.getStatus()) {
				case SINGLE: 
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("status"),
							createLiteral("Single")
							));							

					break;   
				case IN_A_RELATIONSHIP:
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("status"),
							createLiteral("In a relationship")
							));							
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("In_relationship_with"),
							SIB.factgetUserURI(extraInfo.getSpecialFriendIdx())
							));	
					break;
				case ENGAGED:
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("status"),
							createLiteral("Engaged")
							));
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("Engaged_with"),
							SIB.factgetUserURI(extraInfo.getSpecialFriendIdx())
							));						
					
					break;
				case MARRIED:
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("status"),
							createLiteral("Married")
							));

					result.append(createTripleSPOStatic(
							SIB.factgetUserURI((int)profile.getAccountId()),
							SIB.factprefixed("Married_with"),
							SIB.factgetUserURI(extraInfo.getSpecialFriendIdx())
							));
					
					break;
				default:
					break;
			}
			
		
		}
		
		
		//dc:created
		date.setTimeInMillis(profile.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);

		result.append(createTripleSPOStatic(
				SIB.factgetUserURI((int)profile.getAccountId()),
				DC.factprefixed("date"),
				createLiteral(dateString)
				));
		
		
		
		// For the interests
		Iterator it = profile.getSetOfInterests().iterator();
		while (it.hasNext()){
			Integer interestIdx = (Integer)it.next();
			//String interest = "" + interestIdx;
			String interest = interestIdsNames.get(interestIdx);
			result.append(createTripleSPOStatic(SIB.factgetUserURI(profile.getAccountId()),
							SIB.factprefixed("interest"),
							createLiteral(interest)
							));      	
		}	

		//For the friendships
		Friend friends[] = profile.getFriendList();
		for (int i = 0; i < friends.length; i ++){
			if (friends[i] != null){
				
				// Frienship is only written one time for two user
				// Thus, we write for user with smaller Id
				if (profile.getAccountId() < friends[i].getFriendAcc()){
					
					//sib:memb
					result.append(createTripleSPOStatic(
							SIB.factgetFriendshipURI(friendshipId),
							SIB.factprefixed("memb"),
							SIB.factgetUserURI(profile.getAccountId())
							));      	

					result.append(createTripleSPOStatic(
							SIB.factgetFriendshipURI(friendshipId),
							SIB.factprefixed("memb"),
							SIB.factgetUserURI(friends[i].getFriendAcc())
							));      	
					
					
					//sib:initiator
					if (friends[i].getInitiator() == 0){
						result.append(createTripleSPOStatic(
								SIB.factgetFriendshipURI(friendshipId),
								SIB.factprefixed("initiator"),
								SIB.factgetUserURI(friends[i].getFriendAcc())
								));
						
					}
					else
						result.append(createTripleSPOStatic(
								SIB.factgetFriendshipURI(friendshipId),
								SIB.factprefixed("initiator"),
								SIB.factgetUserURI(profile.getAccountId())
								));

					
					//sib:requested
					date.setTimeInMillis(friends[i].getRequestTime());
					dateString = DateGenerator.formatDateDetail(date);

					result.append(createTripleSPOStatic(
							SIB.factgetFriendshipURI(friendshipId),
							SIB.factprefixed("requested"),
							createLiteral(dateString)
							));
					
					if (friends[i].getDeclinedTime() == -1){
						//sib:approved
						date.setTimeInMillis(friends[i].getCreatedTime());
						dateString = DateGenerator.formatDateDetail(date);
						result.append(createTripleSPOStatic(
								SIB.factgetFriendshipURI(friendshipId),
								SIB.factprefixed("approved"),
								createLiteral(dateString)
								));
						
					}
					else{
						if (friends[i].getCreatedTime() == -1){		// No re-approved
							//sib:declined
							date.setTimeInMillis(friends[i].getDeclinedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPOStatic(
									SIB.factgetFriendshipURI(friendshipId),
									SIB.factprefixed("declined"),
									createLiteral(dateString)
									));							
						}
						else{			// has been re-approved
							//sib:declined
							date.setTimeInMillis(friends[i].getDeclinedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPOStatic(
									SIB.factgetFriendshipURI(friendshipId),
									SIB.factprefixed("declined"),
									createLiteral(dateString)
									));							

							//sib:reapproved
							date.setTimeInMillis(friends[i].getCreatedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPOStatic(
									SIB.factgetFriendshipURI(friendshipId),
									SIB.factprefixed("reapproved"),
									createLiteral(dateString)
									));							
						}
						
					}
					
					
					friendshipId++;
				}
				
				
				//foaf:knows
				if (friends[i].getCreatedTime() != -1){
					result.append(createTripleSPOStatic(
							SIB.factgetUserURI(profile.getAccountId()),
							FOAF.factprefixed("knows"),
							SIB.factgetUserURI(friends[i].getFriendAcc())));
				}
				
				// Remove the following triple because it will be duplicated with 
				// the friendship info of the friend of this user
			}
			else
				break; 
		}
		
		
		return result.toString();	
	}
	
	public String convertFriendShip(FriendShip friendShip){
		StringBuffer result = new StringBuffer();
		
		//foaf:knows
		result.append(createTripleSPOStatic(
						SIB.factgetUserURI(friendShip.getUserAcc01()),
						FOAF.factprefixed("knows"),
						SIB.factgetPersonURI((int)friendShip.getUserAcc02())));		
		
		//foaf:knows
		result.append(createTripleSPOStatic(
						SIB.factgetUserURI(friendShip.getUserAcc02()),
						FOAF.factprefixed("knows"),
						SIB.factgetPersonURI((int)friendShip.getUserAcc01())));		
		
		return result.toString();
	}
	
	
	public String convertPost(Post post){
		StringBuffer result = new StringBuffer();
		
		// Post URI
		//a
		result.append(createTripleSPOPostStream(
				SIB.factgetPostURI(post.getForumId(), post.getPostId()),
				"is_a", 
				SIB.factprefixed("Post")
				));		
		
		result.append(createTripleSPOPostStream(
				SIB.factgetPostURI(post.getForumId(), post.getPostId()),
				"is_a", 
				SIOC.factprefixed("Post")
				));				 
		
		//dcterms:title
		result.append(createTripleSPOPostStream(
				SIB.factgetPostURI(post.getForumId(), post.getPostId()),
				DCTERMS.factprefixed("title"), 
				createLiteral(post.getTitle())
				));				 
		
		//dc:created
		date.setTimeInMillis(post.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOPostStream(
				SIB.factgetPostURI(post.getForumId(), post.getPostId()),
				DC.factprefixed("created"), 
				createLiteral(dateString)
				));				 
		


		// Check for the avaiability of user agent
		if (!post.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					SIB.factprefixed("agent"), 
					createLiteral(post.getUserAgent())
					));				 
			
		}
		
		if (post.getBrowserIdx() != -1){
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					SIB.factprefixed("browser"),
					createLiteral(vBrowserNames.get(post.getBrowserIdx())) 
					));				 
		}
		
		//sioc:ip_address
		if (post.getIpAddress() != null)
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					SIOC.factprefixed("ip_address"),
					createLiteral(post.getIpAddress().toString()) 
					));				 
			

		//sioc:content
		result.append(createTripleSPOPostStream(
				SIB.factgetPostURI(post.getForumId(), post.getPostId()),
				SIOC.factprefixed("content"),
				createLiteral(post.getContent())
				));				 
		
		
		//forum sioc:container_of
		result.append(createTripleSPOPostStream(
						SIB.factgetForumURI(post.getForumId()),
						SIOC.factprefixed("container_of"),
						SIB.factgetPostURI(post.getForumId(), post.getPostId())
						));
		
		//user sioc:creator_of
		result.append(createTripleSPOPostStream(
				SIB.factgetUserURI(post.getAuthorId()),
				SIOC.factprefixed("creator_of"),
				SIB.factgetPostURI(post.getForumId(), post.getPostId())
				));
		
		//Tags
		/*
		ArrayList<String> tags = post.getTags();
		Iterator<String> tagIterator = tags.iterator();
		while (tagIterator.hasNext()){
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					createURIref(SIB.tag),
					tagIterator.next()
					));
		}
		
		
		//Likes
		ArrayList<Integer> userLikes = post.getInterestedUserAccs();
		Iterator<Integer> likesIterator = userLikes.iterator();
		while (likesIterator.hasNext()){
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					createURIref(SIB.like),
					SIB.factgetUserURI((int)likesIterator.next())
					));
		}
		*/
		String tags[] = post.getTags();
		for (int i = 0; i < tags.length; i ++){
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					SIB.factprefixed("hashtag"),
					createLiteral(tags[i])
					));
		}
		
		//Likes
		int userLikes[] = post.getInterestedUserAccs();
		for (int i = 0; i < userLikes.length; i ++){
			result.append(createTripleSPOPostStream(
					SIB.factgetUserURI(userLikes[i]),
					SIB.factprefixed("like"),
					SIB.factgetPostURI(post.getForumId(), post.getPostId())
					));
		}

		return result.toString();
	}

	public String convertPost(Post post, boolean isLikeStream){
		StringBuffer result = new StringBuffer();
		
		if (isLikeStream){
			//Likes
			int userLikes[] = post.getInterestedUserAccs();

			for (int j = 0; j < numPostLikeDuplication; j++){
				for (int i = 0; i < userLikes.length; i ++){
					result.append(createTripleSPOPostStream(
							SIB.factgetUserURI(userLikes[i]),
							SIB.factprefixed("like"),
							SIB.factgetPostURI(post.getForumId(), post.getPostId())
							));
				}
			}
		}
		else
		{
		// Post URI
		//a
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					"is_a", 
					SIB.factprefixed("Post")
					));		
			
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					"is_a", 
					SIOC.factprefixed("Post")
					));				 
			
			//dcterms:title
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					DCTERMS.factprefixed("title"), 
					createLiteral(post.getTitle())
					));				 
			
			//dc:created
			date.setTimeInMillis(post.getCreatedDate());
			String dateString = DateGenerator.formatDateDetail(date);
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					DC.factprefixed("created"), 
					createLiteral(dateString)
					));				 
			
	
	
			// Check for the avaiability of user agent
			if (!post.getUserAgent().equals("")){
				//sib:agent
				result.append(createTripleSPOPostStream(
						SIB.factgetPostURI(post.getForumId(), post.getPostId()),
						SIB.factprefixed("agent"), 
						createLiteral(post.getUserAgent())
						));				 
				
			}
			
			if (post.getBrowserIdx() != -1){
				result.append(createTripleSPOPostStream(
						SIB.factgetPostURI(post.getForumId(), post.getPostId()),
						SIB.factprefixed("browser"),
						createLiteral(vBrowserNames.get(post.getBrowserIdx())) 
						));				 
			}
			
			//sioc:ip_address
			if (post.getIpAddress() != null)
				result.append(createTripleSPOPostStream(
						SIB.factgetPostURI(post.getForumId(), post.getPostId()),
						SIOC.factprefixed("ip_address"),
						createLiteral(post.getIpAddress().toString()) 
						));				 
				
	
			//sioc:content
			result.append(createTripleSPOPostStream(
					SIB.factgetPostURI(post.getForumId(), post.getPostId()),
					SIOC.factprefixed("content"),
					createLiteral(post.getContent())
					));				 
			
			
			//forum sioc:container_of
			result.append(createTripleSPOPostStream(
							SIB.factgetForumURI(post.getForumId()),
							SIOC.factprefixed("container_of"),
							SIB.factgetPostURI(post.getForumId(), post.getPostId())
							));
			
			//user sioc:creator_of
			result.append(createTripleSPOPostStream(
					SIB.factgetUserURI(post.getAuthorId()),
					SIOC.factprefixed("creator_of"),
					SIB.factgetPostURI(post.getForumId(), post.getPostId())
					));
			
			String tags[] = post.getTags();
			for (int i = 0; i < tags.length; i ++){
				result.append(createTripleSPOPostStream(
						SIB.factgetPostURI(post.getForumId(), post.getPostId()),
						SIB.factprefixed("hashtag"),
						createLiteral(tags[i])
						));
			}
		
		}
		
		return result.toString();
	}

	public String convertComment(Comment comment){
		StringBuffer result = new StringBuffer();
		
		// Post URI
		//a
		result.append(createTripleSPOPostStream(
				SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
				"is_a",
				SIB.factprefixed("Comment")
				));
		

		result.append(createTripleSPOPostStream(
				SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
				"is_a",
				SIOC.factprefixed("Item")
				));
		
		//dc:created
		date.setTimeInMillis(comment.getCreateDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOPostStream(
				SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
				DC.factprefixed("created"), 
				createLiteral(dateString)
				));
		
		//sioc:reply_of
		if (comment.getReply_of() == -1){
			result.append(createTripleSPOPostStream(
					SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.factprefixed("reply_of"), 
					SIB.factgetPostURI(comment.getForumId(), comment.getPostId())
					));			
		}
		else
			result.append(createTripleSPOPostStream(
					SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.factprefixed("reply_of"), 
					SIB.factgetCommentURI(comment.getPostId(), comment.getReply_of())
					));			
		
		// Check for the avaiability of user agent
		if (!comment.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPOPostStream(
					SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
					SIB.factprefixed("agent"), 
					createLiteral(comment.getUserAgent())
					));			

		}
		
		if (comment.getBrowserIdx() != -1){
			result.append(createTripleSPOPostStream(
					SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
					SIB.factprefixed("browser"),
					createLiteral(vBrowserNames.get(comment.getBrowserIdx()))
					));			
			
		}
		
		//sioc:ip_address
		if (comment.getIpAddress() != null)
			result.append(createTripleSPOPostStream(
					SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.factprefixed("ip_address"),
					createLiteral(comment.getIpAddress().toString())
					));			

		//sioc:content
		result.append(createTripleSPOPostStream(
				SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId()),
				SIOC.factprefixed("content"), 
				createLiteral(comment.getContent())
				));			
		
		
		//forum sioc:container_of
		result.append(createTripleSPOPostStream(
						SIB.factgetPostURI(comment.getForumId(), comment.getPostId()),
						SIOC.factprefixed("container_of"),
						SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId())
						));
		
		//user sioc:creator_of
		result.append(createTripleSPOPostStream(
				SIB.factgetUserURI(comment.getAuthorId()),
				SIOC.factprefixed("creator_of"),
				SIB.factgetCommentURI(comment.getPostId(), comment.getCommentId())
				));
		
		return result.toString();
	}
	
	public String convertPhotoAlbum(PhotoAlbum album){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//rdf:type
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoAlbumURI(album.getAlbumId()),
				RDF.factprefixed("type"), 
				SIOCT.factprefixed("ImageGallery")
				));

		//dcterms:title
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoAlbumURI(album.getAlbumId()),
				DCTERMS.factprefixed("title"), 
				createLiteral(album.getTitle())
				));
		
		
		//dc:created
		date.setTimeInMillis(album.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoAlbumURI(album.getAlbumId()),
				DC.factprefixed("created"), 
				createLiteral(dateString)
				));
		
		
		//user sioc:creator_of
		result.append(createTripleSPOPhotoStream(
				SIB.factgetUserURI(album.getCreatorId()),
				SIOC.factprefixed("creator_of"),
				SIB.factgetPhotoAlbumURI(album.getAlbumId())
				));
		
		return result.toString(); 
	}
	
	public String convertPhoto(Photo photo){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//a
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				"is_a", 
				SIB.factprefixed("Photo")
				));
		
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				"is_a", 
				SIOC.factprefixed("Item")
				));
		
		/*
		//dbpo:location
		result.append(createTriplePO(
				DBPO.factprefixed("location"), 
				SIB.factgetLocationURI(photo.getLocationIdx()))
				);
		*/	
		//dbpo:location
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPO.factprefixed("location"), 
				createLiteral(photo.getLocationName())
				));		

		//dbpprop:latd
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPPROP.factprefixed("latd"), 
				Double.toString(photo.getLatt())
				));		


		//dbpprop:longd
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPPROP.factprefixed("longd"), 
				Double.toString(photo.getLongt())
				));		
		
		
		// Check for the avaiability of user agent
		if (!photo.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.factprefixed("agent"), 
					createLiteral(photo.getUserAgent())
					));		
		}
		

		if (photo.getBrowserIdx() != -1){
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.factprefixed("browser"),
					createLiteral(vBrowserNames.get(photo.getBrowserIdx())) 
					));		
			
		}
		
		//sioc:ip_address
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				SIOC.factprefixed("ip_address"),
				createLiteral(photo.getIpAddress().toString()) 
				));		
		
		
		//dc:created
		date.setTimeInMillis(photo.getTakenTime());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DC.factprefixed("created"), 
				createLiteral(dateString) 
				));		
		
		
		//user sioc:container_of
		result.append(createTripleSPOPhotoStream(
				SIB.factgetPhotoAlbumURI(photo.getAlbumId()),
				SIOC.factprefixed("container_of"),
				SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId())
				));
		
		
		// User tags
		int usertags[] = photo.getTags();
		for (int i = 0; i < usertags.length; i ++){
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.factprefixed("usertag"),
					SIB.factgetUserURI(usertags[i])
					));
		}
		
		//Likes
		int userLikes[] = photo.getInterestedUserAccs();
		for (int i = 0; i < userLikes.length; i ++){
			result.append(createTripleSPOPhotoStream(
					SIB.factgetUserURI(userLikes[i]),
					SIB.factprefixed("like"),
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId())
					));
		}
		
		return result.toString(); 
	}	

	public String convertPhoto(Photo photo, boolean isLikeStream){
		StringBuffer result = new StringBuffer();
		
		if (isLikeStream){
			//Likes
			int userLikes[] = photo.getInterestedUserAccs();
			
			for (int j = 0; j < numPhotoLikeDuplication; j++){
				for (int i = 0; i < userLikes.length; i ++){
					result.append(createTripleSPOPhotoStream(
							SIB.factgetUserURI(userLikes[i]),
							SIB.factprefixed("like"),
							SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId())
							));
				}
			}
		}
		else{
			// PhotoAlbum URI
			//a
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					"is_a", 
					SIB.factprefixed("Photo")
					));
			
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					"is_a", 
					SIOC.factprefixed("Item")
					));
			
			/*
			//dbpo:location
			result.append(createTriplePO(
					DBPO.factprefixed("location"), 
					SIB.factgetLocationURI(photo.getLocationIdx()))
					);
			*/	
			//dbpo:location
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPO.factprefixed("location"), 
					createLiteral(photo.getLocationName())
					));		
	
			//dbpprop:latd
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPPROP.factprefixed("latd"), 
					Double.toString(photo.getLatt())
					));		
	
	
			//dbpprop:longd
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPPROP.factprefixed("longd"), 
					Double.toString(photo.getLongt())
					));		
			
			
			// Check for the avaiability of user agent
			if (!photo.getUserAgent().equals("")){
				//sib:agent
				result.append(createTripleSPOPhotoStream(
						SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.factprefixed("agent"), 
						createLiteral(photo.getUserAgent())
						));		
			}
			
	
			if (photo.getBrowserIdx() != -1){
				result.append(createTripleSPOPhotoStream(
						SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.factprefixed("browser"),
						createLiteral(vBrowserNames.get(photo.getBrowserIdx())) 
						));		
				
			}
			
			//sioc:ip_address
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIOC.factprefixed("ip_address"),
					createLiteral(photo.getIpAddress().toString()) 
					));		
			
			
			//dc:created
			date.setTimeInMillis(photo.getTakenTime());
			String dateString = DateGenerator.formatDateDetail(date);
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DC.factprefixed("created"), 
					createLiteral(dateString) 
					));		
			
			
			//user sioc:container_of
			result.append(createTripleSPOPhotoStream(
					SIB.factgetPhotoAlbumURI(photo.getAlbumId()),
					SIOC.factprefixed("container_of"),
					SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId())
					));
			
			
			// User tags
			int usertags[] = photo.getTags();
			for (int i = 0; i < usertags.length; i ++){
				result.append(createTripleSPOPhotoStream(
						SIB.factgetPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.factprefixed("usertag"),
						SIB.factgetUserURI(usertags[i])
						));
			}
		

		}
		return result.toString(); 
	}	

	public String convertGPS(GPS gps){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//rdf:type
		result.append(createTripleSPOGPSStream(
				SIB.factgetUserURI(gps.getUserId()),
				SIB.factprefixed("trackedAt"), 
				SIB.factgetGPSURI(gpsId)
				));
		
		result.append(createTripleSPOGPSStream(
				SIB.factgetGPSURI(gpsId),
				DBPPROP.factprefixed("latd"),
				Double.toString(gps.getLatt())
				));
		
		result.append(createTripleSPOGPSStream(
				SIB.factgetGPSURI(gpsId),
				DBPPROP.factprefixed("longd"),
				Double.toString(gps.getLongt())
				));
		
		date.setTimeInMillis(gps.getTrackedTime());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOGPSStream(
				SIB.factgetGPSURI(gpsId),
				SIB.factprefixed("trackedTime"),
				createLiteral(dateString)
				));	
		
		result.append(createTripleSPOGPSStream(
				SIB.factgetGPSURI(gpsId),
				SIB.factprefixed("trackedLocation"),
				createLiteral(gps.getTrackedLocation())	
				));	

		gpsId++;
		
		return result.toString(); 
	}
	
	public String convertGroup(Group group){
		StringBuffer result = new StringBuffer(); 
		
		// Group URI
		//a
		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				"is_a",
				SIB.factprefixed("Group")
				));

		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				"is_a",
				SIOC.factprefixed("Usergroup")
				));
		
		 
		//sioc:name
		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				SIOC.factprefixed("name"), 
				createLiteral(group.getGroupName())
				));		
		
		//sioc:subscriber_of
		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				SIOC.factprefixed("subscriber_of"), 
				SIB.factgetForumURI(group.getForumWallId())
				));		

		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				SIOC.factprefixed("subscriber_of"), 
				SIB.factgetForumURI(group.getForumStatusId())
				));		

		
		//dc:created
		
		date.setTimeInMillis(group.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPOStatic(
				SIB.factgetGroupURI(group.getGroupId()),
				DC.factprefixed("created"), 
				createLiteral(dateString)
				));		
		
		
		//sioc:creator_of
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI(group.getModeratorId()),
				SIOC.factprefixed("creator_of"),
				SIB.factgetGroupURI(group.getGroupId())
				));		
		

		//sioc:moderator_of
		result.append(createTripleSPOStatic(
				SIB.factgetUserURI(group.getModeratorId()),
				SIOC.factprefixed("moderator_of"),
				SIB.factgetForumURI(group.getForumWallId())
				));		

		result.append(createTripleSPOStatic(
				SIB.factgetUserURI(group.getModeratorId()),
				SIOC.factprefixed("moderator_of"),
				SIB.factgetForumURI(group.getForumStatusId())
				));		
		
		
		// For two forums of the group
		//a sioc:Forum
		result.append(createTripleSPOStatic(
				SIB.factgetForumURI(group.getForumWallId()),
				"is_a",
				SIOC.factprefixed("Forum")
				));		
		

		result.append(createTripleSPOStatic(
				SIB.factgetForumURI(group.getForumWallId()),
				DC.factprefixed("created"),
				createLiteral(dateString)
				));		

		//a sioc:Forum
		result.append(createTripleSPOStatic(
				SIB.factgetForumURI(group.getForumStatusId()),
				"is_a",
				SIOC.factprefixed("Forum")
				));		


		result.append(createTripleSPOStatic(
				SIB.factgetForumURI(group.getForumStatusId()),
				DC.factprefixed("created"),
				createLiteral(dateString)
				));		

		// Tags of the group
		String groupTags[] = group.getTags();
		for (int i = 0; i < groupTags.length; i ++){
			result.append(createTripleSPOStatic(
					SIB.factgetGroupURI(group.getGroupId()),
					SIB.factprefixed("tag"),
					createLiteral(groupTags[i])
					));
		}
		
		
		// Member of the group
		GroupMemberShip memberShips[] = group.getMemberShips();
		int numMemberAdded = group.getNumMemberAdded();
		for (int i = 0; i < numMemberAdded; i ++){
			result.append(createTripleSPOStatic(
					SIB.factgetGroupURI(group.getGroupId()),
					SIOC.factprefixed("has_member"),
					SIB.factgetUserURI(memberShips[i].getUserId())
					));

			result.append(createTripleSPOStatic(
					SIB.factgetGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.factprefixed("member_of_membership"),
					SIB.factgetUserURI(memberShips[i].getUserId())
					));
			

			result.append(createTripleSPOStatic(
					SIB.factgetGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.factprefixed("group_of_membership"),
					SIB.factgetGroupURI(group.getGroupId())
					));

			//dc:created
			date.setTimeInMillis(memberShips[i].getJoinDate());
			dateString = DateGenerator.formatDateDetail(date);

			result.append(createTripleSPOStatic(
					SIB.factgetGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.factprefixed("added"),
					createLiteral(dateString)
					));

			membershipId++;
		}
		
		
		return result.toString();
	}
	
	//Create Literal
	private String createLiteral(String value)
	{
		StringBuffer result = new StringBuffer();
		result.append("\'");
		result.append(value);
		result.append("\'");
		return result.toString();
	}
	
	//Create typed literal
	private String createDataTypeLiteral(String value, String datatypeURI)
	{
		StringBuffer result = new StringBuffer();
		result.append("\'");
		result.append(value);
		result.append("\'^^");
		result.append(datatypeURI);
		return result.toString();
	}
	
	/*
	 * Create a triple consisting of subject predicate and object, end with "."
	 */
	private String createTripleSPOStream(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append("stream_");
		result.append(predicate);				
		result.append("(");
		result.append(subject);
		result.append(", ");
		result.append(object);
		result.append(").\n");
				
		nrTriples++;
		
		return result.toString();
		
	}
	private String createTripleSPOPostStream(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append("stream_post_");
		result.append(predicate);				
		result.append("(");
		result.append(subject);
		result.append(", ");
		result.append(object);
		result.append(").\n");
				
		nrTriples++;
		
		return result.toString();
		
	}
	private String createTripleSPOPhotoStream(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append("stream_photo_");
		result.append(predicate);				
		result.append("(");
		result.append(subject);
		result.append(", ");
		result.append(object);
		result.append(").\n");
				
		nrTriples++;
		
		return result.toString();
		
	}
	private String createTripleSPOGPSStream(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append("stream_gps_");
		result.append(predicate);				
		result.append("(");
		result.append(subject);
		result.append(", ");
		result.append(object);
		result.append(").\n");
				
		nrTriples++;
		
		return result.toString();
		
	}

	private String createTripleSPOStatic(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append("static_");
		result.append(predicate);				
		result.append("(");
		result.append(subject);
		result.append(", ");
		result.append(object);
		result.append(").\n");
				
		nrTriples++;
		
		return result.toString();
		
	}

	@Override
	public void serialize() {
		//Close files
		try {
			for(int i=0;i<dataFileWriter.length;i++) {
				dataFileWriter[i].flush();
				dataFileWriter[i].close();
			}
		} catch(IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	@Override
	public void setNumLikeDuplication(int numPostLikeDuplication, int numPhotoLikeDuplication) {
		this.numPostLikeDuplication = numPostLikeDuplication;
		this.numPhotoLikeDuplication = numPhotoLikeDuplication;
	}
}

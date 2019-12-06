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

public class NTriples implements Serializer {
	
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
	
	public NTriples(String file, boolean forwardChaining)
	{
		this(file, forwardChaining, 1);
	}
	
	public NTriples(String file, boolean forwardChaining, int nrOfOutputFiles)
	{
		date = new GregorianCalendar();
		int nrOfDigits = ((int)Math.log10(nrOfOutputFiles)) + 1;
		String formatString = "%0" + nrOfDigits + "d";
		try{
			dataFileWriter = new FileWriter[nrOfOutputFiles];
			if(nrOfOutputFiles==1)
				this.dataFileWriter[0] = new FileWriter(file + ".nt");
			else
				for(int i=1;i<=nrOfOutputFiles;i++)
					dataFileWriter[i-1] = new FileWriter(file + String.format(formatString, i) + ".nt");
				
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

        public NTriples(String file, boolean forwardChaining, int nrOfOutputFiles, 
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
                                this.dataFileWriter[0] = new FileWriter(file + ".nt");
                        else
                                for(int i=1;i<=nrOfOutputFiles;i++)
                                        dataFileWriter[i-1] = new FileWriter(file + String.format(formatString, i) + ".nt");

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
				if(socialObject instanceof UserProfile){
					dataFileWriter[currentWriter].append(convertUserProfile((UserProfile)socialObject));
				}
				else if(socialObject instanceof FriendShip){
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
	
	public String convertUserProfile(UserProfile profile){
		StringBuffer result = new StringBuffer();
		//First the uriref for the subject
		
		//a
		result.append(createTripleSPO(
									SIB.getPersonURI((int)profile.getAccountId()),
									"a",
									FOAF.prefixed("Person")
									)
					);
		
		
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				"a",
				SIB.prefixed("User")
				)
		);

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				"a",
				SIOC.prefixed("user")
				)
		);
		
		//sioc:account_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("account_of"),
				SIB.getPersonURI((int)profile.getAccountId())
				)
		);

		//sioc:moderator_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(profile.getForumWallId())
				)
		);

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(profile.getForumStatusId())
				)
		);		
		
		//sioc:subscriber_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("subscriber_of"),
				SIB.getForumURI(profile.getForumWallId())
				)
		);

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("subscriber_of"),
				SIB.getForumURI(profile.getForumStatusId())
				)
		);	
		


		//dc:created
		date.setTimeInMillis(profile.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				DC.prefixed("date"),
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				)
		);	
		
		
		// For the interests
		Iterator it = profile.getSetOfInterests().iterator();
		while (it.hasNext()){
			Integer interestIdx = (Integer)it.next();
			//String interest = "" + interestIdx;
			String interest = interestIdsNames.get(interestIdx);
			result.append(createTripleSPO(SIB.getUserURI(profile.getAccountId()),
							SIB.prefixed("interest"),
							createLiteral(interest)
							));      	
		}	

		//For the friendships
		Friend friends[] = profile.getFriendList();
		for (int i = 0; i < friends.length; i ++){
			if (friends[i] != null){
				//
				//foaf:knows
				result.append(createTripleSPO(
								SIB.getUserURI(profile.getAccountId()),
								FOAF.prefixed("knows"),
								SIB.getUserURI((int)friends[i].getFriendAcc())));		
				
				// Remove the following triple because it will be duplicated with 
				// the friendship info of the friend of this user
			}
			else
				break; 
		}
		
		
		return result.toString();	
	}
	
	public String convertUserProfile(ReducedUserProfile profile, UserExtraInfo extraInfo){
		StringBuffer result = new StringBuffer();
		//First the uriref for the subject
		
		//a
		result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					"a",
					FOAF.prefixed("Person")					
					));
		
		//foaf:firstName
		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				FOAF.prefixed("firstName"),
				createLiteral(extraInfo.getFirstName())					
				));		

		//foaf:lastName
		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				FOAF.prefixed("lastName"),
				createLiteral(extraInfo.getLastName())					
				));		

		//dc:location
		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				FOAF.prefixed("based_near"),
				DBP.prefixed(extraInfo.getLocation())					
				));		

		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				FOAF.prefixed("based_near"),
				createLiteral(extraInfo.getLocation())					
				));		
		
		
		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				DBPPROP.prefixed("latd"),
				createDataTypeLiteral(Double.toString(extraInfo.getLatt()), XSD.prefixed("double"))					
				));		
		

		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				DBPPROP.prefixed("longd"),
				createDataTypeLiteral(Double.toString(extraInfo.getLongt()), XSD.prefixed("double"))					
				));	
		

		//dc:organization	
		if (!extraInfo.getOrganization().equals("")){

			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					FOAF.prefixed("organization"),
					createLiteral(extraInfo.getOrganization())					
					));	
			
		}
		
		//sib:class_year
		if (extraInfo.getClassYear() != -1 ){
			date.setTimeInMillis(extraInfo.getClassYear());
			String dateString = DateGenerator.formatYear(date);
			
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					SIB.prefixed("class_year"),
					createDataTypeLiteral(dateString, XSD.prefixed("date"))
					));	

		}
		
		//sib:workAt
		if (!extraInfo.getCompany().equals("")){
			
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					SIB.prefixed("workAt"),
					createLiteral(extraInfo.getCompany()) 
					));	
			
			
			//sib:workFrom
			date.setTimeInMillis(extraInfo.getWorkFrom());
			String dateString = DateGenerator.formatYear(date);
			
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					SIB.prefixed("workFrom"),
					createDataTypeLiteral(dateString, XSD.prefixed("date"))
					));	
		}
		// For user's extra info
		if (!extraInfo.getGender().equals(""))
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					FOAF.prefixed("gender"),
					createLiteral(extraInfo.getGender()) 
					));	
			
		
		
		if (extraInfo.getDateOfBirth() != -1 ){
			date.setTimeInMillis(extraInfo.getDateOfBirth());
			String dateString = DateGenerator.formatDate(date);
			
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					FOAF.prefixed("birthday"),
					createDataTypeLiteral(dateString, XSD.prefixed("date"))					
					));	
			
		}
		
		if (!extraInfo.getEmail().equals("")){
			result.append(createTripleSPO(
					SIB.getPersonURI((int)profile.getAccountId()),
					SIOC.prefixed("email"),
					createLiteral(extraInfo.getEmail()) 
					));	
		}

		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				SIB.prefixed("browser"),
				createLiteral(vBrowserNames.get(profile.getBrowserIdx())) 
				));	

		result.append(createTripleSPO(
				SIB.getPersonURI((int)profile.getAccountId()),
				SIOC.prefixed("ip_address"),
				createLiteral(profile.getIpAddress().toString()) 
				));	
		

		//a for user
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				"a",
				SIB.prefixed("User") 
				));	

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				"a",
				 SIOC.prefixed("user") 
				));	
		
		
		//sioc:account_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("account_of"),
				SIB.getPersonURI((int)profile.getAccountId())
				));			
		
		//sioc:moderator_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(profile.getForumWallId())
				));			

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(profile.getForumStatusId())
				));		

		//sioc:subscriber_of
		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("subscriber_of"),
				SIB.getForumURI(profile.getForumWallId())
				));			

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				SIOC.prefixed("subscriber_of"),
				SIB.getForumURI(profile.getForumStatusId())
				));		
		
		
		//sib:status
		String status = ""; 
		if (extraInfo.getStatus() != RelationshipStatus.NOSTATUS){
			
			switch (extraInfo.getStatus()) {
				case SINGLE: 
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("status"),
							createLiteral("Single")
							));							

					break;   
				case IN_A_RELATIONSHIP:
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("status"),
							createLiteral("In a relationship")
							));							
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("In_relationship_with"),
							SIB.getUserURI(extraInfo.getSpecialFriendIdx())
							));	
					break;
				case ENGAGED:
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("status"),
							createLiteral("Engaged")
							));
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("Engaged_with"),
							SIB.getUserURI(extraInfo.getSpecialFriendIdx())
							));						
					
					break;
				case MARRIED:
					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("status"),
							createLiteral("Married")
							));

					result.append(createTripleSPO(
							SIB.getUserURI((int)profile.getAccountId()),
							SIB.prefixed("Married_with"),
							SIB.getUserURI(extraInfo.getSpecialFriendIdx())
							));
					
					break;
				default:
					break;
			}
			
		
		}
		
		
		//dc:created
		date.setTimeInMillis(profile.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);

		result.append(createTripleSPO(
				SIB.getUserURI((int)profile.getAccountId()),
				DC.prefixed("date"),
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));
		
		
		
		// For the interests
		Iterator it = profile.getSetOfInterests().iterator();
		while (it.hasNext()){
			Integer interestIdx = (Integer)it.next();
			//String interest = "" + interestIdx;
			String interest = interestIdsNames.get(interestIdx);
			result.append(createTripleSPO(SIB.getUserURI(profile.getAccountId()),
							SIB.prefixed("interest"),
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
					result.append(createTripleSPO(
							SIB.getFriendshipURI(friendshipId),
							SIB.prefixed("memb"),
							SIB.getUserURI(profile.getAccountId())
							));      	

					result.append(createTripleSPO(
							SIB.getFriendshipURI(friendshipId),
							SIB.prefixed("memb"),
							SIB.getUserURI(friends[i].getFriendAcc())
							));      	
					
					
					//sib:initiator
					if (friends[i].getInitiator() == 0){
						result.append(createTripleSPO(
								SIB.getFriendshipURI(friendshipId),
								SIB.prefixed("initiator"),
								SIB.getUserURI(friends[i].getFriendAcc())
								));
						
					}
					else
						result.append(createTripleSPO(
								SIB.getFriendshipURI(friendshipId),
								SIB.prefixed("initiator"),
								SIB.getUserURI(profile.getAccountId())
								));

					
					//sib:requested
					date.setTimeInMillis(friends[i].getRequestTime());
					dateString = DateGenerator.formatDateDetail(date);

					result.append(createTripleSPO(
							SIB.getFriendshipURI(friendshipId),
							SIB.prefixed("requested"),
							createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
							));
					
					if (friends[i].getDeclinedTime() == -1){
						//sib:approved
						date.setTimeInMillis(friends[i].getCreatedTime());
						dateString = DateGenerator.formatDateDetail(date);
						result.append(createTripleSPO(
								SIB.getFriendshipURI(friendshipId),
								SIB.prefixed("approved"),
								createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
								));
						
					}
					else{
						if (friends[i].getCreatedTime() == -1){		// No re-approved
							//sib:declined
							date.setTimeInMillis(friends[i].getDeclinedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPO(
									SIB.getFriendshipURI(friendshipId),
									SIB.prefixed("declined"),
									createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
									));							
						}
						else{			// has been re-approved
							//sib:declined
							date.setTimeInMillis(friends[i].getDeclinedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPO(
									SIB.getFriendshipURI(friendshipId),
									SIB.prefixed("declined"),
									createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
									));							

							//sib:reapproved
							date.setTimeInMillis(friends[i].getCreatedTime());
							dateString = DateGenerator.formatDateDetail(date);
							result.append(createTripleSPO(
									SIB.getFriendshipURI(friendshipId),
									SIB.prefixed("reapproved"),
									createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
									));							
						}
						
					}
					
					
					friendshipId++;
				}
				
				
				//foaf:knows
				if (friends[i].getCreatedTime() != -1){
					result.append(createTripleSPO(
							SIB.getUserURI(profile.getAccountId()),
							FOAF.prefixed("knows"),
							SIB.getUserURI(friends[i].getFriendAcc())));
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
		result.append(createTripleSPO(
						SIB.getUserURI(friendShip.getUserAcc01()),
						FOAF.prefixed("knows"),
						SIB.getPersonURI((int)friendShip.getUserAcc02())));		
		
		//foaf:knows
		result.append(createTripleSPO(
						SIB.getUserURI(friendShip.getUserAcc02()),
						FOAF.prefixed("knows"),
						SIB.getPersonURI((int)friendShip.getUserAcc01())));		
		
		return result.toString();
	}
	
	
	public String convertPost(Post post){
		StringBuffer result = new StringBuffer();
		
		// Post URI
		//a
		result.append(createTripleSPO(
				SIB.getPostURI(post.getForumId(), post.getPostId()),
				"a", 
				SIB.prefixed("Post")
				));		
		
		result.append(createTripleSPO(
				SIB.getPostURI(post.getForumId(), post.getPostId()),
				"a", 
				SIOC.prefixed("Post")
				));				 
		
		//dcterms:title
		result.append(createTripleSPO(
				SIB.getPostURI(post.getForumId(), post.getPostId()),
				DCTERMS.prefixed("title"), 
				createLiteral(post.getTitle())
				));				 
		
		//dc:created
		date.setTimeInMillis(post.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getPostURI(post.getForumId(), post.getPostId()),
				DC.prefixed("created"), 
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));				 
		


		// Check for the avaiability of user agent
		if (!post.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					SIB.prefixed("agent"), 
					createLiteral(post.getUserAgent())
					));				 
			
		}
		
		if (post.getBrowserIdx() != -1){
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					SIB.prefixed("browser"),
					createLiteral(vBrowserNames.get(post.getBrowserIdx())) 
					));				 
		}
		
		//sioc:ip_address
		if (post.getIpAddress() != null)
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					SIOC.prefixed("ip_address"),
					createLiteral(post.getIpAddress().toString()) 
					));				 
			

		//sioc:content
		result.append(createTripleSPO(
				SIB.getPostURI(post.getForumId(), post.getPostId()),
				SIOC.prefixed("content"),
				createLiteral(post.getContent())
				));				 
		
		
		//forum sioc:container_of
		result.append(createTripleSPO(
						SIB.getForumURI(post.getForumId()),
						SIOC.prefixed("container_of"),
						SIB.getPostURI(post.getForumId(), post.getPostId())
						));
		
		//user sioc:creator_of
		result.append(createTripleSPO(
				SIB.getUserURI(post.getAuthorId()),
				SIOC.prefixed("creator_of"),
				SIB.getPostURI(post.getForumId(), post.getPostId())
				));
		
		//Tags
		/*
		ArrayList<String> tags = post.getTags();
		Iterator<String> tagIterator = tags.iterator();
		while (tagIterator.hasNext()){
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					createURIref(SIB.tag),
					tagIterator.next()
					));
		}
		
		
		//Likes
		ArrayList<Integer> userLikes = post.getInterestedUserAccs();
		Iterator<Integer> likesIterator = userLikes.iterator();
		while (likesIterator.hasNext()){
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					createURIref(SIB.like),
					SIB.getUserURI((int)likesIterator.next())
					));
		}
		*/
		String tags[] = post.getTags();
		for (int i = 0; i < tags.length; i ++){
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					SIB.prefixed("hashtag"),
					createLiteral(tags[i])
					));
		}
		
		//Likes
		int userLikes[] = post.getInterestedUserAccs();
		for (int i = 0; i < userLikes.length; i ++){
			result.append(createTripleSPO(
					SIB.getUserURI(userLikes[i]),
					SIB.prefixed("like"),
					SIB.getPostURI(post.getForumId(), post.getPostId())
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
					result.append(createTripleSPO(
							SIB.getUserURI(userLikes[i]),
							SIB.prefixed("like"),
							SIB.getPostURI(post.getForumId(), post.getPostId())
							));
				}
			}

		}
		else
		{
		// Post URI
		//a
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					"a", 
					SIB.prefixed("Post")
					));		
			
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					"a", 
					SIOC.prefixed("Post")
					));				 
			
			//dcterms:title
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					DCTERMS.prefixed("title"), 
					createLiteral(post.getTitle())
					));				 
			
			//dc:created
			date.setTimeInMillis(post.getCreatedDate());
			String dateString = DateGenerator.formatDateDetail(date);
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					DC.prefixed("created"), 
					createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
					));				 
			
	
	
			// Check for the avaiability of user agent
			if (!post.getUserAgent().equals("")){
				//sib:agent
				result.append(createTripleSPO(
						SIB.getPostURI(post.getForumId(), post.getPostId()),
						SIB.prefixed("agent"), 
						createLiteral(post.getUserAgent())
						));				 
				
			}
			
			if (post.getBrowserIdx() != -1){
				result.append(createTripleSPO(
						SIB.getPostURI(post.getForumId(), post.getPostId()),
						SIB.prefixed("browser"),
						createLiteral(vBrowserNames.get(post.getBrowserIdx())) 
						));				 
			}
			
			//sioc:ip_address
			if (post.getIpAddress() != null)
				result.append(createTripleSPO(
						SIB.getPostURI(post.getForumId(), post.getPostId()),
						SIOC.prefixed("ip_address"),
						createLiteral(post.getIpAddress().toString()) 
						));				 
				
	
			//sioc:content
			result.append(createTripleSPO(
					SIB.getPostURI(post.getForumId(), post.getPostId()),
					SIOC.prefixed("content"),
					createLiteral(post.getContent())
					));				 
			
			
			//forum sioc:container_of
			result.append(createTripleSPO(
							SIB.getForumURI(post.getForumId()),
							SIOC.prefixed("container_of"),
							SIB.getPostURI(post.getForumId(), post.getPostId())
							));
			
			//user sioc:creator_of
			result.append(createTripleSPO(
					SIB.getUserURI(post.getAuthorId()),
					SIOC.prefixed("creator_of"),
					SIB.getPostURI(post.getForumId(), post.getPostId())
					));
			
			String tags[] = post.getTags();
			for (int i = 0; i < tags.length; i ++){
				result.append(createTripleSPO(
						SIB.getPostURI(post.getForumId(), post.getPostId()),
						SIB.prefixed("hashtag"),
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
		result.append(createTripleSPO(
				SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
				"a",
				SIB.prefixed("Comment")
				));
		

		result.append(createTripleSPO(
				SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
				"a",
				SIOC.prefixed("Item")
				));
		
		//dc:created
		date.setTimeInMillis(comment.getCreateDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
				DC.prefixed("created"), 
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));
		
		//sioc:reply_of
		if (comment.getReply_of() == -1){
			result.append(createTripleSPO(
					SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.prefixed("reply_of"), 
					SIB.getPostURI(comment.getForumId(), comment.getPostId())
					));			
		}
		else
			result.append(createTripleSPO(
					SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.prefixed("reply_of"), 
					SIB.getCommentURI(comment.getPostId(), comment.getReply_of())
					));			
		
		// Check for the avaiability of user agent
		if (!comment.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPO(
					SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
					SIB.prefixed("agent"), 
					createLiteral(comment.getUserAgent())
					));			

		}
		
		if (comment.getBrowserIdx() != -1){
			result.append(createTripleSPO(
					SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
					SIB.prefixed("browser"),
					createLiteral(vBrowserNames.get(comment.getBrowserIdx()))
					));			
			
		}
		
		//sioc:ip_address
		if (comment.getIpAddress() != null)
			result.append(createTripleSPO(
					SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
					SIOC.prefixed("ip_address"),
					createLiteral(comment.getIpAddress().toString())
					));			

		//sioc:content
		result.append(createTripleSPO(
				SIB.getCommentURI(comment.getPostId(), comment.getCommentId()),
				SIOC.prefixed("content"), 
				createLiteral(comment.getContent())
				));			
		
		
		//forum sioc:container_of
		result.append(createTripleSPO(
						SIB.getPostURI(comment.getForumId(), comment.getPostId()),
						SIOC.prefixed("container_of"),
						SIB.getCommentURI(comment.getPostId(), comment.getCommentId())
						));
		
		//user sioc:creator_of
		result.append(createTripleSPO(
				SIB.getUserURI(comment.getAuthorId()),
				SIOC.prefixed("creator_of"),
				SIB.getCommentURI(comment.getPostId(), comment.getCommentId())
				));
		
		return result.toString();
	}
	
	public String convertPhotoAlbum(PhotoAlbum album){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//rdf:type
		result.append(createTripleSPO(
				SIB.getPhotoAlbumURI(album.getAlbumId()),
				RDF.prefixed("type"), 
				SIOCT.prefixed("ImageGallery")
				));

		//dcterms:title
		result.append(createTripleSPO(
				SIB.getPhotoAlbumURI(album.getAlbumId()),
				DCTERMS.prefixed("title"), 
				createLiteral(album.getTitle())
				));
		
		
		//dc:created
		date.setTimeInMillis(album.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getPhotoAlbumURI(album.getAlbumId()),
				DC.prefixed("created"), 
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));
		
		
		//user sioc:creator_of
		result.append(createTripleSPO(
				SIB.getUserURI(album.getCreatorId()),
				SIOC.prefixed("creator_of"),
				SIB.getPhotoAlbumURI(album.getAlbumId())
				));
		
		return result.toString(); 
	}
	
	public String convertPhoto(Photo photo){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//a
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				"a", 
				SIB.prefixed("Photo")
				));
		
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				"a", 
				SIOC.prefixed("Item")
				));
		
		/*
		//dbpo:location
		result.append(createTriplePO(
				DBPO.prefixed("location"), 
				SIB.getLocationURI(photo.getLocationIdx()))
				);
		*/	
		//dbpo:location
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPO.prefixed("location"), 
				createLiteral(photo.getLocationName())
				));		

		//dbpprop:latd
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPPROP.prefixed("latd"), 
				createDataTypeLiteral(Double.toString(photo.getLatt()), XSD.prefixed("double"))
				));		


		//dbpprop:longd
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DBPPROP.prefixed("longd"), 
				createDataTypeLiteral(Double.toString(photo.getLongt()), XSD.prefixed("double"))
				));		
		
		
		// Check for the avaiability of user agent
		if (!photo.getUserAgent().equals("")){
			//sib:agent
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.prefixed("agent"), 
					createLiteral(photo.getUserAgent())
					));		
		}
		

		if (photo.getBrowserIdx() != -1){
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.prefixed("browser"),
					createLiteral(vBrowserNames.get(photo.getBrowserIdx())) 
					));		
			
		}
		
		//sioc:ip_address
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				SIOC.prefixed("ip_address"),
				createLiteral(photo.getIpAddress().toString()) 
				));		
		
		
		//dc:created
		date.setTimeInMillis(photo.getTakenTime());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
				DC.prefixed("created"), 
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime")) 
				));		
		
		
		//user sioc:container_of
		result.append(createTripleSPO(
				SIB.getPhotoAlbumURI(photo.getAlbumId()),
				SIOC.prefixed("container_of"),
				SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId())
				));
		
		
		// User tags
		int usertags[] = photo.getTags();
		for (int i = 0; i < usertags.length; i ++){
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIB.prefixed("usertag"),
					SIB.getUserURI(usertags[i])
					));
		}
		
		//Likes
		int userLikes[] = photo.getInterestedUserAccs();
		for (int i = 0; i < userLikes.length; i ++){
			result.append(createTripleSPO(
					SIB.getUserURI(userLikes[i]),
					SIB.prefixed("like"),
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId())
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
					result.append(createTripleSPO(
							SIB.getUserURI(userLikes[i]),
							SIB.prefixed("like"),
							SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId())
							));
				}
			}
		}
		else{
			// PhotoAlbum URI
			//a
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					"a", 
					SIB.prefixed("Photo")
					));
			
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					"a", 
					SIOC.prefixed("Item")
					));
			
			/*
			//dbpo:location
			result.append(createTriplePO(
					DBPO.prefixed("location"), 
					SIB.getLocationURI(photo.getLocationIdx()))
					);
			*/	
			//dbpo:location
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPO.prefixed("location"), 
					createLiteral(photo.getLocationName())
					));		
	
			//dbpprop:latd
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPPROP.prefixed("latd"), 
					createDataTypeLiteral(Double.toString(photo.getLatt()), XSD.prefixed("double"))
					));		
	
	
			//dbpprop:longd
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DBPPROP.prefixed("longd"), 
					createDataTypeLiteral(Double.toString(photo.getLongt()), XSD.prefixed("double"))
					));		
			
			
			// Check for the avaiability of user agent
			if (!photo.getUserAgent().equals("")){
				//sib:agent
				result.append(createTripleSPO(
						SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.prefixed("agent"), 
						createLiteral(photo.getUserAgent())
						));		
			}
			
	
			if (photo.getBrowserIdx() != -1){
				result.append(createTripleSPO(
						SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.prefixed("browser"),
						createLiteral(vBrowserNames.get(photo.getBrowserIdx())) 
						));		
				
			}
			
			//sioc:ip_address
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					SIOC.prefixed("ip_address"),
					createLiteral(photo.getIpAddress().toString()) 
					));		
			
			
			//dc:created
			date.setTimeInMillis(photo.getTakenTime());
			String dateString = DateGenerator.formatDateDetail(date);
			result.append(createTripleSPO(
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
					DC.prefixed("created"), 
					createDataTypeLiteral(dateString, XSD.prefixed("dateTime")) 
					));		
			
			
			//user sioc:container_of
			result.append(createTripleSPO(
					SIB.getPhotoAlbumURI(photo.getAlbumId()),
					SIOC.prefixed("container_of"),
					SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId())
					));
			
			
			// User tags
			int usertags[] = photo.getTags();
			for (int i = 0; i < usertags.length; i ++){
				result.append(createTripleSPO(
						SIB.getPhotoURI(photo.getAlbumId(),photo.getPhotoId()),
						SIB.prefixed("usertag"),
						SIB.getUserURI(usertags[i])
						));
			}
		

		}
		return result.toString(); 
	}	

	public String convertGPS(GPS gps){
		StringBuffer result = new StringBuffer();
		
		// PhotoAlbum URI
		//rdf:type
		result.append(createTripleSPO(
				SIB.getUserURI(gps.getUserId()),
				SIB.prefixed("trackedAt"), 
				SIB.getGPSURI(gpsId)
				));
		
		result.append(createTripleSPO(
				SIB.getGPSURI(gpsId),
				DBPPROP.prefixed("latd"),
				createDataTypeLiteral(Double.toString(gps.getLatt()), XSD.prefixed("double"))
				));
		
		result.append(createTripleSPO(
				SIB.getGPSURI(gpsId),
				DBPPROP.prefixed("longd"),
				createDataTypeLiteral(Double.toString(gps.getLongt()), XSD.prefixed("double"))
				));
		
		date.setTimeInMillis(gps.getTrackedTime());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getGPSURI(gpsId),
				SIB.prefixed("trackedTime"),
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));	
		
		result.append(createTripleSPO(
				SIB.getGPSURI(gpsId),
				SIB.prefixed("trackedLocation"),
				createLiteral(gps.getTrackedLocation())	
				));	

		gpsId++;
		
		return result.toString(); 
	}
	
	public String convertGroup(Group group){
		StringBuffer result = new StringBuffer(); 
		
		// Group URI
		//a
		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				"a",
				SIB.prefixed("Group")
				));

		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				"a",
				SIOC.prefixed("Usergroup")
				));
		
		 
		//sioc:name
		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				SIOC.prefixed("name"), 
				createLiteral(group.getGroupName())
				));		
		
		//sioc:subscriber_of
		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				SIOC.prefixed("subscriber_of"), 
				SIB.getForumURI(group.getForumWallId())
				));		

		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				SIOC.prefixed("subscriber_of"), 
				SIB.getForumURI(group.getForumStatusId())
				));		

		
		//dc:created
		
		date.setTimeInMillis(group.getCreatedDate());
		String dateString = DateGenerator.formatDateDetail(date);
		result.append(createTripleSPO(
				SIB.getGroupURI(group.getGroupId()),
				DC.prefixed("created"), 
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));		
		
		
		//sioc:creator_of
		result.append(createTripleSPO(
				SIB.getUserURI(group.getModeratorId()),
				SIOC.prefixed("creator_of"),
				SIB.getGroupURI(group.getGroupId())
				));		
		

		//sioc:moderator_of
		result.append(createTripleSPO(
				SIB.getUserURI(group.getModeratorId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(group.getForumWallId())
				));		

		result.append(createTripleSPO(
				SIB.getUserURI(group.getModeratorId()),
				SIOC.prefixed("moderator_of"),
				SIB.getForumURI(group.getForumStatusId())
				));		
		
		
		// For two forums of the group
		//a sioc:Forum
		result.append(createTripleSPO(
				SIB.getForumURI(group.getForumWallId()),
				"a",
				SIOC.prefixed("Forum")
				));		
		

		result.append(createTripleSPO(
				SIB.getForumURI(group.getForumWallId()),
				DC.prefixed("created"),
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));		

		//a sioc:Forum
		result.append(createTripleSPO(
				SIB.getForumURI(group.getForumStatusId()),
				"a",
				SIOC.prefixed("Forum")
				));		


		result.append(createTripleSPO(
				SIB.getForumURI(group.getForumStatusId()),
				DC.prefixed("created"),
				createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
				));		

		// Tags of the group
		String groupTags[] = group.getTags();
		for (int i = 0; i < groupTags.length; i ++){
			result.append(createTripleSPO(
					SIB.getGroupURI(group.getGroupId()),
					SIB.prefixed("tag"),
					createLiteral(groupTags[i])
					));
		}
		
		
		// Member of the group
		GroupMemberShip memberShips[] = group.getMemberShips();
		int numMemberAdded = group.getNumMemberAdded();
		for (int i = 0; i < numMemberAdded; i ++){
			result.append(createTripleSPO(
					SIB.getGroupURI(group.getGroupId()),
					SIOC.prefixed("has_member"),
					SIB.getUserURI(memberShips[i].getUserId())
					));

			result.append(createTripleSPO(
					SIB.getGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.prefixed("member_of_membership"),
					SIB.getUserURI(memberShips[i].getUserId())
					));
			

			result.append(createTripleSPO(
					SIB.getGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.prefixed("group_of_membership"),
					SIB.getGroupURI(group.getGroupId())
					));

			//dc:created
			date.setTimeInMillis(memberShips[i].getJoinDate());
			dateString = DateGenerator.formatDateDetail(date);

			result.append(createTripleSPO(
					SIB.getGroupMemberShipURI(group.getGroupId(), membershipId),
					SIB.prefixed("added"),
					createDataTypeLiteral(dateString, XSD.prefixed("dateTime"))
					));

			membershipId++;
		}
		
		
		return result.toString();
	}
	
	//Create Literal
	private String createLiteral(String value)
	{
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"");
		return result.toString();
	}
	
	//Create typed literal
	private String createDataTypeLiteral(String value, String datatypeURI)
	{
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"^^");
		result.append(datatypeURI);
		return result.toString();
	}
	
	/*
	 * Create a triple consisting of subject predicate and object, end with "."
	 */
	private String createTripleSPO(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append(subject);		
		result.append(" ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" .\n");
		
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

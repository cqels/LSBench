package sib.objects;

public class UserExtraInfo extends SocialObject {
	String 				gender;
	long 				dateOfBirth;
	String 				email; 
	//String 				sourceIp; 			// Source IP address
	String 				firstName; 
	String 				lastName; 
	String 				location;
	double				latt; 
	double				longt; 
	String 				organization; 
	String 				institution;
	String 				company;

	long 				classYear; 				// When graduate from the institute
	long 				workFrom;				
	
	RelationshipStatus 	status;
	int					specialFriendIdx; 	
	
	
	public long getClassYear() {
		return classYear;
	}
	public void setClassYear(long classYear) {
		this.classYear = classYear;
	}
	RelationshipStatus 		relationshipStatus;		
	
	
	public RelationshipStatus getRelationshipStatus() {
		return relationshipStatus;
	}
	public void setRelationshipStatus(RelationshipStatus relationshipStatus) {
		this.relationshipStatus = relationshipStatus;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public long getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(long dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public long getWorkFrom() {
		return workFrom;
	}
	public void setWorkFrom(long workFrom) {
		this.workFrom = workFrom;
	}
	public RelationshipStatus getStatus() {
		return status;
	}
	public void setStatus(RelationshipStatus status) {
		this.status = status;
	}
	public int getSpecialFriendIdx() {
		return specialFriendIdx;
	}
	public void setSpecialFriendIdx(int specialFriendIdx) {
		this.specialFriendIdx = specialFriendIdx;
	}
	public double getLatt() {
		return latt;
	}
	public void setLatt(double latt) {
		this.latt = latt;
	}
	public double getLongt() {
		return longt;
	}
	public void setLongt(double longt) {
		this.longt = longt;
	}

}


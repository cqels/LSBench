package sib.generator;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import sib.objects.ReducedUserProfile;
import sib.objects.UserProfile;

public class DateGenerator {
	public static long oneDayInMillis = 24*60*60*1000;
	public static long thirtyDayInMillis = (long)24*60*60*1000*30;
	public static long weekDayInMillis = (long)24*60*60*1000*7;
	public static long sevenDayInMillis = (long)24*60*60*1000*7;	
	public static long sixtyYear = (long)24*60*60*1000*365*60;
	public static long tenYear = (long)24*60*60*1000*365*10;
	public static long oneYear = (long)24*60*60*1000*365;
	public static long twoYear = (long)24*60*60*1000*365*2;		
	
	
	private long from, to;
	private Random ranGen;
	private Random ranClassYear;
	private Random ranWorkingYear;
	
	private Random thirtyDayRanGen;
	private Random sevenDayRanGen;
	private PowerDistGenerator disGen;
	
	public DateGenerator(GregorianCalendar from, GregorianCalendar to, Long seed)
	{
		this.from = from.getTimeInMillis();
		this.to = to.getTimeInMillis();
		ranGen = new Random(seed);
		ranClassYear = new Random(seed);
		ranWorkingYear = new Random(seed);
		
	}

	// This constructor is for the case of friendship's created date generator
	public DateGenerator(GregorianCalendar from, GregorianCalendar to, Long seed, Long seedForThirtyday, double alphaForPowerlaw)
	{
		this.from = from.getTimeInMillis();
		this.to = to.getTimeInMillis();
		ranGen = new Random(seed);
		thirtyDayRanGen = new Random(seedForThirtyday);
		sevenDayRanGen = new Random(seedForThirtyday);
		disGen = new PowerDistGenerator(0.0, 1.0, alphaForPowerlaw, seed);
		ranClassYear = new Random(seed);
		ranWorkingYear = new Random(seed);
	}
	
	/*
	 * Date generator with range from - (from+toSpanInDays) 
	 */
	public DateGenerator(GregorianCalendar from, Integer toSpanInDays, Long seed)
	{
		this.from = from.getTimeInMillis();
		this.to = this.from + oneDayInMillis*toSpanInDays;

		ranGen = new Random(seed);
		ranClassYear = new Random(seed);
		ranWorkingYear = new Random(seed);
	}
	
	/*
	 * Date generator with range (to-fromSpanInDays) - to 
	 */
	public DateGenerator(Integer fromSpanInDays, GregorianCalendar to, Long seed)
	{
		this.to = to.getTimeInMillis();
		this.from = this.to - oneDayInMillis*fromSpanInDays;
		ranGen = new Random(seed);
		ranClassYear = new Random(seed);
		ranWorkingYear = new Random(seed);
	}
	
	public DateGenerator(Long seed)
	{
		this.from = 0l;
		this.to = 0l;
		ranGen = new Random(seed);
		ranClassYear = new Random(seed);
		ranWorkingYear = new Random(seed);
	}
	
	/*
	 * Date between from and to
	 */
	public GregorianCalendar randomDate()
	{
		long date = (long)(ranGen.nextDouble()*(to-from)+from);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(date));
		
		return gc;
	}
	
	/*
	 * Date between from and to
	 */
	public Long randomDateInMillis()
	{
		long date = (long)(ranGen.nextDouble()*(to-from)+from);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(date));
		
		return gc.getTimeInMillis();
	}
	
	/*
	 * format the date
	 */
	public static String formatDate(GregorianCalendar c)
	{
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH)+1;		// +1 because month can be 0
		int year = c.get(Calendar.YEAR);
		
		String prefixDay = "";
		String prefixMonth = "";		
		
		if(day<10)
			prefixDay = "0";
		
		if(month<10)
			prefixMonth = "0";
		
		return year+"-"+prefixMonth+month+"-"+prefixDay+day;
	}
	
	public static String formatYear(GregorianCalendar c)
	{
		int year = c.get(Calendar.YEAR);
		
		return year + "";
	}
	/*
	 * format the date with hours and minutes
	 */
	public static String formatDateDetail(GregorianCalendar c)
	{
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH)+1;
		int year = c.get(Calendar.YEAR);
		
		int hour = c.get(Calendar.HOUR);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		String prefixDay = "";
		String prefixMonth = "";
		String prefixHour = "";
		String prefixMinute = "";
		String prefixSecond = "";
		
		if(day<10)
			prefixDay = "0";
		
		if(month<10)
			prefixMonth = "0";
		
		if (hour < 10)
			prefixHour = "0";
		
		if (minute < 10)
			prefixMinute = "0";

		if (second < 10)
			prefixSecond = "0";
		
		return year+"-"+prefixMonth+month+"-"+prefixDay+day +"T"
			   +prefixHour+hour+":"+prefixMinute+minute+":"+prefixSecond+second+"Z";
	}

	/*
	 * format the date
	 */
	public static String formatDate(Long date)
	{
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(date);
		
		return formatDate(c);
	}
	
	public static boolean isTravelSeason(long date){
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(date);
		
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH)+1;
		
		if ((month > 5) && (month < 8)){
			return true; 
		} 
		else if ((month==12) &&  (day > 23)){
			return true; 
		}
		else 
			return false; 
	}
	
	/*
	 * Format date in xsd:dateTime format
	 */
	public static String formatDateTime(Long date) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(date);
		
		String dateString = formatDate(c);
		return dateString + "T00:00:00";
	}
	
	public static String formatDateTime(GregorianCalendar date) {
		String dateString = formatDate(date);
		return dateString + "T00:00:00";
	}
	
	public Long randomDateInMillis(Long from, Long to)
	{
		long date = (long)(ranGen.nextDouble()*(to-from)+from);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(date));
		
		return gc.getTimeInMillis();
	}
	
	public Long randomThirtyDaysSpan(Long from){
		long randomSpanMilis =  (long) (thirtyDayRanGen.nextDouble()* (thirtyDayInMillis));
		return (from + randomSpanMilis);
	}
	public long randomFriendRequestedDate(UserProfile user1, UserProfile user2){
		long fromDate = Math.max(user1.getCreatedDate(), user2.getCreatedDate());
		
		return randomThirtyDaysSpan(fromDate);
	}
	
	public long randomFriendRequestedDate(ReducedUserProfile user1, ReducedUserProfile user2){
		long fromDate = Math.max(user1.getCreatedDate(), user2.getCreatedDate());
		return randomThirtyDaysSpan(fromDate);
	}
	
	public long randomFriendApprovedDate(long requestedDate){
		long randomSpanMilis =  (long) (sevenDayRanGen.nextDouble()* (sevenDayInMillis));
		return (requestedDate + randomSpanMilis);
	}
	public long randomFriendDeclinedDate(long requestedDate){
		long randomSpanMilis =  (long) (sevenDayRanGen.nextDouble()* (sevenDayInMillis));
		return (requestedDate + randomSpanMilis);
	}
	public long randomFriendReapprovedDate(long declined){
		long randomSpanMilis =  (long) (thirtyDayRanGen.nextDouble()* (thirtyDayInMillis));
		return (declined + randomSpanMilis);
	}	
	public long numberOfWeeks(ReducedUserProfile user){
		return (to - user.getCreatedDate())/weekDayInMillis;
	}
	
	public long numberOfWeeks(long fromDate){
		return (to - fromDate)/weekDayInMillis;
	}
	
	public long randomPostCreatedDate(ReducedUserProfile user){
		long createdDate = (long)(ranGen.nextDouble()*(to-user.getCreatedDate())+user.getCreatedDate());
 
		return createdDate; 
	}
	
	public long randomPhotoAlbumCreatedDate(ReducedUserProfile user){
		long createdDate = (long)(ranGen.nextDouble()*(to-user.getCreatedDate())+user.getCreatedDate());
 
		return createdDate; 
	}

	public long randomGroupCreatedDate(ReducedUserProfile user){
		long createdDate = (long)(ranGen.nextDouble()*(to-user.getCreatedDate())+user.getCreatedDate());
 
		return createdDate; 
	}

	public long randomGroupMemberJoinDate(long groupCreateDate, long userCreatedDate){
		long earliestJoinDate = Math.max(groupCreateDate, userCreatedDate);
		long joinDate = (long)(ranGen.nextDouble()*(to - earliestJoinDate) + earliestJoinDate);
 
		return joinDate; 
	}
	
	public long randomGroupPostCreatedDate(long memberJoinDate){
		long createdDate = (long)(ranGen.nextDouble()*(to-memberJoinDate)+memberJoinDate);
 
		return createdDate; 
	}
	
	public long powerlawPostCreatedDate(UserProfile user){
		long createdDate = (long)(disGen.getDouble()*(to-user.getCreatedDate())+user.getCreatedDate());
 
		return createdDate; 
	}
	
	public long randomCommentCreatedDate(long lastCommentCreatedDate){
		long createdDate = (long)(ranGen.nextDouble()*(to-lastCommentCreatedDate)+lastCommentCreatedDate);
		
		return createdDate;

	}
	
	//Assume that this powerlaw generate powerlaw value between 0 - 1 
	public long powerlawCommentCreatDate(long lastCommentCreatedDate){
		long createdDate = (long)(disGen.getDouble() *(to-lastCommentCreatedDate)+lastCommentCreatedDate);
		
		return createdDate; 
	}
	
	public long powerlawCommDateDay(long lastCommentCreatedDate){
		long createdDate = (long)(disGen.getDouble() * oneDayInMillis+lastCommentCreatedDate);
		
		return createdDate; 
	}
	
	// Assume that users are of 10 to 70 years' old
	// Randomly select a long value in this range, then, minus this value from user's created date
	public long getBirthDay(long userCreatedDate){
		long age = (long)(ranGen.nextDouble() * sixtyYear + tenYear);
		return (userCreatedDate - age);
	}

	//If do not know the birthday, first randomly guess the age of user
	//Randomly get the age when user graduate
	//User's age for graduating is from 20 to 30

	public long getClassYear(long userCreatedDate, long birthday){
		long age;
		long graduateage = (ranClassYear.nextInt(10) + 20) * oneYear; 
		if (birthday != -1){
			return (long)(birthday + graduateage); 
		}
		else{
			age = (long)(ranGen.nextDouble() * sixtyYear + tenYear);
			return (userCreatedDate - age + graduateage);
		}
	}
	
	public long getWorkFromYear(long userCreatedDate, long birthday){
		long age;
		long workingage = (ranClassYear.nextInt(10) + 25) * oneYear; 
		if (birthday != -1){
			return (long)(birthday + workingage); 
		}
		else{
			age = (long)(ranGen.nextDouble() * sixtyYear + tenYear);
			return (userCreatedDate - age + workingage);
		}
	}
	
	public long getWorkFromYear(long classYear){
		return (classYear + (long)(ranWorkingYear.nextDouble()*twoYear));
	}
	
	public long getStartDateTime(){
		return from;
	}
	public long getCurrentDateTime(){
		return to; 
	}
}


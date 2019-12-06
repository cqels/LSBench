package sib.generator;

import java.util.GregorianCalendar;
import java.util.Random;

import sib.objects.UserProfile;

class ThreadTest implements Runnable{
	   Thread t;
	   ThreadTest () {
	      t = new Thread(this,"My thread");
	      t.start();
	   }
	   public void run() {
	      System.out.println("Child thread started");
	      System.out.println("Child thread terminated");
	   }
	}
	class Demo {
	   /**
	 * @param args
	 */
	public static void main (String args[]){
		for (int i = 0; i <0 ; i++){
			System.out.println("Value: " + i);
		}
			/*
	      new ThreadTest();
	      System.out.println("Main thread started");
	      System.out.println("Main thread terminated");
	      
	      DateGenerator userCreatedDateGen = new DateGenerator(new GregorianCalendar(2006,01,01),
					new GregorianCalendar(2011,06,23),53223436L,53223436L, 0.5 );
	      
	      long randomDate = userCreatedDateGen.randomDateInMillis();
	      System.out.println("Random date: " + userCreatedDateGen.formatDate(randomDate) );
	      
	      long randomDateSpan = userCreatedDateGen.randomThirtyDaysSpan(randomDate);
	      System.out.println("Random date span: " + userCreatedDateGen.formatDate(randomDateSpan) );
	      */
	      /*
	      HashMap singerNames = new HashMap();
	      for (int i = 0; i < 10; i++){
	    	  singerNames.put("name" + i, new Integer(i+2));
	      }	
	      
	      if (singerNames.containsKey("name3") ){
	    	  System.out.println("Name 3 appears at " + singerNames.get("name3"));
	      }
	      */
	      /*
	      Random rand = new Random(34534654);
	      for (int i = 0; i < 20; i ++){
	    	  System.out.println(" " + rand.nextInt(20));
	      }
	      
	      UserProfile testUser = new UserProfile(); 
	      testUser.setAccountId(231);
	      
	      Demo demo = new Demo(); 
	      testUser = demo.changeValue(); 
	      
	      System.out.println("New account Id is " + testUser.getAccountId());
	      */


	   }
	
		public UserProfile changeValue(){
			UserProfile user = new UserProfile();
			user.setAccountId(100);
			return user; 
		}	
	}
	


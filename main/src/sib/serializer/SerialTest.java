package sib.serializer;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import sib.objects.UserProfile;

public class SerialTest {

	public static void main(String args[]){
		/*
		int empNo = 1000; 
		
		try
		{
			
	        FileOutputStream fileOut =
	        new FileOutputStream("employee.ser");
	        ObjectOutputStream out =
	                           new ObjectOutputStream(fileOut);
	        
	        for (int i = 0; i < empNo; i++){
	        	Employee emp1 = new Employee();
	        	emp1.setAge(i);
	        	emp1.setLocation("Location " + i);
	        	emp1.setName("Name " + i);
	        	out.writeObject(emp1);
	        }


	        out.close();
	        fileOut.close();
	    }
		catch(IOException i)
	    {
	        i.printStackTrace();
	    }
		*/
        try
        {
        	FileInputStream fileIn =
                        new FileInputStream("userProf.ser");
        	ObjectInputStream in = new ObjectInputStream(fileIn);
    		// Deserialization
        	
        	UserProfile userProfile;
            for (int i = 0; i < 100000; i++){
            	userProfile = (UserProfile) in.readObject();
            	//System.out.println("Age of user: " + e.getAge());
            	if (userProfile.getAccountId()==0)
            		userProfile.print();
            }
        	
        	in.close();
        	fileIn.close();
        }
        catch(EOFException eof){
        	eof.printStackTrace();
        }
        catch(IOException i){
        	i.printStackTrace();
        	return;
        }catch(ClassNotFoundException c){
        	c.printStackTrace();
        	return;
        }
	}
}

class Employee implements Serializable{
	String name; 
	int age; 
	String location; 
	
	public void printName(){
		System.out.println("Name is: " + name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
}



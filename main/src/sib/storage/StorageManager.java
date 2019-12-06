package sib.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sib.objects.ReducedUserProfile;
import sib.objects.UserProfile;
import sib.util.ExternalSort;

public class StorageManager {

	FileOutputStream	fos; 
	ObjectOutputStream 	oos;
	
	FileInputStream		fis;
	ObjectInputStream	ois;
	
	String				outUserProfile = "";
	String 				passOutUserProf = "";
	String 				passOutUserProfSorted = "";

	int					cellSize;
	int					windowSize;
	

	int					numberSerializedObject = 0;
	int					numberDeSerializedObject = 0;
	
	//boolean				isMultiFiles = false;
	//int					numFiles; 
	//String 				mulpassOutUserProf[]; 
	String				baseDir; 
	



	public StorageManager(){}
	
	public StorageManager(int _cellSize, int _windowSize, String _outUserProfileName, String _baseDir){
		this.cellSize = _cellSize;
		this.windowSize = _windowSize;
		this.outUserProfile = _outUserProfileName; 
		this.baseDir = _baseDir; 
	}
	
	public StorageManager(int _cellSize, int _windowSize, int pass, 
			String _outUserProfileName, String _baseDir){
		this.cellSize = _cellSize;
		this.windowSize = _windowSize;
		this.outUserProfile = _outUserProfileName;
		this.baseDir = _baseDir; 
		
		passOutUserProf = pass + "_" + outUserProfile;
		passOutUserProfSorted= pass + "_" + outUserProfile + ".sorted";
	}

	public void serializeReducedUserProfiles(int from, int to, int pass, 
			ReducedUserProfile userProfiles[]){
		
		serializeReducedUserProfiles(from, to, pass, userProfiles, oos);
	}
	
	// This function can be used for the case of a file in multiple files storage
	public void serializeReducedUserProfiles(int from, int to, int pass, 
							ReducedUserProfile userProfiles[], ObjectOutputStream _oos) {
		try {
				if (to > windowSize) {
					for (int i = from; i < windowSize; i++) {
						_oos.writeObject((ReducedUserProfile) userProfiles[i]);
						numberSerializedObject++;
					}
					for (int i = 0; i < to - windowSize; i++) {
						_oos.writeObject((ReducedUserProfile) userProfiles[i]);
						numberSerializedObject++;
					}
				} else {
					for (int i = from; i < to; i++) {
						_oos.writeObject((ReducedUserProfile) userProfiles[i]);
						numberSerializedObject++;
				}
			}
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public void initSerialization(int pass) {
		numberSerializedObject = 0; 
		try {
			numberSerializedObject = 0;
			passOutUserProf = pass + "_" + outUserProfile;
			fos = new FileOutputStream(baseDir + passOutUserProf);
			oos = new ObjectOutputStream(fos);
			
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public void endSerialization() {
		try {
			fos.close();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}                                                                              
	}	
	
	public void deserializeWindowlUserProfile(ReducedUserProfile userProfiles[]) {
		try {
			for (int i = 0; i < windowSize; i++) {
				if (userProfiles[i] != null){
					userProfiles[i].clear(); 
					userProfiles[i] = null;
				}
				userProfiles[i] = (ReducedUserProfile) ois.readObject();
				numberDeSerializedObject++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deserializeOneCellUserProfile(int startIdex, int cellSize,
			ReducedUserProfile userProfiles[]) {
		try {
			for (int i = 0; i < cellSize; i++) {
				if (userProfiles[startIdex + i] != null){
					userProfiles[startIdex + i].clear();				
					userProfiles[startIdex + i] = null;
				}
				userProfiles[startIdex + i] = (ReducedUserProfile) ois.readObject();
				numberDeSerializedObject++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);

		}
	}
	public void deserializeOneCellUserProfile(ReducedUserProfile userProfilesCell[]) {
		try {
			for (int i = 0; i < cellSize; i++) {
				if (userProfilesCell[i] != null){
					userProfilesCell[i].clear();
					userProfilesCell[i] = null;
				}
				userProfilesCell[i] = (ReducedUserProfile) ois.readObject();
				numberDeSerializedObject++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);

		}
	}	
	public void initDeserialization(int pass) {
		numberDeSerializedObject = 0;
		try {
			//passOutUserProfSorted= pass + "_" + outUserProfile + ".sorted";
			fis = new FileInputStream(baseDir + passOutUserProfSorted);
			ois = new ObjectInputStream(fis);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void initDeserialization(String inputfile) {
		numberDeSerializedObject = 0;
		try {
			fis = new FileInputStream(baseDir + inputfile);
			ois = new ObjectInputStream(fis);
			

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void endDeserialization() {
		try {
			fis.close();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getCellSize() {
		return cellSize;
	}

	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;
	}
	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	
	public String getPassOutUserProf() {
		return passOutUserProf;
	}

	public void setPassOutUserProf(String passOutUserProf) {
		this.passOutUserProf = passOutUserProf;
	}

	public String getPassOutUserProfSorted() {
		return passOutUserProfSorted;
	}

	public void setPassOutUserProfSorted(String passOutUserProfSorted) {
		this.passOutUserProfSorted = passOutUserProfSorted;
	}
	public int getNumberSerializedObject() {
		return numberSerializedObject;
	}
	public int getNumberDeSerializedObject() {
		return numberDeSerializedObject;
	}

	public void setNumberDeSerializedObject(int numberDeSerializedObject) {
		this.numberDeSerializedObject = numberDeSerializedObject;
	}

}

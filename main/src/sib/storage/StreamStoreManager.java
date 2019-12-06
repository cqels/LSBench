package sib.storage;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sib.objects.GPS;
import sib.objects.PhotoStream;
import sib.objects.PostStream;

public class StreamStoreManager {

	FileOutputStream	fos; 
	ObjectOutputStream 	oos;
	
	FileInputStream		fis;
	ObjectInputStream	ois;
	
	String				outFileName = "";
	String 				sortedFileName = "";

	int					cellSize;
	int					windowSize;
	int 				mapId; 

	int					numberSerializedObject = 0;
	int					numberDeSerializedObject = 0;
	
	String				baseDir; 
	
	public StreamStoreManager(){}
	
	public StreamStoreManager(int _cellSize, int _windowSize, String _outFileName, String _baseDir, int _mapId){
		this.cellSize = _cellSize;
		this.windowSize = _windowSize;
		this.mapId = _mapId; 
		this.outFileName = _mapId + "_" + _outFileName; 
		this.sortedFileName = outFileName + ".sorted";
		this.baseDir = _baseDir; 
	}

	
	public String getOutFileName() {
		return outFileName;
	}

	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}

	public String getSortedFileName() {
		return sortedFileName;
	}

	public void setSortedFileName(String sortedFileName) {
		this.sortedFileName = sortedFileName;
	}

	public void initSerialization() {
		try {
			numberSerializedObject = 0;
			
			fos = new FileOutputStream(baseDir + outFileName);
			oos = new ObjectOutputStream(fos);
			
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public void serialize(PostStream postStream){
		try {
			oos.writeObject(postStream);
			numberSerializedObject++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void serialize(PhotoStream photoStream){
		try {
			oos.writeObject(photoStream);
			numberSerializedObject++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void serialize(GPS gps){
		try {
			oos.writeObject(gps);
			numberSerializedObject++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


package sib.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import sib.objects.PhotoStream;

/*
 * One particular case of External sort
*/
public class PhotoExternalSort {
	
	static int 			DEFAULTMAXTEMPFILES = 1024;
	static int 			sizeOfaPhoto = 42;  //bytes; each integer is 4 bytes 
	
	// we divide the file into small blocks. If the blocks
	// are too small, we shall create too many temporary files. 
	// If they are too big, we shall be using too much memory. 
	public static long estimateBestSizeOfBlocks(File filetobesorted, int maxtmpfiles) {
		//long sizeoffile = filetobesorted.length() * 2;
		long sizeoffile = filetobesorted.length();
		
		System.out.println("Size of file is " + sizeoffile);
		//System.out.println("Total space of file is " + filetobesorted.getTotalSpace());
		
		/**
		* We multiply by two because later on someone insisted on counting the memory
		* usage as 2 bytes per character. By this model, loading a file with 1 character
		* will use 2 bytes.
		*/ 
		// we don't want to open up much more than maxtmpfiles temporary files, better run
		// out of memory first.
		long blocksize = sizeoffile / maxtmpfiles + (sizeoffile % maxtmpfiles == 0 ? 0 : 1) ;
		
		// on the other hand, we don't want to create many temporary files
		// for naught. If blocksize is smaller than half the free memory, grow it.
		long freemem = Runtime.getRuntime().freeMemory();
		/*
		if( blocksize < freemem/2) {
		    blocksize = freemem/2;
		} 
		*/
		if( blocksize < freemem/2) {
		    blocksize = freemem/2;
		} 		
		System.out.println("Freemem is " + freemem);
		long totalmem = Runtime.getRuntime().totalMemory();
		System.out.println("Totalmem is " + totalmem);
		System.out.println("Block size is " + blocksize);
		return blocksize;
	}

	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to  
	 * temporary files that have to be merged later.
	 * 
	 * @param file some flat  file
	 * @param cmp string comparator 
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<PhotoStream> cmp) throws IOException {
		return sortInBatch(file, cmp,DEFAULTMAXTEMPFILES);
	}
	
	
	/**
	 * This will simply load the file by blocks of x rows, then
	 * sort them in-memory, and write the result to 
	 * temporary files that have to be merged later. You can
	 * specify a bound on the number of temporary files that
	 * will be created.
	 * 
	 * @param file some flat  file
	 * @param cmp UserProfle comparator 
	 * @param maxtmpfiles
	 * @return a list of temporary flat files
	 */
	public static List<File> sortInBatch(File file, Comparator<PhotoStream> cmp, int maxtmpfiles) throws IOException {
		List<File> files = new ArrayList<File>();
		ObjectInputStream fbr = new ObjectInputStream(new FileInputStream(file));
		long blocksize = estimateBestSizeOfBlocks(file,maxtmpfiles);// in bytes

		try{
			List<PhotoStream> tmplist =  new ArrayList<PhotoStream>();
			PhotoStream photoStream = new PhotoStream() ;
			try {
					while (photoStream != null){
						long currentblocksize = 0;// in bytes
						while((currentblocksize < blocksize) 
						&&(   (photoStream = (PhotoStream)fbr.readObject()) != null) ){ // as long as you have enough memory
							tmplist.add(photoStream);
							currentblocksize +=  getSizeOfObject(photoStream) ; // Size of the reducedUserProfile object
						}
						files.add(sortAndSave(tmplist,cmp));
						tmplist.clear();
					}
				
			} catch(EOFException oef) {
				System.out.println("End of file --- ");
				if(tmplist.size()>0) {
					files.add(sortAndSave(tmplist,cmp));
					tmplist.clear();
				}
			}
			 catch(Exception e) {
					System.out.println("Unknown error when doing external sorting ");
					e.printStackTrace();
						
			}
		
		} finally {
			fbr.close();
		}
		return files;
	}
	
	public static int getSizeOfObject(PhotoStream photoStream){
		
		int size;
		if (photoStream.isPhoto() == true){
			size = photoStream.getLocationName().length() * 2 + photoStream.getTags().length * 4
					+ photoStream.getInterestedUserAccs().length * 4 + 
					photoStream.getUserAgent().length() * 2 + 60;
		}
		else{
			size = 24 + photoStream.getTitle().length() * 2; 
		}
		
		return size; 
	}

	public static File sortAndSave(List<PhotoStream> tmplist, Comparator<PhotoStream> cmp) throws IOException  {
		Collections.sort(tmplist,cmp);  
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile");
		newtmpfile.deleteOnExit();
		ObjectOutputStream fbw = new ObjectOutputStream(new FileOutputStream(newtmpfile));
		try {
			for(PhotoStream r : tmplist) {
				fbw.writeObject(r);
			}
		} finally {
			fbw.close();
		}
		
		return newtmpfile;
	}
	
	/**
	 * This merges a bunch of temporary flat files 
	 * @param files
	 * @param output file
         * @return The number of lines sorted. (P. Beaudoin)
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<PhotoStream> cmp) throws IOException {
		PriorityQueue<PhotoBinaryFileBuffer> pq = new PriorityQueue<PhotoBinaryFileBuffer>(11, 
            new Comparator<PhotoBinaryFileBuffer>() {
              public int compare(PhotoBinaryFileBuffer i, PhotoBinaryFileBuffer j) {
                return cmp.compare(i.peek(), j.peek());
              }
            }
        );
		for (File f : files) {
			PhotoBinaryFileBuffer bfb = new PhotoBinaryFileBuffer(f);
			pq.add(bfb);
		}
		ObjectOutputStream fbw = new ObjectOutputStream(new FileOutputStream(outputfile));
		int objectCounter = 0;
		try {
			while(pq.size()>0) {
				PhotoBinaryFileBuffer bfb = pq.poll();
				PhotoStream r = bfb.pop();
				fbw.writeObject(r);
				++objectCounter;
				if(bfb.empty()) {
					bfb.fbr.close();
					bfb.originalfile.delete();// we don't need you anymore
				} else {
					pq.add(bfb); // add it back
				}
			}
		} finally { 
			fbw.close();
			for(PhotoBinaryFileBuffer bfb : pq ) bfb.close();
		}
		return objectCounter;
	}
	
	public static void verifySorting(File sortedFile){
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(sortedFile));
			PhotoStream photoStream; 
			for (int i = 0; i < 10; i ++){
				photoStream = (PhotoStream)oos.readObject();
				if (photoStream.isPhoto() == true){
					/*
					System.out.println("PhotoStream " + i + " " + photoStream.getTitle());
					System.out.println("Date " + i + " " + photoStream.getCreatedDate());
					*/
					//photoStream.printPhotoStream();
				}
				else{
					System.out.println("Comment ::: ");
					System.out.println("Date " + i + " " + photoStream.getCreatedDate());
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find the sorted file");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	public void SortPhotos(String[] args) throws IOException {
		
		boolean verbose = false;
		int maxtmpfiles = DEFAULTMAXTEMPFILES;
		String inputfile=null, outputfile=null;
		
		for(int param = 0; param<args.length; ++param) {
			if(args[param].equals("-v") ||  args[param].equals("--verbose"))
			  verbose = true;
			else if ((args[param].equals("-t") ||  args[param].equals("--maxtmpfiles")) && args.length>param+1) {
				param++;
			    maxtmpfiles = Integer.parseInt(args[param]);  
			} else {
				if(inputfile == null) 
				  inputfile = args[param];
				else if (outputfile == null)
				  outputfile = args[param];
				else System.out.println("Unparsed: "+args[param]); 
			}
		}
		
		if(outputfile == null) {
			System.out.println("please provide input and output file names");
			return;
		}
		
		Comparator<PhotoStream> comparator = new Comparator<PhotoStream>() {
			public int compare(PhotoStream u1, PhotoStream u2){
				if (u1.getCreatedDate() < u2.getCreatedDate()){
					return -1; 
				}
				else
					return 1; 
				//return (int)(u1.getCreatedDate()-u2.getCreatedDate()) ;
			}
		};
		
		System.out.println("Input file is " + inputfile);
		System.out.println("Output file is " + outputfile);
		
		List<File> l = sortInBatch(new File(inputfile), comparator, maxtmpfiles) ;
		
		//if(verbose) System.out.println("created "+l.size()+" tmp files");
		System.out.println("created "+l.size()+" tmp files");
		
		mergeSortedFiles(l, new File(outputfile), comparator);
		
		//verifySorting(new File(outputfile));
		
		System.out.println("Done sorting for Photos!!! ==> output file " + outputfile);
	}

}

class PhotoBinaryFileBuffer  {
	public static int BUFFERSIZE = 2048;
	public ObjectInputStream fbr;
	public File originalfile;
	private PhotoStream cache;
	private boolean empty;
	
	public PhotoBinaryFileBuffer(File f) throws IOException {
		originalfile = f;
		//fbr = new ObjectInputStream(new FileInputStream(f), BUFFERSIZE);
		fbr = new ObjectInputStream(new FileInputStream(f));
		reload();
	}
	
	public boolean empty() {
		return empty;
	}
	
	private void reload() throws IOException {
		try {
			this.cache = (PhotoStream)fbr.readObject();
            empty = false;
          
		} catch(EOFException oef) {
			empty = true;
			cache = null;
		} catch(Exception e){
			empty = true;
			cache = null;			
			System.out.println("Exception in Reload ");
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		fbr.close();
	}
	
	
	public PhotoStream peek() {
		if(empty()) return null;
		return cache;
	}
	public PhotoStream pop() throws IOException {
		PhotoStream answer = peek();
		reload();
	  return answer;
	}
}
package sib.old;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Test{
	public static void main(String args[]){
		int[] s = new int[5];
		s[0] = 1; 
		s[1] = 3; 
		HashSet<Integer> h = new HashSet<Integer>();
		h.add(1);
		h.add(3);
		System.out.println("Length of array is " + s.length);
		System.out.println("S[3] = " + s[3]);
		System.out.println("Size of hash is " + h.size());
	}
	
}
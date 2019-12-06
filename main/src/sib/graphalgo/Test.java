package sib.graphalgo;

import java.util.concurrent.CountDownLatch;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int numProcessor = Runtime.getRuntime().availableProcessors();
		CountDownLatch doneSignal = new CountDownLatch(numProcessor);
		
	}

}

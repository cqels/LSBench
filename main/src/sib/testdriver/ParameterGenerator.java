package sib.testdriver;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

//import benchmark.model.ProductType;

public class ParameterGenerator {
	
	
	
	
	/*
	 * Get a random date from (dateMin) to (dateMin+days)
	 */
	public static GregorianCalendar getRandomDate(GregorianCalendar dateMin, ValueGenerator valueGen, int days) {
		Integer dayOffset = valueGen.randomInt(0, days);
		GregorianCalendar gClone = (GregorianCalendar)dateMin.clone();
		gClone.add(GregorianCalendar.DAY_OF_MONTH, dayOffset);
		return gClone;
	}
}

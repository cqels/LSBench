package sib.vocabulary;

import java.util.HashMap;

public class SIOC {
	//The namespace of this vocabulary as String
	public static final String NS = "http://rdfs.org/sioc/ns#";
	
	//Get the URI of this vocabulary
	public static String getURI() { return NS; }
		
	public static final String PREFIX = "sioc:";
	public static final String FACTPREFIX = "sioc_";
	
	private static HashMap<String, String> uriMap = new HashMap<String, String>();
	
	/*
	 * For prefixed versions
	 */
	public static String prefixed(String string) {
		if(uriMap.containsKey(string)) {
			return uriMap.get(string);
		}
		else {
			String newValue = PREFIX + string;
			uriMap.put(string, newValue);
			return newValue;
		}
	}
	
	public static String factprefixed(String string) {
		if(uriMap.containsKey(string)) {
			return uriMap.get(string);
		}
		else {
			String newValue = FACTPREFIX + string;
			uriMap.put(string, newValue);
			return newValue;
		}
	}
	//Resources

    //Properties
    public static final String account =  NS+"account_of";
    public static final String property =  NS+"moderator_of";
    public static final String subscriber =  NS+"subscriber_of";
    
}

package sib.dictionary;

import java.util.Random;
import java.util.Vector;

import sib.generator.DateGenerator;
import sib.objects.Event;
import sib.objects.PopularPlace;

public class EventsDictionary {
	int 					numEvents; 
	PopularPlacesDictionary placeDic; 
	Vector<Event> 			eventSet; 
	Random 					rand; 
	DateGenerator			dateGenerator; 
	
	public EventsDictionary(int _numEvent, PopularPlacesDictionary _placeDic, 
							DateGenerator _dateGenerator, long seed){
		this.numEvents = _numEvent;
		this.eventSet = new Vector<Event>(numEvents);
		this.placeDic = _placeDic; 
		this.rand = new Random(seed);
		this.dateGenerator = _dateGenerator; 
	}
	
	public void initEventSet(){
		int numLocation = placeDic.getNumLocations();
		for (int i = 0; i < numEvents; i++){
			Event event = new Event(); 
			event.setEventTime(dateGenerator.randomDateInMillis());
			
			int placeId = -1;
			int locationId = -1;
			while (placeId == -1){
				locationId = rand.nextInt(numLocation);
				placeId = placeDic.getPopularPlace(locationId);
			}
			PopularPlace place = placeDic.getPopularPlace(locationId, placeId);
			event.setEventPlace(place.getName());
			event.setLatt(place.getLatt());
			event.setLongt(place.getLongt());
			event.setLocationId(locationId);
			event.setPlaceId(placeId);
			
			eventSet.add(event);
		}
	}
	

	
	public int getNumEvents() {
		return numEvents;
	}
	public void setNumEvents(int numEvents) {
		this.numEvents = numEvents;
	}
	public Vector<Event> getEventSet() {
		return eventSet;
	}
	public void setEventSet(Vector<Event> eventSet) {
		this.eventSet = eventSet;
	}
}

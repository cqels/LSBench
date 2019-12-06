package sib.extras;

public class Location {
	int 	id; 
	String 	name;
	double 	latt; 	
	double 	longt; 
	int 	population;

	public Location(){
	}
	public Location(int _id, String _name, double _longt, double _latt, int _population){
		this.id = _id; 
		this.name = _name; 
		this.longt = _longt; 
		this.latt = _latt; 
		this.population = _population;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLongt() {
		return longt;
	}
	public void setLongt(double longt) {
		this.longt = longt;
	}
	public double getLatt() {
		return latt;
	}
	public void setLatt(double latt) {
		this.latt = latt;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	
}

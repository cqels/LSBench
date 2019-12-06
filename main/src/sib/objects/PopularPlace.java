package sib.objects;

public class PopularPlace {

	String name;
	double 	latt; 	
	double 	longt; 
	
	public PopularPlace(String _name, double _latt, double _longt){
		this.name = _name; 
		this.latt = _latt; 
		this.longt = _longt; 
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatt() {
		return latt;
	}

	public void setLatt(double latt) {
		this.latt = latt;
	}

	public double getLongt() {
		return longt;
	}

	public void setLongt(double longt) {
		this.longt = longt;
	}
	
}

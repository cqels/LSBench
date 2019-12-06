package sib.objects;

import java.io.Serializable;

public class IP implements Serializable {
	short ip1;
	short ip2;
	short ip3;
	short ip4;
	public IP(short _ip1, short _ip2, short _ip3, short _ip4){
		this.ip1 = _ip1;
		this.ip2 = _ip2;
		this.ip3 = _ip3;
		this.ip4 = _ip4;
	}
	public String toString(){
		return ip1 + "." + ip2 + "." + ip3 + "." + ip4;
	}
}

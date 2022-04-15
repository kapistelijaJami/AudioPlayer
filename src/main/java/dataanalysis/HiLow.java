package dataanalysis;

public class HiLow {
	public short h, l;

	public HiLow() {
		this(Short.MIN_VALUE, Short.MAX_VALUE);
	}

	public HiLow(short h, short l) {
		this.h = h;
		this.l = l;
	}
	
	public void updateValues(short val) {
		if (val > h) {
			h = val;
		}

		if (val < l) {
			l = val;
		}
	}
}

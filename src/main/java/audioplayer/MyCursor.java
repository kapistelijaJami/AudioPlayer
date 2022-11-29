package audioplayer;

import java.awt.Cursor;

public class MyCursor {
	public int type;
	
	public MyCursor() {
		type = Cursor.DEFAULT_CURSOR;
	}
	
	public MyCursor(int type) {
		this.type = type;
	}
}

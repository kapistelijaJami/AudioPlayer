package dataanalysis.enums;

public enum DividerOrientation {
	HORIZONTAL, VERTICAL;

	public enum ScalingDirection { //Which edge the divider is attached to == what edge should be moving. TOP means that y value is changing (and obviously height too). BOTTOM only height is changing.
		TOP, BOTTOM, LEFT, RIGHT;
	}

	public ScalingDirection getFirst() {
		if (this == HORIZONTAL) {
			return ScalingDirection.BOTTOM; //object is above, so we move the bottom edge
		} else {
			return ScalingDirection.RIGHT; //object is left of the divider, so we move the right edge
		}
	}

	public ScalingDirection getSecond() {
		if (this == HORIZONTAL) {
			return ScalingDirection.TOP;
		} else {
			return ScalingDirection.LEFT;
		}
	}
}

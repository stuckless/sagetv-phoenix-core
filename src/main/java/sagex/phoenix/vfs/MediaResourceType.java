/**
 * Constants representing all Known Core VFS types.
 */
package sagex.phoenix.vfs;

public enum MediaResourceType {
	FILE(1), FOLDER(2), TV(3), RECORDING(4), MUSIC(5), ANY_VIDEO(6), DVD(7), BLURAY(8), HD(9), VIDEODISC(10), PICTURE(11), VIDEO(12), ONLINE(
			13), EPG_AIRING(14), // means, there is not "real" mediafile
									// associated with this, it's simply an EPG
									// item
	HOME_MOVIE(15), DUMMY(99); // means, the node is informational, ie not a
								// real anything;

	private int intValue;

	private MediaResourceType(int value) {
		this.intValue = value;
	}

	public int value() {
		return intValue;
	}

	public static MediaResourceType toMediaResourceType(String id) {
		if (id == null)
			return null;
		id = id.toUpperCase();

		for (MediaResourceType t : values()) {
			if (id.equals(t.name())) {
				return t;
			}
		}

		return null;
	}

	public static MediaResourceType toMediaResourceType(int id) {
		for (MediaResourceType t : values()) {
			if (id == t.intValue) {
				return t;
			}
		}
		return null;
	}
}
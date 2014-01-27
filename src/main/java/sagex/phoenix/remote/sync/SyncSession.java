package sagex.phoenix.remote.sync;

import java.lang.ref.SoftReference;

public class SyncSession {
	public String id;
	public String mediaMask;
	public SoftReference<Object[]> mediaFiles = new SoftReference<Object[]>(null);
}

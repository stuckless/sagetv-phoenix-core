package sagex.phoenix.vfs.builder;

import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.vfs.VFSManager;

public class VFSManagerBuilder extends BaseBuilder {
	protected VFSManager manager = null;

	public VFSManagerBuilder(VFSManager mgr) {
		super();
		manager = mgr;
	}

}

package sagex.phoenix.vfs.util;

import java.io.File;

import sagex.phoenix.vfs.VFSManager;

public class ImmutableVFSManager extends VFSManager {
	public ImmutableVFSManager(File userDir, VFSManager parent) {
		super(parent.getSystemFiles().getDir(), userDir);

		// basically copy the known factories
		this.getVFSFilterFactory().addAll(parent.getVFSFilterFactory().getFactories());
		this.getVFSGroupFactory().addAll(parent.getVFSGroupFactory().getFactories());
		this.getVFSSortFactory().addAll(parent.getVFSSortFactory().getFactories());
		this.getVFSSourceFactory().addAll(parent.getVFSSourceFactory().getFactories());
		this.getVFSViewFactory().addAll(parent.getVFSViewFactory().getFactories());
	}

	@Override
	public void loadConfigurations() {
		// do nothing
	}
}

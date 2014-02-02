package test;

import java.io.File;
import java.util.List;

import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.views.ViewFactory;

public class VFSCompare {
	public static void main(String args[]) throws Exception {
		InitPhoenix.init(true, true);

		VFSManager mgr1 = new VFSManager(new File("target/testing/STVs/Phoenix/vfs"), new File("/tmp"));
		mgr1.loadConfigurations();
		List<ViewFactory> list1 = mgr1.getVFSViewFactory().getFactories(true);

		File f2 = new File("../PhoenixUI/STVs/Phoenix/vfs");
		if (!f2.exists())
			throw new Exception("Invalid Dir: " + f2.getAbsolutePath());
		VFSManager mgr2 = new VFSManager(new File("../PhoenixUI/STVs/Phoenix/vfs"), new File("/tmp"));
		mgr2.loadConfigurations();
		List<ViewFactory> list2 = mgr2.getVFSViewFactory().getFactories(true);
		if (list2.size() == 0)
			throw new Exception("No VFS files " + f2.getAbsolutePath());

		compare("Views", list1, list2);
	}

	private static void compare(String string, List<?> list1, List<?> list2) {
		List<Factory> f1 = (List<Factory>) list1;
		for (Factory f : f1) {
			if (!findFactory(f.getName(), list2)) {
				System.out.println("<<< " + f.getName());
			}
		}
	}

	private static boolean findFactory(String name, List<?> list2) {
		List<Factory> flist = (List<Factory>) list2;
		for (Factory f : flist) {
			if (f.getName().equals(name))
				return true;
		}
		return false;
	}

}

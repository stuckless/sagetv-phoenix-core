package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static test.junit.lib.FilesTestCase.makeDir;
import static test.junit.lib.FilesTestCase.makeFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.DirectoryScanner;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import sagex.phoenix.vfs.util.PathUtils;
import test.InitPhoenix;
import test.junit.lib.FilesTestCase;

public class TestMediaResources {
	private static class Counter {
		public int counter = 0;
	}

	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testMediaFile() {
		File mfile = FilesTestCase.makeFile("test/movie cd1.avi");
		IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(mfile);
		assertEquals("getBasename()", "movie cd1", PathUtils.getBasename(mf));
		assertEquals("getExtension()", "avi", PathUtils.getExtension(mf));
		assertEquals("getName()", "movie cd1.avi", PathUtils.getName(mf));
		assertEquals("getTitle()", "movie cd1.avi", mf.getTitle());
		assertTrue("getType()", mf.isType(MediaResourceType.FILE.value()));

		long time = mf.lastModified();
		assertTrue("mf.lastModified() returned 0", time != 0);

		System.out.println(time);
		mf.touch(time + 1000);

		assertEquals("Not Same", mf.lastModified(), time + 1000);

		mf.delete(null);
		assertEquals("delete()", false, mf.exists());
	}

	@Test
	public void testMediaFolder() {
		File mfile = FilesTestCase.makeDir("test/folder 1");
		IMediaFolder mf = FileResourceFactory.createFolder(mfile);
		assertEquals("getBasename()", "folder 1", PathUtils.getBasename(mf));
		assertEquals("getExtension()", null, PathUtils.getExtension(mf));
		assertEquals("getName()", "folder 1", PathUtils.getName(mf));
		assertEquals("getTitle()", "folder 1", mf.getTitle());
		assertTrue("getType()", mf.isType(MediaResourceType.FOLDER.value()));
		mf.delete(null);
		assertEquals("delete()", false, mf.exists());
	}

	@Test
	public void testBlurayFolders() {
		File br = FilesTestCase.makeFile("movies/Terminator/BDMV/test.m2ts");

		// create using file
		IMediaResource mr = FileResourceFactory.createResource(br);
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isBluRay()", mr.isType(MediaResourceType.BLURAY.value()));
		assertEquals("getTitle()", "Terminator", mr.getTitle());

		// create using BDMV dir
		mr = FileResourceFactory.createResource(br.getParentFile());
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isBluRay()", mr.isType(MediaResourceType.BLURAY.value()));
		assertEquals("getTitle()", "Terminator", mr.getTitle());

		// create using Terminator dir
		mr = FileResourceFactory.createResource(br.getParentFile().getParentFile());
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isBluRay()", mr.isType(MediaResourceType.BLURAY.value()));
		assertEquals("getTitle()", "Terminator", mr.getTitle());

		mr.delete(null);
		assertEquals("delete()", false, mr.exists());
	}

	@Test
	public void testDVDFolders() {
		File br = FilesTestCase.makeFile("movies/Terminator2/VIDEO_TS/test.vob");

		// create using videots dir
		IMediaResource mr = FileResourceFactory.createResource(br.getParentFile());
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isDVD()", mr.isType(MediaResourceType.DVD.value()));
		assertEquals("getTitle()", "Terminator2", mr.getTitle());

		// create using Terminator2 dir
		mr = FileResourceFactory.createResource(br.getParentFile());
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isDVD()", mr.isType(MediaResourceType.DVD.value()));
		assertEquals("getTitle()", "Terminator2", mr.getTitle());

		mr.delete(null);
		assertEquals("delete()", false, mr.exists());

		// create using .vob files
		br = FilesTestCase.makeFile("movies/Terminator3/test.vob");
		mr = FileResourceFactory.createResource(br);
		assertTrue("isFILE()", mr.isType(MediaResourceType.FILE.value()));
		assertTrue("isDVD()", mr.isType(MediaResourceType.DVD.value()));
		assertEquals("getTitle()", "Terminator3", mr.getTitle());

		mr.delete(null);
		assertEquals("delete()", false, mr.exists());
	}

	@Test
	public void testMediaScan() {
		makeFile("test/movies/dira/Nemo.avi");
		makeFile("test/movies/dira/Nemo.avi.properties");
		makeFile("test/movies/dira/Nemo.jpg");
		makeFile("test/movies/dira/Terminator.avi");
		makeFile("test/movies/dirb/x.avi");
		makeFile("test/movies/dirc/y/VIDEO_TS/test.vob");
		makeFile("test/movies/dirc/y/VIDEO_TS/test.buf");
		makeFile("test/movies/dird/dire/z.avi");
		makeFile("test/movies/dire/BDMV/test.m2ts");
		makeFile("test/movies/merlin/video.vob");

		final List<IMediaResource> files = new ArrayList<IMediaResource>();
		IMediaResourceVisitor vis = new IMediaResourceVisitor() {
			@Override
			public boolean visit(IMediaResource res, IProgressMonitor monitor) {
				if (res.isType(MediaResourceType.ANY_VIDEO.value())) {
					files.add(res);
				}
				return true;
			}
		};

		IMediaFolder mf = FileResourceFactory.createFolder(makeDir("test/movies"));
		mf.accept(vis, null, IMediaResource.DEEP_UNLIMITED);
		assertEquals("visitor failed", 7, files.size());
		assertEquals("children", 6, mf.getChildren().size());

		int fileCtr = 0, dirs = 0;
		for (IMediaResource r : mf.getChildren()) {
			if (r instanceof IMediaFile) {
				fileCtr++;
			} else {
				dirs++;
			}
		}

		assertEquals("files", 2, fileCtr);
		assertEquals("folders", 4, dirs);

		// media folder delete will delete everything...
		mf.delete(null);
		assertEquals("delete()", false, mf.exists());

		final List filesLeft = new LinkedList();
		sagex.phoenix.util.DirectoryScanner scanner = new DirectoryScanner(FileFileFilter.FILE);
		scanner.scan(makeDir("test/movies"), filesLeft);

		assertEquals("files left", 0, filesLeft.size());
	}

	@Test
	public void testFindChild() {
		VirtualMediaFolder folder1 = new VirtualMediaFolder(null, "1", "1", "Test1");
		VirtualMediaFolder folder2 = new VirtualMediaFolder(folder1, "2", "2", "Test2");
		VirtualMediaFolder folder3 = new VirtualMediaFolder(folder2, "3", "3", "Test3");
		VirtualMediaFile file = new VirtualMediaFile(folder3, "4", "4", "Test4.avi");
		folder3.addMediaResource(file);
		folder2.addMediaResource(folder3);
		folder1.addMediaResource(folder2);
		System.out.println("File: " + PathUtils.getLocation(file));

		IMediaResource r = folder1.findChild("/Test2/Test3/Test4.avi");
		assertNotNull(r);
		assertTrue(r == file);

		r = folder1.findChild("/Test2/Test3/");
		assertNotNull(r);
		assertTrue(r == folder3);

		r = folder1.findChild("Test2");
		assertNotNull(r);
		assertTrue(r == folder2);
	}

	@Test
	public void testPaths() {
		VirtualMediaFolder folder1 = new VirtualMediaFolder("Test1");
		VirtualMediaFolder folder2 = new VirtualMediaFolder(folder1, "Test2");
		VirtualMediaFolder folder3 = new VirtualMediaFolder(folder2, "Test3");
		VirtualMediaFile file = new VirtualMediaFile(folder3, "Test4.avi");
		folder3.addMediaResource(file);
		folder2.addMediaResource(folder3);
		folder1.addMediaResource(folder2);
		assertEquals("/Test1", phoenix.media.GetPath(folder1, false));
		assertEquals("/Test1/Test2/Test3", phoenix.media.GetPath(folder3, false));
		assertEquals("/Test1/Test2/Test3/Test4.avi", phoenix.media.GetPath(file, false));

		assertNull("folder relative to root should be null", phoenix.media.GetPath(folder1, true));
		assertEquals("/Test2/Test3", phoenix.media.GetPath(folder3, true));
		assertEquals("/Test2/Test3/Test4.avi", phoenix.media.GetPath(file, true));
	}

	@Test
	public void testCollector() {
		VirtualMediaFolder root = new VirtualMediaFolder("root");
		VirtualMediaFolder f2 = new VirtualMediaFolder("F2");
		VirtualMediaFolder f3 = new VirtualMediaFolder("F3");
		VirtualMediaFolder f4 = new VirtualMediaFolder("F4");
		root.addMediaResource(f2);
		root.addMediaResource(f3);
		root.addMediaResource(f4);

		f2.addMediaResource(new VirtualMediaFile("T1"));
		f2.addMediaResource(new VirtualMediaFile("T2"));
		f3.addMediaResource(new VirtualMediaFile("T3"));
		f3.addMediaResource(new VirtualMediaFile("T4"));
		f4.addMediaResource(new VirtualMediaFile("T5"));
		f4.addMediaResource(new VirtualMediaFile("T6"));

		List files = phoenix.media.GetAllChildren(root);
		assertEquals(6, files.size());

		files = phoenix.media.GetAllChildren(root, 3);
		assertEquals(3, files.size());

		files = phoenix.media.GetAllChildren(root, 5, MediaResourceType.FOLDER.name());
		// root folder + 3 child folders
		assertEquals(4, files.size());

	}
}

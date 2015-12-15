package test;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;

public class TestSageVFS {
    public static void main(String args[]) throws Throwable {
        InitPhoenix.init(true, true);

        // System.out.println("Source Folders");
        // IMediaFolder folder = phoenix.api.CreateSource("sageimports", null);
        // MediaFolderTraversal.walk(folder, new SimpleSTDOUTVisitor());

        // System.out.println("\n\nSource Folders Combined");
        // folder = phoenix.api.CreateSource("sageimports","combine: true");
        // MediaFolderTraversal.walk(folder, new SimpleSTDOUTVisitor());

        // System.out.println("\n\nSource Folders Combined Using View");
        IMediaFolder folder = phoenix.api.CreateView("videobyyear", "combine: false");
        for (IMediaResource r : folder) {
            System.out.println("Item: " + r.getTitle() + "; Folder: " + r.isType(MediaResourceType.FOLDER.value()));
        }

        System.out.println("-- DONE --");
    }
}

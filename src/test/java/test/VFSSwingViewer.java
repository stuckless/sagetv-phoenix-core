package test;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.views.ViewFolder;

public class VFSSwingViewer extends JPanel implements TreeSelectionListener {
	public class TreeLabel {
		private IMediaResource res;

		public TreeLabel(IMediaResource res) {
			this.res = res;
		}

		public IMediaResource getResource() {
			return res;
		}

		public String toString() {
			if (res == null)
				return "N/A";
			return res.getTitle();
		}
	}

	private JEditorPane htmlPane;
	private JTree tree;
	private URL helpURL;
	private static boolean DEBUG = false;

	// Optionally play with line styles. Possible values are
	// "Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal";

	// Optionally set the look and feel.
	private static boolean useSystemLookAndFeel = false;

	public VFSSwingViewer() throws IOException {
		super(new GridLayout(1, 0));

		InitPhoenix.init(true, true);

		// ViewFolder folder = phoenix.api.CreateView("sageimports",
		// "combine: false");
		ViewFolder folder = phoenix.api.CreateView("sagevideoimports", null);

		// Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TreeLabel(folder), true);
		createNodes(top);

		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		if (playWithLineStyle) {
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		// Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(100);
		splitPane.setPreferredSize(new Dimension(500, 300));

		// Add the split pane to this panel.
		add(splitPane);
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (node == null)
			return;

		TreeLabel treeLabel = (TreeLabel) node.getUserObject();
		IMediaResource res = treeLabel.getResource();

		if (node.isLeaf()) {
			if (node.getChildCount() == 0 && (res instanceof IMediaFolder)) {
				createNodes(node);
			}
		} else {
		}
	}

	private void createNodes(DefaultMutableTreeNode top) {
		TreeLabel tl = (TreeLabel) top.getUserObject();
		ViewFolder folder = (ViewFolder) tl.getResource();
		System.out.println("Creating Tree Items for Folder: " + folder.getTitle() + "; items: " + folder.getChildren().size());
		for (IMediaResource r : folder) {
			if (r instanceof IMediaFolder) {
				top.add(new DefaultMutableTreeNode(new TreeLabel(r), true));
			} else {
				top.add(new DefaultMutableTreeNode(new TreeLabel(r), false));
			}
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 * 
	 * @throws IOException
	 */
	private static void createAndShowGUI() throws IOException {
		if (useSystemLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				System.err.println("Couldn't use system look and feel.");
			}
		}

		// Create and set up the window.
		JFrame frame = new JFrame("VFS View Browser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new VFSSwingViewer());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Throwable {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
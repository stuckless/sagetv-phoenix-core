package sagex.phoenix.vfs.visitors;

import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.DecoratedMediaFolder;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;

/**
 * Will Build a debug string of the folder contents.
 * 
 * @author seans
 */
public class PrintTreeVisitor extends StructureVisitor {
	public static final String HINT_DUMP_CLASS = "dump_class";

	private PrintWriter pw = null;
	private int padding = 0;
	private Hints hints = null;

	public PrintTreeVisitor(PrintWriter pw) {
		this(pw, (Hints) null);
	}

	public PrintTreeVisitor(PrintWriter pw, Hints hints) {
		super();
		this.pw = pw;
		if (hints == null) {
			hints = new Hints();
		}
		this.hints = hints;
	}

	public PrintTreeVisitor(PrintWriter pw, String... hints) {
		this(pw, (Hints) null);

		for (String h : hints) {
			this.hints.setBooleanHint(h, true);
		}
	}

	@Override
	public void file(IMediaFile r, IProgressMonitor mon) {
		pw.print(padding());
		pw.printf("  %s", r.getTitle());
		if (hints.getBooleanValue(HINT_DUMP_CLASS, false)) {
			pw.printf(" [%s]", r.getClass().getName());
			if (r instanceof DecoratedMediaFile) {
				pw.printf(" [%s]", ((DecoratedMediaFile) r).getDecoratedItem().getClass().getName());
			}
		}
		pw.println();
	}

	@Override
	public void beforeFolder(IMediaFolder folder, IProgressMonitor monitor) {
		pw.print(padding());
		pw.printf("+ %s (%s)", folder.getTitle(), folder.getChildren().size());

		if (hints.getBooleanValue(HINT_DUMP_CLASS, false)) {
			pw.printf(" [%s]", folder.getClass().getName());
			if (folder instanceof DecoratedMediaFolder) {
				pw.printf(" [%s]", ((DecoratedMediaFolder) folder).getUndecoratedFolder().getClass().getName());
			}
		}

		pw.println(" {");
		padding += 3;
	}

	@Override
	public void afterFolder(IMediaFolder folder, IProgressMonitor monitor) {
		padding -= 3;
		pw.print(padding());
		pw.println("}");
		pw.flush();
	}

	private String padding() {
		return StringUtils.leftPad("", padding);
	}
}

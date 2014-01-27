package sagex.phoenix.vfs.visitors;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

/**
 * Updates the Genres to a fixed number of Genres.  It will also split out genres like,
 * "Action and Adventure" or "Action/Adventure" into 2 separate genres. 
 * @author seans
 */
public class FixGenresVisitor implements IMediaResourceVisitor {
	Pattern p1 = Pattern.compile("([^/]+)/(.*)"); 
	Pattern p2 = Pattern.compile("([a-z0-9]+)\\s+and\\s+([a-z0-9]+)", Pattern.CASE_INSENSITIVE);
	
	private int maxGenres = 2;
	
	public FixGenresVisitor() {
		this(2);
	}
	
	public FixGenresVisitor(int maxGenres) {
		this.maxGenres = maxGenres;
	}

	@Override
	public boolean visit(IMediaResource res, IProgressMonitor monitor) {
		if (res instanceof IMediaFile) {
			IMetadata md = ((IMediaFile)res).getMetadata();
			if (md!=null) {
				List<String> genres = md.getGenres();
				if (genres == null || genres.size()==0) {
					return true;
				}
				
				Set<String> newGenres = new TreeSet<String>();
				for (String s: genres) {
					fixGenres(newGenres, s);
				}
				
				// nothing changes, so, leave it alone
				if (newGenres.size() ==  genres.size()) {
					return true;
				}
				
				// copy new genres
				genres.clear();
				for (String s: newGenres) {
					genres.add(s);
					
					// break when we've filled it as per the user's maxGenres
					if (genres.size()>=maxGenres) break;
				}
			}
		}
		
		return true;
	}
	
	public void fixGenres(Set<String> list, String newGenre) {
		Matcher m = p1.matcher(newGenre);
		if (m.find()) {
			fixGenres(list, m.group(1).trim());
			fixGenres(list, m.group(2).trim());
			return;
		}
		
		
		m = p2.matcher(newGenre);
		if (m.find()) {
			fixGenres(list, m.group(1).trim());
			fixGenres(list, m.group(2).trim());
			return;
		}
		
		list.add(newGenre);
	}
}

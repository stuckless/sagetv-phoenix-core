package sagex.phoenix.vfs.groups;


/**
 * Groups a based on a regular expression against the title
 *  
 * @author sean
 */
public class RegexTitleGrouper extends RegexGrouper {
    public RegexTitleGrouper() {
    	super(new TitleGrouper());
    }
}

package sagex.phoenix.vfs.groups;

/**
 * Groups based on the first letter of the title
 *  
 * @author jusjoken
 */
public class FirstLetterTitleGrouper  extends FirstLetterTitleRegexGrouper {
    public FirstLetterTitleGrouper() {
    	super(new TitleGrouper());
    }    
}

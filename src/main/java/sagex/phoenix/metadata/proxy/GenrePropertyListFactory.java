package sagex.phoenix.metadata.proxy;

/**
 * List factory for Genres, since they can only store 2 items.
 *
 * @author seans
 */
public class GenrePropertyListFactory extends StringPropertyListFactory {
    public GenrePropertyListFactory() {
        super("/", "/;");
    }
}

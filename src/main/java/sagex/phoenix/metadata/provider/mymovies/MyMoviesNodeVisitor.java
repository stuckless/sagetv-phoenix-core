package sagex.phoenix.metadata.provider.mymovies;

import org.w3c.dom.Element;

public interface MyMoviesNodeVisitor {
    public void visitMovie(Element el);
}

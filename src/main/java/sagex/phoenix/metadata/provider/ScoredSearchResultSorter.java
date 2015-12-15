package sagex.phoenix.metadata.provider;

import sagex.phoenix.metadata.IMetadataSearchResult;

import java.util.Comparator;

public class ScoredSearchResultSorter implements Comparator<IMetadataSearchResult> {
    public static final ScoredSearchResultSorter INSTANCE = new ScoredSearchResultSorter();

    public ScoredSearchResultSorter() {
    }

    public int compare(IMetadataSearchResult o1, IMetadataSearchResult o2) {
        if (o1.getScore() > o2.getScore())
            return -1;
        if (o1.getScore() < o2.getScore())
            return 1;
        return 0;
    }
}

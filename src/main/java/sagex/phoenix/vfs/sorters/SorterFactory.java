package sagex.phoenix.vfs.sorters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaResource;

import java.util.Comparator;
import java.util.Set;

public class SorterFactory extends Factory<Sorter> {
    private Sorter sorter = null;

    public SorterFactory(String klass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this(((Class<Comparator<IMediaResource>>) Class.forName(klass)).newInstance());
    }

    public SorterFactory(Comparator<IMediaResource> comparator) {
        sorter = new Sorter(comparator);
    }

    public Sorter create(Set<ConfigurableOption> configurableOptions) {
        Sorter newSort;
        try {
            newSort = (Sorter) sorter.clone();
            newSort.setName(getName());
            newSort.setLabel(getLabel());
        } catch (CloneNotSupportedException e) {
            log.warn("Failedt to create new sorter!", e);
            return null;
        }

        // simply clone the original sorter and return it fully configured with
        // the new options
        newSort.configure(configurableOptions);
        newSort.setTags(getTags());
        return newSort;
    }
}

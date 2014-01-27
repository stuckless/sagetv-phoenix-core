package sagex.phoenix.vfs.filters;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;

public class FilterFactory extends Factory<Filter> implements Comparable<FilterFactory> {
    protected Filter resourceFilter = null;
    
    public FilterFactory() {
    }
    
    @SuppressWarnings("unchecked")
    public FilterFactory(String klass) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this(((Class<IResourceFilter>) Class.forName(klass)).newInstance());
    }
    
    public FilterFactory(IResourceFilter f) {
        if (f instanceof Filter) {
        	resourceFilter = (Filter) f;
        } else {
        	resourceFilter = new WrappedResourceFilter(f);
        }
    }
    
    public Filter create(Set<ConfigurableOption> configurableOptions) {
    	// just clone the master filter and add our custom options
    	Filter newFilter;
		try {
			newFilter = (Filter) resourceFilter.clone();
		} catch (CloneNotSupportedException e) {
			log.warn("Failed to create filter: " + resourceFilter);
			return null;
		}
		newFilter.setLabel(getLabel());
		newFilter.setFactoryid(getName());
		newFilter.configure(getOptions());
    	newFilter.configure(configurableOptions);
    	newFilter.setTags(getTags());
    	return newFilter;
    }

    /**
     * 2 Filter Factories are comparable if they share the name.
     */
	@Override
	public int compareTo(FilterFactory o) {
		String name = getName();
		if (name==null) return -1;
		return name.compareTo(o.getName());
	}
}

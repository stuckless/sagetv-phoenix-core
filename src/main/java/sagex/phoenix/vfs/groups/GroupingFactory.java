package sagex.phoenix.vfs.groups;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;

public class GroupingFactory extends Factory<Grouper> {
    private Grouper grouper = null;

    public GroupingFactory(String klass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    	this(((Class<IGrouper>) Class.forName(klass)).newInstance());
    }

    public GroupingFactory(IGrouper grouper) {
    	this.grouper = new Grouper(grouper);
	}

	public Grouper create(Set<ConfigurableOption> configurableOptions) {
		Grouper newGrouper;
		try {
			newGrouper = (Grouper) grouper.clone();
		} catch (CloneNotSupportedException e) {
			log.warn("Failed to create Grouper", e);
			return null;
		}
		newGrouper.setLabel(getLabel());
		newGrouper.setFactoryId(getName());
		newGrouper.configure(configurableOptions);
		newGrouper.setTags(getTags());
		return newGrouper;
    }
	
}

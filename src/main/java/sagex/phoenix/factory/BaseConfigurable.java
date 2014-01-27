package sagex.phoenix.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import sagex.phoenix.util.PublicCloneable;

/**
 * @author seans
 */
public class BaseConfigurable implements Iterable<ConfigurableOption>, IConfigurable, PublicCloneable, Cloneable {
    protected transient Logger log = Logger.getLogger(this.getClass());
    protected Set<ConfigurableOption> configurableOptions = new LinkedHashSet<ConfigurableOption>();
    protected boolean changed = false;
    protected Set<String> tags = new TreeSet<String>();
    
	public BaseConfigurable() {
		super();
	}

	public Set<ConfigurableOption> getOptions() {
		return configurableOptions;
	}

	public ConfigurableOption getOption(String name) {
		return findOption(name, configurableOptions);
	}
	
	public boolean hasOption(String name) {
		return findOption(name, configurableOptions)!=null;
	}

	public ConfigurableOption findOption(String name, Set<ConfigurableOption> opts) {
		if (name==null) return null;
		if (opts!=null) {
			for (ConfigurableOption o: opts) {
				if (name.equals(o.getName())) return o;
			}
		}
		return null;
	}

	public ConfigurableOption getOption(String name, Set<ConfigurableOption> altOptions) {
		ConfigurableOption co = null;
		if (altOptions!=null) {
			co = findOption(name, altOptions); 
		}
		if (co==null) {
			co = findOption(name, configurableOptions);
		}
		
		return co;
	}

	public void addOption(ConfigurableOption configurableOption) {
		ConfigurableOption co = getOption(configurableOption.getName());
		if (co!=null) {
			updateOption(configurableOption);
		} else {
			configurableOptions.add(configurableOption);
			setChanged(true);
		}
	}

	@Override
	public void updateOption(ConfigurableOption configurableOption) {
		ConfigurableOption co = getOption(configurableOption.getName());
		if (co==null) {
			addOption(configurableOption);
		} else {
			// if the option exists, update it's label and value
			// co.setLabel(configurableOption.getLabel());
			// co.value().setValue(configurableOption.value().getValue());
			co.updateFrom(configurableOption);
		}
		
		setChanged(true);
	}

	@Override
	public Iterator<ConfigurableOption> iterator() {
		return configurableOptions.iterator();
	}
	
	public List<String> getOptionNames() {
		List<String> l = new ArrayList<String>();
		for (ConfigurableOption co : configurableOptions) {
			l.add(co.getName());
		}
		return l;
	}
	
	public boolean removeOption(ConfigurableOption opt) {
		return configurableOptions.remove(opt);
	}

	@Override
	public void configure(Set<ConfigurableOption> opts) {
		if (opts==null) return;
		
		for (ConfigurableOption o: opts) {
			addOption(o);
		}
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public void clearChanged() {
		setChanged(false);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		BaseConfigurable c =  (BaseConfigurable) super.clone();
		c.configurableOptions=new LinkedHashSet<ConfigurableOption>();
		for (ConfigurableOption o: configurableOptions) {
			c.configurableOptions.add((ConfigurableOption) o.clone());
		}
		
		return c;
	}

	/**
	 * Subclasses should override this method to provide implementination for thier list
	 * items.  Should never return null.
	 */
	@Override
	public Map<String, String> getOptionList(String id) {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return "BaseConfigurable [changed=" + changed + ", " + (configurableOptions != null ? "configurableOptions=" + configurableOptions : "") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (changed ? 1231 : 1237);
		result = prime * result + ((configurableOptions == null) ? 0 : configurableOptions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseConfigurable other = (BaseConfigurable) obj;
		if (changed != other.changed)
			return false;
		if (configurableOptions == null) {
			if (other.configurableOptions != null)
				return false;
		} else if (!configurableOptions.equals(other.configurableOptions))
			return false;
		return true;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}
	
	public boolean HasTag(String tag) {
		return tag!=null && tags.contains(tag);
	}
}
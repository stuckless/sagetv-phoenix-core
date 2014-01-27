package sagex.phoenix.factory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IConfigurable {
	public ConfigurableOption getOption(String name);
	public void addOption(ConfigurableOption configurableOption);
	public void updateOption(ConfigurableOption configurableOption);
	public Iterator<ConfigurableOption> iterator();
	public List<String> getOptionNames();
	public boolean removeOption(ConfigurableOption opt);
	public void configure(Set<ConfigurableOption> opts);
	public boolean isChanged();
	public void setChanged(boolean changed);
	public void clearChanged();
	public Map<String,String> getOptionList(String id);
	public Set<String> getTags();
}

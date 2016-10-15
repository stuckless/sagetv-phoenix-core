package sagex.phoenix.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class FactoryRegistry<F extends Factory<?>> {
    protected Logger log = Logger.getLogger(this.getClass());
    private Map<String, F> factories = new LinkedHashMap<String, F>();

    private boolean sort = true;
    private String name;

    public FactoryRegistry(String name) {
        this.name = name;
    }

    public void addFactory(F factory) {
        if (factory==null) {
            log.warn("Attempted to add a NULL factory, ignoring");
            return;
        }
        F old = factories.put(factory.getName(), factory);
        if (old != null) {
            log.debug("Old Factory replaced by new factory: " + factory.getName());
        }
    }

    public F getFactory(String id) {
        return factories.get(id);
    }

    public F removeFactory(String id) {
        return factories.remove(id);
    }

    public String[] getFactoryIds() {
        return factories.keySet().toArray(new String[factories.size()]);
    }

    public List<F> getFactories() {
        return getFactories(false);
    }

    public List<F> getFactories(boolean includeInvisible) {

        List<F> all = new LinkedList<F>();

        for (F fact : factories.values()) {
            if (fact.isVisible() || includeInvisible) {
                all.add(fact);
            }
        }

        if (sort)
            Collections.sort(all, FactoryComparator.INSTANCE);
        return all;
    }

    public List<F> getFactoriesWithErrors() {

        List<F> all = new LinkedList<F>();

        for (F fact : factories.values()) {
            if (fact.hasErrors()) all.add(fact);
        }

        if (sort)
            Collections.sort(all, FactoryComparator.INSTANCE);
        return all;
    }

    public Set<F> getFactories(Set<String> tags, boolean includeInvisible) {
        Set<F> set = new TreeSet<F>((sort) ? FactoryComparator.INSTANCE : null);

        for (String s : tags) {
            for (F f : factories.values()) {
                //if ((f.isVisible() || includeInvisible) && (f.getTags().size() == 0 || f.hasTag(s))) {
                if ((f.isVisible() || includeInvisible) && (f.hasTag(s))) {
                    set.add(f);
                }
            }
        }

        return set;
    }

    public Set<F> getFactories(Set<String> tags) {
        return getFactories(tags, false);
    }

    public Set<F> getFactories(String taglist) {
        return getFactories(taglist, false);
    }

    public Set<F> getFactories(String taglist, boolean includeInvisible) {
        if (taglist == null) {
            return Collections.emptySet();
        }

        String tags[] = taglist.split("\\s*,\\s*");
        return getFactories(new TreeSet<String>(Arrays.asList(tags)), includeInvisible);
    }

    public Set<String> getTags() {
        Set<String> all = new TreeSet<String>();
        for (F t : factories.values()) {
            all.addAll(t.getTags());
        }
        return all;
    }

    public void clear() {
        factories.clear();
    }

    public void addAll(Collection<F> factories) {
        if (factories != null) {
            for (F f : factories) {
                addFactory(f);
            }
        }
    }

    @Override
    public String toString() {
        return "FactoryRegistry [name=" + name + "]";
    }
}

package sagex.phoenix.metadata.proxy;

/**
 * Simple List of String were each String is separated by a semi-colon by
 * default
 *
 * @author seans
 */
public class StringPropertyListFactory implements IPropertyListFactory {
    private String listItemSeparator;
    private String listSplitter;

    private int maxItems = Integer.MAX_VALUE;

    public StringPropertyListFactory() {
        this(";", ";");
    }

    public StringPropertyListFactory(String listItemSeparator) {
        this(listItemSeparator, listItemSeparator);
    }

    /**
     * @param listItemSeparator - character that is used to join list items
     * @param listItemSplitter  - character(s) used to split list items
     */
    public StringPropertyListFactory(String listItemSeparator, String listItemSplitter) {
        this.listItemSeparator = listItemSeparator;
        this.listSplitter = listItemSplitter;
    }

    @Override
    public Object decode(String item) {
        if (item == null)
            return null;
        return item;
    }

    @Override
    public String encode(Object item) {
        if (item == null)
            return "";
        return String.valueOf(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String fromList(PropertyList list) {
        StringBuilder sb = new StringBuilder();

        if (list != null) {
            // Some lists, such as genres, for ex, can only encode a fixed # of
            // items.
            int s = Math.min(list.size(), maxItems);
            for (int i = 0; i < s; i++) {
                sb.append(encode(list.get(i)));
                if (i + 1 < s)
                    sb.append(listItemSeparator);
            }
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public PropertyList toList(String data) {
        PropertyList plist = new PropertyList(this);
        if (data != null && data.length() > 0) {
            String items[] = data.split("\\s*[" + listSplitter + ",;]\\s*");
            for (String item : items) {
                plist.add(decode(item));
            }
        }
        return plist;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }
}

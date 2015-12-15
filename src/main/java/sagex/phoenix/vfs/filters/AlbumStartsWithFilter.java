package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaResource;

import java.util.Map;

public class AlbumStartsWithFilter extends Filter {
    boolean ignoreThe = false;
    boolean ignoreAll = false;

    public AlbumStartsWithFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "Album", null, DataType.string));
        addOption(new ConfigurableOption("ignore-the", "Disregard 'the' when sorting", "false", DataType.bool, true,
                ListSelection.single, "true:Yes,no:No"));
        addOption(new ConfigurableOption("ignore-all", "Disregard 'a', 'an', and 'the' when sorting", "false", DataType.bool, true,
                ListSelection.single, "true:Yes,no:No"));
    }

    @Override
    public boolean canAccept(IMediaResource res) {
        String album = getOption(OPT_VALUE).getString(null);
        if (album == null)
            return false;
        if (ignoreAll || ignoreThe) {
            return TitleStartsWithFilter.removeLeadingArticles(phoenix.music.GetName(res), ignoreAll).startsWith(album);
        } else {
            return phoenix.music.GetName(res).startsWith(album);
        }
    }

    @Override
    protected void onUpdate() {
        ignoreThe = getOption("ignore-the").getBoolean(false);
        ignoreAll = getOption("ignore-all").getBoolean(false);
    }

    @Override
    public Map<String, String> getOptionList(String id) {
        return super.getOptionList(id);
    }

}

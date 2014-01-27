package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.IMediaResource;

public class ArtistStartsWithFilter extends Filter {
    public ArtistStartsWithFilter() {
    	super();
    	addOption(new ConfigurableOption(OPT_VALUE, "Artist", null, DataType.string));
    }
    
    public boolean canAccept(IMediaResource res) {
        String artist = getOption(OPT_VALUE).getString(null);
        if (artist==null) return false;
        return phoenix.music.GetArtist(res).startsWith(artist);
    }
}

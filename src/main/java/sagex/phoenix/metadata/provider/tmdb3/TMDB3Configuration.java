package sagex.phoenix.metadata.provider.tmdb3;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "TheMovieDB 3 Configuration", path = "phoenix/metadata/tmdb3", description = "Configuration for The TheMovieDB3")
public class TMDB3Configuration extends GroupProxy {
    @AField(label = "Language", description = "2 letter language code (all lowercase)", scope = ConfigScope.SERVER)
    private FieldProxy<String> language = new FieldProxy<String>("en");

    @AField(label = "Country", description = "2 letter country code (all uppercase) (used mainly for MPAA rating code)", scope = ConfigScope.SERVER)
    private FieldProxy<String> country = new FieldProxy<String>("US");

    @AField(label = "Max Posters", description = "Maximum number of posters to fetch", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> maxPosters = new FieldProxy<Integer>(5);

    @AField(label = "Max Backgrounds", description = "Maximum number of backgrounds to fetch", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> maxBackgrounds = new FieldProxy<Integer>(5);

    // priority ratings
    public static final String PREFER_HIRES = "1";
    public static final String PREFER_USER_RATING = "2";

    @AField(label = "Fanart Ordering", description = "How fanart should be prioritized when selecting", scope = ConfigScope.SERVER, type = ConfigType.CHOICE, list = "1:Prefer Larger Size,2:Prefer Better User Rating")
    private FieldProxy<String> fanartPriorityOrdering = new FieldProxy<String>("1");

    @AField(label = "Include Adult Content", description = "If true, adult content will searched as well", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> includeAdult = new FieldProxy<Boolean>(false);

    public TMDB3Configuration() {
        super();
        init();
    }

    public String getLanguage() {
        return language.get();
    }

    public void setLanguage(String lang) {
        this.language.set(lang);
    }

    public String getCountry() {
        return country.get();
    }

    public void setCountry(String c) {
        this.country.set(c);
    }

    public int getMaxPosters() {
        return maxPosters.get();
    }

    public void setMaxPosters(int maxPosters) {
        this.maxPosters.set(maxPosters);
    }

    public int getMaxBackgrounds() {
        return maxBackgrounds.get();
    }

    public void setMaxBackgrounds(int maxBackgrounds) {
        this.maxBackgrounds.set(maxBackgrounds);
    }

    public String getFanartPriorityOrdering() {
        return fanartPriorityOrdering.get();
    }

    public void setFanartPriorityOrdering(String fanartPriorityOrdering) {
        this.fanartPriorityOrdering.set(fanartPriorityOrdering);
    }

    public boolean getIncludeAdult() {
        return includeAdult.get();
    }

    public void setIncludeAdult(boolean includeAdult) {
        this.includeAdult.set(includeAdult);
    }

}

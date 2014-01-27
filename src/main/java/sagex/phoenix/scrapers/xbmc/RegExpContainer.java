package sagex.phoenix.scrapers.xbmc;

public interface RegExpContainer {
    public void addRegExp(RegExp regexp);
    public RegExp[] getRegExps();
    public boolean hasRegExps();
}

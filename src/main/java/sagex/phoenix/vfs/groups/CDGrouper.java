package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDGrouper implements IGrouper {
    private Pattern pattern = Pattern.compile("[ _\\\\.-]+(cd|dvd|part|disc)[ _\\\\.-]*([0-9a-d]+)");

    public CDGrouper() {
    }

    public String getGroupName(IMediaResource res) {
        if (res instanceof IMediaFile) {
            String title = null;

            title = res.getTitle();
            if (title == null)
                return null;

            Matcher m = pattern.matcher(title);
            if (m.find()) {
                return title.substring(0, m.start());
            }

            return title;
        }
        return null;
    }

}

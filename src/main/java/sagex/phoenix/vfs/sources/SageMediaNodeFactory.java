package sagex.phoenix.vfs.sources;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import sagex.api.MediaNodeAPI;
import sagex.api.WidgetAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.sage.MediaNodeMediaFolder;

/**
 * Creates a MediaFolder from a given MediaNode Content String
 * 
 * @author seans
 *
 */
public class SageMediaNodeFactory extends Factory<IMediaFolder> {
    public SageMediaNodeFactory() {
        super();
        addOption(new ConfigurableOption("content", "Content", null, DataType.string, true, ListSelection.single, "Filesystem:File System,VideoNavigator:Videos,MusicNavigator:Music,MusicVideosNavigator:Music Videos,MoviesNavigator:Movies,TVNavigator:TV,VideosByFolder:Videos by Folder"));
        addOption(new ConfigurableOption("grouping", "Grouped By", null, DataType.string, true, ListSelection.single, "Folder,Genre,Year,Album,Director,Actor,Studio,Title,Series,Artist,Channel"));    
        addOption(new ConfigurableOption("subdir", "Relative Root", null, DataType.string));
        addOption(new ConfigurableOption("expression", "Data Expression", null, DataType.string));
    }

    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
        String content = getOption("content", configurableOptions).getString(null);
        String grouping = getOption("grouping", configurableOptions).getString(null);
        String subdir = getOption("subdir", configurableOptions).getString(null);
        String expression = getOption("expression", configurableOptions).getString(null);
        if (!StringUtils.isEmpty(subdir)) {
        	log.info("Creating Media Node source using relative subdir: " + subdir + "; Content is forced to Filesystem");
        	return new MediaNodeMediaFolder(null, MediaNodeAPI.GetRelativeMediaSource("Filesystem", subdir));
        } else if (!StringUtils.isEmpty(expression)) {
        	log.info("Creating Media Node source using expression: " + expression);
        	return new MediaNodeMediaFolder(null, MediaNodeAPI.GetMediaView(getLabel(), WidgetAPI.EvaluateExpression(expression)));
        }
        String source = getContentName(content, grouping);
        log.info("Creating Media Node source using " + source);
        return new MediaNodeMediaFolder(null, MediaNodeAPI.GetMediaSource(source));
    }

	private String getContentName(String content, String grouping) {
		if (StringUtils.isEmpty(content)) return "Filesystem";
		if (content.contains("By")) return content;
		if (StringUtils.isEmpty(grouping)) return content;
		return content + "By" + grouping;
	}
}

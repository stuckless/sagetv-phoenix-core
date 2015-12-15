package sagex.phoenix.vfs.sources;

import java.util.List;
import java.util.Set;

import sagex.api.AiringAPI;
import sagex.api.MediaFileAPI;
import sagex.api.WidgetAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.util.ElapsedTimer;
import sagex.phoenix.vfs.IMediaFolder;

public class SageExpressionSourceFactory extends Factory<IMediaFolder> {
    public SageExpressionSourceFactory() {
        this(null);
    }

    public SageExpressionSourceFactory(String expression) {
        super();
        addOption(new ConfigurableOption("expression", "Sage Expression", expression, DataType.string));
    }

    public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
        ElapsedTimer et = new ElapsedTimer();
        String expr = getOption("expression", configurableOptions).getString(null);
        log.info("Evaluating Expression for media items: " + expr);
        Object result = WidgetAPI.EvaluateExpression(expr);
        Object files[] = null;
        if (result != null) {
            if (result.getClass().isArray()) {
                files = (Object[]) result;
            } else if (result instanceof List) {
                files = ((List) result).toArray();
            } else if (AiringAPI.IsAiringObject(result) || MediaFileAPI.IsMediaFileObject(result)) {
                files = new Object[]{result};
            } else {
                log.warn("Expression " + expr + " returned an non array/list: " + result);
            }
        }
        log.info("Expression: " + expr + " resulted in " + (files == null ? "no" : files.length) + " files in " + et.delta() + "ms");
        return phoenix.umb.GetMediaAsFolder(files, getLabel());
    }
}

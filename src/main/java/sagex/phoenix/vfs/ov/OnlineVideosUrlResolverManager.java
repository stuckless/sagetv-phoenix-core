package sagex.phoenix.vfs.ov;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.common.SystemConfigurationFileManager.ConfigurationFileVisitor;
import sagex.phoenix.util.PhoenixManagedScriptEngineProxy;
import sagex.phoenix.vfs.ov.youtube.YoutubeUrlResolver;

public class OnlineVideosUrlResolverManager extends SystemConfigurationFileManager implements ConfigurationFileVisitor {

    private List<IUrlResolver> resolvers = new ArrayList<IUrlResolver>();

    public OnlineVideosUrlResolverManager(File systemDir, File userDir) {
        super(systemDir, userDir, new WildcardFileFilter(new String[]{"*.java", "*.groovy", "*.py"}, IOCase.INSENSITIVE));
    }

    @Override
    public void visitConfigurationFile(ConfigurationType type, File file) {
        log.info("Loaded URL Resolver: " + file);
        resolvers.add(PhoenixManagedScriptEngineProxy.newInstance(file, IUrlResolver.class));
    }

    @Override
    public void loadConfigurations() {
        log.info("Being Loading URL Resolvers");
        accept(this);
        resolvers.add(new YoutubeUrlResolver());
        log.info("End Loading URL Resolvers");
    }

    public List<IUrlResolver> getResolvers() {
        return resolvers;
    }
}

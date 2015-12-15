package sagex.phoenix.fanart;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.vfs.IMediaFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Any fanart lookup providers needs to implement this interface
 *
 * @author sean
 */
public interface IFanartSupport2 {
    /**
     * Friendly now for the fanart provider
     *
     * @return
     */
    public String getDisplayInformation();

    /**
     * Should return true if your provider is enabled
     *
     * @return
     */
    public boolean IsFanartEnabled();

    /**
     * You must disable your lookups when disabled
     *
     * @param value
     */
    public void SetIsFanartEnabled(boolean value);

    /**
     * Get the directory of where fanart is located for these options. You
     * should handle the possibility that ANY of the fields being passed may be
     * null.
     *
     * @param mf            {@link IMediaFile} that is being used (can be null if doing
     *                      genre or actor fanart)
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param metadata      any additional metadata used for the lookup, such as
     *                      SeasonNumber and EpisodeNumber
     * @return
     */
    public File GetFanartArtifactsDir(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                      String artifactTitle, Map<String, String> metadata);

    /**
     * Get all possible fanart artifacts for the given information. You should
     * handle the possibility that ANY of the fields being passed may be null.
     *
     * @param mf            {@link IMediaFile} that is being used (can be null if doing
     *                      genre or actor fanart)
     * @param mediaType
     * @param mediaTitle
     * @param artifactType
     * @param artifactTitle
     * @param metadata      any additional metadata used for the lookup, such as
     *                      SeasonNumber and EpisodeNumber
     * @return
     */
    public List<File> GetFanartArtifacts(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                         String artifactTitle, Map<String, String> metadata);
}

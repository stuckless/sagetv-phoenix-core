package sagex.phoenix.fanart;

import java.io.File;
import java.util.Map;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;

@Deprecated
public interface IFanartSupport {
	public File GetFanartArtifactDir(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata, boolean create);

	public File GetFanartArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata);

	public File[] GetFanartArtifacts(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata);

	public void SetFanartArtifact(Object mediaObject, File fanart, MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata);

	public boolean IsFanartEnabled();

	public void SetIsFanartEnabled(boolean value);

	public String GetFanartCentralFolder();

	public void SetFanartCentralFolder(String folder);
}

package sagex.phoenix.remote;

import java.lang.reflect.Type;
import java.util.List;

import sagex.api.AiringAPI;
import sagex.api.ChannelAPI;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.views.ViewFolder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MediaResourceSerializer implements JsonSerializer<IMediaResource> {
	public MediaResourceSerializer() {
	}

	@Override
	public JsonElement serialize(IMediaResource mf, Type arg1, JsonSerializationContext ctx) {
		RemoteContext rem = RemoteContext.get();

		JsonObject root = new JsonObject();
		root.addProperty("title", mf.getTitle());
		root.addProperty("id", mf.getId());
		root.addProperty("path", mf.getPath());

		if (mf instanceof ViewFolder) {
			root.addProperty("view", ((ViewFolder) mf).getViewFactory().getName());
			root.addProperty("level", ((ViewFolder) mf).getPresentationLevel());
		}

		if (mf instanceof IMediaFolder) {
			if (mf.isType(MediaResourceType.ONLINE.value())) {
				// don't serialize online videos
				root.addProperty("children.size", -1);
			} else {
				root.addProperty("children.size", ((IMediaFolder) mf).getChildren().size());
				if (rem.getSerializeDepth() <= 0) {
					// do everything
					root.add("children", ctx.serialize(getChildren(((IMediaFolder) mf).getChildren(), rem)));
				} else {
					Integer i = rem.getData("depth");
					if (i == null) {
						i = 0;
					}
					rem.setData("depth", ++i);
					if (i <= rem.getSerializeDepth()) {
						root.add("children", ctx.serialize(getChildren(((IMediaFolder) mf).getChildren(), rem)));
					} else {
						// set an empty array to denote children, but we are not
						// going to serialize them
						root.add("children", new JsonArray());
					}
				}
			}
		} else {
			// media item
			IMediaFile f = (IMediaFile) mf;
			IMetadata md = f.getMetadata();
			// File file = PathUtils.getFirstFile(f);
			// root.addProperty("file",
			// (file==null)?null:file.getAbsolutePath());

			if (f.isType(MediaResourceType.TV.value())) {
				root.addProperty("isTV", true);
				root.addProperty("season", md.getSeasonNumber());
				root.addProperty("episode", md.getEpisodeNumber());
				root.addProperty("episodeName", md.getEpisodeName());
				root.addProperty("airingTime", f.getStartTime());
				root.addProperty("isDontLike", f.isDontLike());
			}

			root.addProperty("watched", mf.isWatched());

			if (md.getAiringTime() != null) {
				root.addProperty("airingTime", md.getAiringTime().getTime());
			}

			if (mf.isType(MediaResourceType.RECORDING.value())) {
				root.addProperty("isRecording", true);
				root.addProperty("airingid", AiringAPI.GetAiringID(mf.getMediaObject()));
			}

			root.addProperty("description", md.getDescription());

			if (f.isType(MediaResourceType.EPG_AIRING.value())) {
				root.addProperty("isEPG", true);
				Object channel = AiringAPI.GetChannel(mf.getMediaObject());
				root.addProperty("channelNumber", ChannelAPI.GetChannelNumber(channel));
				root.addProperty("channelName", ChannelAPI.GetChannelName(channel));
				root.addProperty("isDontLike", f.isDontLike());
				root.addProperty("isFavorite", AiringAPI.IsFavorite(mf.getMediaObject()));
				root.addProperty("isManualRecord", AiringAPI.IsManualRecord(mf.getMediaObject()));
				root.addProperty("airingid", AiringAPI.GetAiringID(mf.getMediaObject()));
			}

			if (f.isType(MediaResourceType.ONLINE.value())) {
				root.addProperty("isONLINE", true);
			}

			if (f.isType(MediaResourceType.MUSIC.value())) {
				root.addProperty("isMUSIC", true);
			}

			if (f.isType(MediaResourceType.PICTURE.value())) {
				root.addProperty("isPICTURE", true);
			}

			if (f.isType(MediaResourceType.ANY_VIDEO.value())) {
				root.addProperty("isVIDEO", true);
				root.addProperty("runtime", md.getRunningTime());
				root.addProperty("year", md.getYear());
			}

			root.addProperty("mediatype", md.getMediaType());
			root.addProperty("imdbid", md.getIMDBID());
		}

		return root;
	}

	private Object getChildren(List<IMediaResource> children, RemoteContext rem) {
		if (Boolean.TRUE.equals(rem.getData("useranges"))) {
			Loggers.LOG.debug("REMOTEAPI: Using Ranges");
			rem.setData("useranges", false);
			int size = children.size() - 1;
			return children.subList(Math.min(size, (Integer) rem.getData("start")), Math.min(size, (Integer) rem.getData("end")));
		} else {
			Loggers.LOG.debug("REMOTEAPI: Range not set");
			return children;
		}
	}
}

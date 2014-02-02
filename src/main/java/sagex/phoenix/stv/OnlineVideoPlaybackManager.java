package sagex.phoenix.stv;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.phoenix.event.PhoenixEvent;
import sagex.phoenix.vfs.IMediaFile;
import sagex.plugin.SageEvents;

public class OnlineVideoPlaybackManager {

	/*
	 * THIS IS FOR MY OWN REFERENCE Keyboard shortcuts... F5 = Page Up F6 = Page
	 * Down F7 = Page Left F8 = Page Right {Up} = Up {Down} = Down {Left} = Left
	 * {Right}=Right {Home} = Home/Main Menu {PageUp} = Channel Up/Page Up
	 * {PageDown} = Channel Down/Page Down {ctrl} left arrow = Left/Volume Down
	 * {ctrl} right arrow = Right/Volume Up {ctrl} up arrow = Up/Channel Up
	 * {ctrl} down arrow = Down/Channel Down {ctrl} A Skip Bkwd/Page Left {ctrl}
	 * D Play {ctrl} E Volume Down {ctrl} F = Skip Fwd/Page Right {ctrl} G =
	 * Time Scroll {ctrl} I = Info {ctrl} J = Don't Like {ctrl} K = Favorite
	 * {ctrl} M = Play Faster {ctrl} N = Play Slower {ctrl} O = Options {ctrl} R
	 * = Volume Up {ctrl} S = Pause {ctrl} V = TV {ctrl} w = Watched {ctrl} X =
	 * Guide {ctrl} Y = Record {ctrl} Z = Power {ctrl}{shift} F = Full
	 * Screen/Windows Screen {ctrl}{shift} S = Play/Pause {ctrl}{shift} M = Mute
	 * {pause] = Pause {alt}F4 = Exit/Close Program
	 * 
	 * 
	 * CTRL+G seems to do a STOP
	 */

	private static final String UICONTEXT_ARG = "UIContext";
	private static final String Duration_ARG = "Duration";
	private static final String MEDIA_TIME_ARG = "MediaTime";

	private Logger log = Logger.getLogger(OnlineVideoPlaybackManager.class);

	private Map<String, OnlineVideoPlayer> players = new HashMap<String, OnlineVideoPlayer>();

	public OnlineVideoPlaybackManager() {
	}

	public void addPlayer(OnlineVideoPlayer player) {
		String ctx = player.getUIContext();
		destroyPlayer(ctx);
		log.info("Adding OnlinePlayer " + player);
		players.put(ctx, player);
	}

	protected void destroyPlayer(String ctx) {
		OnlineVideoPlayer oldPlayer = players.get(ctx);
		if (oldPlayer != null) {
			log.info("Destroying Player " + oldPlayer);
			oldPlayer.destroy();
			removePlayer(oldPlayer);
		}
	}

	@PhoenixEvent(SageEvents.PlaybackStopped)
	public void onPlaybackStopped(Map args) {
		String ctx = (String) args.get(UICONTEXT_ARG);
		OnlineVideoPlayer player = players.get(ctx);
		log.info("Playback Stopped for " + player);
		if (player != null) {
			setWatched(player.getMediaFile(), (Long) args.get(Duration_ARG), (Long) args.get(MEDIA_TIME_ARG));
			destroyPlayer(ctx);
		}
	}

	protected void setWatched(IMediaFile mediaFile, Long duration, Long currentTime) {
		log.info("SetWatch NOT Implemented! File: " + mediaFile.getTitle() + "; Dur: " + duration + "; CurTime: " + currentTime);
	}

	@PhoenixEvent(SageEvents.PlaybackFinished)
	public void onPlaybackFinished(Map args) {
		String ctx = (String) args.get(UICONTEXT_ARG);
		OnlineVideoPlayer player = players.get(ctx);
		log.info("Playback Finished for " + player);
		if (player != null) {
			setWatched(player.getMediaFile(), (Long) args.get(Duration_ARG), (Long) args.get(MEDIA_TIME_ARG));
			destroyPlayer(ctx);
		}
	}

	public void removePlayer(OnlineVideoPlayer onlineVideoPlayer) {
		if (onlineVideoPlayer == null)
			return;
		if (players.containsKey(onlineVideoPlayer.getUIContext())) {
			log.info("Removing online video player " + onlineVideoPlayer);
			players.remove(onlineVideoPlayer.getUIContext());
		}
	}
}

package sagex.phoenix.metadata;

import sagex.phoenix.metadata.proxy.SageProperty;

/**
 * <pre>
 * 7. Added support for the following properties which are automatically determined by the core if not defined when using the GetMediaFileMetadata API call: 
 * Format.Video.Height, 
 * Format.Video.Width, 
 * Format.Video.FPS, 
 * Format.Video.Interlaced, 
 * Format.Video.Progressive, 
 * Format.Audio.NumStreams, 
 * Format.Audio.[StreamNum].XXX (where StreamNum < NumStreams and XXX is one of the Format.Audio.XXX properties), 
 * Format.Subtitle.NumStreams, 
 * Format.Subtitle.[StreamNum].XXX (where StreamNum < NumStreams and XXX is one of the Format.Subtitle.XXX properties), 
 * Format.Subtitle.Codec and Format.Container.
 * 17. Added support for the following metadata values (case-insensitive) when using GetMediaFileMetadata if the values are not defined by an import plugin: 
 * Format.Video.Codec, 
 * Format.Video.Resolution, 
 * Format.Video.Aspect, 
 * Format.Audio.Codec, 
 * Format.Audio.Channels, 
 * Format.Audio.Language, 
 * Format.Audio.SampleRate, 
 * Format.Audio.BitsPerSample, 
 * Format.Audio.Bitrate (in kbps), 
 * Format.Subtitle.Language, 
 * Format.Video.Bitrate (in Mbps)
 * </pre>
 * @author seans
 *
 */
public interface ISageFormatPropertyRO extends ISageMetadata {
    @SageProperty("Format.Video.Height")
    public String getFormatVideoHeight();
    
    @SageProperty("Format.Video.Width")
    public String getFormatVideoWidth();
    
    @SageProperty("Format.Video.FPS")
    public String getFormatVideoFPS();
    
    @SageProperty("Format.Video.Interlaced")
    public String getFormatVideoInterlaced();
    
    @SageProperty("Format.Video.Progressive")
    public String getFormatVideoProgressive();
    
    @SageProperty("Format.Audio.NumStreams")
    public int getFormatAudioNumStreams();
    
    // (where StreamNum < NumStreams and XXX is one of the Format.Audio.XXX properties)
    @SageProperty(value="Format.Audio.[{0}].{1}", format=true)
    public String getFormatAudioStreamNumProperty(int streamNum, String prop);
    
    @SageProperty("Format.Subtitle.NumStreams")
    public int FormatSubtitleNumStreams();
    
    //  (where StreamNum < NumStreams and XXX is one of the Format.Subtitle.XXX properties),
    @SageProperty(value="Format.Subtitle.[{0}].{1}", format=true)
    public String getFormatSubtitleStreamNumPropery();
    
    @SageProperty("Format.Subtitle.Codec")
    public String getFormatSubtitleCodec();
    
    @SageProperty("Format.Container")
    public String getFormatContainer();
    
    @SageProperty("Format.Video.Codec")
    public String getFormatVideoCodec();
    
    @SageProperty("Format.Video.Resolution")
    public String getFormatVideoResolution();
    
    @SageProperty("Format.Video.Aspect")
    public String getFormatVideoAspect();
    
    @SageProperty("Format.Audio.Codec")
    public String getFormatAudioCodec();
    
    @SageProperty("Format.Audio.Channels")
    public String FormatAudioChannels();
    
    @SageProperty("Format.Audio.Language")
    public String getFormatAudioLanguage();
    
    @SageProperty("Format.Audio.SampleRate")
    public String getFormatAudioSampleRate();
    
    @SageProperty("Format.Audio.BitsPerSample")
    public String getFormatAudioBitsPerSample();
    
    @SageProperty("Format.Audio.Bitrate") // (in kbps),
    public String getFormatAudioBitrateKbps();
    
    @SageProperty("Format.Subtitle.Language")
    public String getFormatSubtitleLanguage();
    
    @SageProperty("Format.Video.Bitrate") // (in Mbps)
    public String getFormatVideoBitrateMbps();
}

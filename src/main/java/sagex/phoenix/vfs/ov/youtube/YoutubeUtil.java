package sagex.phoenix.vfs.ov.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.util.url.UrlUtil;

public class YoutubeUtil {
    private static final Logger log = Logger.getLogger(YoutubeUtil.class);

    public static Map<String, String> mappedUrls = new HashMap<String, String>();

    public static Pattern idpat = Pattern.compile("v=([^&]+)");

    public static List<String> getYoutubeVideoURLs(String url) throws IOException {
        String id = parseYoutubeId(url);

        log.info("resolving youtube video url " + url);
        String lineStart = "fmt_url_map=";
        String linkStart = "fmt_url_map=";
        String linkEnd = "&";

        String tstring = null;
        BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        try {
            String curLine = null;
            int playerswfidx = 0;
            while ((curLine = r.readLine()) != null) {
                playerswfidx = curLine.indexOf(lineStart);
                if (playerswfidx != -1) {
                    curLine = curLine.substring(playerswfidx);
                    playerswfidx = curLine.indexOf(linkStart);
                    curLine = curLine.substring(playerswfidx + linkStart.length());
                    tstring = curLine.substring(0, curLine.indexOf(linkEnd));
                    break;
                }
            }
        } finally {
            if (r != null) {
                IOUtils.closeQuietly(r);
            }
        }

        if (StringUtils.isEmpty(tstring)) {
            log.warn("unable to resolve video url for " + url);
            return null;
        }

        ArrayList<String> urls = new ArrayList<String>();
        String targs = URLDecoder.decode(tstring, "UTF-8");
        String fmts[] = targs.split(",");
        if (fmts != null && fmts.length > 0) {
            for (String fmt : fmts) {
                String f[] = fmt.split("\\|");
                int fmtid = NumberUtils.toInt(f[0], -1);
                if (fmtid >= 0 && fmtid <= 22) {
                    // we are skipping HD formats for now...
                    urls.add(f[1]);
                }
            }

            if (urls.size() > 0) {
                // add in default
                urls.add("http://youtube.com/get_video?video_id=" + id + "&t=" + tstring);
            }
        }

        if (urls.size() == 0) {
            log.info("Could not autodetect formats, using fixed list for " + url);
            for (String fmt : new String[]{"22", "18", ""}) {
                StringBuilder newurl = new StringBuilder("http://youtube.com/get_video?video_id=" + id + "&t=" + tstring);
                if (!StringUtils.isEmpty(fmt)) {
                    newurl.append("&fmt=" + fmt);
                }
                urls.add(newurl.toString());
            }
        }

        return urls;
    }

    public static String parseYoutubeId(String url) {
        Matcher m = idpat.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Map<String, String> parseArgs(String url) {
        String parts[] = url.split("&");

        Map<String, String> args = new HashMap<String, String>();
        for (String s : parts) {
            String nvp[] = s.split("=");
            if (nvp.length == 2) {
                try {
                    args.put(nvp[0], URLDecoder.decode(nvp[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                args.put(nvp[0], "");
            }
        }
        return args;
    }

    public static String getYoutubeVideoURLByInfo(String url) throws IOException {
        Matcher m = idpat.matcher(url);
        if (m.find()) {
            String id = m.group(1);
            String info = "http://www.youtube.com/get_video_info?video_id=" + URLEncoder.encode(id, "UTF-8");
            URL u = new URL(info);
            String data = IOUtils.toString(u.openStream(), "UTF-8");
            String parts[] = data.split("&");

            Map<String, String> args = new HashMap<String, String>();
            for (String s : parts) {
                String nvp[] = s.split("=");
                if (nvp.length == 2) {
                    args.put(nvp[0], URLDecoder.decode(nvp[1], "UTF-8"));
                } else {
                    args.put(nvp[0], "");
                }
            }

            Map<String, String> vargs = new HashMap<String, String>();
            vargs.put("video_id", id);
            vargs.put("t", args.get("token"));
            vargs.put("fmt", "18");
            String videourl = UrlUtil.buildURL("http://youtube.com/get_video", vargs);
            log.info("Youtube Url: " + videourl);
            return videourl;
        }
        return null;
    }

    /**
     *
     * status=ok&fexp=903311&watermark=http%3A%2F%2Fs.ytimg.com%2Fyt%2Fswf%2F
     * logo
     * -vfl_bP6ud.swf%2Chttp%3A%2F%2Fs.ytimg.com%2Fyt%2Fswf%2Fhdlogo-vfloR6wva
     * .swf&timestamp=1299351849&has_cc=False&allow_embed=1&fmt_stream_map=34%7
     * Chttp
     * %3A%2F%2Fv1.lscache2.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252
     * Cexpire
     * %252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc
     * %253AU0dYTlNSVF9FSkNNOF9LTFhJ
     * %26fexp%3D903311%26algorithm%3Dthrottle-factor
     * %26itag%3D34%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D77634
     * BED84E569D0C20E95FA4852453661AE0FBD
     * .7478865680BF09244FA43A0D947CE4B02FEC3ADE%
     * 26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7%7C%7Ctc.v1.cache2.c.youtube.com%2C18%7Chttp%3A%2F%2Fv18.lscache7.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252Cexpire%252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc%253AU0dYTlNSVF9FSkNNOF9LTFhJ%26fexp%3D903311%26algorithm%3Dthrottle-factor%26itag%3D18%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D7B5223800CB84AF64506CBC348F8DDE8DBF0F416.488BF1A6C128409D2EF599BCC80853AAB9EE4CAA%26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7%7C%7Ctc.v18.cache7.c.youtube.com%2C5%7Chttp%3A%2F%2Fv14.lscache6.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252Cexpire%252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc%253AU0dYTlNSVF9FSkNNOF9LTFhJ%26fexp%3D903311%26algorithm%3Dthrottle-factor%26itag%3D5%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D46BB0893C1B1A0EBD57D9DFE6E412C4E1C66346C.91DDD8D71A4CF64388C9532895E9BDD63F7856FB%26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7%7C%7Ctc.v14.cache6.c.youtube.com&fmt_url_map=34%7Chttp%3A%2F%2Fv1.lscache2.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252Cexpire%252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc%253AU0dYTlNSVF9FSkNNOF9LTFhJ%26fexp%3D903311%26algorithm%3Dthrottle-factor%26itag%3D34%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D77634BED84E569D0C20E95FA4852453661AE0FBD.7478865680BF09244FA43A0D947CE4B02FEC3ADE%26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7%2C18%7Chttp%3A%2F%2Fv18.lscache7.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252Cexpire%252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc%253AU0dYTlNSVF9FSkNNOF9LTFhJ%26fexp%3D903311%26algorithm%3Dthrottle-factor%26itag%3D18%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D7B5223800CB84AF64506CBC348F8DDE8DBF0F416.488BF1A6C128409D2EF599BCC80853AAB9EE4CAA%26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7%2C5%7Chttp%3A%2F%2Fv14.lscache6.c.youtube.com%2Fvideoplayback%3Fsparams%3Did%252Cexpire%252Cip%252Cipbits%252Citag%252Calgorithm%252Cburst%252Cfactor%252Coc%253AU0dYTlNSVF9FSkNNOF9LTFhJ%26fexp%3D903311%26algorithm%3Dthrottle-factor%26itag%3D5%26ipbits%3D0%26burst%3D40%26sver%3D3%26signature%3D46BB0893C1B1A0EBD57D9DFE6E412C4E1C66346C.91DDD8D71A4CF64388C9532895E9BDD63F7856FB%26expire%3D1299376800%26key%3Dyt1%26ip%3D0.0.0.0%26factor%3D1.25%26id%3D8cdd275a3bf378d7&leanback_module=http%3A%2F%2Fs.ytimg.com%2Fyt%2Fswfbin%2Fleanback_module-vflt3sFnU.swf&allow_ratings=1&hl=en_US&tmi=1&keywords=Crimpeadora%2CPatch%2CCord%2CRJ45%2CRJ11%2CRj9%2Cequipo%2Cinform%C3%A1tico%2Celectr%C3%B3nica%2Carmado%2Cde%2Ccable%2Cred%2Cpatch%2Ccord&track_embed=0&endscreen_module=http%3A%2F%2Fs.ytimg.com%2Fyt%2Fswfbin%2Fendscreen-vflis_pza.swf&fmt_list=34%2F640x360%2F9%2F0%2F115%2C18%2F640x360%2F9%2F0%2F115%2C5%2F320x240%2F7%2F0%2F0&author=lorenzotools&muted=0&avg_rating=5.0&video_id=jN0nWjvzeNc&length_seconds=133&fmt_map=34%2F640x360%2F9%2F0%2F115%2C18%2F640x360%2F9%2F0%2F115%2C5%2F320x240%2F7%2F0%2F0&vq=auto&token=vjVQa1PpcFOvZwplKyxcgJj40FbM1KcVl1EP0Ywo784%3D&thumbnail_url=http%3A%2F%2Fi3.ytimg.com%2Fvi%2FjN0nWjvzeNc%2Fdefault.jpg&plid=AASdwO1wKAj1cBiw&title=Armado+de+Cable+de+Red+con+Crimpeadora+RJ45+RJ11+Rj9&ftoken
     * =
     *
     */

    /**
     * Pattern idpat = Pattern.compile("v=([^&]+)"); Matcher m =
     * idpat.matcher(pu.getUrl()); String id=null; if (m.find()) {
     * id=m.group(1); }
     */

    /**
     * <Action Name="tstring = null" Sym="BASE-82729"> <Action Name=
     * "&quot;REM Download the enclosure and get the flash video URL&quot;"
     * Sym="BASE-82730"> <Action Name=
     * "PageURL = new_java_net_URL(&quot;http://www.youtube.com/watch?v=&quot; + VideoID)"
     * Sym="BASE-82731"> <Action
     * Name="DebugLog(&quot;PageURL=[&quot; + PageURL + &quot;]&quot; )"
     * Sym="OPUS4-106627"/> <Action Name="CopyText = PageURL + &quot;&quot;"
     * Sym="BASE-82732"> <Action Name=
     * "&quot;REM Copy CopyText to clipboard if gCopyTextToClipboard is true&quot;"
     * Sym="BASE-82733"> <Conditional Name="gCopyTextToClipboard"
     * Sym="BASE-82734"> <Action Name=
     * "CopyClipboard = java_awt_Toolkit_getSystemClipboard(java_awt_Toolkit_getDefaultToolkit())"
     * Sym="BASE-82735"> <Action Name=
     * "TextTransfer = new_java_awt_datatransfer_StringSelection(CopyText)"
     * Sym="BASE-82736"> <Action Name=
     * "java_awt_datatransfer_Clipboard_setContents(CopyClipboard, TextTransfer, TextTransfer)"
     * Sym="BASE-82737"/> </Action> </Action> </Conditional> </Action> </Action>
     * <Action Name=
     * "URLReader = new_java_io_BufferedReader(new_java_io_InputStreamReader(java_net_URL_openStream(PageURL)))"
     * Sym="BASE-82738"> <Action ID="59613"
     * Name="CurrLine = java_io_BufferedReader_readLine(URLReader)"
     * Sym="BASE-82739"> <Conditional Name="CurrLine != null" Sym="BASE-82740">
     * <Action
     * Name="playerswfidx = StringIndexOf(CurrLine, VideoFinderLineStart)"
     * Sym="BASE-82741"> <Conditional Name="playerswfidx != -1"
     * Sym="BASE-82742"> <Branch Name="true" Sym="BASE-82743"> <Action Name=
     * "DebugLog(&quot;YouTube: found VideoFinderLineStart of &lt;&quot; + VideoFinderLineStart + &quot;&gt;&quot; )"
     * Sym="OPUS4A-179700"> <Value>" + PageURL + "</Value> </Action> <Action
     * Name="CurrLine = Substring(CurrLine, playerswfidx, -1)" Sym="BASE-82744">
     * <Action
     * Name="playerswfidx = StringIndexOf(CurrLine, VideoFinderLinkStart)"
     * Sym="BASE-82745"> <Action Name=
     * "CurrLine = Substring(CurrLine, playerswfidx + Size(VideoFinderLinkStart), -1)"
     * Sym="BASE-82746"> <Action Name=
     * "tstring = Substring(CurrLine, 0, StringIndexOf(CurrLine, VideoFinderLinkEnd))"
     * Sym="BASE-82747"> <Action Name=
     * "DebugLog(&quot;YouTube tstring=&lt;&quot; + tstring + &quot;&gt;&quot; )"
     * Sym="OPUS4A-179699"> <Value>" + PageURL + "</Value> </Action> </Action>
     * </Action> </Action> </Action>
     */

    // http://youtube.com/get_video?video_id=&quot; + VideoID +
    // &quot;&amp;t=&quot; + tstring + &quot;&amp;fmt=22&quot;
}

package phoenix.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.SageAPI;
import sagex.UIContext;
import sagex.api.*;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.stv.Toaster;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.PhoenixDiagnostics;
import sagex.phoenix.vfs.IMediaFile;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * General Purpose Utility Methods
 *
 * @author seans
 */
@API(group = "util")
public class UtilAPI {
    private static Random random = new Random();
    private static final Logger log = Logger.getLogger(UtilAPI.class);

    /**
     * Create a Random number less than max
     *
     * @param max ceiling for the random #
     * @return random number less than max
     */
    public int GetRandomNumber(int max) {
        return random.nextInt(max);
    }

    /**
     * Return a random element from the array.
     *
     * @param array Object array
     * @return random element
     */
    public Object GetRandomElement(Object[] array) {
        if (array == null || array.length == 0)
            return null;
        return array[GetRandomNumber(array.length)];
    }

    /**
     * Take an Object array or List and Shuffle it.
     *
     * @param in
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object Shuffle(Object objects) {
        if (objects == null)
            return null;
        if (objects.getClass().isArray()) {
            Object[] in = (Object[]) objects;
            for (int i = 0; i < in.length; i++) {
                int randomPosition = new Random().nextInt(in.length);
                Object temp = in[i];
                in[i] = in[randomPosition];
                in[randomPosition] = temp;
            }
        } else if (objects instanceof List) {
            Collections.shuffle((List) objects);
        } else {
            log.error("Can't Shuffle Object Type: " + objects.getClass().getName());
        }
        return objects;
    }

    /**
     * return a sub section of the passed array or list.
     * <p/>
     * if the length is passed as -1 then all elements beginning at start are
     * passed back.
     *
     * @param array  Array or List
     * @param start  0 based starting element
     * @param length # of elements to return (or -1 for all remaining elements)
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object SubArray(Object array, int start, int length) {
        if (array == null)
            return null;
        try {
            if (array.getClass().isArray()) {
                return subList(Arrays.asList((Object[]) array), start, length).toArray();
            } else if (array instanceof List) {
                return subList((List) array, start, length);
            } else {
                log.error("SubArray(): Unknown array type: " + array);
            }
        } catch (Exception e) {
            log.error("SubArray(): Failed!", e);
        }
        return array;
    }

    /**
     * Calls SubArray(array, start, -1)
     *
     * @param array
     * @param start
     * @return
     */
    public Object SubArray(Object array, int start) {
        return SubArray(array, start, -1);
    }

    @SuppressWarnings("unchecked")
    private List subList(List l, int start, int length) {
        if (length == -1) {
            length = l.size();
        }

        int end = Math.min(start + length, l.size());
        start = Math.min(start, (end));
        return l.subList(start, end);
    }

    /**
     * Runs some tests on the Phoenix subsystems, creates report for an end user
     * to see if there are issues with their system.
     * <p/>
     * if reportType == quick, then it will do a simple configuration check,
     * testing versions, etc. <br/>
     * if reportType == detailed, then it will run the report in a background
     * thread and perform a test on every media file in your library. The report
     * will be written to a file. <br/>
     * if reportType is a MediaFile then it will a single report on the
     *
     * @param quick Just perform a quick check, usually takes less than a second.
     * @return report or report status
     */
    public String RunPhoenixDiagnostics(String report) {
        try {
            PhoenixDiagnostics pd = new PhoenixDiagnostics();
            return pd.report(report);
        } catch (Exception e) {
            return "Failed to run report.";
        }
    }

    /**
     * Can test if the first version is at least equal to or greater than the
     * second version.
     *
     * @param verToTest   first version
     * @param baseVersion second version
     * @return true if verToTest is >= baseVersion
     */
    public boolean IsAtLeastVersion(String verToTest, String baseVersion) {
        if (verToTest == null || baseVersion == null)
            return false;
        if (verToTest.equals(baseVersion))
            return true;

        verToTest = verToTest.replaceAll("-", "\\.");
        baseVersion = baseVersion.replaceAll("-", "\\.");

        String v1[] = verToTest.split("\\.");
        String v2[] = baseVersion.split("\\.");

        for (int i = 0; i < v1.length && i < v2.length; i++) {
            int n1 = NumberUtils.toInt(v1[i], -1);
            int n2 = NumberUtils.toInt(v2[i], -1);
            if (n1 > n2)
                return true;
            if (n1 < n2)
                return false;
        }

        if (v1.length > v2.length)
            return true;

        return false;
    }

    /**
     * Removes the Metadata From a Library or TV Show in the Sage System. The
     * actual process of removing it's metadata involves removing/deleting the
     * file and then re-adding it.
     *
     * @param mediaFile
     * @return New Sage MediaFile Object if sucessful, or null, if it failed.
     */
    public Object RemoveMetadataFromMediaFile(Object mediaFile) {
        log.warn("Remove Metadata Requested for File: " + mediaFile);
        Object mf = null;

        if (mediaFile instanceof IMediaFile) {
            mf = phoenix.media.GetSageMediaFile(mediaFile);
        } else if (MediaFileAPI.IsMediaFileObject(mediaFile)) {
            mf = mediaFile;
        } else if (AiringAPI.IsAiringObject(mediaFile)) {
            mf = AiringAPI.GetMediaFileForAiring(mediaFile);
        }

        if (mf == null) {
            log.error("No MediaFile for mediafile: " + mediaFile);
            return null;
        }

        String relPath = MediaFileAPI.GetMediaFileRelativePath(mf);

        int oldID = MediaFileAPI.GetMediaFileID(mf);

        File f[] = MediaFileAPI.GetSegmentFiles(mf);
        if (f.length > 1) {
            log.error("MediaFile has more than 1 File Segments; " + mf);
            return null;
        }

        File cur = f[0];
        File tmp = new File(cur.getParentFile(), cur.getName() + ".tmp");
        log.info("Removing Metadata from file: " + cur);
        log.info("Renaming file to " + tmp);
        boolean rename = Utility.RenameFilePath(cur, tmp);
        if (!rename) {
            log.error("Failed to rename mediafile: " + cur.getAbsolutePath() + "; to tmp file: " + tmp.getAbsolutePath());
            return null;
        }

        // file is renamed, now let's delete it....
        log.info("Deleting file to remove metadata for " + cur);
        if (!MediaFileAPI.DeleteFileWithoutPrejudice(mf)) {
            log.error("Failed to delete mediafile: " + mf);
            if (!Utility.RenameFilePath(tmp, cur)) {
                log.error("Failed to rename the failed mediafile back to it's original filename: " + cur.getAbsolutePath()
                        + "; Currently Named file is: " + tmp.getAbsolutePath());
            }
            return null;
        }

        if (cur.exists()) {
            log.warn("Current file still exists, rename must have failed for " + cur);
        }

        // original is deleted, now lets rename the old back
        if (!Utility.RenameFilePath(tmp, cur)) {
            log.error("Failed to rename the failed mediafile back to it's original filename: " + cur.getAbsolutePath()
                    + "; Currently Named file is: " + tmp.getAbsolutePath());
            return null;
        }

        log.info("Adding MediaFile back to SageTV: " + cur);
        // now add the original back into the library
        if (relPath == null)
            relPath = cur.getAbsolutePath();
        mf = MediaFileAPI.AddMediaFile(cur, relPath);
        if (mf == null) {
            log.error("Failed to add new file to library: " + cur.getAbsolutePath());
            return null;
        }

        log.info("MediaFile has been stripped of metadata; oldID: " + oldID + "; newID: " + MediaFileAPI.GetMediaFileID(mf)
                + "; Sage MediaFile: " + mf);
        return mf;
    }

    /**
     * Test of 2 objects are Equals. If the objects are Arrays, then the arrays
     * are tested to be Equal.
     * <p/>
     * If Object1 is an Array, then Object2 must also be an array.
     *
     * @param o1 Object or Array
     * @param o2 Object or Array
     * @return true of the objects are equal
     */
    public boolean Equals(Object o1, Object o2) {
        if (o1 == null || o2 == null)
            return false;
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        } else {
            return o1.equals(o2);
        }
    }

    /**
     * This should be called before each Menu Load to ensure that the Phoenix
     * Context is set correctly for SageTV UI Call.
     * <p/>
     * By Calling this from the UI,
     */
    public void InitializePhoenixContext(String UIContextName) {
        log.info("InitializePhoenixContext() called with context " + UIContextName);
        // initializes the Sagex API to use a UI Context for all calls in this
        // thread
        SageAPI.setUIContext(UIContextName);

        // sets and updates the current user profile
        phoenix.profile.GetCurrentUserProfile();
    }

    /**
     * Given a source, or array or sources, then calculate the relative path
     * offset for the given media file.
     *
     * @param sources   A single File, or an Array of File objects in which the
     *                  mediaFile may reside.
     * @param mediaFile A String path, or File Object, or Sage MediaFile object from
     *                  which to get a path.
     * @return relative path of the mediaFile to one of the sources.
     */
    public String GetRelativePath(Object sources, Object mediaFile) {
        File all[] = null;
        if (sources == null) {
            all = Configuration.GetLibraryImportPaths();
        } else {
            if (sources instanceof File) {
                all = new File[]{(File) sources};
            } else if (sources instanceof String) {
                all = new File[]{new File((String) sources)};
            } else {
                all = (File[]) sources;
            }
        }

        String mediaPath = null;
        if (mediaFile instanceof String) {
            mediaPath = (String) mediaFile;
        } else if (mediaFile instanceof File) {
            mediaPath = ((File) mediaFile).getAbsolutePath();
        } else {
            File f = MediaFileAPI.GetFileForSegment(mediaFile, 0);
            if (f != null) {
                mediaPath = f.getAbsolutePath();
            }
        }

        if (all == null || mediaPath == null) {
            log.warn("Missing Sources or MediaFile; Sources: " + ArrayUtils.toString(all, "No Files") + "; MediaFile: " + mediaPath);
            return null;
        }

        for (File f : all) {
            String basePath = f.getAbsolutePath();

            if (mediaPath.startsWith(basePath)) {
                String path = mediaPath.substring(basePath.length() + 1);
                log.debug("Returning Relative Path: " + path + " for item: " + mediaPath);
                return path;
            }
        }

        log.warn("Could not find the relative path for mediafile: " + mediaFile + "; sources: "
                + ArrayUtils.toString(all, "No Sources"));
        // we have nothing
        return null;
    }

    /**
     * Given an Object return a String of the object. This is used in cases
     * where you need to ensure that Object being passed as an Arg is a String
     *
     * @param o Object that needs to be converted to a String
     * @return String of the Object, or an empty String if the Object is null
     */
    public String ToString(Object o) {
        if (o == null) {
            return "";
        }

        return String.valueOf(o);
    }

    /**
     * Logs a message to the log.
     *
     * @param level 0=error, 1=warn, 2+=debug
     * @param msg
     */
    public void Log(int level, String msg) {
        if (level == 0) {
            log.error(msg);
        } else if (level == 1) {
            log.warn(msg);
        } else {
            log.debug(msg);
        }
    }

    /**
     * Converts a JSONString into a Map<String,String> or simply returns the Map
     * if the options is already in a map.
     * <p/>
     * The json string can be in the format, {key: "value", key2, "value2"} or
     * the {} can be optional.
     *
     * @param options a Map<String,String> or a json string
     * @return Map<String,String> or null if the Map can't be created as
     * Map<String,String>
     */
    public Map<String, String> ToMap(Object options) {
        if (options == null)
            return null;
        if (options instanceof String) {
            String s = ((String) options).trim();
            if (!s.startsWith("{")) {
                s = "{" + s + "}";
            }

            try {
                Map<String, String> opts = new HashMap<String, String>();
                JSONObject json = new JSONObject(s);
                for (String key : JSONObject.getNames(json)) {
                    opts.put(key, json.optString(key));
                }
                return opts;
            } catch (JSONException e) {
                log.warn("Failed to create JSON Object from String: " + s, e);
            }
        } else if (options instanceof Map) {
            return (Map<String, String>) options;
        } else {
            log.warn("Map Options is not a valid options type: " + options);
        }
        return null;
    }

    private static Map<Character, String> keyregex = new HashMap<Character, String>();

    static {
        keyregex.put('1', "[\\p{Punct}1]");
        keyregex.put('2', "[abcABC2]");
        keyregex.put('3', "[defDEF3]");
        keyregex.put('4', "[ghiGHI4]");
        keyregex.put('5', "[jklJKL5]");
        keyregex.put('6', "[mnoMNO6]");
        keyregex.put('7', "[pqrsPQRS7]");
        keyregex.put('8', "[tuvTUV8]");
        keyregex.put('9', "[wxyzWXYZ9]");
        keyregex.put('0', "[ 0]");
    }

    /**
     * Given a keypad of numbers return a regex that can be used to find titles
     * based on the number pattern
     *
     * @param numbers
     * @return
     */
    public String CreateRegexFromKeypad(String numbers) {
        if (numbers == null)
            return null;
        int s = numbers.length();
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < s; i++) {
            String reg = keyregex.get(numbers.charAt(i));
            if (reg != null) {
                regex.append(reg);
            } else {
                log.debug("Invalid Charact for KeyPad Regex Search: " + numbers.charAt(i) + " in " + numbers);
            }
        }
        log.debug("KeyPad Search Regex: " + regex);
        return regex.toString();
    }

    /**
     * Removes any matching items in list2 form list1 and return the remaining
     * list. It's basically list substraction
     *
     * @param l1 List or Array
     * @param l2 List or Array
     * @return subtracted list.
     */
    public List RemoveAll(Object l1, Object l2) {
        if (l1 == null)
            return Collections.EMPTY_LIST;
        List src = null;
        if (l1.getClass().isArray()) {
            src = new ArrayList(Arrays.asList((Object[]) l1));
        } else {
            src = (List) l1;
        }

        if (l2 == null)
            return src;
        List dest = null;
        if (l2.getClass().isArray()) {
            dest = Arrays.asList((Object[]) l2);
        } else {
            dest = (List) l2;
        }

        src.removeAll(dest);

        return src;
    }

    /**
     * Converts a list to an array
     *
     * @param l1 List
     * @return Object Array
     */
    public Object[] ToArray(List l1) {
        if (l1 == null)
            return null;
        return l1.toArray();
    }

    /**
     * Returns true if this is an extender. An extender is defined as being a
     * RemoteUI, but it does not have a DesktopUI. Extenders are similar to
     * Placeshifters, except that Placeshifters have a DesktopUI. Like a
     * Placeshifter, extenders run in the same JVM space as the Server.
     *
     * @param ctx UIContext to test, can be null. if it is null, the context
     *            will be discovered.
     * @return
     */
    public boolean IsExtender(UIContext ctx) {
        ctx = (ctx != null) ? ctx : new UIContext(Global.GetUIContextName());
        return (Global.IsRemoteUI(ctx) && !Global.IsDesktopUI(ctx));
    }

    /**
     * Returns true if this is a placeshifter. Al placeshifter has a DesktopUI
     * and it is Remote, but it is not a Client (as per Global.IsClient). A
     * Placeshifter runs the same JVM instance of the Server.
     *
     * @param ctx UIContext to test, can be null. if it is null, the context
     *            will be discovered.
     * @return
     */
    public boolean IsPlaceshifter(UIContext ctx) {
        ctx = (ctx != null) ? ctx : new UIContext(Global.GetUIContextName());
        return (Global.IsRemoteUI(ctx) && Global.IsDesktopUI(ctx)) && !Global.IsClient(ctx);
    }

    /**
     * Returns true if the Server Resources are local to the caller. ie,
     * Extenders, Placeshifters, and ServerUI all can access the Server
     * Resources (ie, files) locally.
     *
     * @param ctx UIContext to test, can be null. if it is null, the context
     *            will be discovered.
     * @return
     */
    public boolean IsServerLocal(UIContext ctx) {
        if (ctx == null || ctx.getName() == null) {
            String name = Global.GetUIContextName();

            // basically if we don't have context, it's because we are running
            // as the server.
            if (name == null)
                return true;

            ctx = new UIContext(name);
        }

        return (Global.IsServerUI(ctx) || IsExtender(ctx) || IsPlaceshifter(ctx));
    }

    /**
     * Given a Path (String or File) return an array of files that match the
     * extensions passed.
     *
     * @param path    source directory (string or file)
     * @param exts    array of extensions ie {"jpg","gif","png"}
     * @param recurse if true, then subdirectories will be searched as well
     * @return File array or null, if no files were found
     */
    public File[] GetFiles(Object path, String[] exts, boolean recurse) {
        if (path == null)
            return null;
        File f = null;
        if (path instanceof File) {
            f = (File) path;
        } else if (path instanceof String) {
            f = new File((String) path);
        } else {
            log.warn("GetFiles(): Can't determine type for " + path);
            return null;
        }

        if (!f.exists()) {
            return null;
        }

        File[] files = new ArrayList<File>(FileUtils.listFiles(f, exts, recurse)).toArray(new File[]{});
        if (files.length == 0)
            return null;
        return files;
    }

    /**
     * Returns all the image files (jpg, png, and gif) for the given path. Will
     * not search subdirectories.
     *
     * @param path source path as a String or File
     * @return {@link File} array or null if nothing was found.
     */
    public File[] GetImages(Object path) {
        return GetFiles(path, ImageUtil.IMAGE_FORMATS, false);
    }

    /**
     * Converts an object to int
     *
     * @param data
     * @param def
     * @return
     */
    public int ToInt(Object data, int def) {
        if (data == null)
            return def;
        return NumberUtils.toInt(data.toString(), def);
    }

    /**
     * Converts an object to long
     *
     * @param data
     * @param def
     * @return
     */
    public long ToLong(Object data, long def) {
        if (data == null)
            return def;
        return NumberUtils.toLong(data.toString(), def);
    }

    /**
     * Converts an object to float
     *
     * @param data
     * @param def
     * @return
     */
    public float ToFloat(Object data, float def) {
        if (data == null)
            return def;
        return NumberUtils.toFloat(data.toString(), def);
    }

    /**
     * Converts and object to double
     *
     * @param data
     * @param def
     * @return
     */
    public double ToDouble(Object data, double def) {
        if (data == null)
            return def;
        return NumberUtils.toDouble(data.toString(), def);
    }

    /**
     * Converts an object to boolean
     *
     * @param data
     * @return
     */
    public boolean ToBoolean(Object data) {
        if (data == null)
            return false;
        return BooleanUtils.toBoolean(data.toString());
    }

    /**
     * Converts object to int using 0 as the default
     *
     * @param data
     * @return
     */
    public int ToInt(Object data) {
        return ToInt(data, 0);
    }

    /**
     * Converts object to long using 0 as the default
     *
     * @param data
     * @return
     */
    public long ToLong(Object data) {
        return ToLong(data, 0);
    }

    /**
     * Converts object to float using 0f as the default
     *
     * @param data
     * @return
     */
    public float ToFloat(Object data) {
        return ToFloat(data, 0f);
    }

    /**
     * Converts object to double using 0d as the default
     *
     * @param data
     * @return
     */
    public double ToDouble(Object data) {
        return ToDouble(data, 0d);
    }

    /**
     * Moves the given item down the list... ie, closer to the bottom or end of
     * the list
     *
     * @param list list the holds the element
     * @param el   element to move
     */
    public void MoveDown(List list, Object el) {
        if (list != null && el != null) {
            int p = list.indexOf(el);
            if (p == -1 || (p + 1) >= list.size())
                return;
            Collections.swap(list, p, p + 1);
        }
    }

    /**
     * Much like the SageTV EvaluateExpression except that it will process
     * multiple commands separated by semi colons
     *
     * @param cmds
     * @return
     */
    public Object[] Eval(String cmds) {
        if (cmds == null)
            return null;
        String cmdarr[] = cmds.split("\\s*;\\s*");
        Object replies[] = new Object[cmdarr.length];
        for (int i = 0; i < cmdarr.length; i++) {
            try {
                replies[i] = WidgetAPI.EvaluateExpression(cmdarr[i]);
            } catch (Throwable e) {
                replies[i] = e;
            }
        }
        return replies;
    }

    /**
     * Moves the given item up the list... ie, closer to the top or start of the
     * list
     *
     * @param list list the holds the element
     * @param el   element to move
     */
    public void MoveUp(List list, Object el) {
        if (list != null && el != null) {
            int p = list.indexOf(el);
            if (p == -1 || (p - 1) < 0)
                return;
            Collections.swap(list, p, p - 1);
        }
    }

    /**
     * Sends a Message to a active UI context as a popup, that goes away after
     * the specified time.
     *
     * @param ctx     Client Context
     * @param message Message to show
     * @param timeout hides the message after the given timeout
     */
    public void Toast(String ctx, String message, long timeout) {
        Toaster.toast(ctx, message, null, timeout);
    }

    /**
     * Sends the message to All connected clients.
     *
     * @param msg
     * @param timeout
     * @return Set of client ids that were given the message
     */
    public Set<String> ToastAll(String msg, long timeout) {
        Set<String> clients = phoenix.client.GetConnectedClients();
        if (clients != null && clients.size() > 0) {
            for (String s : clients) {
                Toaster.toast(s, msg, null, timeout);
            }
        }
        return clients;
    }

    /**
     * Returns the String that is Passed in. Used for testing.
     *
     * @param string
     * @return
     */
    public String Echo(String string) {
        return string;
    }

}

package phoenix.impl;

import org.apache.commons.lang.StringUtils;
import sagex.api.SystemMessageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.progress.NullProgressMonitor;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.groups.GroupingFactory;
import sagex.phoenix.vfs.sorters.SorterFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.visitors.ActorSearchVisitor;
import sagex.phoenix.vfs.visitors.TitleSearchVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remote API is a helper API class for helping Remote devices (Web, Android,
 * iOS, etc). The APIs included here are basically "convenience" apis to make
 * the communication with the remote device easier.
 *
 * @author sls
 */
@API(group = "remote")
public class RemoteAPI {
    /**
     * Since you cannot delete a system message by ID, this is a convenience
     * method for finding and then deleting a system message by name and time.
     * This is mainly used by remote systems, such as web ui, or android for
     * deleting messages.
     *
     * @param message
     * @param startTime
     * @param endTime
     * @return
     */
    public boolean DeleteSystemMessage(String message, long startTime, long endTime) {
        if (StringUtils.isEmpty(message))
            return false;
        for (Object o : SystemMessageAPI.GetSystemMessages()) {
            String sysname = SystemMessageAPI.GetSystemMessageString(o);
            long sysStartTime = SystemMessageAPI.GetSystemMessageTime(o);
            long sysEndTime = SystemMessageAPI.GetSystemMessageEndTime(o);

            if (message.equals(sysname) && startTime == sysStartTime && endTime == sysEndTime) {
                SystemMessageAPI.DeleteSystemMessage(o);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the System Messages as a List containing a Map of elements that
     * represent the System Message. This is meant as a convenience method that
     * creates a structure that can easily be serialized for json transport.
     *
     * @return
     */
    public List GetSystemMessages() {
        Object[] messages = SystemMessageAPI.GetSystemMessages();
        List msgs = new ArrayList();

        if (messages != null) {
            for (Object o : messages) {
                String name = SystemMessageAPI.GetSystemMessageString(o);
                long time = SystemMessageAPI.GetSystemMessageTime(o);
                if (time == 0 && StringUtils.isEmpty(name)) {
                    // skip empty messages
                    continue;
                }

                Map mo = new HashMap();
                mo.put("message", name);
                mo.put("startTime", time);
                mo.put("endTime", SystemMessageAPI.GetSystemMessageEndTime(o));
                mo.put("level", SystemMessageAPI.GetSystemMessageLevel(o));
                mo.put("repeatCount", SystemMessageAPI.GetSystemMessageRepeatCount(o));
                mo.put("typeCode", SystemMessageAPI.GetSystemMessageTypeCode(o));
                mo.put("typeName", SystemMessageAPI.GetSystemMessageTypeName(o));
                String vn[] = SystemMessageAPI.GetSystemMessageVariableNames(o);
                if (vn != null && vn.length > 0) {
                    Map vars = new HashMap();
                    for (String s : vn) {
                        vars.put(s, SystemMessageAPI.GetSystemMessageVariable(o, s));
                    }
                    mo.put("vars", vars);
                }
                msgs.add(mo);
            }
        }

        return msgs;
    }

    /**
     * Searches a given view for a title containing the passed title string. If
     * TV files are used, then both the TITLE adn the EPISODENAME is searched.
     *
     * @param titleContains
     * @param baseViewName
     * @return
     */
    public List<IMediaResource> SearchMediaByTitle(String titleContains, String baseViewName) {
        List<IMediaResource> files = new ArrayList<IMediaResource>();

        IMediaFolder base = phoenix.umb.CreateView(baseViewName);
        if (base == null) {
            return files;
        }

        TitleSearchVisitor vis = new TitleSearchVisitor(titleContains, files);
        base.accept(vis, NullProgressMonitor.INSTANCE, IMediaFolder.DEEP_UNLIMITED);

        return files;
    }

    /**
     * Searches a given view for all media files that contain the given "actor"
     * criteria.
     *
     * @param actorContains
     * @param baseViewName
     * @return
     */
    public List<IMediaResource> SearchMediaByActor(String actorContains, String baseViewName) {
        List<IMediaResource> files = new ArrayList<IMediaResource>();

        IMediaFolder base = phoenix.umb.CreateView(baseViewName);
        if (base == null) {
            return files;
        }

        ActorSearchVisitor vis = new ActorSearchVisitor(actorContains, files);
        base.accept(vis, NullProgressMonitor.INSTANCE, IMediaFolder.DEEP_UNLIMITED);

        return files;
    }

    /**
     * Returns the current filters, sorters and grouper for a given view
     *
     * @param view
     * @return
     */
    public Map GetViewInfo(String view) {
        ViewFolder f = phoenix.umb.CreateView(view);
        Map map = new HashMap();
        map.put("view", view);
        map.put("presentations", f.getViewFactory().getViewPresentations());
        return map;
    }

    public List<FilterFactory> GetAllFilters() {
        return Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories(true);
    }

    public List<SorterFactory> GetAllSorters() {
        return Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactories(true);
    }

    public List<GroupingFactory> GetAllGroupers() {
        return Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactories(true);
    }
}

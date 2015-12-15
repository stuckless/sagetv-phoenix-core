package sagex.phoenix.profiles;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sagex.api.Global;

public class ProfileManager {
    private static final String DEFAULT_CONTEXT = "default";
    private static final String DEFAULT_PROFILE = "default";

    private static final Logger log = Logger.getLogger(ProfileManager.class);

    private Map<String, Profile> profiles = new HashMap<String, Profile>();

    private Profile defaultProfile = new Profile(DEFAULT_PROFILE);
    private Set<Profile> loadedProfiles = new LinkedHashSet<Profile>();

    /**
     * The current profile is associated with the current user for any given
     * client. The same user client can be connected to multiple clients, but a
     * single client can only be connected to a single client.
     * <p/>
     * If no profile has been assigned to a given client, then a "default"
     * profile is created and used.
     *
     * @return
     */
    public Profile getCurrentProfile() {
        Profile profile = null;
        String context = getProfileContext();
        profile = profiles.get(context);
        if (profile == null) {
            profile = defaultProfile;
            setCurrentProfile(profile);
        }
        profile.setLastAccessed(System.currentTimeMillis());
        return profile;
    }

    public void setCurrentProfile(Profile profile) {
        String context = getProfileContext();
        profile.setContext(context);
        profiles.put(context, profile);

        if (!loadedProfiles.contains(profile)) {
            log.warn("Current Profile: " + profile + " not was previously loaded.");
            addProfile(profile);
        }

        log.info("Setting Current User Profile: " + profile);
    }

    public Profile[] getProfiles() {
        if (loadedProfiles.size() == 0) {
            loadProfiles();
        }
        return loadedProfiles.toArray(new Profile[loadedProfiles.size()]);
    }

    public void addProfile(Profile profile) {
        loadedProfiles.add(profile);
    }

    public void removeProfile(Profile profile) {
        loadedProfiles.remove(profile);
    }

    public Profile getProfile(String user) {
        if (user == null)
            return null;
        for (Profile p : loadedProfiles) {
            if (user.equals(p.getUser())) {
                return p;
            }
        }
        return null;
    }

    private void loadProfiles() {
        // TODO Auto-generated method stub
        getCurrentProfile();
    }

    protected String getProfileContext() {
        String context = Global.GetUIContextName();
        if (context == null) {
            context = DEFAULT_CONTEXT;
            log.error("GetUIContextName() returned Null!! Creating a default context: " + context);
        }
        return context;
    }
}

package phoenix.impl;

import sagex.phoenix.Phoenix;
import sagex.phoenix.profiles.Profile;
import sagex.phoenix.tools.annotation.API;

/**
 * Functions for managing user profiles
 * 
 * @author seans
 *
 */
@API(group="profile")
public class UserProfileAPI {
	/**
	 * Return the current user
	 * 
	 * @return
	 */
	public String GetCurrentUser() {
		return GetUserProfileName(GetCurrentUserProfile());
	}
	
    /**
     * Get the current User Profile that is active for a given client.  If not profile is active, then a default profile
     * is created and set as the active profile.
     * 
     * @return Current User Profile
     */
    public Profile GetCurrentUserProfile() {
        return Phoenix.getInstance().getProfileManager().getCurrentProfile();
    }
    
    /**
     * Set the current User Profile to the current client.
     * 
     * @param profile
     */
    public void SetCurrentUserProfile(Profile profile) {
    	Phoenix.getInstance().getProfileManager().setCurrentProfile(profile);
    }
    
    /**
     * Return the username for the given profile
     * 
     * @param profile
     * @return
     */
    public String GetUserProfileName(Profile profile) {
        return profile.getUser();
    }
 
    /**
     * Return an array of the currently known user profiles
     * 
     * TODO: Currently this returns a static list
     * 
     * @return
     */
    public Profile[] GetUserProfiles() {
        return Phoenix.getInstance().getProfileManager().getProfiles();
    }
 
    /**
     * Creates and adds a new User profile for the given name.
     * 
     * @param userName
     * @return
     */
    public Profile AddUserProfile(String userName) {
        Profile p = new Profile(userName);
        Phoenix.getInstance().getProfileManager().addProfile(p);
        return p;
    }
    
    /**
     * Removes a user profile
     * 
     * @param profile
     */
    public void RemoveUserProfile(Profile profile) {
    	Phoenix.getInstance().getProfileManager().removeProfile(profile);
    }

    /**
     * Finds and returns a profile for the given username.
     * 
     * Returns null if no profile is found.
     * 
     * @param user
     * @return
     */
    public Profile GetUserProfile(String user) {
        return Phoenix.getInstance().getProfileManager().getProfile(user);
    }
}

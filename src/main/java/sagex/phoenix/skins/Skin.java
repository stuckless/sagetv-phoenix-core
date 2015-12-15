package sagex.phoenix.skins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.image.ImageUtil;

public class Skin implements ISkin {
    public enum State {
        FAILED, UNINSTALLED, INSTALLED, RESOLVED, STOPPING, STARTING, ACTIVE
    }

    private String id;
    private String name;
    private File directory;
    private String version;
    private State state = State.UNINSTALLED;
    private List<String> depends = new ArrayList<String>();
    private String description;
    private String status;
    private SkinManager manager = null;

    public Skin(File dir) {
        setDirectory(dir);
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getName()
     */
    /*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getName()
	 */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setName(java.lang.String)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setName(java.lang.String)
	 */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getDirectory()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getDirectory()
	 */
    public File getDirectory() {
        return directory;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setDirectory(java.io.File)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setDirectory(java.io.File)
	 */
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getState()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getState()
	 */
    public State getState() {
        return state;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setState(sagex.phoenix.skins.Skin.State)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setState(sagex.phoenix.skins.Skin.State)
	 */
    public void setState(State state) {
        this.state = state;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getId()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getId()
	 */
    public String getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setId(java.lang.String)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setId(java.lang.String)
	 */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getVersion()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getVersion()
	 */
    public String getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setVersion(java.lang.String)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setVersion(java.lang.String)
	 */
    public void setVersion(String version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getDependencies()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getDependencies()
	 */
    public String[] getDependencies() {
        return depends.toArray(new String[depends.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#addDependency(java.lang.String)
     */
    public void addDependency(String depends) {
        this.depends.add(depends);
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getDescription()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getDescription()
	 */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#setStatus(java.lang.String)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#setStatus(java.lang.String)
	 */
    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getStatus()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getStatus()
	 */
    public String getStatus() {
        return status;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getManager()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getManager()
	 */
    public SkinManager getManager() {
        return manager;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sagex.phoenix.skins.ISkin#setManager(sagex.phoenix.skins.SkinManager)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sagex.phoenix.skins.ISkin#setManager(sagex.phoenix.skins.SkinManager)
	 */
    public void setManager(SkinManager manager) {
        this.manager = manager;
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getResource(java.lang.String)
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getResource(java.lang.String)
	 */
    public File getResource(String path) {
        return new File(directory, path);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Skin [");
        if (id != null) {
            builder.append("id=");
            builder.append(id);
            builder.append(", ");
        }
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (directory != null) {
            builder.append("directory=");
            builder.append(directory);
            builder.append(", ");
        }
        if (state != null) {
            builder.append("state=");
            builder.append(state);
            builder.append(", ");
        }
        if (status != null) {
            builder.append("status=");
            builder.append(status);
            builder.append(", ");
        }
        if (version != null) {
            builder.append("version=");
            builder.append(version);
        }
        builder.append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see sagex.phoenix.skins.ISkin#getScreenShots()
     */
	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.skins.ISkin#getScreenShots()
	 */
    public String[] getScreenShots() {
        File screenShots = getResource("ScreenShots");
        if (screenShots == null || !screenShots.isDirectory()) {
            return null;
        }
        File[] pics = screenShots.listFiles(ImageUtil.ImagesFilter);
        String files[] = new String[pics.length];
        for (int i = 0; i < pics.length; i++) {
            files[i] = pics[i].getAbsolutePath();
        }
        return files;
    }
}

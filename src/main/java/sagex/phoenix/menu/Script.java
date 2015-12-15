package sagex.phoenix.menu;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Script {
    private String language = "JavaScript";
    private String script;

    public Script() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

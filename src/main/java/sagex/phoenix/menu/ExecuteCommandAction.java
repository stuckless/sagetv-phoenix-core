package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.Global;
import sagex.api.Utility;
import sagex.phoenix.util.var.DynamicVariable;

import java.io.File;

public class ExecuteCommandAction extends OutputVariableAction {
    public ExecuteCommandAction() {
    }

    private String commandArgs;
    private String os;
    private DynamicVariable<String> workingDir = new DynamicVariable<String>(String.class, null);

    @Override
    public boolean invoke() {
        try {
            if (os != null) {
                // check if match the os
                String sageos = Global.GetOS();
                if (sageos == null || !sageos.startsWith(os)) {
                    log.debug("Skipping Action: " + this + "; since the Sage OS: " + sageos + " doesn't match the required OS: "
                            + os);
                    return true;
                }
            }

            String action = action().get();
            log.debug("Execute Command Action: " + action);

            File workDir = null;
            if (workingDir.get() != null) {
                workDir = new File(workingDir.get());
            } else {
                workDir = new File(".");
            }

            if (getOutputVariable() != null) {
                Object output = Utility.ExecuteProcessReturnOutput(UIContext.getCurrentContext(), action, getArgs(), workDir, true,
                        true);
                updateOutputVariable(output);
            } else {
                Utility.ExecuteProcess(UIContext.getCurrentContext(), action, getArgs(), workDir, false);
            }

            return true;
        } catch (Throwable t) {
            log.error("Failed to invoke the command: " + action());
            return false;
        }
    }

    public String getOS() {
        return os;
    }

    public void setOS(String os) {
        this.os = os;
    }

    public String getArgs() {
        return commandArgs;
    }

    public void setArgs(String commandArgs) {
        this.commandArgs = commandArgs;
    }

    public DynamicVariable<String> workingDir() {
        return workingDir;
    }
}
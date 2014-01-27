package sagex.phoenix.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import sagex.UIContext;

public class Command {
	public static enum Encoder {JSON, IMAGE}
	
	private String command;
	private String className;
	private String methodName;
	private List<String> args = new ArrayList<String>();
	private UIContext context;
	private String referenceName;
	private long referenceExpiry;
	private Encoder encoder = Encoder.JSON;
	private IOContext IOContext;
	
	private Class[] signature=null;
	
	public Command(IOContext ioContext, String cmd) {
		this.IOContext = ioContext; 
		setCommand(cmd);
	}
	
	public String getCommand() {
		return command;
	}
	
	/**
	 * Creates a command using either the api name, ie, "phoenix.umb.CreateView", or as function "phoenix.util.GetRandomNumber(10)".
	 * 
	 * Note, if you are pass the the command as a function with mulitple args, then do not quote the args, just comma separate them.
	 * @param cmd
	 */
	public void setCommand(String cmd) {
		if (cmd.contains("(")) {
			Pattern p = Pattern.compile("([^(]+)\\(\\s*([^)]*)\\s*\\)", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(cmd);
			if (m.find()) {
				this.command = m.group(1);
				String parts[] = m.group(2).split("\\s*,\\s*");
				for (String s: parts) {
					if (!StringUtils.isEmpty(s)) {
						getArgs().add(s);
					}
				}
			}
		} else {
			this.command = cmd;
		}
		
		if (command==null) throw new RuntimeException("Missing Command");
		if (!command.startsWith("phoenix.")) throw new RuntimeException("Invalid Phoenix API: " + cmd);
		int pos = command.lastIndexOf('.');
		className = command.substring(0,pos);
		methodName = command.substring(pos+1);

	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public List<String> getArgs() {
		return args;
	}

	public UIContext getContext() {
		return context;
	}
	
	public void setContext(UIContext context) {
		this.context = context;
	}

	public String getReferenceName() {
		return referenceName;
	}

	/**
	 * If the result of command should be stored as a reference, then the name must be non-null.  References can later be
	 * retrieved using the ref: prefix or by calling {@link RemoteContext}.getReference(Name)
	 * 
	 * @param referenceName
	 */
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}

	public long getReferenceExpiry() {
		return referenceExpiry;
	}

	/**
	 * If the reference expiry > 0 then the reference will be removed after the given the expiry delay.  ie, to expire a reference
	 * after 10 seconds use, '10000' as the expiry.
	 *  
	 * @param referenceExpiry
	 */
	public void setReferenceExpiry(long referenceExpiry) {
		this.referenceExpiry = referenceExpiry;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	public IOContext getIOContext() {
		return IOContext;
	}

	public Class[] getSignature() {
		return signature;
	}

	public void setSignature(Class[] signature) {
		this.signature = signature;
	}
}

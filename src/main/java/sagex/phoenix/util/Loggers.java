package sagex.phoenix.util;

import org.apache.log4j.Logger;

/**
 * Loggers returns an instances of special loggers that can be used in
 * additional the normal log4j logging. For example, you may need, in some
 * cases, an instance of a vfs debug logger that can be tweaked differently than
 * a normal class logger.
 * 
 * typically when you get an instance of these loggers, for debugging, you
 * should always check logger.isDebugLogEnabled() before do any debug logging,
 * since you are probably logging very expensive operations.
 * 
 * @author seans
 * 
 */
public class Loggers {
	/**
	 * return an instance of the vfs logger that can be configured using
	 * log4j.logger.phoenix.vfs
	 * 
	 * the VFS logger is used mainly for debugging low level vfs operations, and
	 * it should normally never be configured unless it is requested.
	 */
	public static final Logger VFS_LOG = Logger.getLogger("phoenix.vfs");

	/**
	 * return an instance of the vfs logger that can be configured using
	 * log4j.logger.phoenix.log
	 * 
	 * This is general purpose logger used mostly by util classes that don't
	 * want to keep a reference to their own logger
	 */
	public static final Logger LOG = Logger.getLogger("phoenix.log");

	/**
	 * returns an instance of the metadata logger that can be configured using
	 * log4j.logger.phoenix.metadata
	 * 
	 * This is a statistical logger for tracking the metadata imports, updates
	 * and skips for a media item
	 */
	public static final Logger METADATA = Logger.getLogger("phoenix.metadata");
}

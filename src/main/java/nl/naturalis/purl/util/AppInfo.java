package nl.naturalis.purl.util;

import java.io.File;
import java.io.IOException;

import nl.naturalis.purl.ApplicationInitializationException;

import org.domainobject.util.ConfigObject;
import org.domainobject.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton responsible for initializing logging and loading the
 * configuration for the PURL REST service.
 * 
 * @author Ayco Holleman
 *
 */
public class AppInfo {

	private static final Logger logger = LoggerFactory.getLogger(AppInfo.class);

	/**
	 * System property that tells us where the configuration directory
	 * (containing at least purl.properties) is. When using
	 * Wildfly or JBoss, this system property is likely set in standalone.xml.
	 */
	public static final String SYSPROP_CONFIG_DIR = "nl.naturalis.purl.conf.dir";

	/*
	 * Name of the central configuration file for the PURL REST service.
	 */
	private static final String CONFIG_FILE_NAME = "purl.properties";

	private static AppInfo instance;

	private final File confDir;
	private final ConfigObject config;


	/**
	 * Do curcuial initialization work required before handling any service
	 * request. If anything goes wrong during the initializion process an
	 * {@link ApplicationInitializationException} is thrown, causing the entire
	 * web application to die already during startup. An explanation of what
	 * went wrong is written to the Wildfly log (standalone/log/server.log).
	 */
	public static void initialize()
	{
		if (instance == null) {
			instance = new AppInfo();
		}
	}


	/**
	 * Return the one and only instance of this class. Will call
	 * {@link #initialize()} first.
	 * 
	 * @return
	 */
	public static AppInfo instance()
	{
		initialize();
		return instance;
	}


	/**
	 * Return a {@code ConfigObject} with the properties from purl.properties.
	 * 
	 * @return
	 */
	public ConfigObject getConfig()
	{
		return config;
	}


	/**
	 * Get the directory designated to contain the application's configuration
	 * files. This directory will contain at least purl.properties and
	 * logback.xml, but may also contain more user-oriented configuration files.
	 * 
	 * @return
	 */
	public File getConfDir()
	{
		return confDir;
	}


	private AppInfo()
	{
		confDir = confDir();
		config = loadConfig();
	}


	private static File confDir()
	{
		String path = System.getProperty(SYSPROP_CONFIG_DIR);
		if (path == null) {
			String msg = String.format("Missing system property \"%s\"", SYSPROP_CONFIG_DIR);
			throw new ApplicationInitializationException(msg);
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			String msg = String.format("Invalid value for system property \"%s\": \"%s\" (no such directory)", SYSPROP_CONFIG_DIR, path);
			throw new ApplicationInitializationException(msg);
		}
		try {
			dir = dir.getCanonicalFile();
			logger.info("Configuration directory for this application: " + dir.getAbsolutePath());
			return dir;
		}
		catch (IOException e) {
			throw new ApplicationInitializationException(e);
		}
	}


	private ConfigObject loadConfig()
	{
		File file = FileUtil.newFile(confDir, CONFIG_FILE_NAME);
		if (!file.isFile()) {
			String msg = String.format("Configuration file missing: %s", file.getPath());
			throw new ApplicationInitializationException(msg);
		}
		logger.info("Loading application configuration from " + file.getAbsolutePath());
		return new ConfigObject(file);
	}

}

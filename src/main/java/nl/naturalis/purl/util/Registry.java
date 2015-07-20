package nl.naturalis.purl.util;

import java.io.File;
import java.io.IOException;

import nl.naturalis.nda.client.ClientConfig;
import nl.naturalis.nda.client.ClientFactory;
import nl.naturalis.nda.client.MultiMediaClient;
import nl.naturalis.nda.client.SpecimenClient;
import nl.naturalis.purl.ApplicationInitializationException;

import org.domainobject.util.ConfigObject;
import org.domainobject.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton responsible for configuring and handing out various types of
 * objects to interested classes.
 * 
 * @author Ayco Holleman
 *
 */
public class Registry {

	private static final Logger logger = LoggerFactory.getLogger(Registry.class);

	/**
	 * System property that tells us where the configuration directory
	 * (containing at least purl.properties) is. When using Wildfly or JBoss,
	 * this system property is likely set in standalone.xml.
	 */
	public static final String SYSPROP_CONFIG_DIR = "nl.naturalis.purl.conf.dir";

	/*
	 * Name of the central configuration file for the PURL REST service.
	 */
	private static final String CONFIG_FILE_NAME = "purl.properties";

	private static Registry instance;

	private final File confDir;
	private final ConfigObject config;


	/**
	 * Do initialization work required before handling any service request. If
	 * anything goes wrong during the initializion process an
	 * {@link ApplicationInitializationException} is thrown, causing the entire
	 * web application to die already during startup. An explanation of what
	 * went wrong is written to the Wildfly log (standalone/log/server.log).
	 */
	public static void initialize()
	{
		if (instance == null) {
			instance = new Registry();
		}
	}


	/**
	 * Return the one and only instance of this class. Will call
	 * {@link #initialize()} first.
	 * 
	 * @return
	 */
	public static Registry getInstance()
	{
		initialize();
		return instance;
	}


	private Registry()
	{
		confDir = confDir();
		config = loadConfig();
	}


	/**
	 * Return a {@code ConfigObject} for purl.properties.
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
	 * logback.xml, but may also contain other, more user-oriented configuration
	 * files.
	 * 
	 * @return
	 */
	public File getConfDir()
	{
		return confDir;
	}


	public SpecimenClient getSpecimenClient()
	{
		String nbaBaseUrl = config.required("nl.naturalis.purl.baseurl.nba");
		ClientConfig cfg = new ClientConfig(nbaBaseUrl);
		return ClientFactory.getInstance(cfg).createSpecimenClient();
	}


	public MultiMediaClient getMultiMediaClient()
	{
		String nbaBaseUrl = config.required("nl.naturalis.purl.baseurl.nba");
		ClientConfig cfg = new ClientConfig(nbaBaseUrl);
		return ClientFactory.getInstance(cfg).createMultiMediaClient();
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

}

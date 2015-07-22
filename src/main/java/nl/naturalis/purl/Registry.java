package nl.naturalis.purl;

import java.io.File;
import java.io.IOException;

import nl.naturalis.nda.client.ClientConfig;
import nl.naturalis.nda.client.ClientFactory;
import nl.naturalis.nda.client.MultiMediaClient;
import nl.naturalis.nda.client.SpecimenClient;

import org.domainobject.util.ConfigObject;
import org.domainobject.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for configuring and handing out various types of objects to
 * interested classes.
 * 
 * @author Ayco Holleman
 * @created Jul 9, 2015
 *
 */
public class Registry {

	private static final Logger logger = LoggerFactory.getLogger(Registry.class);

	/*
	 * System property that tells us where the configuration directory
	 * (containing purl.properties) is. When using Wildfly, this system property
	 * is probably set in standalone.xml
	 */
	private static final String SYSPROP_CONFIG_DIR = "nl.naturalis.purl.conf.dir";

	/*
	 * Name of the central configuration file for the PURL REST service.
	 */
	private static final String CONFIG_FILE_NAME = "purl.properties";

	private static Registry instance;

	private File confDir;
	private ConfigObject config;


	/**
	 * Instantiates a {@code Registry} instance. Should be called before
	 * handling any PURL request. If anything goes wrong while instantiating the
	 * {@code Registry}, an {@link ApplicationInitializationException} is
	 * thrown, causing the application to die already during startup. An
	 * explanation of what went wrong is written to the Wildfly log
	 * (standalone/log/server.log).
	 */
	public static void initialize()
	{
		if (instance == null) {
			instance = new Registry();
		}
	}


	/**
	 * Return a {@code Registry} instance.
	 * 
	 * @return The one and only instance of this class
	 */
	public static Registry getInstance()
	{
		initialize();
		return instance;
	}


	private Registry()
	{
		establishConfigDirectory();
		loadConfiguration();
	}


	/**
	 * Return a {@code ConfigObject} for the main configuration file
	 * (purl.properties).
	 * 
	 * @return
	 */
	public ConfigObject getConfig()
	{
		return config;
	}


	/**
	 * Get the directory designated to contain the application's configuration
	 * files. This directory will contain at least purl.properties, but may
	 * contain additional files that the application expects to be there.
	 * 
	 * @return
	 */
	public File getConfDir()
	{
		return confDir;
	}


	/**
	 * Returns a native Java client for the NBA specimen resource.
	 * 
	 * @return
	 */
	public SpecimenClient getSpecimenClient()
	{
		ClientConfig cfg = new ClientConfig(getNbaBaseUrl());
		return ClientFactory.getInstance(cfg).createSpecimenClient();
	}


	/**
	 * Returns a native Java client for the NBA multimedia resource.
	 * 
	 * @return
	 */
	public MultiMediaClient getMultiMediaClient()
	{
		ClientConfig cfg = new ClientConfig(getNbaBaseUrl());
		return ClientFactory.getInstance(cfg).createMultiMediaClient();
	}


	/**
	 * Get base url of the NBA, as defined in purl.properties.
	 * 
	 * @return
	 */
	public String getNbaBaseUrl()
	{
		return config.required("nba.baseurl");
	}


	/**
	 * Get base url of the BioPortal, as defined in purl.properties.
	 * 
	 * @return
	 */
	public String getBioportalBaseUrl()
	{
		return config.required("bioportal.baseurl");
	}


	private void establishConfigDirectory()
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
			confDir = dir.getCanonicalFile();
			logger.info("Configuration directory for this application: " + dir.getAbsolutePath());
		}
		catch (IOException e) {
			throw new ApplicationInitializationException(e);
		}
	}


	private void loadConfiguration()
	{
		File file = FileUtil.newFile(confDir, CONFIG_FILE_NAME);
		if (!file.isFile()) {
			String msg = String.format("Configuration file missing: %s", file.getPath());
			throw new ApplicationInitializationException(msg);
		}
		logger.info("Loading application configuration from " + file.getAbsolutePath());
		this.config = new ConfigObject(file);
	}

}

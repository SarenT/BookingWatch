import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ActionListener{
	
	private Properties defaults = new Properties(), properties;
	
	public static final String SERVER_ADDRESS = "SERVER_ADDRESS";
	public static final String LOCAL_CALENDAR = "LOCAL_CALENDAR";
	public static final String REFRESH_RATE = "REFRESH_RATE";
	public static final String ALARM_BEFORE = "ALARM_BEFORE";
	
	private static final String DEFAULT_SERVER_ADDRESS = "";
	private static final String DEFAULT_LOCAL_CALENDAR = "booked.ics";
	private static final String DEFAULT_REFRESH_RATE = "30";
	private static final String DEFAULT_ALARM_BEFORE = "30";
	
	private String OS = (System.getProperty("os.name")).toUpperCase();
	private String configPath;
	private static String FILE_NAME = "settings.conf";
	private static String LOG_FILE_NAME = "BookingWatch.log";
	
	private MenuItem settingsItem = new MenuItem("Settings");
	private MenuItem aboutItem = new MenuItem("About");
	private MenuItem closeItem = new MenuItem("Close");
	private final PopupMenu popup = new PopupMenu();
	private final TrayIcon trayIcon = new TrayIcon(createImage("icon.png", "tray icon"));
	private final SystemTray tray = SystemTray.getSystemTray();
	
	private SyncWorker syncWorker;
	private Settings settings;
	private About about = new About();
	
	private File configFile, configDir, logFile;
	
	private BlockingQueue<Properties> propertiesReporter = new LinkedBlockingQueue<Properties>();
	
	final Logger logger = LoggerFactory.getLogger(Main.class);
	public final static org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(Main.class);
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Runnable guiCreator = new Runnable() {
	        public void run() {
	        	Main main = new Main();
	        }
	    };
	    SwingUtilities.invokeLater(guiCreator);
		
		
	}
	
	public Main() { 
		try {
			defaults.setProperty(LOCAL_CALENDAR, DEFAULT_LOCAL_CALENDAR);
			defaults.setProperty(SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
			defaults.setProperty(REFRESH_RATE, DEFAULT_REFRESH_RATE);
			defaults.setProperty(ALARM_BEFORE, DEFAULT_ALARM_BEFORE);
			
			properties = new Properties(defaults);
			
			settingsItem = new MenuItem("Settings");
			aboutItem = new MenuItem("About");
			closeItem = new MenuItem("Close");
			settingsItem.addActionListener(this);
			aboutItem.addActionListener(this);
			closeItem.addActionListener(this);
			popup.add(settingsItem);
			popup.addSeparator();
			popup.add(aboutItem);
			popup.add(closeItem);
	        
	        trayIcon.setPopupMenu(popup);
	        trayIcon.setImageAutoSize(true);
	        
			tray.add(trayIcon);
			
			if (OS.contains("WIN")){
				configPath = System.getenv("AppData") + "/BookingWatch/";
			} else {
				configPath = System.getProperty("user.home");
				if(OS.contains("MAC")) {
					configPath += "/Library/Application Support/BookingWatch/";
				}
				if(OS.contains("NUX")) {
					configPath += "/.local/share/BookingWatch/";
				}
			}
			
			checkSettings(properties);
			
			SimpleLayout layout = new SimpleLayout();
			try {
				FileAppender appender = new FileAppender(layout, logFile.getAbsolutePath(), false);
		    	log4jLogger.addAppender(appender);
			} catch(IOException e) {
				logger.error(exceptionStacktraceToString(e));
			}
			
			
			try {
				loadSettings();
			} catch (IOException e1) {
				logger.error(exceptionStacktraceToString(e1));
			}
			settings = new Settings(this, this.properties);
			
			String serverURL = properties.getProperty(SERVER_ADDRESS);
			String localPath = properties.getProperty(LOCAL_CALENDAR);
			Integer refreshRate = getIntegerProperty(properties, defaults, REFRESH_RATE);
			Integer alarmBefore = getIntegerProperty(properties, defaults, ALARM_BEFORE);
			
			syncWorker = new SyncWorker(this, propertiesReporter, serverURL, localPath, refreshRate, alarmBefore);
			syncWorker.execute();
			
		} catch (AWTException e) {
			logger.error(exceptionStacktraceToString(e));
		}
	}

	/** Retrieves integer type property according to the key. If property is missing, default is received.
	 * @param properties Settings
	 * @param defaults Default settings
	 * @param key Key to the integer type property
	 * @return Returns integer type property.
	 */
	private Integer getIntegerProperty(Properties properties, Properties defaults, String key) {
		String propertyStr = properties.getProperty(key);
		Integer property;
		if(propertyStr != null) {
			try {
				property = Integer.valueOf(propertyStr);
			}catch (NumberFormatException e) {
				property = Integer.valueOf(defaults.getProperty(key));
			}
		} else {
			property = Integer.valueOf(defaults.getProperty(key));
		}
		return property;
	}

	private void loadSettings() throws IOException{
		FileReader fr = new FileReader(configFile);
		properties.load(fr);
		fr.close();
	}

	private void checkSettings(Properties properties) {
		configDir = new File(configPath);
		if(!configDir.exists()) {
			if(!configDir.mkdir()) {
				System.out.println("Configuration directory doesn't exist and could not be created. Exiting...");
				return;
			}
		}
		
		configFile = new File(configDir, FILE_NAME);
		try {
			if(!configFile.exists()) {
				if(configDir.canRead() && configDir.canWrite()) {
					configFile.createNewFile();
					saveSettings(defaults);
				}
			}
		}catch (IOException e) {
			logger.error(exceptionStacktraceToString(e));
			return;
		}
		
		logFile = new File(configDir, LOG_FILE_NAME);
		try {
			if(!logFile.exists()) {
				if(logFile.canRead() && configDir.canWrite()) {
					logFile.createNewFile();
				}
			}
		}catch (IOException e) {
			logger.error(exceptionStacktraceToString(e));
			return;
		}
	}

	public void saveSettings(Properties properties) throws IOException {
		FileWriter fw = new FileWriter(configFile);
		properties.store(fw, "Please refer to settings in the tray icon to change the settings.");
		fw.close();
		propertiesReporter.add(properties);
	}
	

	// Taken from Oracle and/or its affiliates: https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://
	//docs.oracle.com/javase/tutorial/uiswing/examples/misc/TrayIconDemoProject/src/misc/TrayIconDemo.java
	protected static Image createImage(String path, String description) {
        URL imageURL = Main.class.getResource(path);
        
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == settingsItem) {
			settings.setVisible(true);
		}else if(e.getSource() == aboutItem) {
			about.setVisible(true);
		}else if(e.getSource() == closeItem) {
			if(syncWorker != null) {
				if(syncWorker.getState() == StateValue.STARTED) {
					syncWorker.cancel(true);
				}else {
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
		}
	}

	public void syncWorkerFinished(Boolean success) {
		if(success) {
			tray.remove(trayIcon);
			System.exit(0);
		}else {
			//TODO start another syncworker?
		}
		
	}

	public void process(List<String> progress) {
		for(String s : progress) {
			System.out.println(s);
		}
	}
	
	public static String exceptionStacktraceToString(Exception e) {
	    return Arrays.toString(e.getStackTrace());
	}

}

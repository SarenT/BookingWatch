import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;

public class SyncWorker extends SwingWorker<Boolean, String> {
	private Main main;
	private String serverURL, localPath;
	private Integer refreshRate;
	private boolean goOn = true;
	
	private static final String MW_MATCH_NAME = "X-WM_MATCH";
	private static final String MW_MATCHED = "MATCHED";
	private static final String MW_NEWLY_ADDED_MATCHED = "NEWLY_ADDED_MATCHED";
	private static final long TIMEOUT = 40;
	private static CalendarBuilder builder = new CalendarBuilder();
	
	private BlockingQueue<Properties> propertiesSource;
	
	final Logger logger = LoggerFactory.getLogger(SyncWorker.class);
	
	public SyncWorker(Main main, BlockingQueue<Properties> propertiesSource, String serverURL, String localPath, 
			Integer refreshRate, Integer alarmBefore) {
		this.main = main;
		this.serverURL = serverURL;
		this.localPath = localPath;
		this.refreshRate = refreshRate;
		this.propertiesSource = propertiesSource;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		while(goOn) {
			try {
				Properties properties = propertiesSource.poll(TIMEOUT, TimeUnit.MILLISECONDS);
				
				if(properties != null) {
					this.serverURL = properties.getProperty(Main.SERVER_ADDRESS);
					this.localPath = properties.getProperty(Main.LOCAL_CALENDAR);
					this.refreshRate = Integer.valueOf(properties.getProperty(Main.REFRESH_RATE));
				}
				if(!serverURL.equals("")) {
					URL remoteURL = new URL(serverURL);
					HttpURLConnection remoteCon = (HttpURLConnection) remoteURL.openConnection();
					remoteCon.setInstanceFollowRedirects(true);
					HttpURLConnection.setFollowRedirects(true);
					InputStream remoteIS = remoteCon.getInputStream();
					
					try {
						Calendar remoteCal = builder.build(remoteIS);
					
						
						File localCalFile = new File(localPath);
						if(!localCalFile.exists()) {
							if(localCalFile.canRead()) {
								if(localCalFile.canWrite()) {
									boolean localCalFileCreated = localCalFile.createNewFile();
									if(localCalFileCreated) {
										publish("Local calendar file was missing nut now created.");
									}
								} else {
									publish("Can't write to local file.");
									break;
								}
							} else {
								publish("Can't read the local file.");
								break;
							}
						} else {
							publish("Local calendar file exists.");
						}
						Calendar localCal = null; FileInputStream localIS = null;
						FileOutputStream fos;
						boolean fetchedRemotely = false;
						if(localCalFile.length() == 0) {
							fos = new FileOutputStream(localPath);
							publish("Local calendar file is empty.");
							ReadableByteChannel rbc = Channels.newChannel(remoteURL.openStream());
							fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
							fetchedRemotely = true;
							fos.close();
						}
						
						localIS = new FileInputStream(localCalFile);
						localCal = builder.build(localIS);
						
						if(fetchedRemotely) {
							localCal.getComponents().clear();
						}
						
						System.out.println("Local calendar file is synchronizing with remote source.");
						ComponentList<CalendarComponent> remoteEvents = remoteCal.getComponents();
						ComponentList<CalendarComponent> localEvents = localCal.getComponents();
						
						// Match events, add missing new events
						boolean isMatch = false;
						for(CalendarComponent remoteEvent : remoteEvents) {
							Property remoteEventUID = remoteEvent.getProperty(Property.UID);
							if(remoteEvent instanceof VEvent) {
								for(CalendarComponent localEvent : localEvents) {
									if(localEvent instanceof VEvent) {
										Property localEventUID = localEvent.getProperty(Property.UID);
										
										if(remoteEventUID.equals(localEventUID)) {
											isMatch = true;
											publish("Event " + remoteEventUID.getName() + ":" + remoteEventUID.getValue() + 
													" was matched.");
											localEvent.getProperties().add(new XProperty(MW_MATCH_NAME, MW_MATCHED));
											break;
										}
									}
								}
								
								if(isMatch) { // event already exists
									isMatch = false;
									continue;
								}else{ // event does not exist so add with reminder
									VEvent remoteToLocalEvent = (VEvent) remoteEvent.copy();
									VAlarm alarm = new VAlarm(new Dur(0, 0, -30, 0));
									alarm.getProperties().add(Action.DISPLAY);
									alarm.getProperties().add(new Description("Microscope is reserved for 30min later!"));
									remoteToLocalEvent.getAlarms().add(alarm);
									remoteToLocalEvent.getProperties().add(
											new XProperty(MW_MATCH_NAME, MW_NEWLY_ADDED_MATCHED));
									localCal.getComponents().add(remoteToLocalEvent);
									publish("Event " + remoteEventUID.getName() + ":" + remoteEventUID.getValue() + 
											" was not matched but copied with an alarm.");
								}
							}	
						}
						System.out.println("Removing cancelled events.");
						// Remove canceled events
						ComponentList<CalendarComponent> eventsToRemove = new ComponentList<CalendarComponent>();
						for(CalendarComponent localEvent : localEvents) {
							if(localEvent instanceof VEvent) {
								if(localEvent.getProperty(MW_MATCH_NAME) == null) {
									eventsToRemove.add(localEvent);
									publish("Event: " + ((VEvent) localEvent).getStartDate().getName() + 
											" was removed remotely.");
								}else{
									localEvent.getProperties().remove(localEvent.getProperty(MW_MATCH_NAME));
								}
							}
						}
						localEvents.removeAll(eventsToRemove);
						
						remoteIS.close();
						if(localIS != null) {
							localIS.close();
						}
						
						fos = new FileOutputStream(localPath);
						CalendarOutputter outputter = new CalendarOutputter();
						outputter.output(localCal, fos);
						fos.close();
					} catch (ValidationException e) {
						logger.warn(Main.exceptionStacktraceToString(e));
						Thread.sleep(refreshRate * 60 * 1000);
						continue;
					}
				}
				Thread.sleep(refreshRate * 60 * 1000);
			} catch (MalformedURLException e) {
				publish(e.getMessage());
				logger.error(Main.exceptionStacktraceToString(e));
			} catch (IOException e) {
				publish(e.getMessage());
				logger.error(Main.exceptionStacktraceToString(e));
			} catch (ParserException e) {
				logger.error(Main.exceptionStacktraceToString(e));
			} catch (ParseException e) {
				publish(e.getMessage());
				logger.error(Main.exceptionStacktraceToString(e));
			} catch (URISyntaxException e) {
				publish(e.getMessage());
				logger.error(Main.exceptionStacktraceToString(e));
			} catch (InterruptedException e) {
				logger.info(Main.exceptionStacktraceToString(e));
				return true;
			}
			
			
		}
		return true;
	}
	
	@Override
	protected void process(List<String> progress) {
		main.process(progress);
	}
	
	
	
	@Override
    protected void done() {
        try {
        	if(isCancelled()) {
        		main.syncWorkerFinished(true);
        	}else {
        		main.syncWorkerFinished(get());
        	}
            
        } catch (Exception ignore) {
        	logger.warn(Main.exceptionStacktraceToString(ignore));
        }
    }

}

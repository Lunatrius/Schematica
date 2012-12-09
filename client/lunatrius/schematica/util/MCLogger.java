package lunatrius.schematica.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public class MCLogger extends Logger {
	private static final class MCFormatter extends Formatter {
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		@Override
		public String format(LogRecord record) {
			StringBuilder message = new StringBuilder();

			message.append(String.format(Locale.ENGLISH, "%s [%s] [%s] %s%s", new Object[] {
					dateFormat.format(Long.valueOf(record.getMillis())),
					record.getLevel().getName(),
					record.getLoggerName(),
					record.getMessage(),
					System.getProperty("line.separator")
			}));

			Throwable throwable = record.getThrown();
			if (throwable != null) {
				StringWriter throwableDump = new StringWriter();
				throwable.printStackTrace(new PrintWriter(throwableDump));
				message.append(throwableDump.toString());
			}

			return message.toString();
		}
	}

	private static final Map<String, MCLogger> loggers = new HashMap<String, MCLogger>();
	private static final File logDirectory = new File(Minecraft.getMinecraftDir(), "logs");

	private MCLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}

	public static MCLogger getLogger(String name) {
		return getLogger(name, null);
	}

	public static MCLogger getLogger(String name, String resourceBundleName) {
		if (!loggers.containsKey(name)) {
			MCLogger logger = new MCLogger(name, resourceBundleName);

			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);

			MCFormatter formatter = new MCFormatter();

			ConsoleHandler handler = new ConsoleHandler();
			handler.setFormatter(formatter);
			handler.setLevel(Level.FINEST);
			logger.addHandler(handler);

			try {
				if (!logDirectory.exists()) {
					logDirectory.mkdirs();
				}

				File logFile = new File(logDirectory, name + "-%g.log");

				FileHandler fileHandler = new FileHandler(logFile.getPath(), 0, 5);
				fileHandler.setFormatter(formatter);
				fileHandler.setLevel(Level.ALL);
				logger.addHandler(fileHandler);
			} catch (Exception e) {
			}

			loggers.put(name, logger);
		}

		return loggers.get(name);
	}

	public void log(Throwable thrown) {
		log(Level.SEVERE, thrown.getMessage(), thrown);
	}
}

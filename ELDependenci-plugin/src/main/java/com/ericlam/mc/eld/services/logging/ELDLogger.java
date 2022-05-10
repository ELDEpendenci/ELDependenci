package com.ericlam.mc.eld.services.logging;

import com.ericlam.mc.eld.bukkit.ELDConfig;
import com.ericlam.mc.eld.misc.DebugLogger;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ELDLogger extends Logger implements DebugLogger {

    private final ELDConfig config;


    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @throws MissingResourceException if the resourceBundleName is non-null and
     *                                  no corresponding resource can be found.
     */
    private ELDLogger(String name, String resourceBundleName, ELDConfig config) {
        super(name, resourceBundleName);
        this.config = config;
        setParent(Bukkit.getLogger());
        setLevel(Level.ALL);
    }

    public ELDLogger(String name, ELDConfig config) {
        this(name, null, config);
    }


    public ELDLogger(Class<?> cls, ELDConfig config) {
        this(cls.getName(), null, config);
    }


    @Override
    public void debug(String message, Object... args) {
        if (config.debugLogging) {
            super.info("[DEBUG] " + MessageFormat.format(message, args));
        } else {
            super.config(MessageFormat.format(message, args));
        }
    }

    @Override
    public void debug(Throwable throwable, String message, Object... args) {
        if (config.debugLogging) {
            super.log(Level.INFO, "[DEBUG] " + MessageFormat.format(message, args), throwable);
        } else {
            super.log(Level.CONFIG, MessageFormat.format(message, args), throwable);
        }
    }

    @Override
    public void debugF(String message, Object... args) {
        if (config.debugLogging) {
            super.info("[DEBUG] " + String.format(message, args));
        } else {
            super.config(String.format(message, args));
        }
    }

    @Override
    public void debugF(Throwable throwable, String message, Object... args) {
        if (config.debugLogging) {
            super.log(Level.INFO, "[DEBUG] " + String.format(message, args), throwable);
        } else {
            super.log(Level.CONFIG, String.format(message, args), throwable);
        }
    }

    @Override
    public void debug(Throwable throwable) {
        if (config.debugLogging) {
            super.log(Level.INFO, "[DEBUG] " + throwable.getMessage(), throwable);
        } else {
            super.log(Level.CONFIG, throwable.getMessage(), throwable);
        }
    }

    @Override
    public void info(String message, Object... args) {
        super.info(MessageFormat.format(message, args));
    }

    @Override
    public void info(Throwable throwable, String message, Object... args) {
        super.log(Level.INFO, MessageFormat.format(message, args), throwable);
    }

    @Override
    public void infoF(String message, Object... args) {
        super.info(String.format(message, args));
    }

    @Override
    public void infoF(Throwable throwable, String message, Object... args) {
        super.log(Level.INFO, String.format(message, args), throwable);
    }

    @Override
    public void info(Throwable throwable) {
        super.log(Level.INFO, throwable.getMessage(), throwable);
    }

    @Override
    public void warn(String message, Object... args) {
        super.warning(MessageFormat.format(message, args));
    }

    @Override
    public void warn(Throwable throwable, String message, Object... args) {
        super.log(Level.WARNING, MessageFormat.format(message, args), throwable);
    }

    @Override
    public void warnF(String message, Object... args) {
        super.warning(String.format(message, args));
    }

    @Override
    public void warnF(Throwable throwable, String message, Object... args) {
        super.log(Level.WARNING, String.format(message, args), throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        super.log(Level.WARNING, throwable.getMessage(), throwable);
    }

    @Override
    public Logger getRealLogger() {
        return this;
    }
}

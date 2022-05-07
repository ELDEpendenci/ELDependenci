package com.ericlam.mc.eld.services.logging;

import com.ericlam.mc.eld.bukkit.ELDConfig;
import com.ericlam.mc.eld.misc.DebugLogger;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ELDLogger extends Logger implements DebugLogger {

    @Inject
    private ELDConfig config;


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
    private ELDLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }


    public ELDLogger(String name) {
        this(name, null);
    }


    public ELDLogger(Class<?> cls){
        this(cls.getName(), cls.getPackageName());
    }


    @Override
    public void debug(String message, Object... args) {
        if (config.debugLogging) {
            this.info("[DEBUG] " + MessageFormat.format(message, args));
        }else{
            super.config(MessageFormat.format(message, args));
        }
    }

    @Override
    public void debug(Throwable throwable, String message, Object... args) {
        if (config.debugLogging) {
            this.info("[DEBUG] " + MessageFormat.format(message, args));
        }else{
            super.log(Level.CONFIG, MessageFormat.format(message, args), throwable);
        }
    }

    @Override
    public void debug(Throwable throwable) {
        if (config.debugLogging) {
            super.log(Level.INFO, "[DEBUG] " + throwable.getMessage(), throwable);
        }else{
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
    public void warn(Throwable throwable) {
        super.log(Level.WARNING, throwable.getMessage(), throwable);
    }

    @Override
    public Logger getRealLogger() {
        return this;
    }
}

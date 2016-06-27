//

//

package com.cloud.utils;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Journal is used to kept what has happened during a process so someone can track
 * what happens during a process.
 */
public class Journal {
    String _name;
    ArrayList<Pair<String, Object[]>> _entries;

    public Journal(final String name) {
        _name = name;
        _entries = new ArrayList<>();
    }

    public void record(final String msg, final Object... params) {
        log(msg, params);
    }

    private void log(final String msg, final Object... params) {
        final Pair<String, Object[]> entry = new Pair<>(msg, params);
        assert msg != null : "Message can not be null or else it's useless!";
        _entries.add(entry);
    }

    public void record(final Logger logger, final Level p, final String msg, final Object... params) {
        if (logger.isEnabledFor(p)) {
            final StringBuilder buf = new StringBuilder();
            toString(buf, msg, params);
            final String entry = buf.toString();
            log(entry);
            logger.log(p, entry);
        } else {
            log(msg, params);
        }
    }

    protected void toString(final StringBuilder buf, final String msg, final Object[] params) {
        buf.append(msg);
        if (params != null) {
            buf.append(" - ");
            final int i = 0;
            for (final Object obj : params) {
                buf.append('P').append(i).append('=');
                buf.append(obj != null ? obj.toString() : "null");
                buf.append(", ");
            }
            buf.delete(buf.length() - 2, buf.length());
        }
    }

    @Override
    public String toString() {
        return toString("; ");
    }

    public String toString(final String separator) {
        final StringBuilder buf = new StringBuilder(_name).append(": ");
        for (final Pair<String, Object[]> entry : _entries) {
            toString(buf, entry.first(), entry.second());
            buf.append(separator);
        }
        return buf.toString();
    }

    public static class LogJournal extends Journal {
        Logger _logger;

        public LogJournal(final String name, final Logger logger) {
            super(name);
            _logger = logger;
        }

        @Override
        public void record(final String msg, final Object... params) {
            record(_logger, Level.DEBUG, msg, params);
        }
    }
}

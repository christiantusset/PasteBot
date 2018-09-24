package net.notfab.pastebot.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.concurrent.CopyOnWriteArraySet;

public class ConsoleFilter extends AbstractMatcherFilter<ILoggingEvent> {

    private static CopyOnWriteArraySet<String> excludedNames = new CopyOnWriteArraySet<>();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // So we can block names during runtime
        if (!excludedNames.isEmpty()) {
            for (String name : excludedNames) {
                if (event.getLoggerName().contains(name)) {
                    return FilterReply.DENY;
                }
                if (event.getMessage().contains(name)) {
                    return FilterReply.DENY;
                }
                if (event.getThreadName().contains(name)) {
                    return FilterReply.DENY;
                }
            }
        }
        return FilterReply.ACCEPT;
    }

}

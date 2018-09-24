package net.notfab.pastebot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class FileNormal extends AbstractMatcherFilter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if(event.getLevel().levelInt <= Level.DEBUG.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

}

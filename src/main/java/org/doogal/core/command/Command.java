package org.doogal.core.command;

public interface Command {
    String getDescription();

    String getLargeIcon();

    String getSmallIcon();

    CommandType getType();
}

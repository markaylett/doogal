package org.doogal.notes.command;

public interface Command {
    String getDescription();

    String getLargeIcon();

    String getSmallIcon();

    CommandType getType();
}

package org.doogal.core;

public interface Command {
    String getDescription();

    String getLargeIcon();

    String getSmallIcon();

    CommandType getType();
}

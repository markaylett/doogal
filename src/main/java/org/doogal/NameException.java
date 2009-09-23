package org.doogal;

final class NameException extends Exception {
    private static final long serialVersionUID = 1L;

    NameException() {
    }

    NameException(String s) {
        super(s);
    }
}

package org.doogal;

final class ExitException extends Exception {
	private static final long serialVersionUID = 1L;

	ExitException() {
		super("exit");
	}
}

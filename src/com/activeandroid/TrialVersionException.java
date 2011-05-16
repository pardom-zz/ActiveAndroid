package com.activeandroid;

public class TrialVersionException extends RuntimeException {
	private static final long serialVersionUID = 7340107277960725627L;

	public TrialVersionException() {
		super();
	}
	
	public TrialVersionException(String string) {
		super(string);
	}
	
	@Override
	public String getMessage() {
		return "The trial version of ActiveAndroid only runs in the emulator.";
	}
}

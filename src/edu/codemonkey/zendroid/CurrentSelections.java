package edu.codemonkey.zendroid;

public class CurrentSelections {
	private CurrentSelections() {
		
	}
	private ScanObject currentScanObject = new ScanObject();
	private static CurrentSelections instance = new CurrentSelections();
	public static CurrentSelections getInstance() {
		return instance;
	}
	public ScanObject getCurrentScanObject() {
		return currentScanObject;
	}
	public void setCurrentScanObject(ScanObject scanObject) {
		currentScanObject = scanObject;
	}
}

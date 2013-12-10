package edu.codemonkey.zendroid;

import edu.codemonkey.zendroid.Utilities.Utilities;

public class ScanObject {
	private String fullCommand ="";	
	private String result="";
	private String oldfullCommand="";
	private String oldResult="";
	private int id = 0;
	private boolean saved = true;
	public ScanObject() {
		this.fullCommand = "";
		this.result = "";
		id = LocalDBHandler.getInstance().getNextId();
		this.saved = false;
	}
	
	public ScanObject(int id, String fullCommand, String result) {
		this.fullCommand = fullCommand;
		oldfullCommand = fullCommand;
		this.result = result;
		oldResult = result;
		this.id = id;
		this.saved = true;
	}
	public String getFullCommand() {
		return fullCommand;
	}
	public void setFullCommand(String fullCommand) {
		this.fullCommand = fullCommand;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public boolean deleteScan() {
		if(!oldResult.equals("") ) {
			Utilities.removeFile(oldResult);
		}
		if(LocalDBHandler.getInstance().deleteScan(id)) {
			return LocalDBHandler.getInstance().scanList.remove(this);
		}
		return false;
	}
	public boolean saveScan() {
		if (!saved) {
			
			oldResult = result;
			if(LocalDBHandler.getInstance().saveScan(id, fullCommand, result)){
				LocalDBHandler.getInstance().scanList.add(this);
				saved = true;
				return true;
			}
			return false;
		}
		if(oldResult.equals("") || !oldResult.equals(result)) {
			Utilities.removeFile(oldResult);
		}
		oldResult = result;
		return LocalDBHandler.getInstance().scanUpdate(id, fullCommand, result);	
	}
	public int getId() {
		return id;
	}
	public String toString() {
		return id+" "+fullCommand;
	}
	
	public boolean equals(ScanObject scan) {
		return (scan.getId() == id);
	}
}

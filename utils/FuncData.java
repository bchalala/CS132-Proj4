package utils;

import java.util.Vector;
import java.util.HashMap;
import java.util.Map;

import cs132.vapor.ast.VFunction;

// Should store the variable name
public class FuncData {

	// Data structures to map names to their 
	private Vector<String> names = new Vector<String>();
	private HashMap<String, Integer> vToL = new HashMap<String, Integer>();
	private int curLocal = 0;

	private Vector<String> paramNames = new Vector<String>();
	private HashMap<String, Integer> paramVToL = new HashMap<String, Integer>();
	private int curParam = 0;

	private int numOut = 0;

	private String body = "";

	public FuncData() {return;}

	// Test if the names of variables get correctly added
	public void writeNames() {
		for (String s: names) {
			System.out.println(s + " : local " + getNameLocal(s));
		}

		for (String s: paramNames) {
			System.out.println(s + " : in " + getParamNameIn(s));
		}

		return;
	}

	public void writeBody() {
		System.out.println(body);
	}

	// Returns the string that refers to the variable name. Could be local[] or in[] or register 
	// At this point, I am not implementing registers until I am done with everything and have 
	// ample time left.
	public String getString(String name) {
		Integer val = vToL.get(name);
		String ret = "local[";

		// Checks if the string is in either map. (Will eventually implement register retrieval)
		if (val == null) {
			ret = "in[";
			val = paramVToL.get(name);
			if (val == null) {
				return name;
			}
		}

		ret += val.intValue() + "]";
		return ret;
	}

	// Utility function to see if the name is in either dictionary.
	public boolean isNotVar(String s) {
		Integer val = vToL.get(s);
		if (val == null) {
			val = paramVToL.get(s);
			if (val == null) {
				return true;
			}
		}

		return false;
	}

	// Adds variable names to hashmap
	public void addName(String name) {
		names.add(name);
		vToL.put(name, curLocal);
		curLocal++;
	}

	public int getNameLocal(String name) {
		Integer retval =  vToL.get(name);
		if (retval == null) {
			return -1;
		}
		return retval.intValue();
	}

	// Adds params to their own hashmap
	public void addParamName(String name) {
		paramNames.add(name);
		paramVToL.put(name, curParam);
		curParam++;
	}

	public int getParamNameIn(String name) {
		Integer retval =  paramVToL.get(name);
		if (retval == null) {
			return -1;
		}
		return retval.intValue();
	}


	// Sets the number that should be in the out segment of the function
	public boolean setNumOut(int out) {
		if (numOut < out) {
			numOut = out;
			return true;
		}

		return false;
	}

	// The number of parameters that should be in the stack
	public int numLocal() {
		return curLocal;
	}

	public int numIn() {
		return curParam;
	}

	public int numOut() {
		return numOut;
	}

	public void addLine(String line) {
		body = body + line + "\n";
	}

	public void addHeader(String line) {
		body = line + "\n" + body;
	}

}
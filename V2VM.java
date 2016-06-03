import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;

import utils.FuncData;
import myvisitor.MInstrVisitor;

public class V2VM {

	public static void main(String[] args) {
		try { 
			// Parse the vapor program
			VaporProgram program = parseVapor(System.in, System.err); 
			if (program.dataSegments != null) {
				for (VDataSegment ds: program.dataSegments) {
					System.out.println("const " + ds.ident);
					if (ds.values != null) {
						for (VOperand.Static v: ds.values) {
							System.out.println(v.toString());
						}
					}
					System.out.println("");
				}
			}

			// Parse the individual functions within the vapor program
			Vector<FuncData> fdata = new Vector<FuncData>();
			int i = 0;
			for (VFunction vf: program.functions) {
				fdata.add(translateFunction(vf, i));
				fdata.lastElement().writeBody();
				i++;
			}

		}
		catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public static VaporProgram parseVapor(InputStream in, PrintStream err)
	throws IOException
	{
		Op[] ops = {
			Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
			Op.PrintIntS, Op.HeapAllocZ, Op.Error,
		};
		boolean allowLocals = true;
		String[] registers = null;
		boolean allowStack = false;

		VaporProgram program;
		try {
			program = VaporParser.run(new InputStreamReader(in), 1, 1,
				java.util.Arrays.asList(ops),
				allowLocals, registers, allowStack);
		}
		catch (ProblemException ex) {
			err.println(ex.getMessage());
			return null;
		}

		return program;
	}

	public static FuncData translateFunction(VFunction func, int index) {
		// 
		FuncData curFData = new FuncData();

		// Assign all parameters to an in variable
		for (VVarRef.Local p: func.params) {
			curFData.addParamName(p.toString());
		}

		for (String name: func.vars) {
			if (curFData.getParamNameIn(name) == -1) {
				curFData.addName(name);
			}
		}

		// Visit all the instructions and translate them.
		MInstrVisitor instrvis = new MInstrVisitor(curFData);
		int linenum = 1;
		int labelnum = 0;
		int maxlabelnum = func.labels.length;
		for (VInstr inst: func.body) {
			try {
				if (labelnum < maxlabelnum 
					&& func.labels[labelnum].instrIndex < linenum) {
					curFData.addLine(func.labels[labelnum].ident + ":");
					labelnum++;
				}
				inst.accept(instrvis);
				linenum++;
			}
			catch (Throwable t) {
				System.out.println("Error, a throwable was thrown! Oh no! Not throwables!");
			}
		}

		curFData = instrvis.funcdata;
		String header = "func " + func.ident + " [in " + curFData.numIn() 
						+ ", out " + curFData.numOut() 
						+ ", local " + curFData.numLocal() + "]";

		curFData.addHeader(header);

		// Return the completed function data
		return curFData;
	}

}

package myvisitor;

import java.util.*;
import cs132.vapor.ast.*;
import utils.*;

public class MInstrVisitor extends VInstr.Visitor<Throwable>{

	public static FuncData funcdata;

	public MInstrVisitor(FuncData f) {
		funcdata = f;
	}

	public void visit(VAssign a) throws Throwable {
		String l1 = "$t0 = " + funcdata.getString(a.source.toString());
		String l2 = funcdata.getString(a.dest.toString()) + " = $t0";
		funcdata.addLine(l1);
		funcdata.addLine(l2);
	} 

	public void visit(VBranch b) throws Throwable {

		// Build the string for the if line.
		String l1 = "$t0 = " + funcdata.getString(b.value.toString());
		String ifline = "if";
		if (!b.positive) {
			ifline += "0";
		}

		ifline += " $t0 goto " + b.target.toString();
		funcdata.addLine(l1);
		funcdata.addLine(ifline);

	}

	public void visit(VBuiltIn c) throws Throwable {
		// Number of the current temp we are using
		int curT = 1;

		// Begin to build translation. Postamble applies if the built-in returns a value.
		String biCall = "";
		String biCallPostamble = "";
		String biCallPreamble = "";
		if (c.dest != null) {
			biCall = "$t0 = ";
			biCallPostamble = "\n" + funcdata.getString(c.dest.toString()) + " = $t0";
		}

		biCall += c.op.name + "(";

		// Iterate over parameters, add them to the call and generate preamble if necessary.
		for (int i = 0; i < c.op.numParams; i++) {
			String arg = c.args[i].toString();
			if (i != 0)
				biCall += " ";

			// If the argument is a number, don't write preamble.
			if (funcdata.isNotVar(arg)) {
				biCall += arg;
				continue;
			}

			biCallPreamble += "$t" + curT + " = " + funcdata.getString(arg) + "\n";
			biCall += "$t" + curT;
			curT++;
		}

		biCall += ")";
		biCall = biCallPreamble + biCall + biCallPostamble;
		funcdata.addLine(biCall);

	}

	public void visit(VCall c) throws Throwable {
		int outlen = c.args.length;
		funcdata.setNumOut(outlen);

		for (int i = 0; i < outlen; i++) {
			String l1 = "$t0 = " + funcdata.getString(c.args[i].toString()) + "\n";
			l1 += "out[" + i + "] = $t0";
			funcdata.addLine(l1);
		}
		String call = "$t0 = " + funcdata.getString(c.addr.toString()) + "\n";
		call += "call $t0\n";
		call += funcdata.getString(c.dest.toString()) + " = $v0";
		funcdata.addLine(call);
	}

	public void visit(VGoto g) throws Throwable {
		funcdata.addLine("goto " + funcdata.getString(g.target.toString()));
	}

	public void visit(VMemRead r) throws Throwable {
		// Typecase the VMemRef in order to get the string and offset.
		VMemRef.Global source = (VMemRef.Global) r.source;
		String l0 = "$t0 = " + funcdata.getString(source.base.toString());

		String memref = "$t0";
		if (source.byteOffset != 0)
			memref += "+" + source.byteOffset;

		String l1 = "$t1 = [" + memref + "]";
		String l2 = funcdata.getString(r.dest.toString()) + " = $t1";
		funcdata.addLine(l0);
		funcdata.addLine(l1);
		funcdata.addLine(l2);
	}

	public void visit(VMemWrite w) throws Throwable {
		VMemRef.Global dest = (VMemRef.Global) w.dest;
		String l0 = "$t0 = " + funcdata.getString(dest.base.toString());

		String memref = "$t0";
		if (dest.byteOffset != 0) 
			memref += "+" + dest.byteOffset;

		String l1 = "$t1 = " + funcdata.getString(w.source.toString());
		String l2 = "[" + memref + "] = $t1";
		funcdata.addLine(l0);
		funcdata.addLine(l1);
		funcdata.addLine(l2);
	}

	public void visit(VReturn r) throws Throwable {
		// Only writes to the v0 reg if there is a value being returned.
		if (r.value != null) {
			String l1 = "$v0 = " + funcdata.getString(r.value.toString());
			funcdata.addLine(l1);
		}

		String l2 = "ret";
		funcdata.addLine(l2);
	}

}
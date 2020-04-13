package mint.inference.gp.tree.terminals;

import java.util.Random;

import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 14/11/2017.
 */
public class ReadDef extends StringVariableAssignmentTerminal {

	Random rand;
	VariableAssignment<Double> dvar = null;

	public ReadDef(VariableAssignment<Double> var, Random r) {
		super(new StringVariableAssignment("result"), true, false);
		this.rand = r;
		this.dvar = var;
	}

	@Override
	public StringVariableAssignmentTerminal copy() {
		return new ReadDef(dvar.copy(), rand);
	}

}

package mint.inference.gp.tree.terminals;

import java.util.HashSet;
import java.util.Set;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;

import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NodeVisitor;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.StringVariableAssignment;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 04/03/15.
 */
public class StringVariableAssignmentTerminal extends VariableTerminal<StringVariableAssignment> {

	public StringVariableAssignmentTerminal(VariableAssignment<String> var, boolean constant, boolean latent) {
		super(constant, latent);
		this.terminal = (StringVariableAssignment) var;
	}

	// For initialising constants
	public StringVariableAssignmentTerminal(String value) {
		super(true, false);
		StringVariableAssignment var = new StringVariableAssignment(value, value, true);
		this.terminal = var;
	}

	@Override
	public StringVariableAssignmentTerminal copy() {
		VariableAssignment<String> copied = terminal.copy();
		return new StringVariableAssignmentTerminal(copied, constant, LATENT);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		visitor.visitEnter(this);
		return visitor.visitExit(this);
	}

	@Override
	public void setValue(Object val) {
		terminal.setValue(val.toString());
	}

	@Override
	protected Terminal<StringVariableAssignment> getTermFromVals() {
		StringVariableAssignment svar = new StringVariableAssignment("res", vals.iterator().next().toString());
		StringVariableAssignmentTerminal term = new StringVariableAssignmentTerminal(svar, true, false);
		return term;
	}

	@Override
	public Expr toZ3(Context ctx) {
		if (this.isConstant()) {
			String val = this.getTerminal().getValue();
			return ctx.mkString(val);
		}
		if (this.isLatent()) {
			return ctx.mkConst(ctx.mkFuncDecl("latent" + this.getName(), new Sort[] {}, ctx.mkStringSort()));
		}

		return ctx.mkConst(ctx.mkFuncDecl(this.getName(), new Sort[] {}, ctx.mkStringSort()));
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> v = new HashSet<VariableTerminal<?>>();
		v.add(this.copy());
		return v;
	}

	@Override
	public Datatype[] typeSignature() {
		return new Datatype[] { Datatype.STRING };
	}

	@Override
	protected Node<StringVariableAssignment> newInstance() {
		return new StringVariableAssignmentTerminal(terminal, constant, LATENT);
	}
}

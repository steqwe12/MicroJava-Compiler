package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.FormPart;
import rs.ac.bg.etf.pp1.ast.VarId;
import rs.ac.bg.etf.pp1.ast.VarIdArray;
import rs.ac.bg.etf.pp1.ast.VarPart;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {

	protected int count;
	
	public int getCount() {
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor{
		
		public void visit(FormPart formPart) {
			count++;
		}
	}
	
	public static class VarCounter extends CounterVisitor{
		
	//	public void visit(VarPart varPart) { // ili mozda zbog erora samo VarId i VarIdArray
	//		count++;
	//	}
		
		public void visit(VarId VarId) { 
			count++;
		}
		
		public void visit(VarIdArray VarIdArray) { 
			count++;
		}
	}
	
	
}

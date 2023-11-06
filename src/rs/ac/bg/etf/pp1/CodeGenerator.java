package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	private int brojac=0;
	//private Stack<Obj> stek=new Stack<>();
	
/*	class Member{
		public String labela;
		public int pc;
		
		Member(String labela, int pc){this.labela=labela;this.pc=pc;}
	};
	
	private List<Member> hehelista = new ArrayList<>();
	
	hehelista.add(new Member...);
	
	for (Member m : hehelista){
		if() // koristi se .equals
	}
	
	
	Code.put(Code.jmp);
	int fup=Code.pc;
	Code.put2(0);
		
	...
	
	Code.fixup(fup);
	
	
	
	int unazad=Code.pc;
	
	...
	
	Code.put(Code.jmp);
	Code.put2(unazad-Code.pc+1);
	
	
	
	*/
	
	public int getMainPc() {
		return mainPc;
	}
	
	
	public void visit(PrintStatement printStatement) {
		if (printStatement.getExpr().struct.getKind() == Struct.Int || printStatement.getExpr().struct.getKind() == Struct.Bool) {
			Code.loadConst(5);
			Code.put(Code.print);
		}else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}						
	}
	
	public void visit(PrintStatementNumArg printStatementNumArg) {
		if (printStatementNumArg.getExpr().struct.getKind() == Struct.Int || printStatementNumArg.getExpr().struct.getKind() == Struct.Bool) {
			Code.loadConst(printStatementNumArg.getNumber());
			Code.put(Code.print);
		}else {
			Code.loadConst(printStatementNumArg.getNumber());
			Code.put(Code.bprint);
		}				
	}
	
	public void visit(ReadStatement readStatement) {
		Obj o = readStatement.getDesignator().obj;
		
			if(o.getType().getKind()==Struct.Int || o.getType().getKind()==Struct.Bool) 
				Code.put(Code.read);
			
			else 
				Code.put(Code.bread);
			
			Code.store(o);														
	}
	
	public void visit(FactorNum factorNum) {
		Obj con = Tab.insert(Obj.Con, "$", factorNum.struct);
		con.setLevel(0);
		con.setAdr(factorNum.getN1());
		
		Code.load(con);
	}
	
	public void visit(FactorChar factorChar) {
		Obj con = Tab.insert(Obj.Con, "$", factorChar.struct);
		con.setLevel(0);
		con.setAdr(factorChar.getC1());
		
		Code.load(con);
	}
	
	public void visit(FactorBool factorBool) {
		Obj con = Tab.insert(Obj.Con, "$", factorBool.struct);
		con.setLevel(0);
		
		if(factorBool.getB1() == true) con.setAdr(1);
		else con.setAdr(0);
		
		Code.load(con);
	}
	
	public void visit(RtrnTypVoid rtrnTypVoid) {
		
		if("main".equalsIgnoreCase(rtrnTypVoid.getMethName())) {
			mainPc = Code.pc;
		}
		
		rtrnTypVoid.obj.setAdr(Code.pc);
		// collect arguments and local variables
		SyntaxNode methodNode = rtrnTypVoid.getParent();
		
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		// generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
				
	}
	
	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(DesignatorStatementAopExpr designatorStatementAopExpr) {	
			Code.store(designatorStatementAopExpr.getDesignator().obj);			
	}
	
			
	
	public void visit(FactorDes factorDes) {
		Code.load(factorDes.getDesignator().obj);
	}
	
	public void visit(FactorNewArray factorNewArray) {			// za = new xyz[expr]	
		Code.put(Code.newarray);
        if ( factorNewArray.getType().struct == Tab.intType ) 
			Code.put(1); 
        else 
			Code.put(0);
	}
	
	public void visit(DesignatorSingle designatorSingle) {  // za (DesignatorStatementAopExpr) Designator:d Assignop Expr:e a i za ostale(inc,dec)
		SyntaxNode parent = designatorSingle.getParent();	// ako je Elem niza
		if (parent.getClass()==DesignatorMultiExpr.class)
			Code.load(designatorSingle.obj);				
	}
	
	public void visit(DesignatorStatementInc designatorStatementInc) { //inc
		
		if(designatorStatementInc.getDesignator().obj.getKind()==Obj.Var) {
			Code.load(designatorStatementInc.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(designatorStatementInc.getDesignator().obj);
		}
		
		
		if(designatorStatementInc.getDesignator().obj.getKind()==Obj.Elem) {
			Code.put(Code.dup2);
			Code.load(designatorStatementInc.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(designatorStatementInc.getDesignator().obj);
		}
	}
	
	public void visit(DesignatorStatementDec designatorStatementDec) {
		
		if(designatorStatementDec.getDesignator().obj.getKind()==Obj.Var) {
			Code.load(designatorStatementDec.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(designatorStatementDec.getDesignator().obj);
		}
		
		
		if(designatorStatementDec.getDesignator().obj.getKind()==Obj.Elem) {
			Code.put(Code.dup2);
			Code.load(designatorStatementDec.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(designatorStatementDec.getDesignator().obj);
		}
	}
	
	// mulop, addop
	
	public void visit(MulopMul mulopMul) {
		mulopMul.obj = new Obj(Obj.NO_VALUE,"",Tab.noType,1,Obj.NO_VALUE);
	}
	
	public void visit(MulopDiv mulopDiv) {
		mulopDiv.obj = new Obj(Obj.NO_VALUE,"",Tab.noType,2,Obj.NO_VALUE);
	}
	
	public void visit(MulopRem mulopRem) {
		mulopRem.obj = new Obj(Obj.NO_VALUE,"",Tab.noType,3,Obj.NO_VALUE);	
	}
	
	public void visit(AddopPlus addopPlus) {
		addopPlus.obj = new Obj(Obj.NO_VALUE,"",Tab.noType,4,Obj.NO_VALUE);
	}
	
	public void visit(AddopMinus addopMinus) {
		addopMinus.obj = new Obj(Obj.NO_VALUE,"",Tab.noType,5,Obj.NO_VALUE);
	}
	
	
	public void visit(TermMulti termMulti) {
		if(termMulti.getMulop().obj.getAdr()==1) Code.put(Code.mul);
		else if(termMulti.getMulop().obj.getAdr()==2) Code.put(Code.div);
		else if(termMulti.getMulop().obj.getAdr()==3) Code.put(Code.rem);
	}
	
	public void visit(ExprMulti exprMulti) {
		if(exprMulti.getAddop().obj.getAdr()==4) Code.put(Code.add);
		else if(exprMulti.getAddop().obj.getAdr()==5) Code.put(Code.sub);
	}
	
	public void visit(ExprSingleMinus exprSingleMinus) {
		Code.put(Code.neg);
	}
	
	// AVGUST/SEPT DOPUNA
		public void visit(StatementFindAny statementFindAny) {
			Code.load(statementFindAny.getDesignator1().obj);
			Code.put(Code.arraylength);
			Code.loadConst(1);
			Code.put(Code.sub);			// expr n-1
			
			int b=Code.pc;
			
			Code.put(Code.dup2);		// expr n-1 expr n-1
			Code.load(statementFindAny.getDesignator1().obj);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);			// expr n-1 expr niz n-1
			if (statementFindAny.getDesignator1().obj.getType().getElemType().getKind()==Struct.Int)
				Code.put(Code.aload);				
			else
				Code.put(Code.baload);
										// expr n-1 expr niz[n-1]
			Code.put(Code.jcc+Code.eq);
			int a=Code.pc;
			Code.put2(0);
			
			
			Code.loadConst(1);
			Code.put(Code.sub);			
			Code.put(Code.dup);			// expr n-2 n-2
			Code.loadConst(0);			// expr n-2 n-2 0
			Code.put(Code.jcc+Code.ge);
			Code.put2(b-Code.pc+1);
			
			//ovde pushujemo false i preskacemo bezuslovno pushovaje true vrednosti
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.loadConst(0);
					
			Code.put(Code.jmp);
			int c=Code.pc;
			Code.put2(0);
			
			
			Code.fixup(a);
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.loadConst(1);
			// ovde pushujemo true
			
			
			Code.fixup(c);
			
			
			
			
			if(statementFindAny.getDesignator().obj.getKind()==Obj.Elem) {
				if (statementFindAny.getDesignator().obj.getType().getKind()==Struct.Int)
					Code.put(Code.astore);				
				else
					Code.put(Code.bastore);
			}
			else {
				Code.store(statementFindAny.getDesignator().obj);
			}
			
		}
	
	
/*	//najtezi deo
	
	public void visit(Designatoraa designatoraa) {
		brojac++;
		stek.push(designatoraa.obj);
	}
	
	public void visit(NoDesignatorr noDesignatorr) {
		brojac++;
		stek.push(noDesignatorr.obj);
	}
	
	
	public void visit(DesignatorStatementDesOptList designatorStatementDesOptList) {
		Code.load(designatorStatementDesOptList.getDesignator().obj);
		Code.put(Code.dup);
		Code.put(Code.arraylength);
		Code.loadConst(brojac);
		Code.put(Code.jcc+Code.ge); 
		Code.put2(5);
		
		Code.put(Code.trap);
		Code.loadConst(1);
		
		for(int i=brojac ; i>0 ; i--){
			Obj o = stek.pop();
			
			if(o.getKind()==Obj.Elem) {
				if(i!=1)
					Code.put(Code.dup_x2);
				Code.loadConst(i-1);
								
				if(designatorStatementDesOptList.getDesignator().obj.getType().getElemType().getKind()==Struct.Int)
					Code.put(Code.aload);				
				else
					Code.put(Code.baload);
				
				Code.store(o);	
					
			}
			else if(o.getKind()==Obj.Var) {
				if(i!=1)
					Code.put(Code.dup);
				Code.loadConst(i-1);
				
				if(designatorStatementDesOptList.getDesignator().obj.getType().getElemType().getKind()==Struct.Int)
					Code.put(Code.aload);
				else
					Code.put(Code.baload);
				
				Code.store(o);	
			}			
		}
		
		
		
		//reset
		brojac=0;
	}
	
	*/
	
	
	
}

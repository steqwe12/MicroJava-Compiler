package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	
	boolean errorDetected = false;
	Struct var_type = Tab.noType;
	// Struct const_type = Tab.noType;
	String cn;
	String trenKlasa = " ";
	int imaMain = 0;
	int imaVoid=0,imaNaziv=0,nemaArg=0;
	int nVars;
	
	
	
	public String stampaj (Obj o) {
		StringBuffer sb = new StringBuffer();            
		
		switch (o.getKind()) {
			case Obj.Con: sb.append("Con "); break;
			case Obj.Var: sb.append("Var "); break;
			case Obj.Type: sb.append("Type "); break;
			case Obj.Meth: sb.append("Meth "); break;
			case Obj.Fld: sb.append("Fld ");  break;
			case Obj.Prog: sb.append("Prog "); break;
			case Obj.Elem: sb.append("Elem "); break;
		}
		sb.append(o.getName());
		sb.append(": ");
		
		switch (o.getType().getKind()) {
			case Struct.None: sb.append("notype"); break;
			case Struct.Int: sb.append("int"); break;
			case Struct.Char: sb.append("char"); break;
			case Struct.Array: sb.append("Arr of "); 
                        	 switch (o.getType().getElemType().getKind()) {
                        	 	case Struct.None: sb.append("notype"); break;
                        	 	case Struct.Int: sb.append("int"); break;
                        	 	case Struct.Char: sb.append("char"); break;
                        	 	case Struct.Bool: sb.append("bool"); 			             
		               }
			         break;
			case Struct.Bool: sb.append("bool"); break;
		}		
		sb.append(", ");
		if (o.getType().getKind()==Struct.Char && o.getKind()==Obj.Con)
			sb.append((char)(o.getAdr()));
		else
			sb.append(o.getAdr());
		sb.append(", ");
		sb.append(o.getLevel()+" ");
				 		
		return sb.toString();
	}

	
	
	
	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info, Obj o) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
			msg.append("  ").append(stampaj(o));
		log.info(msg.toString());
	}	
	
    public void visit(ProgName progName) {
    	progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	Tab.openScope();
    }
    
    public void visit(Program program) {
    	nVars = Tab.currentScope.getnVars();
    	Tab.chainLocalSymbols(program.getProgName().obj);
    	Tab.closeScope();
    	
    	if (imaMain!=1)
    		report_error("program mora da ima jednu main metodu  void main() " + program.getProgName() + " ", null);
    }
    
    public void visit(Type type) {
    	if (trenKlasa==type.getTypeName()) report_error("greska:deklarisanje promenljive tipa klase koja jos nije deklarisana "+type.getTypeName()+"",null);
    	
    	Obj typeNode = Tab.find(type.getTypeName());
    	if(typeNode == Tab.noObj){
    		report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
    		var_type = type.struct = Tab.noType;
    	}else{
    		if(Obj.Type == typeNode.getKind()){
    			var_type = type.struct = typeNode.getType();
    		}else{
    			report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
    			var_type = type.struct = Tab.noType;
    		}
    	}
    }
    // ovde
    public void visit(VarId varId) {
    	Obj res = Tab.currentScope.findSymbol(varId.getVarName());
    	if (res!=null) {
    		report_error("var/const sa imenom "+varId.getVarName()+" vec postoji ***",varId);
    	}else
    	 Tab.insert(Obj.Var, varId.getVarName(), var_type);
    }
    // ovde
    public void visit(VarIdArray varIdArray) {
    	Obj res = Tab.currentScope.findSymbol(varIdArray.getVarName());
    	if (res!=null) {
    		report_error("var/const sa imenom "+varIdArray.getVarName()+" vec postoji ***",varIdArray);
    	}else
    	Tab.insert(Obj.Var, varIdArray.getVarName(), new Struct(Struct.Array, var_type));
    	
    }
    
    public void visit(RtrnTyp rtrnTyp) {
    	Struct t = rtrnTyp.getType().struct;
    	rtrnTyp.obj = Tab.insert(Obj.Meth, rtrnTyp.getMethName(), t);
    	
    	Tab.openScope();
    }
   
    public void visit(RtrnTypVoid rtrnTypVoid) {
    	rtrnTypVoid.obj = Tab.insert(Obj.Meth, rtrnTypVoid.getMethName(), Tab.noType);
    	
    	imaVoid = 1; 
    	 if("main".equalsIgnoreCase(rtrnTypVoid.getMethName())) 
    		imaNaziv = 1;
    	
    	Tab.openScope();
    }
    
    public void visit(NoFrmPrs noFrmPrs) {
    	nemaArg=1;
    }
    
    
    
    public void visit(MethodDecl methodDecl) {
    	Tab.chainLocalSymbols(methodDecl.getReturnTyp().obj);
    	
    	if(imaVoid==1 && imaNaziv==1 && nemaArg==1) imaMain=imaMain+1;   	
    	imaVoid=0;  imaNaziv=0;  nemaArg=0;
    	
    	Tab.closeScope();
    }
    
    
    public void visit(ConstPartName constPartName) {
    	cn = constPartName.getConstName();
    }
 // ovde
    public void visit(NumConst numConst) {
    	Obj res = Tab.currentScope.findSymbol(cn);
    	if (res!=null) {
    		report_error("var/const sa imenom "+cn+" vec postoji ***",null);
    	}else {
    	
	    	if(var_type.getKind()==Struct.Int) {    		
	    		Obj obj = Tab.insert(Obj.Con, cn, Tab.intType);
	    		obj.setAdr((numConst.getVal()).intValue());
	    	} else report_error("Greska: pogresan tip ", numConst);
    	}
    }
 // ovde
    public void visit(CharConst charConst) {
    	Obj res = Tab.currentScope.findSymbol(cn);
    	if (res!=null) {
    		report_error("var/const sa imenom "+cn+" vec postoji ***",null);
    	}else {
    	    	    	
	    	if(var_type.getKind()==Struct.Char) {
	    		Obj obj = Tab.insert(Obj.Con, cn, Tab.charType);
	    		obj.setAdr((charConst.getVal()).charValue());
	    	} else report_error("Greska: pogresan tip ", charConst);
    	}
    }
 // ovde
    public void visit(BoolConst boolConst) {
    	Obj res = Tab.currentScope.findSymbol(cn);
    	if (res!=null) {
    		report_error("var/const sa imenom "+cn+" vec postoji ***",null);
    	}else {
    	
	    	if(var_type.getKind()==Struct.Bool) {
	    		Obj obj = Tab.insert(Obj.Con, cn, TabExt.boolType);
	    			if(boolConst.getVal().booleanValue()==true)
	    				obj.setAdr(1);
	    			else obj.setAdr(0);	    		
	    	} else report_error("Greska: pogresan tip ", boolConst);
    	}
    }
       
    public void visit(DesignatorMultiExpr designatorMultiExpr) {
    	Obj o = designatorMultiExpr.getDesignator().obj;
    	if (o.getType().getKind()!=Struct.Array) {
    		report_error("Greska na liniji " + designatorMultiExpr.getLine()+ " : ime "+designatorMultiExpr.getDesignator()+" ocekivan niz ", null);
    	}
    	
    	if (designatorMultiExpr.getExpr().struct.getKind()!=Struct.Int)
    		report_error("expr mora biti int, linija " +designatorMultiExpr.getLine(),null);
    	
    	
    	
    	//designatorMultiExpr.obj = new Obj(Obj.Elem, designatorMultiExpr.getDesignator().obj.getName(), designatorMultiExpr.getDesignator().obj.getType().getElemType());
    	designatorMultiExpr.obj = new Obj(Obj.Elem, o.getName(), o.getType().getElemType()); // mozda ime o.getName()
    }
       
    public void visit(DesignatorSingle designatorSingle) {
    	Obj o = Tab.find(designatorSingle.getId());
    	
    	if(o == Tab.noObj){
			report_error("Greska na liniji " + designatorSingle.getLine()+ " : ime "+designatorSingle.getId()+" nije deklarisano! ", null);
    	}
    	if(o != Tab.noObj) {
    		report_info("nadjeno " +designatorSingle.getId(),designatorSingle, o); 
    	}
    	
    	//designatorSingle.obj = new Obj(o.getKind(),o.getName(),o.getType());
    	designatorSingle.obj = o;
    }
 
    ///////////////111111111111111
    
    public void visit(DesignatorStatementAopExpr designatorStatementAopExpr) {
    	Obj o = designatorStatementAopExpr.getDesignator().obj;
    	if(!(o.getKind()==Obj.Var) && !(o.getKind()==Obj.Elem)) {
    		report_error("mora biti prom ili elem niza, linija "+designatorStatementAopExpr.getLine()+" ",null);
    	}
    	
    	Struct s = designatorStatementAopExpr.getExpr().struct;
    	if(!s.assignableTo(o.getType()))     
    		report_error("nisu kompatibilni tipovi, linija "+designatorStatementAopExpr.getLine()+" ",null);
    	  	
    }
    
    
    public void visit(ExprSingle exprSingle) {
    	exprSingle.struct = exprSingle.getTerm().struct;
    }
    
    public void visit(ExprSingleMinus exprSingleMinus) {
    	if(exprSingleMinus.getTerm().struct.getKind() != Struct.Int)
    		report_error("term mora biti tipa int, linija "+exprSingleMinus.getLine()+"",null);
    	
    	exprSingleMinus.struct = exprSingleMinus.getTerm().struct;
    }
    
    public void visit(ExprMulti exprMulti) {
    	if(exprMulti.getExpr().struct.getKind()!=Struct.Int || exprMulti.getTerm().struct.getKind()!=Struct.Int)
    		report_error("expr i term moraju biti tipa int, linija "+exprMulti.getLine()+"",null);
    	
    	exprMulti.struct = exprMulti.getTerm().struct;
    }
    
    
    public void visit(TermSingle termSingle) {
    	
    	termSingle.struct = termSingle.getFactor().struct;
    }
    
    public void visit(TermMulti termMulti) {
    	if(termMulti.getTerm().struct.getKind()!=Struct.Int || termMulti.getFactor().struct.getKind()!=Struct.Int)
    		report_error("expr i term moraju biti tipa int, linija "+termMulti.getLine()+"",null);
    	
    	termMulti.struct = termMulti.getTerm().struct;
    }
    
    public void visit(FactorDes factorDes) {
    	factorDes.struct = factorDes.getDesignator().obj.getType();
    }
    
    public void visit(FactorNum factorNum) {
    	factorNum.struct = Tab.intType;
    }
    
    public void visit(FactorChar factorChar) {
    	factorChar.struct = Tab.charType;
    }
    
    public void visit(FactorBool factorBool) {
    	factorBool.struct = TabExt.boolType;
    }
    
    public void visit(FactorNewArray factorNewArray) {
    	if(factorNewArray.getExpr().struct.getKind()!=Struct.Int)
    		report_error("expr mora biti tipa int, linija "+factorNewArray.getLine()+"",null);
    	
    	factorNewArray.struct = new Struct(Struct.Array,var_type);
    }
    
    public void visit(FactorExpr factorExpr) {
    	factorExpr.struct = factorExpr.getExpr().struct;
    }
      
    
    //designator inc/dec
    
    public void visit(DesignatorStatementInc designatorStatementInc) {
    	Obj o = designatorStatementInc.getDesignator().obj;
    	if(o.getKind()!=Obj.Var && o.getKind()!=Obj.Elem) {
    		report_error("mora biti prom ili elem niza, linija "+designatorStatementInc.getLine()+" ",null);
    	}
    	if (o.getType().getKind()!=Struct.Int)
    		report_error("mora biti tipa int, linija "+designatorStatementInc.getLine()+" ",null);
    }
    
    public void visit(DesignatorStatementDec designatorStatementDec) {
    	Obj o = designatorStatementDec.getDesignator().obj;
    	if(o.getKind()!=Obj.Var && o.getKind()!=Obj.Elem) {
    		report_error("mora biti prom ili elem niza, linija "+designatorStatementDec.getLine()+" ",null);
    	}
    	if (o.getType().getKind()!=Struct.Int)
    		report_error("mora biti tipa int, linija "+designatorStatementDec.getLine()+" ",null);
    }
    
    //read,print
    
    public void visit(ReadStatement readStatement) {
    	Obj o = readStatement.getDesignator().obj;
    	if(o.getKind()!=Obj.Var && o.getKind()!=Obj.Elem) {
    		report_error("mora biti prom ili elem niza, linija "+readStatement.getLine()+" ",null);
    	}
    	if (o.getType().getKind()!=Struct.Int && o.getType().getKind()!=Struct.Char && o.getType().getKind()!=Struct.Bool)
    		report_error("mora biti tipa int ili char ili bool, linija "+readStatement.getLine()+" ",null);
    }
    
    public void visit(PrintStatementNumArg printStatementNumArg) {
    	Struct s = printStatementNumArg.getExpr().struct;
    	
    	if (s.getKind()!=Struct.Int && s.getKind()!=Struct.Char && s.getKind()!=Struct.Bool)
    		report_error("mora biti tipa int ili char ili bool, linija "+printStatementNumArg.getLine()+" ",null);
    }
    
    public void visit(PrintStatement printStatement) {
    	Struct s = printStatement.getExpr().struct;
    	
    	if (s.getKind()!=Struct.Int && s.getKind()!=Struct.Char && s.getKind()!=Struct.Bool)
    		report_error("mora biti tipa int ili char ili bool, linija "+printStatement.getLine()+" ",null);    
    	
    	
    }
    

 // AVGUST/SEPT DOPUNA
    public void visit(StatementFindAny statementFindAny) {
    	Obj o1 = statementFindAny.getDesignator().obj;
    	Obj o2 = statementFindAny.getDesignator1().obj;
    	
    	if (o1.getType().getKind()!=Struct.Bool)
    		report_error("leva strana mora biti promenljiva tipa bool, linija "+statementFindAny.getLine()+" ",null);
    	if (o1.getKind()!=Obj.Var && o1.getKind()!=Obj.Elem)
    		report_error("leva strana mora biti promenljiva tipa bool, linija "+statementFindAny.getLine()+" ",null);
    	
    	if (o2.getKind()!=Obj.Var || o2.getType().getKind()!=Struct.Array)
    		report_error("desna strana mora biti promenljiva nizovnog tipa, linija "+statementFindAny.getLine()+" ",null);
    	
    	if (o2.getType().getElemType().getKind()!=statementFindAny.getExpr().struct.getKind())
    		report_error("tip expr i tip elemenata niza moraju biti isti, linija "+statementFindAny.getLine()+" ",null);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public boolean passed() {
    	return !errorDetected;
    }
    
}

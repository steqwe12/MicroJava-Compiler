
package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\n" 	{ } 
"\r\n" 	{ }
"\f" 	{ }

"program"   { return new_symbol(sym.PROG, yytext());}
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"return" 	{ return new_symbol(sym.RETURN, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"++"		{ return new_symbol(sym.INC, yytext());}
"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"=" 		{ return new_symbol(sym.EQUAL, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"(" 		{ return new_symbol(sym.LPAREN, yytext()); }
")" 		{ return new_symbol(sym.RPAREN, yytext()); }
"{" 		{ return new_symbol(sym.LBRACE, yytext()); }
"}"			{ return new_symbol(sym.RBRACE, yytext()); }


"--"		{ return new_symbol(sym.DEC, yytext());}
"-"			{ return new_symbol(sym.MINUS, yytext());}
"*"			{ return new_symbol(sym.MULTI, yytext());}
"/"			{ return new_symbol(sym.DIV, yytext());}
"%"			{ return new_symbol(sym.REM, yytext());}
"=="		{ return new_symbol(sym.EQUALS, yytext());}
"!="		{ return new_symbol(sym.NOTEQ, yytext());}
">"			{ return new_symbol(sym.GRT, yytext());}
">="		{ return new_symbol(sym.GRTEQ, yytext());}
"<"			{ return new_symbol(sym.LT, yytext());}
"<="		{ return new_symbol(sym.LTEQ, yytext());}
"&&"		{ return new_symbol(sym.AND, yytext());}
"||"		{ return new_symbol(sym.OR, yytext());}


":"			{ return new_symbol(sym.COLON, yytext());}
"."			{ return new_symbol(sym.DOT, yytext());}
"["			{ return new_symbol(sym.LBRACKET, yytext());}
"]"			{ return new_symbol(sym.RBRACKET, yytext());}
"=>"		{ return new_symbol(sym.ARROW, yytext());}

"break"		{ return new_symbol(sym.BREAK, yytext());}
"class"		{ return new_symbol(sym.CLASS, yytext());}
"else"		{ return new_symbol(sym.ELSE, yytext());}
"const"		{ return new_symbol(sym.CONST, yytext());}
"if"		{ return new_symbol(sym.IF, yytext());}
"while"		{ return new_symbol(sym.WHILE, yytext());}
"new"		{ return new_symbol(sym.NEW, yytext());}
"read"		{ return new_symbol(sym.READ, yytext());}
"extends"	{ return new_symbol(sym.EXTENDS, yytext());}
"continue"	{ return new_symbol(sym.CONTINUE, yytext());}
"foreach"	{ return new_symbol(sym.FOREACH, yytext());}
"findAny"	{ return new_symbol(sym.FINDANY, yytext());}

"//" {yybegin(COMMENT);}
<COMMENT>"\n" {yybegin(YYINITIAL);} 
<COMMENT> . {yybegin(COMMENT);}
<COMMENT> "\r\n" { yybegin(YYINITIAL); }

("true"|"false") { return new_symbol(sym.BOOLCONST, new Boolean (yytext())); }
"'"[\040-\176]"'" {return new_symbol (sym.CHARCONST, new Character (yytext().charAt(1)));} 
// dec:32-126 0:48 9:57 z:122 A:65 Z:90

[0-9]+  { return new_symbol(sym.NUMBER, new Integer (yytext())); }
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 	{return new_symbol (sym.IDENT, yytext()); }


. { System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1)); }











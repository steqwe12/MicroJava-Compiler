# MJ-Compiler
 Lexical, syntax and semantic analysis and code generation of MicroJava programs.
Apache Ant needed for running the build.xml file.  

1.	Cilj  
Cilj projektnog zadatka je realizacija kompajlera za programski jezik Mikrojavu. Kompajler omogućava prevodjenje sintaksno i semantički ispravnih Mikrojava programa u Mikrojava bajtkod koji se izvršava na virtuelnoj mašini za Mikrojavu.   
Programski prevodilac za Mikrojavu ima četiri osnovne funkcionalnosti: leksičku analizu, sintaksnu analizu, semantičku analizu i generisanje koda.  
  
2.	Pokretanje  
U zeljeni fajl sa ekstenzijom .mj napisati korisnicni MJ kod.  
Pokrenuti test/rs.ac.bg.etf.pp1/Compiler.java (prvi argument koji se navodi je putanja do .mj fajla sa MJ kodom, a drugi argument putanja do izlaznog .obj fajla).  
Izmeniti build.xml “disasm” i “runObj” delove tako da se navede putanja do .obj fajla koji zelimo da pokrenemo.  
DKlik na build.xml -> run as -> Ant build -> izabrati samo runObj -> Run  
  
3.	Test primeri  
Test primer program.mj je primer najosnovnijeg MJ programa koji je potpuno ispravan.  
Test primeri program1.mj I test301.mj su primeri koji su takodje potpuno ispravni ali su slozeniji.  
Test primer sintaknoNeisp.mj je primer MJ programa koji je sintaksno neispravan.  
Test primer semantickiNeisp.mj je primer MJ programa koji je semanticki neispravan.  
  
4.	Klase  
Klasa CodeGenerator prosiruje klasu VisitorAdaptor; posecuju se cvorovi stabla metodama visit() koje sadrze instrukcije za generisanje MJ koda.  
Klasa CounterVisitor prosiruje VisitorAdaptor I sluzi da izbroji koliko ima promenljivih da bi mogao da se generise entry za f-je(main).  
Klasa SemanticAnalyzer prosiruje klasu VisitorAdaptor; posecuju se cvorovi stabla metodama visit() u kojima se odradjuje semanticka obrada i dodavanje u tabelu simbola.  
Klasa TabExt prosiruje klasu Tab i sluzi samo da bi se u tabelu simbola uveo Boolean tip boolType.  
Klasa Compiler sadrzi main metodu kojom se pokrecu analize MJ koda, ispisuje se rezultat parsiranja u konzolu i generisani kod se upisuje u .obj fajl.  
  

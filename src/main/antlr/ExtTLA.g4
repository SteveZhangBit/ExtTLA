grammar ExtTLA;

@lexer::members {
    public static final int COMMENTS_CHANNEL = 1;
}

spec : module* ;

module : 'module' IDENT extend? '{' module_content '}' ;

extend : 'extends' IDENT (',' IDENT)* ;

module_content : ( imports
                 | constants
                 | assumes
                 | variables
                 | enumerations
                 | operations
                 | implement
                 | shadow
                 | invariants )* ;

imports : 'import' IDENT (',' IDENT)* ';' ;

constants : 'const' const_decl (',' const_decl)* ';'
          | 'override' 'const' const_decl ';'
          ;

const_decl : IDENT ('=' (literal | TLA_EXP))? ;

assumes : 'assume' TLA_EXP ';' ;

variables : 'var' IDENT ':' (IDENT | TLA_EXP) '=' var_init_val ';' ;

var_init_val : IDENT | TLA_EXP ;

enumerations : 'override'? 'enum' IDENT '=' '{' IDENT (',' IDENT)* '}' ';' ;

operations : 'override'? 'recursive'? IDENT '(' arguments? ')' TLA_EXP ;

arguments : arg (',' arg)* ;

arg : IDENT ':' (IDENT | TLA_EXP) ;

implement : 'implements' IDENT ('by' TLA_EXP)? ;

shadow : 'shadow' IDENT ';' ;

invariants : 'inv' IDENT TLA_EXP ;

literal : INT
        | FLOAT
        | STRING
        ;

IDENT : ID_LETTER (ID_LETTER | DIGIT)* ;
fragment ID_LETTER : 'a'..'z' | 'A'..'Z' | '_' ;
fragment DIGIT : '0'..'9' ;

INT : [0-9]+ ;

FLOAT : DIGIT+ '.' DIGIT+
      |        '.' DIGIT+
      ;

STRING : '"' (ESCAPE | .)*? '"' ;
fragment ESCAPE : '\\"' | '\\\\' ;

TLA_EXP : '{{' .*? '}}' ;

LINE_COMMENT : '//' .*? '\r'? '\n' -> channel(1) ;

COMMENT : '/*' .*? '*/' -> channel(1) ;

WS : [ \t\n\r]+ -> skip;

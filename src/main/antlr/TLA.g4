grammar TLA;

tla_operator : IDENT ('(' ident_list ')')? '==' tla_exp;

tla_type_decl : '{' tla_exp_list? '}'
              | '{' ident_or_tuple '\\in' tla_exp ':' tla_exp '}'
              | '{' tla_exp ':' tla_quantifier_list '}'
              | '[' tla_exp '->' tla_exp ']'
              | '[' tla_record_list ']'
              | tla_prefix_op tla_exp
              | IDENT '(' tla_arg_list ')'
              | IDENT
              ;

tla_exp : '(' tla_exp ')'
        | '{' tla_exp_list? '}'
        | '{' ident_or_tuple '\\in' tla_exp ':' tla_exp '}'
        | '{' tla_exp ':' tla_quantifier_list '}'
        | tla_exp '[' tla_exp_list ']'
        | '[' tla_quantifier_list '|->' tla_exp ']'
        | '[' tla_exp '->' tla_exp ']'
        | '[' tla_mapping_list ']'
        | '[' tla_record_list ']'
        | '[' tla_exp 'EXCEPT' tla_except_list ']'
        | '<<' tla_exp_list? '>>'
        | tla_exp (('\\X' | '\\times') tla_exp)+
        | '[' tla_exp ']_' tla_exp
        | '<<' tla_exp '>>_' tla_exp
        | ('WF_' | 'SF_') tla_exp '(' tla_exp ')'
        | 'IF' tla_exp 'THEN' tla_exp 'ELSE' tla_exp
        | 'CASE' tla_case_arm ('[]' tla_case_arm)* ('[]' 'OTHER' '->' tla_exp)?
        | 'LET' (tla_operator)+ 'IN' tla_exp
        | ('/\\' tla_exp)+
        | ('\\/' tla_exp)+
        | tla_exp tla_postfix_op
        | tla_prefix_op tla_exp
        | tla_exp '.' tla_exp
        | tla_exp ('^^' | '^') tla_exp
        | tla_exp ('\\star' | '\\o' | '\\circ' | '\\div'
                  | '\\bullet' | '\\bigcirc' | '//' | '/' | '**' | '*'
                  | '(\\X)' | '\\otimes' | '(/)' | '\\oslash' | '(.)'
                  | '\\odot' | '&&' | '&') tla_exp
        | tla_exp ('--' | '-') tla_exp
        | tla_exp ('(-)' | '\\ominus' | '||' | '|'
                  | '%%' | '%') tla_exp
        | tla_exp ('++' | '+' | '(+)' | '\\oplus') tla_exp
        | tla_exp '\\wr' tla_exp
        | tla_exp ('\\uplus' | '\\sqcup' | '\\sqcap' | '??' | '$$' | '$'
                  | '##' | '!!') tla_exp
        | tla_exp ('...' | '..') tla_exp
        | tla_exp ('\\cup' | '\\union' | '\\cap'
                  | '\\intersect' | '\\') tla_exp
        | tla_exp ('<:' | ':>') tla_exp
        | tla_exp '@@' tla_exp
        | tla_exp '\\cdot' tla_exp
        | tla_exp ('|=' | '|-' | '\\supseteq' | '\\supset' | '\\succeq' | '\\succ'
                  | '\\subseteq' | '\\subset' | '\\sqsubseteq' | '\\sqsupset'
                  | '\\sqsupseteq' | '\\sqsubset' | '\\simeq' | '\\sim'
                  | '\\propto' | '\\preceq' | '\\prec' | '\\ll' | '\\leq' | '=<'
                  | '\\notin' | '\\in' | '\\gg' | '\\geq' | '>=' | '\\doteq'
                  | '\\cong' | '\\asymp' | '\\approx' | '>' | '=|' | '='
                  | '<' | ':=' | '::=' | '-|' | '/=' | '#') tla_exp
        | tla_exp ('\\lor' | '\\/' | '\\land' | '/\\') tla_exp
        | tla_exp ('~>' | '<=>' | '\\equiv' | '-+>') tla_exp
        | tla_exp '=>' tla_exp
        | ('\\A' | '\\E') tla_quantifier_list ':' tla_exp
        | ('\\A' | '\\E' | '\\AA' | '\\EE') ident_list ':' tla_exp
        | 'CHOOSE' ident_or_tuple '\\in'? tla_exp ':' tla_exp
        | IDENT '(' tla_arg_list ')'
        | IDENT
        | INT
        | FLOAT
        | STRING
        | '@'
        ;

tla_exp_list : tla_exp (',' tla_exp)* ;

tla_arg_list : tla_exp (',' tla_exp)* ;

tla_quantifier_list : tla_quantifier (',' tla_quantifier)* ;

tla_quantifier : (ident_or_tuple | ident_list) '\\in' tla_exp ;

ident_or_tuple : IDENT | '<<' ident_list '>>' ;

ident_list : IDENT (','  IDENT)* ;

tla_mapping_list : tla_mapping (',' tla_mapping)* ;

tla_mapping : IDENT '|->' tla_exp ;

tla_record_list : tla_record (',' tla_record)* ;

tla_record : IDENT ':' tla_exp ;

tla_except_list : tla_except (',' tla_except)* ;

tla_except : '!' ('.' IDENT | '[' tla_exp_list ']')+ '=' tla_exp ;

tla_case_arm : tla_exp '->' tla_exp ;

tla_prefix_op : '-' | '~' | '\\lnot' | '\\neg' | '[]' | '<>'
              | 'DOMAIN' | 'ENABLED' | 'SUBSET' | 'UNCHANGED' | 'UNION'
              ;

tla_postfix_op : '^+' | '^*' | '^#' | '\'' ;

IDENT : ID_LETTER (ID_LETTER | DIGIT)* ;
fragment ID_LETTER : 'a'..'z' | 'A'..'Z' | '_' ;
fragment DIGIT : '0'..'9' ;

INT : [0-9]+ ;

FLOAT : DIGIT+ '.' DIGIT+
      |        '.' DIGIT+
      ;

STRING : '"' (ESCAPE | .)*? '"' ;
fragment ESCAPE : '\\"' | '\\\\' ;

WS : [ \t\r]+ -> skip;

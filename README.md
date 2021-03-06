# jLua

This is a tool that aims to recreate Lua with Java as its host language.
This tool is inspired by [Crafting Interpreters and Lox](https://github.com/munificent/craftinginterpreters) and [Lua](https://www.lua.org/)

## Set Up
### REPL
* Navigate to project root
* With Java in PATH do `java com.craftinginterpreters.lua.Lua`

### Custom Scripts
* Navigate to project root
* With Java in PATH do `java com.craftinginterpreters.lua.Lua ./NAME/OF/SCRIPT/.lua`

## Compiling
* To compile do `javac com/craftinginterpreters/lua/*.java`


## EBNF

chunk ::= block

block ::= stat* retstat?

stat ::= ";" | 
	 varlist "=" explist | 
	 functioncall | 
	 label | 
	 break | 
	 goto Name | 
	 do block end | 
	 while exp do block end | 
	 repeat block until exp | 
	 if exp then block (elseif exp then block)* (else block)? end | 
	 for Name "=" exp "," exp ["," exp] do block end | 
	 for namelist in explist do block end | 
	 function funcname funcbody | 
	 local function Name funcbody | 
	 local attnamelist ["=" explist]

attnamelist ::=  Name attrib ("," Name attrib)*

attrib ::= ("<" Name ">")?

retstat ::= return (explist)? (";")?

label ::= "::" Name "::"

funcname ::= Name ("." Name)* (":" Name)?

varlist ::= var ("," var)*

var ::=  Name | prefixexp "[" exp "]" | prefixexp "." Name 

namelist ::= Name ("," Name)*

explist ::= exp ("," exp)*

exp ::=  nil | false | true | Numeral | LiteralString | "..." | functiondef | 
		 prefixexp | tableconstructor | exp binop exp | unop exp 

prefixexp ::= var | functioncall | "(" exp ")"

functioncall ::=  prefixexp args | prefixexp ":" Name args 

args ::=  "(" explist? ")" | tableconstructor | LiteralString 

functiondef ::= function funcbody

funcbody ::= "(" parlist? ")" block end

parlist ::= namelist ("," "...")? | "..."

tableconstructor ::= "{" fieldlist? "}"

fieldlist ::= field (fieldsep field)* fieldsep?

field ::= "[" exp "]" "=" exp | Name "=" exp | exp

fieldsep ::= "," | ";"

binop ::=  "+" | "-" | "*" | "/" | "//" | "^" | "%" | 
	 "&" | "~" | "|" | ">>" | "<<" | ".." | 
	 "<" | "<=" | ">" | ">=" | "==" | "~=" | 
	 and | or

unop ::= "-" | not | "#" | "~"

(ns parsers.core
  (:gen-class)
  (:require [instaparse.core :as insta]))

;; First Parser
;; Sequence of 'a' followed by a sequence of 'b'
(def as-and-bs
  (insta/parser
   "S = AB*
     AB = A B
     A = 'a'+
     B = 'b'+"))

(as-and-bs "abbbbab")

;; Parse as and bs surrounded by parenthesis
(def parens-ab
  (insta/parser
   "paren-wrapped = '(' seq-of-A-or-B ')'
     seq-of-A-or-B = ('a' | 'b' )*"))

(parens-ab "(ab)")

;; Parse a's and b's surrounded by parenthesis
;; Support nested parenthesis
(def parens-ab-nested
  (insta/parser
   "paren-wrap = <'('> (a-b | paren-wrap) * <')'>
    a-b = ('a' | 'b')*"))

(parens-ab-nested "(ab(aba(bba)))")

;; In Cobol, an identifier consists of letters, digits, and hyphens (‘-’), but always starts
;; with a letter. Hyphens may not occur consecutively, nor at the end of an identifier.
;; Thus ‘setup’, ‘set-up’, and ‘s-et-up’ are well-formed identifiers,
;; but ‘set--up’, ‘- setup’, and ‘setup-’ are ill-formed.
;; Write an EBNF grammar of Cobol identifiers.
(def cobol-identifier
  (insta/parser
   "identifier = letter (tail-char | dash)* tail-char
    dash = <'-'>
    <tail-char> = letter | digit
    letter = #'[a-zA-Z]'
    digit = #'[0-9]+'"))

(insta/visualize (cobol-identifier "hello-there"))
(cobol-identifier "hello-there")

;; In Roam [[backlinks]] are used
;; [[backlinks[[can nest]]]] and be used anywhere on the line
(def backlink-parser
  (insta/parser
   "Line = (back-link / text)*
    back-link = <'[' '['> (back-link / text)* <']' ']'> (* Use PEG syntax for preference*)
    <text> = #'.'"))

(backlink-parser "Arbitrary **Text**! [[hello[[nested-link]] there [[this]]]]")
(insta/visualize (backlink-parser "[[Back Link![[Inside!]]]]"))

;; In Roam **emphasis** is supported for bolding text
;; **emphasis** nesting is meaningless and doesn't need to be supported
(def emphasis-parser
  (insta/parser
   "line = (emphasis / text)*
    emphasis = <'*' '*'> (text)* <'*' '*'>
    text = #'.' "))

(emphasis-parser "Text **strong**")

;; In Roam __ittalics__ are supported for bolding text
;; __ittalics__ nesting is meaningless and doesn't need to be supported
(def ittalic-parser
  (insta/parser
   "line = (ittalics / text)*
    ittalics = <'_' '_'> (text)* <'_' '_'>
    text = #'.' "))

(ittalic-parser "Text __ittalic__")

;; First attempt at creating a parser for Roam-Research
(def roam-parser
  (insta/parser
   "line = (back-link / emphasis / highlight / latex / italics / ref / roam-render / img / alias / char)+ | epsilon
    back-link = <'[' '['> back-link-able* <']' ']'>

    (* Visual Style Forms *)
    emphasis = <'*' '*'> emphasis-able* <'*' '*'>
    italics = <'_' '_'> emphasis-able* <'_' '_'>
    highlight = <'^' '^'> highlight-able* <'^' '^'>
    latex = <'$' '$'> char* <'$' '$'>
    ref = <'(' '('> char* <')' ')'>
    roam-render = <'{' '{'> roam-render-able* <'}' '}'>

    (* Aliases and Images *)
    description = <'['> alias-able* <']'>
    url = <'('> char* <')'>
    alias = description url
    img = <'!'> description url

    (* Define allowed inner elements (including nesting control) *)
    <back-link-able> = back-link / emphasis / char
    <emphasis-able> = back-link / char
    <highlight-able> = emphasis / italics / back-link / char
    <roam-render-able> = roam-render / char
    <alias-able> = alias / img / char

    <char> = (!'$' '$') (!'^' '^') / (!'*' '*') / (!'_' '_') / (#'.')  (*Greedy solution for now?*)"))

(insta/visualize ( roam-parser "[![name](imgrul)](http://www.)"))


(time
  (insta/parse
   roam-parser
   "- [alias![imageAlia`s](imageUrl)](this) {{roam {{render}}}} ((ref)) $$latex$$ ^^__yes__^^ *[[**em[[meme]]**]] __i__ **em** **em** **em**")))

(def remove-ambiguity
  (insta/parser
   "S = (em / char)+ | epsilon
    em = <'*' '*'> char* <'*' '*'>
    <char> = #'.'"))

(insta/parses remove-ambiguity "**em** **em**")



;; Parser for Toy Robot:
;; There is a table top robot that follows commands in the following format:
;; `PLACE X,Y,DIRECTION` Where Direction is NORTH, EAST, SOUTH or WEST
;; `MOVE`
;; `LEFT`
;; `RIGHT`
;; `REPORT`
;; `QUIT`

(def toy-robot
  (insta/parser
   "Command = PLACE | MOVE | LEFT | RIGHT | REPORT | QUIT
    DIRECTION = 'NORTH' | 'SOUTH' | 'EAST' | 'WEST'
    PLACE = 'PLACE', ' "))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))



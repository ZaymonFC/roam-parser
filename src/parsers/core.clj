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


(doall
 (map
  (partial insta/parse cobol-identifier)
  ["hello9" "-hello" "hello-there" "set--up" "number-on-the-end-a" "setup-"]))

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


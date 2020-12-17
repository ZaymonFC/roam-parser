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

(cobol-identifier "hello-there")

;; In Roam [[backlinks]] are used
;; [[backlinks[[can nest]]]] and be used anywhere on the line
(def backlink-parser
  (insta/parser
   "Line = (back-link / text)*
    back-link = <'[' '['> (back-link / text)* <']' ']'> (* Use PEG syntax for preference*)
    <text> = #'.'"))

(backlink-parser "Arbitrary **Text**! [[hello[[nested-link]] there [[this]]]]")

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

;; Attempt to remove ambiguity from the parser in the case of
;; **em** **em**
(def remove-ambiguity
  (insta/parser
   "S = (em / char)+ | epsilon
    em = <'*' '*'> char* <'*' '*'>
    <char> = !'**' #'.'"))

(insta/parses remove-ambiguity "**em** **em**")

;; First attempt at creating a parser for Roam-Research
(def roam-parser
  (insta/parser
   "line = ( ( back-link | emphasis | highlight | latex | italics | ref | roam-render | img | alias | code-inline | code-block) / text / newline )+ | epsilon
    back-link = <'[['> back-link-able* <']]'>

    (* Visual Forms *)
    emphasis = <'**'> emphasis-able* <'**'>
    italics = <'__'> emphasis-able* <'__'>
    highlight = <'^^'> highlight-able* <'^^'>

    (* Logic Forms *)
    latex = <'$$'> char* <'$$'>
    ref = <'(('> text <'))'>
    roam-render = <'{{'> roam-render-able* <'}}'>

    (* Aliases and Images *)
    description = <'['> alias-able* <']'>
    url = <'('> link <')'>
    alias = description url
    img = <'!'> description url

    (* Code Blocks *)
    code-inline = <'`'> text <'`'>
    code-block-language = letter+
    <code-block-start> = newline <'```'> [code-block-language]
    <code-block-end> = <'```'>
    code-block-content = text
    code-block = code-block-start newline code-block-content newline code-block-end

    (* Define allowed inner elements (including nesting control) 
       - Easily define which forms are valid inside others (Ability to create common rules here)
       - Define which forms can recursively appear within themselves *)
    <back-link-able> = back-link / emphasis / text
    <emphasis-able> = back-link / text
    <highlight-able> = emphasis / italics / back-link / text
    <roam-render-able> = roam-render / back-link / text
    <alias-able> = alias / img / text

    <newline> = <'\n'>
    (* PEG Negative Lookahead `!'__'`: proceeds with concatenation chain if the lookahead doesn't match *)
    text = char*
    link = char*
    <char> = !'**' !'^^' !'__' !'$$' (#'.') (* This might have to be implemented for each form type *)
    <letter> = #'[A-Za-z]' "))

;; Define a way to transform a tree from the bottom up into a more meaningful representation for display.
(defn text [& children] [:p (apply str children)])
(defn link [& children] [:a (apply str children)])

(defn emphasis [& children] [:strong children])
(defn latex [& children] [:latex (apply str children)])
(defn image [& children] [:image (apply str children)])

(defn code-inline [& children] [:code-inline (apply str children)])
(defn code-block-language [& children] [:code-block-languge (apply str children)])
(defn code-block-content [& children] [:code-block-content (apply str children)])

(def transformation-map
  {:text text
   :link link
   :emphasis emphasis
   :latex latex
   :image image
   :code-inline code-inline
   :code-block-language code-block-language
   :code-block-content code-block-content})

(defn tree-transform [t] (insta/transform transformation-map t))

(defn parse-text [text]
  (->> text
       roam-parser
       (tree-transform)))

(parse-text "[alias![imageAlias](imageUrl)](this) {{roam {{[[Done]]}}}} ((ref)) $$latex$$ ^^__yes__^^ *[[**em[[meme]]**]] __i__ **em** **em** **em** `let inlineCode = 100`\n```clojure\n(def code-block-contents [x 100])\n```")


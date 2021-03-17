# roam-parser
A complete parser for the roam research document format.

### Features
- Specified in [EBNF](https://www.ics.uci.edu/~pattis/misc/ebnf2.pdf)
- Implemented with the Clojure parsing library [Instaparse](https://github.com/Engelberg/instaparse)
- Outputs parse results into a syntax tree
- Some tree processing to get the output format closer to HTML

[View the final parser](https://github.com/ZaymonFC/roam-parser/blob/c9fc2e7d346a71c32063d854998aeb59c7a60855/src/parsers/core.clj#L89)

### Limitations
- Parser implementation fast, but not fast enough for generating AST of thousands of lines of with acceptable UX.
- Doesn't output entirely valid HTML but it's close.

### Example usage

![image](https://user-images.githubusercontent.com/12402727/111412777-ccd6c180-8728-11eb-9198-9e33a3d477ba.png)

# KorAP Tokenizer
Interface and implementation of a tokenizer and sentence splitter that can be used

* for German, English, French, and with some limitations also for other languages
* as standalone tokenizer and/or sentence splitter
* or within the KorAP ingestion pipeline
* or within the [OpenNLP tools](https://opennlp.apache.org) framework

The included implementations (`DerekoDfaTokenizer_de, DerekoDfaTokenizer_en, DerekoDfaTokenizer_fr`) are highly efficient DFA tokenizers and sentence splitters with character offset output based on [JFlex](https://www.jflex.de/).
The de-variant is used for the German Reference Corpus DeReKo. Being based on finite state automata,
the tokenizers are potentially not as accurate as language model based ones, but with ~5 billion words per hour typically more efficient.
An important feature in the DeReKo/KorAP context is also that token character offsets can be reported, which can be used for applying standoff annotations.
 
The include mplementations of the `KorapTokenizer` interface also implement the [`opennlp.tools.tokenize.Tokenizer`](https://opennlp.apache.org/docs/1.8.2/apidocs/opennlp-tools/opennlp/tools/tokenize/Tokenizer.html)
and [`opennlp.tools.sentdetect.SentenceDetector`](https://opennlp.apache.org/docs/1.8.2/apidocs/opennlp-tools/opennlp/tools/sentdetect/SentenceDetector.html)
interfaces and can thus be used as a drop-in replacements in OpenNLP applications.

The underlying scanner is based on the Lucene scanner with modifications from [David Hall](https://github.com/dlwh).

Our changes mainly concern a good coverage of German, or optionally of some English and French abbreviations,
and some updates for handling computer mediated communication, optimized and tested, in the case of German, against the gold data from the [EmpiriST 2015](https://sites.google.com/site/empirist2015/) shared task (Beißwenger et al. 2016).


## Installation
```shell script
mvn clean install
```
#### Note
Because of the large table of abbreviations, the conversion from the jflex source to java,
i.e. the calculation of the DFA, takes about 5 to 30 minutes, depending on your hardware,
and requires a lot of heap space.

## Examples Usage
By default, KorAP tokenizer reads from standard input and writes to standard output. It supports multiple modes of operations.

#### Split English text into tokens
```
$ echo "It's working." | java -jar target/KorAP-Tokenizer-2.2.2-standalone.jar -l en
It
's
working
.
```
#### Split French text into tokens and sentences
```
$ echo "C'est une phrase. Ici, il s'agit d'une deuxième phrase." \
  | java -jar target/KorAP-Tokenizer-2.2.2-standalone.jar -s -l fr
C'
est
une
phrase
.

Ici
,
il
s'
agit
d'
une
deuxième
phrase
.

```

#### Print token character offsets
With the `--positions` option, for example, the tokenizer prints all offsets of the first character of a token and the first character after a token.
In order to end a text, flush the output and reset the character position, an EOT character (0x04) can be used.
```
$ echo -n -e 'This is a text.\x0a\x04\x0aAnd this is another text.\n\x04\n' |\
     java -jar target/KorAP-Tokenizer-2.2.2-standalone.jar  --positions
This
is
a
text
.
0 4 5 7 8 9 10 14 14 15
And
this
is
another
text
.
0 3 4 8 9 11 12 19 20 24 24 25
```
#### Print token and sentence offset
```
echo -n -e ' This ist a start of a text. And this is a sentence!!! But what the hack????\x0a\x04\x0aAnd this is another text.'  |\
   java -jar target/KorAP-Tokenizer-2.2.2-standalone.jar --no-tokens --positions --sentence-boundaries
1 5 6 9 10 11 12 17 18 20 21 22 23 27 27 28 29 32 33 37 38 40 41 42 43 51 51 54 55 58 59 63 64 67 68 72 72 76
1 28 29 54 55 76
0 3 4 8 9 11 12 19 20 24 24 25
0 25
```

### Adding Support for more Languages
To adapt the included implementations to more languages, take one of the `language-specific_<language>.jflex-macro` files as template and
modify for example the macro for abbreviations `SEABBR`. Then add an `execution` section for the new language
to the jcp ([java-comment-preprocessor](https://github.com/raydac/java-comment-preprocessor)) artifact in `pom.xml` following the example of one of the configurations there.
After building the project (see below) your added language specific tokenizer / sentence splitter should be selectable with the `--language` option.

Alternatively, you can also provide `KorAPTokenizer` implementations independently on the class path and select them with the `--tokenizer-class` option.

## Development and License

**Authors**: 
* [Marc Kupietz](https://www.ids-mannheim.de/digspra/personal/kupietz.html)
* [Nils Diewald](https://www.ids-mannheim.de/digspra/personal/diewald.html)

Copyright (c) 2021, [Leibniz Institute for the German Language](http://www.ids-mannheim.de/), Mannheim, Germany

This package is developed as part of the [KorAP](http://korap.ids-mannheim.de/)
Corpus Analysis Platform at the Leibniz Institute for German Language
([IDS](http://www.ids-mannheim.de/)).

The package contains code from [Apache Lucene](https://lucene.apache.org/) with modifications by Jim Hall.

It is published under the [Apache 2.0 License](LICENSE).

## Contributions

Contributions are very welcome!

Your contributions should ideally be committed via our [Gerrit server](https://korap.ids-mannheim.de/gerrit/)
to facilitate reviewing (see [Gerrit Code Review - A Quick Introduction](https://korap.ids-mannheim.de/gerrit/Documentation/intro-quick.html)
if you are not familiar with Gerrit). However, we are also happy to accept comments and pull requests
via GitHub.

## References
- Beißwenger, Michael / Bartsch, Sabine / Evert, Stefan / Würzner, Kay-Michael. (2016). EmpiriST 2015: A Shared Task on the Automatic Linguistic Annotation of Computer-Mediated Communication and Web Corpora. 44-56. 10.18653/v1/W16-2606. 

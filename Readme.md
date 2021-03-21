# KorAP Tokenizer
Interface and implementation of a tokenizer and sentence splitter that can be used

* as standalone tokenizer and/or sentence splitter
* within the KorAP ingestion pipeline
* within the [OpenNLP tools](https://opennlp.apache.org) framework

## DeReKo Tokenizer (included default implementation)
The included default implementation (`DerekoDfaTokenizer_de`) is a highly efficient DFA tokenizer and sentence splitter with character offset output based on [JFlex](https://www.jflex.de/), suitable for German and other European languages.
It is used for the German Reference Corpus DeReKo. Being based on a finite state automaton, 
it is not accurate as language model based tokenizers, but with ~5 billion words per hour typically more efficient.
An important feature in the DeReKo/KorAP context is also, that it reliably reports the character offsets of the tokens 
so that this information can be used for applying standoff annotations.
 
`DerekoDfaTokenizer_de` and any implementation of the `KorapTokenizer` interface also implement the [`opennlp.tools.tokenize.Tokenizer`](https://opennlp.apache.org/docs/1.8.2/apidocs/opennlp-tools/opennlp/tools/tokenize/Tokenizer.html)
and [`opennlp.tools.sentdetect.SentenceDetector`](https://opennlp.apache.org/docs/1.8.2/apidocs/opennlp-tools/opennlp/tools/sentdetect/SentenceDetector.html)
interfaces and can thus be used as a drop-in replacement in OpenNLP applications.

The scanner is based on the Lucene scanner with modifications from [David Hall](https://github.com/dlwh).  

Our changes mainly concern a good coverage of German abbreviations, 
and some updates for handling computer mediated communication, optimized and tested against the gold data from the [EmpiriST 2015](https://sites.google.com/site/empirist2015/) shared task (Beißwenger et al. 2016).

### Adding Support for more Languages
To adapt the included implementations to more languages, take one of the `language-specific_<language>.jflex-macro` files as template and 
modify for example the macro for abbreviations `SEABBR`. Then add an `execution` section for the new language
to the jcp ([java-comment-preprocessor](https://github.com/raydac/java-comment-preprocessor)) artifact in `pom.xml` following the example of one of the configurations there.
After building the project (see below) your added language specific tokenizer / sentence splitter should be selectable with the `--language` option.

Alternatively, you can also provide `KorAPTokenizer` implementations independently on the class path and select them with the `--tokenizer-class` option.

## Installation
```shell script
$ MAVEN_OPTS="-Xss2m" mvn clean install
```
#### Note
Because of the large table of abbreviations, the conversion from the jflex source to java,
i.e. the calculation of the DFA, takes about 4 to 20 minutes, depending on your hardware,
and requires a lot of heap space.

## Documentation
The KorAP tokenizer reads from standard input and writes to standard output. It supports multiple modes of operations.

#### Split into tokens
```
$ echo 'This is a sentence. This is a second sentence.' | java -jar target/KorAP-Tokenizer-2.0.0-standalone.jar
This
is
a
sentence
.
This
is
a
second
sentence
.

```
#### Split into tokens and sentences
```
$ echo 'This is a sentence. This is a second sentence.' | java -jar target/KorAP-Tokenizer-2.0.0-standalone.jar -s
This
is
a
sentence
.

This
is
a
second
sentence
.

```

#### Print token character offsets
With the `--positions` option, for example, the tokenizer prints all offsets of the first character of a token and the first character after a token.
In order to end a text, flush the output and reset the character position, an EOT character (0x04) can be used.
```
$ echo -n -e 'This is a text.\x0a\x04\x0aAnd this is another text.\n\x04\n' |\
     java -jar target/KorAP-Tokenizer-2.0.0-standalone.jar  --positions
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
   java -jar target/KorAP-Tokenizer-2.0.0-standalone.jar --no-tokens --positions --sentence-boundaries
1 5 6 9 10 11 12 17 18 20 21 22 23 27 27 28 29 32 33 37 38 40 41 42 43 51 51 54 55 58 59 63 64 67 68 72 72 76
1 28 29 54 55 76
0 3 4 8 9 11 12 19 20 24 24 25
0 25
```

## Development and License

**Authors**: 
* [Marc Kupietz](https://www1.ids-mannheim.de/digspra/personal/kupietz.html)
* [Nils Diewald](https://www1.ids-mannheim.de/digspra/personal/diewald.html)

Copyright (c) 2020, [Leibniz Institute for the German Language](http://www.ids-mannheim.de/), Mannheim, Germany

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

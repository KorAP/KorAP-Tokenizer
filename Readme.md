# KorAP Tokenizer
Efficient, [OpenNLP tools](https://opennlp.apache.org) compatible DFA tokenizer and sentence splitter with character offset output based on [JFlex](https://www.jflex.de/), suitable for German and other European languages.

## Description
The KorAP tokenizer is used for the German Reference Corpus DeReKo. Being based on a finite state automaton, 
it is not accurate as language model based tokenizers, but with ~5 billion words per hour typically more efficient.
An important feature in the DeReKo/KorAP context is also, that it reliably reports the character offsets of the tokens 
so that this information can be used for applying standoff annotations.
 
The main class `KorAPTokenizerImpl` implements the [`opennlp.tools.tokenize.Tokenizer`](https://opennlp.apache.org/docs/1.8.2/apidocs/opennlp-tools/opennlp/tools/tokenize/Tokenizer.html)
interface and can thus be used as a drop-in replacement in OpenNLP applications.

The scanner is based on the Lucene scanner with modifications from [David Hall](https://github.com/dlwh).  

Our changes mainly concern a good coverage of German abbreviations, 
and some updates for handling computer mediated communication, optimized and tested against the gold data from the [EmpiriST 2015](https://sites.google.com/site/empirist2015/) shared task (Beißwenger et al. 2016).

## Installation
```shell script
$ mvn clean install
```
#### … with changed jflex tokenizer source
Because of the large table of abbreviations, the conversion from the jflex source to java, i.e. the calculation of the DFA, takes more than 10 minutes and requires a lot of heap space.

For this reason the java source that depends on the jflex source is distributed with the source code and not deleted on `mvn clean`.

If you want to modify the jflex source, while keeping the abbreviation lists, you will need ad least 10 GB of free RAM and set
the maven option accordingly, e.g.:
```shell script
$ MAVEN_OPTS="-Xss600m -Xmx16000m" mvn clean install
```
## Documentation
The KorAP tokenizer reads from standard input and writes to standard output. It currently supports two modes.

In the default mode, the tokenizer prints all offsets of the first character of a token and the first character after a token.
In order to end a text, flush the output and reset the character position, the magic escape sequence `\n\x03\n` .
### Invocation Example
```
$ echo -n -e 'This is a text.\x0a\x03\x0aAnd this is another text.\n\x03\n' |\
   java -jar target/KorAP-Tokenizer-1.2-SNAPSHOT.jar

0 4 5 7 8 9 10 15 
0 3 4 8 9 11 12 19 20 25 
```
### With sentence splitting
```
$ echo -n -e ' This text. And this is a sentence!!! But what the hack????\x0a\x03\x0aAnd this is another text.\n\x03\nAnd this a sentence without marker\n' |java -jar target/KorAP-Tokenizer-1.2-SNAPSHOT.jar -s

1 5 6 10 10 11 12 15 16 20 21 23 24 25 26 34 34 37 38 41 42 46 47 50 51 55 55 59 
1 11 12 37 38 59
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

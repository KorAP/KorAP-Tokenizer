# Changelog

## 2.2.1

* "du." is no longer treated as an abbreviation.

## 2.2.0.9000

* "Dir." and "dir." are no longer treated as abbreviations.

## 2.2.0

* Apostrophe and hyphen marked contractions and clitics in English (I've, isn't, Peter's, …) 
  and French (j'ai, d'un, l'art, sont-elles, …) are now separated.


## 2.1.0

* GitHub CI test workflow added
* Dependencies updated
* `-Xss2m` added to maven jvm config

### Potentially breaking change

* `--sentence-boundaries|-s` now prints sentence boundaries only if `--positions|-p` is also present

## 2.0.0

* Dependencies updated
* Tokenizer and sentence splitter for English (`-l en` option) added
* Tokenizer and sentence splitter for French (`-l fr` option) added
* Support for adding more languages
* `UTF-8` input encoding is now expected by default, different encodings can be set by the `--encoding <enc>` option
* By default, tokens are now printed to stdout (use options `--no-tokens --positions` to print character offsets
  instead)
* Abbreviated German street names like *Kunststr.* are now recognized as tokens
* Added heuristics for distinguishing between *I.* as abbrevation vs PPER / CARD
* URLs without URI-scheme are now recognized as single tokens if they start wit `www.`

## 1.3

+ Standard EOT/EOF character x04 is used instead of magic escape \n\x03\n

* Quoted email names containing space characters, like "John Doe"@xx.com, are no longer interpreted as single tokens
* Sentence splitter functionality added (`--sentence-boundaries` option)

## 1.2

* First version published on https://korap.ids-mannheim.de/gerrit/plugins/gitiles/KorAP/KorAP-Tokenizer
* Extracted from KorAP-internal ingestion pipeline

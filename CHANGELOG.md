# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.0.0 [Unreleased]
* Tokenizer and sentence splitter for English (`-l en` option) added
* Tokenizer and sentence splitter for French (`-l fr` option) added 
* Support for adding more languages
* `UTF-8` input encoding is now expected by default, different encodings can be set by the `--encoding <enc>` option
* By default tokens are now printed to stdout (use options `--no-tokens --positions` to print character offsets instead)
* Abbreviated German street names like *Kunststr.* are now recognized as tokens
* Added heuristics for distinguishing between *I.* as abbrevation vs PPER / CARD
* URLs without URI-scheme are now recognized as single tokens if they start wit `www.`
## 1.3
+ Standard EOT/EOF character x04 is used instead of magic escape \n\x03\n
* Quoted email names containing space characters, like "John Doe"@xx.com, are no longer interpreted as single tokens
* Sentence splitter functionality added (`--sentence-boundaries` option)
## 1.2
* Extracted from KorAP-internal ingestion pipeline
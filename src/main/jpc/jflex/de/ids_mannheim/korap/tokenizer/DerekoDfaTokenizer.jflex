package de.ids_mannheim.korap.tokenizer;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 Modifications 
 Copyright 2014 David Hall

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

/**
 Further Modifications 
 Copyright 2016 Marc Kupietz

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
import java.io.*;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.List;
import opennlp.tools.util.Span;

@Languages({ /*$"\""+target.language+"\" })$*/ /*-*/ ""})
%%

/**
* Based on the Epic tokenizer (https://github.com/dlwh/epic)
* ... which is ...
* Based on Lucene's StandardTokenizerImpl, but heavily modified.
*/
%class DerekoDfaTokenizer_/*$target.language$*/
%unicode
%public
%implements KorapTokenizer, opennlp.tools.tokenize.Tokenizer, opennlp.tools.sentdetect.SentenceDetector
%type Span
%function getNextToken
%char

%{
    private static final CharSequence[] targetLanguages = { /*$"\""+target.language+"\"};$*/ /*-*/ "" };
    private boolean xmlEcho = false;
    private boolean normalize = false;
    private boolean debug = false;
    private boolean newSentence = true;
    private long startOffset = 0;
    private long previousFileEndOffset = -1;
    private int tokenId = 0;
    private boolean atEOT = false;
    private boolean splitSentences = false;
    private boolean echo = false;
    private boolean printOffsets = false;
    private boolean printTokens = false;
    private PrintStream outputStream = System.out;

    @Override
    public CharSequence[] getTargetLanguages() {
        return targetLanguages;
    }

    public DerekoDfaTokenizer_/*$target.language$*/() {
        this.zzReader = null;
    }

    @Override
    public void setInputReader(Reader inputReader) {
        this.zzReader = inputReader;
    }

    @Override
    public void setSplitSentences(boolean splitSentences) {
        this.splitSentences = splitSentences;
    }

    @Override
    public void setEcho(boolean echo) {
        this.echo = echo;
    }

    @Override
    public void setPrintOffsets(boolean printOffsets) {
        this.printOffsets = printOffsets;
    }

    @Override
    public void setPrintTokens(boolean printTokens) {
        this.printTokens = printTokens;
    }

    @Override
    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    @Override
    public void scan() throws IOException {
        List<Span> list = new ArrayList<Span>();
        Span token;
        while (!zzAtEOF) {
            token = this.getNextToken();
            if (atEOT) {
                if (echo && printOffsets) {
                    printTokenPositions(list, splitSentences);
                }
                list.clear();
                atEOT = false;
            }
            if (token != null) {
                list.add(token);
            }
        }
    }

    @Override
    public String[] tokenize(String s) {
        Span[] spans;
        int i;
        String[] tokens;

        spans = tokenizePos(s);
        tokens = new String[spans.length];
        for (i = 0; i < spans.length; i++) {
            tokens[i] = spans[i].getType();
        }
        return tokens;
    }

    void printTokenPositions(List<Span> spanList, boolean sentencize) {
        int sentenceStart = -1;
        StringBuilder tokenStringBuffer = new StringBuilder();
        StringBuilder sentenceStringBuffer = new StringBuilder();
        for (int i = 0; i < spanList.size(); i++) {
            Span s = spanList.get(i);
            if (sentenceStart == -1)
                sentenceStart = s.getStart();
            if (printOffsets) {
                tokenStringBuffer.append(s.getStart())
                        .append(" ")
                        .append(s.getEnd());
                if (i < spanList.size() - 1)
                    tokenStringBuffer.append(" ");
            }
            if (isSentenceBound(s.getType()) || (i == spanList.size() - 1)) {
                sentenceStringBuffer.append(sentenceStart)
                        .append(" ")
                        .append(s.getEnd());
                sentenceStart = -1;
                if (i < spanList.size() - 1)
                    sentenceStringBuffer.append(" ");
            }
        }
        outputStream.println(tokenStringBuffer.toString());
        if (sentencize)
            outputStream.println(sentenceStringBuffer.toString());
    }

    @Override
    public Span[] tokenizePos(String s) {
        Span token;
        int i = 0;
        List<Span> list = new ArrayList<Span>();
        tokenId = 0;
        yyreset(new StringReader(s));
        try {
            while (!this.zzAtEOF) {
                token = this.getNextToken();
                if (atEOT) {
                    if (echo) {
                        printTokenPositions(list, splitSentences);
                        list.clear();
                    }
                    atEOT = false;
                }
                if (token != null) {
                    list.add(token);
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("IO error scanning " + s);
            System.err.println(e);
        }
        return (list.toArray(new Span[list.size()]));
    }

    @Override
    public String[] sentDetect(String s) {
        Span[] spans;
        int i;
        String[] sentences;

        spans = sentPosDetect(s);
        sentences = new String[spans.length];
        for (i = 0; i < spans.length; i++) {
            sentences[i] = spans[i].getType();
        }
        return sentences;
    }

    @Override
    public Span[] sentPosDetect(String s) {
        final Span tokens[] = tokenizePos(s);
        ArrayList<Span> sentences = new ArrayList<Span>();
        int sentenceStart = 0;
        if (tokens.length > 0)
            tokens[0].getStart();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].getType().matches("^[.?!]+$") || i == tokens.length - 1) {
                sentences.add(new Span(sentenceStart, tokens[i].getEnd(), s.substring(sentenceStart, tokens[i].getEnd())));
                if (i < tokens.length - 1) {
                    sentenceStart = tokens[i + 1].getStart();
                }
            }
        }
        return sentences.toArray(new Span[0]);
    }

    public final long yychar() {
        return yychar;
    }

    final Span currentToken() {
        return currentToken(yytext());
    }

    public boolean isSentenceBound(String s) {
        return s.matches("^[.?!]+$");
    }

    final Span currentToken(String normalizedValue) {
        String value;
        long lengthDiff = 0;
        previousFileEndOffset = -1;

        if (normalize) {
            value = normalizedValue;
        } else {
            value = yytext();
            lengthDiff = value.length() - value.codePointCount(0, value.length());
        }
        if (startOffset > yychar || startOffset < 0) { // how can this happen?
            startOffset = 0;
        }
        long from = (yychar - startOffset),
                to = (yychar - startOffset + yylength() - lengthDiff);
        if (xmlEcho) {
            outputStream.println("<span id=\"t_" + tokenId + "\" from=\"" + from + "\" to=\"" + to + "\"/>\n" + value);
        } else if (echo && printTokens) {
            outputStream.println(value);
            if (splitSentences && isSentenceBound(normalizedValue))
                outputStream.println("");
        }
        startOffset += lengthDiff;
        tokenId++;
        return new Span((int) from, (int) to, value);
    }

    final void fileEnd() {
        startOffset = yychar + yylength();
        // do not end a file multiple times because of additional EOT characters
        if (startOffset == previousFileEndOffset)
            return;
        atEOT = true;
        previousFileEndOffset = startOffset;
        tokenId = 0;
    }

    final Span xmlPassage() {
        if (xmlEcho) {
            String dings = yytext();
            if (dings.indexOf("<text") >= 0) {
                startOffset = yychar + yylength();
                tokenId = 0;
            }
            outputStream.println(dings.replaceAll("[\n\r]+", ""));
            return null;
        } else {
            return currentToken();
        }
    }

    final void zipArchive() {
        String name;
        String matched = yytext();
        int start = 10;
        name = matched.substring(start, matched.length() - 1);
        outputStream.println("<archive name=\"" + name + "\"/>");
    }

    final void zippedFile() {
        String name;
        String matched = yytext();
        int start = 13;
        name = matched.substring(start, matched.length() - 3);
        outputStream.println("<file name=\"" + name + "\"/>");
    }
%}

THAI       = [\u0E00-\u0E59]

// basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|{THAI}|[:digit:]|_)+

// case insensitivity is useful sometimes
a = [aA]
// b = [bB]
c = [cC]
// d = [dD]
// e = [eE]
// f = [fF]
g = [gG]
// h = [hH]
// i = [iI]
// j = [jJ]
// k = [kK]
l = [lL]
// m = [mM]
// n = [nN]
o = [oO]
p = [pP]
q = [qQ]
// r = [rR]
// s = [sS]
// t = [tT]
// u = [uU]
// v = [vV]
w = [wW]
// x = [xX]
// y = [yY]
// z = [zZ]

ALPHA      = ({LETTER}|¨)+

NEWLINE = [\n\r]

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
// ABBRNYM    =  {LETTER} "." ({LETTER} ".")+

// ACRONYM_DEP	= {ALPHANUM} "." ({ALPHANUM} ".")+

// hostname
HOST       =  ({ALPHANUM}|"-"){4,15} ((".") ({ALPHANUM}|"-"){2,16})+

EMDASH = (--|---|[\u2014\u2015\u2e3a\u2e3b\ufe58]+)

DASH = ([\-\u2011\u2012\u2013\u2e1a\ufe63\uff0d])

SLASH = [⁄∕／/]


// url

// url spec lifted from Lucene

// URL and E-mail syntax specifications:
//
//     RFC-952:  DOD INTERNET HOST TABLE SPECIFICATION
//     RFC-1035: DOMAIN NAMES - IMPLEMENTATION AND SPECIFICATION
//     RFC-1123: Requirements for Internet Hosts - Application and Support
//     RFC-1738: Uniform Resource Locators (URL)
//     RFC-3986: Uniform Resource Identifier (URI): Generic Syntax
//     RFC-5234: Augmented BNF for Syntax Specifications: ABNF
//     RFC-5321: Simple Mail Transfer Protocol
//     RFC-5322: Internet Message Format

// http://code.ohloh.net/file?fid=wEylHt__FppVh8Ub_GTsx__CTK4&cid=d0f5PFFYrnk&s=UAX29URLEmailTokenizerImpl&filterChecked=true&fp=473333&mp,=1&ml=1&me=1&md=1&projSelected=true#L0

DomainLabel = [A-Za-z0-9] ([-A-Za-z0-9]* [A-Za-z0-9])?
DomainNameLoose  = {DomainLabel} (("."|"[dot]") {DomainLabel})*
WWWDomainName = "www" (("."|"[dot]") {DomainLabel})*

IPv4DecimalOctet = "0"{0,2} [0-9] | "0"? [1-9][0-9] | "1" [0-9][0-9] | "2" ([0-4][0-9] | "5" [0-5])
IPv4Address  = {IPv4DecimalOctet} ("." {IPv4DecimalOctet}){3}
IPv6Hex16Bit = [0-9A-Fa-f]{1,4}
IPv6LeastSignificant32Bits = {IPv4Address} | ({IPv6Hex16Bit} ":" {IPv6Hex16Bit})
IPv6Address =                                                  ({IPv6Hex16Bit} ":"){6} {IPv6LeastSignificant32Bits}
            |                                             "::" ({IPv6Hex16Bit} ":"){5} {IPv6LeastSignificant32Bits}
            |                            {IPv6Hex16Bit}?  "::" ({IPv6Hex16Bit} ":"){4} {IPv6LeastSignificant32Bits}
            | (({IPv6Hex16Bit} ":"){0,1} {IPv6Hex16Bit})? "::" ({IPv6Hex16Bit} ":"){3} {IPv6LeastSignificant32Bits}
            | (({IPv6Hex16Bit} ":"){0,2} {IPv6Hex16Bit})? "::" ({IPv6Hex16Bit} ":"){2} {IPv6LeastSignificant32Bits}
            | (({IPv6Hex16Bit} ":"){0,3} {IPv6Hex16Bit})? "::"  {IPv6Hex16Bit} ":"     {IPv6LeastSignificant32Bits}
            | (({IPv6Hex16Bit} ":"){0,4} {IPv6Hex16Bit})? "::"                         {IPv6LeastSignificant32Bits}
            | (({IPv6Hex16Bit} ":"){0,5} {IPv6Hex16Bit})? "::"                         {IPv6Hex16Bit}
            | (({IPv6Hex16Bit} ":"){0,6} {IPv6Hex16Bit})? "::"

URIunreserved = [-._~A-Za-z0-9]
URIpercentEncoded = "%" [0-9A-Fa-f]{2}
URIsubDelims = [!$&\'()*+,;=]
URIloginSegment = ({URIunreserved} | {URIpercentEncoded} | {URIsubDelims})*
URIlogin = {URIloginSegment} (":" {URIloginSegment})? "@"
URIquery    = "?" ({URIunreserved} | {URIpercentEncoded} | {URIsubDelims} | [:@/?])*
URIfragment = "#" ({URIunreserved} | {URIpercentEncoded} | {URIsubDelims} | [:@/?])*
URIport = ":" [0-9]{1,5}
URIhostStrict = ("[" {IPv6Address} "]") | {IPv4Address}
URIhostLoose  = ("[" {IPv6Address} "]") | {IPv4Address} | {DomainNameLoose}

URIauthorityStrict =             {URIhostStrict} {URIport}?
URIauthorityLoose  = {URIlogin}? {URIhostLoose}  {URIport}?

HTTPsegment = ({URIunreserved} | {URIpercentEncoded} | [;:@&=])*
HTTPpath = ("/" {HTTPsegment})*
HTTPscheme = [hH][tT][tT][pP][sS]? "://"
HTTPurlFull = {HTTPscheme} {URIauthorityLoose}  {HTTPpath}? {URIquery}? {URIfragment}?
// {HTTPurlNoScheme} excludes {URIlogin}, because it could otherwise accept e-mail addresses
HTTPurlNoScheme =          ( {URIauthorityStrict} | {WWWDomainName} ) {HTTPpath}? {URIquery}? {URIfragment}?
HTTPurl = {HTTPurlFull} | {HTTPurlNoScheme}

FTPorFILEsegment = ({URIunreserved} | {URIpercentEncoded} | [?:@&=])*
FTPorFILEpath = "/" {FTPorFILEsegment} ("/" {FTPorFILEsegment})*
FTPtype = ";" [tT][yY][pP][eE] "=" [aAiIdD]
FTPscheme = [fF][tT][pP] "://"
FTPurl = {FTPscheme} {URIauthorityLoose} {FTPorFILEpath} {FTPtype}? {URIfragment}?

FILEscheme = [fF][iI][lL][eE] "://"
FILEurl = {FILEscheme} {URIhostLoose}? {FTPorFILEpath} {URIfragment}?

URL = {HTTPurl} | {FTPurl} | {FILEurl}

// EMAILquotedString without space
// EMAILquotedString = [\"] ([\u0001-\u0008\u000B\u000C\u000E-\u001F\u0021\u0023-\u005B\u005D-\u007E] | [\\] [\u0000-\u007F])* [\"]
// original version from lucene
// EMAILquotedString = [\"] ([\u0001-\u0008\u000B\u000C\u000E-\u0021\u0023-\u005B\u005D-\u007E] | [\\] [\u0000-\u007F])* [\"]
EMAILatomText = [A-Za-z0-9!#$%&\'*+-/=?\^_`{|}~]
EMAILlabel = {EMAILatomText}+
EMAILlocalPart = {EMAILlabel} ("." {EMAILlabel})*
EMAILdomainLiteralText = {ALPHANUM}|{DomainNameLoose}
//EMAILdomainLiteralText = ([\u0001-\u0008\u000B\u000C\u000E-\u005A\u005E-\u007F]|[\\][\u0000-\u007F])*{ALPHANUM}
// DFA minimization allows {IPv6Address} and {IPv4Address} to be included
// in the {EMAILbracketedHost} definition without incurring any size penalties,
// since {EMAILdomainLiteralText} recognizes all valid IP addresses.
// The IP address regexes are included in {EMAILbracketedHost} simply as a
// reminder that they are acceptable bracketed host forms.
EMAILbracketedHost = "["? ({EMAILdomainLiteralText}+ | {IPv4Address} | [iI][pP][vV] "6:" {IPv6Address}) "]"?
EMAIL = {EMAILlocalPart} ("@"|"["at"]") ({EMAILbracketedHost})

 //  {ALPHANUM} "://" {HOST} (ALPHANUM|\/)*
// URL =  ({ALPHA}({ALPHANUM}|-)+:(/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)([^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))


// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)


/* floating point literals */
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

// punctuation
P	         = ("_"|"-"|"."|",")|{SLASH}

Q = [’\'`]

PUNCT = ({P}|{Q}|[?!@#$%\^&*_:;\]\[\"»«\202\204\206\207\213\221\222\223\224\225\226\227\233])

// at least one digit
HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*


LETTER     = ([:letter:]|¨)

ENGLISH_CLITIC = ({Q}(ll|d|ve|s|re|LL|D|VE|S|RE|m|M|n|N|[eE][mM])?|[nN]{Q}[Tt])

FRENCH_CLITIC = (-t-elles?|-t-ils?|-t-on|-ce|-elles?|-ils?|-je|-la|-les?|-leur|-lui|-mêmes?|-m\'|-moi|-nous|-on|-toi|-tu|-t\'|-vous|-en|-y|-ci|-là)

IRISH_O = [Oo]{Q}

FRENCH_INIT_CLITIC = ([dcjlmnstDCJLNMST]\'|[Qq]u\'|[Jj]usqu\'|[Ll]orsqu\')

ENGLISH_MARKERS_FOR_NON_ABBREVIATION_I = (am|was|will|have|had|would|do|did|and|War|than|not|[Pp]art)

INIT_CLITIC = ({FRENCH_INIT_CLITIC})

POLISH_CONDITIONAL_CLITIC = (by)

POLISH_CONDITIONAL_ENDING = (m|ś|śmy|ście)?

POLISH_PAST_ENDING_1 = (ś|śmy|ście)
POLISH_PAST_ENDING_2 = ([mś]?|śmy|ście)

WHITESPACE = \s

ENDMARKER = (\n?\004\n?)
XML = <(\/text|\?xml|\?xml-model|\/?raw_text|\/?metadata) ?[^\004\n>]{0,100}>

EMOTICON = ( [<>]?[BX;8:=][o\-\']?[DdPp()\/3>oO*]+|<\/?3+|ಠ_ಠ|\(-.-\)|\(T_T\)|\(♥_♥\)|\)\':|\)-:|\(-:|\)=|\)o:|\)x|:\'C|:\/|:<|:C|:[|=\(|=\)|=D|=P|>:|D\':|D:|\:|]:|x\(|\^\^|o.O|oO|\\{o}\/|\\m\/|:;\)\)|_\)\)|\*_\*|._.|:wink:|>_<|\*<:-\)|[:;]\)|[;;]" "\))

LC_CONSONANT = [bcdfgjklmnpqrstvwxs]
OMISSIONWORD = ({p}resqu'île|{a}ujourd\'hui|{q}uelqu\'une?|[^\P{L}Qq]{LETTER}?[^dcjlmnstDCJLNMST][\'`]|{LETTER}+\*\*+{LETTER}*|{LETTER}+\*{LETTER}+|!(!({LETTER}+[\'`]{LC_CONSONANT})|{INIT_CLITIC})){LETTER}*

EXTENSION = (html?|doc|docx?|pptx?|xlsx?|pdf|jpe?g|mp[34]|ogg|png|avi|txt|xml|aac|HTML?|DOCX?|PPTX?|XLSX?|GIF|JPE?G|TXT)
FNAME = ({LETTER}:[\\/]{LETTER})?({LETTER}|[\\_/-])+\.{EXTENSION}

PLUSAMPERSAND = (&amp;|&apos;|&gt;|&K|&lt;|&M|&quot;|&RQ|\+Ale|\+ALe|\+Anima|\+APD|\+co|\+Co|\+GF\+|\+Leif|\+Strang|\+Teamgeist|A&A|A&E|A&F|A&M|A&O|A&P|A&R|A&V|A&W|A\+\+|A\+\+\+|A\+E|A\+f|AAC\+|ABC&D|AC\+|AD&D|AE&E|AES\+F|AEW&C|AFM\+E|AGTL\+|Altenpflege\+ProPflege|Analyse\+kritik|anlagen\+verfahren|ANT\+|Anynet\+|Applus\+|Arch\+|ARCH\+|ART\+COM|AS&P|ASC\+T|ASEAN\+|Asis&t|AT&L|AT&S|AT&SF|AT&T|ATV\+|Auer\+Weber|Auer\+Weber\+Assoziierte|Axis&Allies|B&B|B&C|B&F|B&G|B&H|B&I|B&K|B&M|B&MTJR|B&NES|B&O|B&Q|B&R|B&T|B&V|B&W|B\+B|B\+R|B\+T|Baby&Co|Bayern\+|BB&T|BD\+|Beast\+|BEAST\+|Beck\+Schubert|Belle&Sebastian|BFE\+|BG\+BRG|BIBEL\+ORIENT|Bild\+Funk|Binder\+Co|Blohm\+Voss|Blood\+|Blut\+Eisen|BM&F|BM&FBovespa|Bolles\+Wilson|Bottega\+Ehrhardt|Brangs\+Heinrich|BRF\+|Briner\+Kern|BUCH&media|Burghardt\+Schmidt|bus\+bahn|C&A|C&C|C&D|C&L|C&M|C&O|C&P|C&R|C&S|C&T|C&W|C\+\+|C\+\+Builder|C\+c|C\+C|C\+M\+B|Ca\+\+|Cafe\+co|Cafe\+Co|Canal\+|Cantata\+\+|CB&I|CC&G|CCC&StL|CD&E|CD&V|CD\+DVD|CD\+G|CDIA\+|Celtic\+|Cendres\+M|Chage&Aska|Chage&Asuka|Channel\+smile|Charm\+\+|Chip&Chap|CI&CEQ|CI\+|Click&Buy|Cocl&Seff|Com&Com|COM\+|Comicplus\+|COR&FJA|CS&S|CT&T|ctc\+\+|Ctrl\+Alt\+Del|CTRL\+ALT\+DEL|Cube\+|Cyfra\+|CYFRA\+|D&A|D&AD|D&b|D&B|D&D|D&G|D&O|D&RGW|D&S|D&W|D\+Q|DAB\+|DACH\+HOLZ|DAML\+OIL|DBM&T|Dc\+\+|DC\+\+|DDDBM&T|Despe&Siga|DF&S|Digital\+|DirectConnect\+\+|Dissing\+Weitling|DL\+NT|DLSW\+|Do&Co|Dok&Deb|Dorma\+kaba|DP&L|Drm\+|DRM\+|DTS\+\+|DU&ICH|DVD\+R|Dvd\+rw|DVD\+RW|E&a|E&N|E&Y|E\+|E\+e|E\+h|E\+H|EAAC\+|Ebert\+Jacobi|ECO\+|EG&G|Eigen\+Art|Eins\+Alles|Electromobility\+|En\+|Endress\+Hauser|Erasmus\+|ES&T|ETV\+|EV\+|Eve&rave|Every\+|F&A|F&B|F&E|F&F|F&K|F\+F|F\+U|Familie&Co|FAT\+|Film\+|FILM\+SCHULE|Fischer\+Kr|Fix\+Foxi|FLUXUS\+|FMHL\+|Form\+zweck|fuhrpark\+management|G&B|G&D|G&IF|G&L|G&V|G\+\+|G\+H|G\+J|G\+tt|GC&CS|GDI\+|ge\+her|GG&L|Go\+|GO\+|Google\+|Goran\+Vujic|GRAF\+ZYX|Gruner\+Jahr|Gtk\+|GTK\+|GTL\+|GTX\+|Guide\+|H&BC|H&H|H&K|H&M|h&m|H&N|H&R|H&S|H\+BEDV|H\+H|H\+N|H\+S|Haase&band|Hahn\+Kolb|HAHN\+KOLB|Hasta\+Coda|Haubitz&Zoche|Haubitz\+Zoche|HBCI\+|HD\+|Health&Care|Heim\+Handwerk|Heute\+|HFS\+|hne\+Nagel|HSPA\+|HT&L|HTML\+TIME|Huber\+Suhner|Hunger&Seide|I&A|I&K|I&Q|I&u|I&U|I\+D|I\+R|Ich\+Ich|ID&T|Idee\+spiel|Ihp\+|II\+|IIc\+|III\+|IK\+|In&phone|In&Phone|info\+|Interkama\+|IT&Production|J&B|J&D|J&J|J&M|J&P|J&S|J&T|J\+\+|J\+S|Jazz\+Az|Jenna\+Ron|Johnson&Johnson|JU\+TE|Jugend\+Sport|Jugend\+Technik|Jump&Run|K&k|K&K|K&L|K&M|K&N|K&R|K&S|K&U|K\+\+|K\+A|K\+H|K\+K|k\+Metal|K\+R|K\+S|K\+W|Kai\+Sven|Kaiser\+Kraft|KAISER\+KRAFT|Kino&Co|KINO&CO|Kino\+|Kirche\+Leben|Klassik&JazzMagazin|Kurz&F|L&B|L&C|L&M|L&N|L&P|L&S|L\+R|L\+T|Lancia\+Voyager|Landis\+Gyr|LB&SCR|Leader\+|LEADER\+|Lederer\+Ragnarsd|Leicht&Cross|Lenord\+Bauer|Leslie\+Lohman|Libsigc\+\+|Life&Style|LIFE\+|Light\+Building|Lippmann\+Rau|LISA\+|Lords&Knights|LT&SR|Lussi\+Halter|M&A|M&B|M&D|M&G|M&i|M&I|M&M|M&Ms|M&N|M&S|M&T|M\+a|M\+C|M\+M|M\+O|M\+s|M\+S|M\+W|Maildir\+\+|Mann\+Hummel|Markt\+Technik|Means\+\+|Melodie&Rhythmus|Metadata\+|Miles&more|Milk\+|Mining\+geo|Mix&Genest|mmerly\+Frey|Monet\+|Motion\+picture|MPP\+|MS&D|MS&L|MStP&SSM|Music&Voice|N&CRR|N&ER|n&gut|N&R|N&W|N\+M|Na\+|NADHH\+|Nah&gut|Natur\+kosmos|natur\+mensch|Nc\+|NI&Co|nig\+Neurath|Nike\+iPod|Nintendogs\+Cats|Notepad\+\+|NYW&B|O&K|O&L|O&M|Ola\+|OMNeT\+\+|ORFsport\+|Ost\+Front|P&A|P&C|P&E|P&G|P&I|P&ID|P&L|P&M|P&O|P&P|P&R|P&T|P&TLuxembourg|P&W|P\+M|P\+R|P\+S|PAL\+|Pan&Scan|Papier&Stift|Park&Charge|Park&Rail|Park&Ride|Park&Suites|PB&J|Peek&Cloppenburg|Pen&Paper|Pepperl&Fuchs|Pepperl\+Fuchs|Peste&Sida|PG&E|Pirelli&C|Pittel\+Brausewetter|Plug&play|Plus\+|POB&A|Pol&is|POL&IS|POLO\+|Poses\+\+|PP&P|Pratt&Whitney|Princess\+|Prius\+|Procter&Gamble|Prozac\+|PS&P|Pur\+|Q&A|Q&Q|Q\+Q|Quanta\+|R&A|R&B|R&D|R&ER|R&F|R&G|R&I|R&M|R&Q|R&R|R&S|R\+C|R\+S|R\+V|Rail&Fly|REDD\+|Reise&Touristik|Relax\+ng|RF&P|Richter\+Frenzel|Rio\+|Rohde&Schwarz|RT\+|Run&Dine|S&B|S&D|S&G|S&H|S&K|S&M|S&P|S&T|S&w|S&W|S\+D|S\+G|S\+T|S\+U|Sales&Services|Sam&Max|Schedule\+|Schiff&Hafen|Schlund\+Partner|Schmelzle\+Partner|Schmidt\+Clemens|science\+business|Science\+Business|sd&m|Sd&m|Sdr\+|Serve&Volley|Severin\+K|SiMPLE\+\+|SMS&park|SMW\+|Soap&Skin|Solo\+|Spar\+Kreditbank|Spar\+Leihkasse|speed\+|Speed\+|Spoga\+gafa|SPORT\+|Sport\+Technik|SS\+|St&H|St&Z|Standard&Poor|Standard&Poors|Station&Service|Steib\+Steib|Stil&Stadt|Strategy&|Strg\+Alt\+Entf|StrongDC\+\+|Such&Find|Sumol\+Compal|SVS&E|SVWZ\+|SW&S|Swift\+|SXGA\+|T&D|T&L|T&N|T&T|T\+A|T\+T|TACACS\+|Tanz&FolkFest|Taylor&Francis|text\+kritik|TEXT\+KRITIK|textil\+mode|Timidity\+\+|TMRM\+|Toni&Guy|toon\+|Touch&Travel|Track\+|Trends\+More|TT&C|TT&R|ttir\+Oei|TV\+Synchron|U&D|U\+\+|U\+F|Ultimate\+\+|Urban&Fischer|URW\+\+|USC&GS|UTC\+|V&A|V&R|V&S|V&W|Valentien\+Valentien|VC\+\+|VF\+|Vieweg\+Teubner|VISEO\+|Vision\+Technik|VisualDSP\+\+|VIVA\+|VL&D|Vorschau\+R|Vorster&Gr|VT&MA|W&B|W&F|W&G|W&H|W&p|W&V|W&W|WB\+|Wein\+Markt|Wienstroth&Hammans|Winkler\+D|Wirtschaft\+Markt|WP&YR|WS&P|WSXGA\+|WXGA\+|X\+\+|X\+Y|Xbase\+\+|XHTML\+SMIL|Y&R|Y&T|Yin&Yang|Yotsuba&|Young&Queer|Z\+W|Zeidler&Wimmel|Zinc&Germanium|[23]G\+?(-Regeln?)?)

TWITTER_HANDLE = @{ALPHA}{ALPHANUM}?
TWITTER_HASHTAG = #{ALPHANUM}

// blocks of question marks and exclamation marks are one token
LONG_END_PUNCT = [?!][?!1]+

WORD = ({IRISH_O}?{ALPHANUM}+|[Qq]ur{Q}an)

// pragmas used for anonymization etc.
PRAGMA = \[_[A-Z\-]+_\]

%include language-specific_/*$target.language$*/.jflex-macro

%s OPEN_QUOTE POLISH_CONDITIONAL_MODE JUST_AFTER_PERIOD CLITIC_MODE ENGLISH_NON_ABBREVIATION_I_MODE

%%
{ENDMARKER}                                             { fileEnd(); return null; }


// dates and fractions

<POLISH_CONDITIONAL_MODE>{POLISH_CONDITIONAL_CLITIC} / {POLISH_CONDITIONAL_ENDING}                                      { yybegin(YYINITIAL); return currentToken(); }
<POLISH_CONDITIONAL_MODE>[^b].                                        { throw new RuntimeException("..." + currentToken());}
{EMDASH}                                                 {return currentToken();}
{URL}                                                         { return currentToken(); }

// special words
{c}an / not                                                     {return currentToken();}
{l}em / me                                                      {return currentToken();}
{g}on / na                                                      {return currentToken();}
{g}im / me                                                      {return currentToken();}
{w}an / na                                                      {return currentToken();}
{g}ot / ta                                                      {return currentToken();}

// M. I. Baxter was killed in World War I.<s> So was I.<s>
{ENGLISH_MARKERS_FOR_NON_ABBREVIATION_I} / {WHITESPACE} [I] \.  {yybegin(ENGLISH_NON_ABBREVIATION_I_MODE); return currentToken(); }
<ENGLISH_NON_ABBREVIATION_I_MODE>[I] / \.                       {yybegin(YYINITIAL); return currentToken(); }

{LETTER}\.                                                      {return currentToken();}
{LETTER}{2,12} / \.[:uppercase:]                                {return currentToken();}
{PLUSAMPERSAND}                                                 {return currentToken();}
{SEABBR}\.                                                      {return currentToken();}
{PRAGMA}                                                        {return currentToken();}
{FNAME}                                                         {return currentToken();}

// contractions and other clitics
{INIT_CLITIC}                                                   {return currentToken();}
<CLITIC_MODE>{CLITIC}                                           {yybegin(YYINITIAL); return currentToken();}
{WORD} / {CLITIC}                                               {yybegin(CLITIC_MODE); return currentToken();}
d{Q} / ye                                                       {return currentToken(); }
{Q}[Tt] / is                                                    {return currentToken(); }

// polish clitics
{ALPHANUM}{ALPHANUM}+[lł][aeoiy]? / {POLISH_CONDITIONAL_CLITIC}{POLISH_CONDITIONAL_ENDING}             {yybegin(POLISH_CONDITIONAL_MODE); return currentToken(); }
{ALPHANUM}{ALPHANUM}+[lł][aeoiy]? / {POLISH_PAST_ENDING_1}                    {return currentToken(); }
// need to not let lam through....
{ALPHANUM}{ALPHANUM}+[ł][aeoiy]? / {POLISH_PAST_ENDING_2}                    {return currentToken(); }

// times
[01]?[0-9]{WHITESPACE}?:[0-6][0-9]                              { return currentToken(yytext().replaceAll("\\s+","")); }

// ordinals
[0-9]{1,3}\.                                           {return currentToken();}

// quotes
<YYINITIAL>\"/{WHITESPACE}*{ALPHANUM}              { yybegin(OPEN_QUOTE); return currentToken("``"); }
<YYINITIAL>\'/{WHITESPACE}*{ALPHANUM}               { yybegin(OPEN_QUOTE); return currentToken("`"); }
‘                                                  { yybegin(OPEN_QUOTE); return currentToken("`"); }
’                                                  { yybegin(YYINITIAL); return currentToken("'"); }
<OPEN_QUOTE>\"                                                 { yybegin(YYINITIAL); return currentToken("''"); }
“                                                 { yybegin(YYINITIAL); return currentToken("``"); }
”                                                 { yybegin(YYINITIAL); return currentToken("''"); }
\"/.*{ALPHANUM}+                                  { yybegin(OPEN_QUOTE); return currentToken("``"); }
\"                                                { yybegin(YYINITIAL); return currentToken("''"); }

":!:"                                                             { return currentToken();}
"->"                                                             { return currentToken();}
"<-"                                                             { return currentToken();}
\*\*+                                                          { return currentToken();}
\[\[+                                                          { return currentToken();}
\]\]+                                                          { return currentToken();}

// normal stuff
// dashed words
{WORD}({DASH}{NEWLINE}*{WORD})+                                { return currentToken();}
{WORD}{DASH}                                                   { return currentToken();}
{TWITTER_HANDLE}                                               { return currentToken(); }
{TWITTER_HASHTAG}                                              { return currentToken(); }
{WORD}                                                         { return currentToken();}
{OMISSIONWORD}                                                 { return currentToken();}
//{ABBRNYM}                                                      { return currentToken(); }
{EMAIL}                                                        { return currentToken(); }
{HOST}                                                         { return currentToken(); }
{NUM}                                                          { return currentToken(); }
//{ACRONYM_DEP}                                                  { return currentToken(); }
{NEWLINE}                                                     { }
{WHITESPACE}                                                  { }

// KorAP-XML spcecifics
^{WHITESPACE}*{XML}{NEWLINE}*                                 {xmlPassage(); }
\<\/text>{NEWLINE}*                                              {xmlPassage(); }
^"Archive:  "[^ \n]+".zip"\n                           {zipArchive(); }  // handle unzip -c
^"  "+inflating: [^\n]{1,255}"  "\n                              {zippedFile(); }

// \(                                                  {return currentToken("-LRB-");}
// \)                                                  {return currentToken("-RRB-");}
//\{                                                  {return currentToken("-LCB-");}
//\}                                                  {return currentToken("-RCB-");}
//\[                                                  {return currentToken("-LSB-");}
//\]                                                  {return currentToken("-RSB-");}
([.][.]+|…+)                                                 {return currentToken("...");}
{LONG_END_PUNCT}                                        { return currentToken();}
{PUNCT}                                               { return currentToken();}
{EMOTICON}                                          { return currentToken();}
{DASH}{DoubleLiteral}                               { return currentToken();}
<<EOF>>                                             { fileEnd(); return null;}
.                                                   { return currentToken();}



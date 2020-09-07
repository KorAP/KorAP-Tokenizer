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

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.List;
import opennlp.tools.util.Span;
%%

/**
* Based on the Epic tokenizer (https://github.com/dlwh/epic)
* ... which is ...
* Based on Lucene's StandardTokenizerImpl, but heavily modified.
*/
%class KorAPTokenizerImpl
%unicode
%public
%implements opennlp.tools.tokenize.Tokenizer
%type Span
%function getNextToken
%char

%{

	public boolean xmlEcho = false;
	public boolean normalize = false;
	public boolean debug = false;
	private long startOffset = 0;
	private int tokenId = 0;
	private StringBuffer bounds = null;
	
  public KorAPTokenizerImpl() {
    this.zzReader = null;
  }

	public String[] tokenize(String s) {
		Span[] spans;
		int i;
		String[] tokens;
		
		spans = tokenizePos(s);
		tokens = new String[spans.length];
		for(i=0; i<spans.length; i++) {
			tokens[i]=spans[i].getType();
		}
		return tokens;
	}
	
	public Span[] tokenizePos(String s) {
		Span token;
		int i=0;
		List<Span> list = new ArrayList<Span>();
		tokenId=0;
		yyreset(new StringReader(s));
		try {
			while(!this.zzAtEOF) {
				token = this.getNextToken();
				if(token != null) {
					list.add(token);
				}
			} 
		} catch (java.io.IOException e) {
			System.out.println("IO error scanning "+s);
			System.out.println(e);
		}
		return(list.toArray(new Span[list.size()]));
	}

	public int[] tokenizeMilestones(String s) {
		Span[] spans;
		int i;
		int[] milestones;
		
		spans = tokenizePos(s);
		milestones = new int[2*spans.length];
		for(i=0; i<spans.length; i++) {
			milestones[i*2]=spans[i].getStart();
			milestones[i*2+1]=spans[i].getEnd();
		}
		return milestones;
	}

	public final long yychar()	{
    return yychar;
	}
	
	final Span  currentToken() {
    return currentToken(yytext());
	}
	
	final Span currentToken(String normalizedValue) {
		String value;
		long lengthDiff=0;

		if(normalize) {
			value = normalizedValue;
		} else {
			value = yytext();
			lengthDiff = value.length() - value.codePointCount(0, value.length());
		}
		if(startOffset > yychar || startOffset < 0) { // how can this happen?
			startOffset = 0;
		}
		long from = (yychar-startOffset),
			to =  (yychar-startOffset+yylength()-lengthDiff);
		if(xmlEcho) {
			System.out.println("<span id=\"t_"+tokenId+"\" from=\""+from+"\" to=\"" + to + "\"/>\n"+value);
		}
		startOffset += lengthDiff;
		tokenId++;
		if(bounds != null) {
			if(debug) {
				System.err.println(from+"-"+to+":"+ value);
			}
			bounds.append(from+" "+to+" ");
		}
		return new Span((int)from, (int)to, value);
	}
	
	final void fileEnd() {
		startOffset = yychar+yylength();
		tokenId=0;
		if(bounds != null && !xmlEcho) {
			System.out.println(bounds.toString());
			bounds.setLength(0);
		}
	}

	final Span xmlPassage() {
		if(xmlEcho) {
			String dings = yytext();
			if(dings.indexOf("<text")>=0 ) {
				startOffset = yychar+yylength();
				tokenId=0;
			}
			System.out.println(dings.replaceAll("[\n\r]+",""));
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
		System.out.println("<archive name=\"" + name + "\"/>");
	}

	final void zippedFile() {
		String name;
		String matched = yytext();
		int start = 13;
		name = matched.substring(start, matched.length() - 3);
		System.out.println("<file name=\"" + name + "\"/>");
	}

  public static void main(String argv[]) {
		int args=argv.length;
		int j=0;
		boolean xmlout = false;
		boolean normalize = false;

		for (int i = 0; i < argv.length && argv[i].indexOf("-") == 0; i++) {
			if(argv[i].equals("-ktt")) { // act as a tokenizer for KorAP TreeTagger
				xmlout=true; 
			} else if(argv[i].equals("-n")) { // do some normailization
				normalize=true; 
			}
			j++;
		}
		
		for (int i = j; i < argv.length || (i == j && argv.length == j); i++) {
			KorAPTokenizerImpl scanner = null;
			String fn = (argv.length > j ? argv[i] : "-");
			try {
		    BufferedReader br = "-".equals(fn) ? new BufferedReader(new InputStreamReader(System.in)) :
		        new BufferedReader(new java.io.FileReader(fn));
				scanner = new KorAPTokenizerImpl(br);
				scanner.bounds = new StringBuffer(1280000);
				scanner.xmlEcho=xmlout;
				scanner.normalize=normalize;
				while ( !scanner.zzAtEOF ) { scanner.getNextToken(); }
			}
			catch (java.io.FileNotFoundException e) {
				System.out.println("File not found : \""+fn+"\"");
			}
			catch (java.io.IOException e) {
				System.out.println("IO error scanning file \""+fn+"\"");
				System.out.println(e);
			}
			catch (Exception e) {
				System.out.println("Unexpected exception:");
				e.printStackTrace();
			}
		}
  }


%}

THAI       = [\u0E00-\u0E59]

// basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|{THAI}|[:digit:]|_)+

// case insensitivity is useful sometimes
// a = [aA]
// b = [bB]
c = [cC]
// d = [dD]
e = [eE]
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
// p = [pP]
// q = [qQ]
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
HTTPurlNoScheme =          {URIauthorityStrict} {HTTPpath}? {URIquery}? {URIfragment}?
HTTPurl = {HTTPurlFull} | {HTTPurlNoScheme}

FTPorFILEsegment = ({URIunreserved} | {URIpercentEncoded} | [?:@&=])*
FTPorFILEpath = "/" {FTPorFILEsegment} ("/" {FTPorFILEsegment})*
FTPtype = ";" [tT][yY][pP][eE] "=" [aAiIdD]
FTPscheme = [fF][tT][pP] "://"
FTPurl = {FTPscheme} {URIauthorityLoose} {FTPorFILEpath} {FTPtype}? {URIfragment}?

FILEscheme = [fF][iI][lL][eE] "://"
FILEurl = {FILEscheme} {URIhostLoose}? {FTPorFILEpath} {URIfragment}?

URL = {HTTPurl} | {FTPurl} | {FILEurl}

EMAILquotedString = [\"] ([\u0001-\u0008\u000B\u000C\u000E-\u0021\u0023-\u005B\u005D-\u007E] | [\\] [\u0000-\u007F])* [\"]
EMAILatomText = [A-Za-z0-9!#$%&\'*+-/=?\^_`{|}~]
EMAILlabel = {EMAILatomText}+ | {EMAILquotedString}
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

CLITIC = ({ENGLISH_CLITIC}|{FRENCH_CLITIC})

INIT_CLITIC = ({FRENCH_INIT_CLITIC})

POLISH_CONDITIONAL_CLITIC = (by)

POLISH_CONDITIONAL_ENDING = (m|ś|śmy|ście)?

POLISH_PAST_ENDING_1 = (ś|śmy|ście)
POLISH_PAST_ENDING_2 = ([mś]?|śmy|ście)

WHITESPACE = \s

ENDMARKER = (\n?\003\n)
XML = <(\/text|\?xml|\?xml-model|\/?raw_text|\/?metadata) ?[^\003\n>]{0,100}>

EMOTICON = ( [<>]?[BX;8:=][o\-\']?[DdPp()\/3>oO*]+|<\/?3+|ಠ_ಠ|\(-.-\)|\(T_T\)|\(♥_♥\)|\)\':|\)-:|\(-:|\)=|\)o:|\)x|:\'C|:\/|:<|:C|:[|=\(|=\)|=D|=P|>:|D\':|D:|\:|]:|x\(|\^\^|o.O|oO|\\{o}\/|\\m\/|:;\)\)|_\)\)|\*_\*|._.|:wink:|>_<|\*<:-\)|[:;]\)|[;;]" "\))

OMISSIONWORD = ({LETTER}+\*\*+{LETTER}*|{LETTER}+\*{LETTER}+|{LETTER}+[\'`]{LETTER}+)

EXTENSION = (html|htm|doc|docx|pdf|jpg|mp3|mp4|ogg|png|avi|txt|xls|xml|aac|DOC|DOCX|GIF|JPG|JPEG)
FNAME = (({LETTER}:[\\/])?|\/)?({LETTER}+|[\\_/-])+\.{EXTENSION}

PLUSAMPERSAND = (&amp;|&apos;|&gt;|&K|&lt;|&M|&quot;|&RQ|\+Ale|\+ALe|\+Anima|\+APD|\+co|\+Co|\+GF\+|\+Leif|\+Strang|\+Teamgeist|A&A|A&E|A&F|A&M|A&O|A&P|A&R|A&V|A&W|A\+\+|A\+\+\+|A\+E|A\+f|AAC\+|ABC&D|AC\+|AD&D|AE&E|AES\+F|AEW&C|AFM\+E|AGTL\+|Altenpflege\+ProPflege|Analyse\+kritik|anlagen\+verfahren|ANT\+|Anynet\+|Applus\+|Arch\+|ARCH\+|ART\+COM|AS&P|ASC\+T|ASEAN\+|Asis&t|AT&L|AT&S|AT&SF|AT&T|ATV\+|Auer\+Weber|Auer\+Weber\+Assoziierte|Axis&Allies|B&B|B&C|B&F|B&G|B&H|B&I|B&K|B&M|B&MTJR|B&NES|B&O|B&Q|B&R|B&T|B&V|B&W|B\+B|B\+R|B\+T|Baby&Co|Bayern\+|BB&T|BD\+|Beast\+|BEAST\+|Beck\+Schubert|Belle&Sebastian|BFE\+|BG\+BRG|BIBEL\+ORIENT|Bild\+Funk|Binder\+Co|Blohm\+Voss|Blood\+|Blut\+Eisen|BM&F|BM&FBovespa|Bolles\+Wilson|Bottega\+Ehrhardt|Brangs\+Heinrich|BRF\+|Briner\+Kern|BUCH&media|Burghardt\+Schmidt|bus\+bahn|C&A|C&C|C&D|C&L|C&M|C&O|C&P|C&R|C&S|C&T|C&W|C\+\+|C\+\+Builder|C\+c|C\+C|C\+M\+B|Ca\+\+|Cafe\+co|Cafe\+Co|Canal\+|Cantata\+\+|CB&I|CC&G|CCC&StL|CD&E|CD&V|CD\+DVD|CD\+G|CDIA\+|Celtic\+|Cendres\+M|Chage&Aska|Chage&Asuka|Channel\+smile|Charm\+\+|Chip&Chap|CI&CEQ|CI\+|Click&Buy|Cocl&Seff|Com&Com|COM\+|Comicplus\+|COR&FJA|CS&S|CT&T|ctc\+\+|Ctrl\+Alt\+Del|CTRL\+ALT\+DEL|Cube\+|Cyfra\+|CYFRA\+|D&A|D&AD|D&b|D&B|D&D|D&G|D&O|D&RGW|D&S|D&W|D\+Q|DAB\+|DACH\+HOLZ|DAML\+OIL|DBM&T|Dc\+\+|DC\+\+|DDDBM&T|Despe&Siga|DF&S|Digital\+|DirectConnect\+\+|Dissing\+Weitling|DL\+NT|DLSW\+|Do&Co|Dok&Deb|Dorma\+kaba|DP&L|Drm\+|DRM\+|DTS\+\+|DU&ICH|DVD\+R|Dvd\+rw|DVD\+RW|E&a|E&N|E&Y|E\+|E\+e|E\+h|E\+H|EAAC\+|Ebert\+Jacobi|ECO\+|EG&G|Eigen\+Art|Eins\+Alles|Electromobility\+|En\+|Endress\+Hauser|Erasmus\+|ES&T|ETV\+|EV\+|Eve&rave|Every\+|F&A|F&B|F&E|F&F|F&K|F\+F|F\+U|Familie&Co|FAT\+|Film\+|FILM\+SCHULE|Fischer\+Kr|Fix\+Foxi|FLUXUS\+|FMHL\+|Form\+zweck|fuhrpark\+management|G&B|G&D|G&IF|G&L|G&V|G\+\+|G\+H|G\+J|G\+tt|GC&CS|GDI\+|ge\+her|GG&L|Go\+|GO\+|Google\+|Goran\+Vujic|GRAF\+ZYX|Gruner\+Jahr|Gtk\+|GTK\+|GTL\+|GTX\+|Guide\+|H&BC|H&H|H&K|H&M|h&m|H&N|H&R|H&S|H\+BEDV|H\+H|H\+N|H\+S|Haase&band|Hahn\+Kolb|HAHN\+KOLB|Hasta\+Coda|Haubitz&Zoche|Haubitz\+Zoche|HBCI\+|HD\+|Health&Care|Heim\+Handwerk|Heute\+|HFS\+|hne\+Nagel|HSPA\+|HT&L|HTML\+TIME|Huber\+Suhner|Hunger&Seide|I&A|I&K|I&Q|I&u|I&U|I\+D|I\+R|Ich\+Ich|ID&T|Idee\+spiel|Ihp\+|II\+|IIc\+|III\+|IK\+|In&phone|In&Phone|info\+|Interkama\+|IT&Production|J&B|J&D|J&J|J&M|J&P|J&S|J&T|J\+\+|J\+S|Jazz\+Az|Jenna\+Ron|Johnson&Johnson|JU\+TE|Jugend\+Sport|Jugend\+Technik|Jump&Run|K&k|K&K|K&L|K&M|K&N|K&R|K&S|K&U|K\+\+|K\+A|K\+H|K\+K|k\+Metal|K\+R|K\+S|K\+W|Kai\+Sven|Kaiser\+Kraft|KAISER\+KRAFT|Kino&Co|KINO&CO|Kino\+|Kirche\+Leben|Klassik&JazzMagazin|Kurz&F|L&B|L&C|L&M|L&N|L&P|L&S|L\+R|L\+T|Lancia\+Voyager|Landis\+Gyr|LB&SCR|Leader\+|LEADER\+|Lederer\+Ragnarsd|Leicht&Cross|Lenord\+Bauer|Leslie\+Lohman|Libsigc\+\+|Life&Style|LIFE\+|Light\+Building|Lippmann\+Rau|LISA\+|Lords&Knights|LT&SR|Lussi\+Halter|M&A|M&B|M&D|M&G|M&i|M&I|M&M|M&Ms|M&N|M&S|M&T|M\+a|M\+C|M\+M|M\+O|M\+s|M\+S|M\+W|Maildir\+\+|Mann\+Hummel|Markt\+Technik|Means\+\+|Melodie&Rhythmus|Metadata\+|Miles&more|Milk\+|Mining\+geo|Mix&Genest|mmerly\+Frey|Monet\+|Motion\+picture|MPP\+|MS&D|MS&L|MStP&SSM|Music&Voice|N&CRR|N&ER|n&gut|N&R|N&W|N\+M|Na\+|NADHH\+|Nah&gut|Natur\+kosmos|natur\+mensch|Nc\+|NI&Co|nig\+Neurath|Nike\+iPod|Nintendogs\+Cats|Notepad\+\+|NYW&B|O&K|O&L|O&M|Ola\+|OMNeT\+\+|ORFsport\+|Ost\+Front|P&A|P&C|P&E|P&G|P&I|P&ID|P&L|P&M|P&O|P&P|P&R|P&T|P&TLuxembourg|P&W|P\+M|P\+R|P\+S|PAL\+|Pan&Scan|Papier&Stift|Park&Charge|Park&Rail|Park&Ride|Park&Suites|PB&J|Peek&Cloppenburg|Pen&Paper|Pepperl&Fuchs|Pepperl\+Fuchs|Peste&Sida|PG&E|Pirelli&C|Pittel\+Brausewetter|Plug&play|Plus\+|POB&A|Pol&is|POL&IS|POLO\+|Poses\+\+|PP&P|Pratt&Whitney|Princess\+|Prius\+|Procter&Gamble|Prozac\+|PS&P|Pur\+|Q&A|Q&Q|Q\+Q|Quanta\+|R&A|R&B|R&D|R&ER|R&F|R&G|R&I|R&M|R&Q|R&R|R&S|R\+C|R\+S|R\+V|Rail&Fly|REDD\+|Reise&Touristik|Relax\+ng|RF&P|Richter\+Frenzel|Rio\+|Rohde&Schwarz|RT\+|Run&Dine|S&B|S&D|S&G|S&H|S&K|S&M|S&P|S&T|S&w|S&W|S\+D|S\+G|S\+T|S\+U|Sales&Services|Sam&Max|Schedule\+|Schiff&Hafen|Schlund\+Partner|Schmelzle\+Partner|Schmidt\+Clemens|science\+business|Science\+Business|sd&m|Sd&m|Sdr\+|Serve&Volley|Severin\+K|SiMPLE\+\+|SMS&park|SMW\+|Soap&Skin|Solo\+|Spar\+Kreditbank|Spar\+Leihkasse|speed\+|Speed\+|Spoga\+gafa|SPORT\+|Sport\+Technik|SS\+|St&H|St&Z|Standard&Poor|Standard&Poors|Station&Service|Steib\+Steib|Stil&Stadt|Strategy&|Strg\+Alt\+Entf|StrongDC\+\+|Such&Find|Sumol\+Compal|SVS&E|SVWZ\+|SW&S|Swift\+|SXGA\+|T&D|T&L|T&N|T&T|T\+A|T\+T|TACACS\+|Tanz&FolkFest|Taylor&Francis|text\+kritik|TEXT\+KRITIK|textil\+mode|Timidity\+\+|TMRM\+|Toni&Guy|toon\+|Touch&Travel|Track\+|Trends\+More|TT&C|TT&R|ttir\+Oei|TV\+Synchron|U&D|U\+\+|U\+F|Ultimate\+\+|Urban&Fischer|URW\+\+|USC&GS|UTC\+|V&A|V&R|V&S|V&W|Valentien\+Valentien|VC\+\+|VF\+|Vieweg\+Teubner|VISEO\+|Vision\+Technik|VisualDSP\+\+|VIVA\+|VL&D|Vorschau\+R|Vorster&Gr|VT&MA|W&B|W&F|W&G|W&H|W&p|W&V|W&W|WB\+|Wein\+Markt|Wienstroth&Hammans|Winkler\+D|Wirtschaft\+Markt|WP&YR|WS&P|WSXGA\+|WXGA\+|X\+\+|X\+Y|Xbase\+\+|XHTML\+SMIL|Y&R|Y&T|Yin&Yang|Yotsuba&|Young&Queer|Z\+W|Zeidler&Wimmel|Zinc&Germanium)

TWITTER_HANDLE = @{ALPHA}{ALPHANUM}?
TWITTER_HASHTAG = #{ALPHANUM}

// blocks of question marks and exclamation marks are one token
LONG_END_PUNCT = [?!][?!1]+

WORD = ({IRISH_O}?{ALPHANUM}+|[Qq]ur{Q}an)

// pragmas used for anonymization etc.
PRAGMA = \[_[A-Z\-]+_\]

// Use the same abbreviations as the IDS sentence splitter /takes 30min to compile)
// cat /usr/local/res/SatzEnde.abb8bit | recode latin1..utf8 | perl -wne \'chomp; $a .= "$1|" if(/^\+([^.]{1,}$)/); BEGIN {$a="";}; END {chop $a; print "SEABBREV=($a)\n"}\'  > abbr
SEABBR = (A|AAnw|AAnz|ABC-Dir|ABest|ABez|ABgm|ABl|ABlAllKdtr|ABlEurGem|ABlSch|ADAnw|ADOSt|ADSp|ADir|AGDir|AGPräs|AGr|AH-Rdschr|AHKABl|ARSt|ASchr|ASekr|ASp|ASt|AT-Ang|AVNot|AVVFStr|AVers|AVfg|AVorm|Aa|Ab|AbGes|Abb|Abbl|Abbr|Abd|Abdr|Abf|Abfl|Abfr|Abg|Abgn|Abh|AbhSch|Abhd|Abhn|Abit|Abk|Abl|Abn|Abr|Abrd|Abs|Abschl|Abschn|Abschr|Absp|Abspr|Abst|Abstr|Abt|Abtlg|Abtlgn|Abtn|Abtr|Abw|Abz|Abzw|Ac|Acc|Add|Adh|Adj|Adm|Admst|Adr|Adv|Aero|Aeron|Afgh|Afr|Ag|Aggr|Agit|Agm|Agr|Agt|Agtr|Ah|Ahg|Ahp|Amerik|Amtm|Ang|Anh|Anm|Anst|Anw|Ap|Apg|Apl|Apok|Apokr|Apoth|App|Appl|Appos|Appr|Approb|Approx|Apr|Aq|Aqll|Aqr|Ar|Arab|Arb|ArbEins|ArbGEntsch|ArbVerm|Arbf|Arbg|ArblFürs|ArblVers|Arbn|Arbtg|Arch|ArchAss|ArchDir|Archvl|Arg|Arith|Ariz|Ark|Arp|Arpegg|Arr|Arret|Ars|Art|Asb|Aschr|Asp|Asph|Ass|AssPr|Assist|Assyr|Ast|Asth|Astr|Astrol|Astron|Astrophot|Asz|At|Atl|Atm|Att|Attr|Aubew|Aud|Audiogr|Audiom|Auf|Aufb|AufbewBest|Auff|Aufg|Aufh|AufhBek|AufhBest|AufhVorschr|AufhVschr|Aufk|Aufkl|Aufl|Aufn|Aufr|Aufs|Aufsch|Aufschl|Aufschr|Aufst|Auft|Auftlg|Auftr|Aufw|Aufz|Aug|Ausb|Ausbild|Ausf|AusfAnw|AusfBek|AusfBest|AusfErl|AusfFörd|Ausg|Ausgest|Ausgl|AusglSt|Aush|Ausk|Ausl|Ausn|Ausr|Auss|Aussch|Ausschl|Ausspr|Ausst|Ausstatt|Ausstattg|Aust|Austr|Austral|Ausv|Ausverk|Ausw|AuswBeh|Ausz|Aut|Autogr|Autom|Auton|Außenst|Ave|Aw|Az|B|BAArbVerm|BAAss|BABl|BADir|BAInsp|BAOInsp|BAOSekr|BASt|BAaz|BAnw|BArch|BAss|BAssess|BAssist|BAvAv|BBBI|BBauBI|BBed|BBeir|BBev|BDIr|BDiAnw|BDr|BELFMBl|BFMBl|BFStr|BGBI|BGHSt|BGOArch|BGSt|BGr|BHGr|BI|BII|BIIckp|BIerSt|BInsp|BJPl|BKBI|BMAt|BMAusw|BMFa|BMGes|BMSchatz|BMVg|BMVt|BMVtdg|BMWBl|BMWi|BMWo|BOAmtm|BOKraft|BOSekr|BOStrab|BPMin|BPatGer|BPol|BPr|BPrSt|BR-Beschl|BR-Best|BRDrucks|BReg|BSch|BSt|BStAnz|BStBI|BTDrucks|BTr|BVBI|BVST|BVSt|BVers|BVersBl|BVorst|BWGBI|BWGoD-Ausl|BWKGAusl|BWMBl|BZBl|BZKom|Ba|BaI|BaII|BaIt|Bacc|Bach|Bahngel|Bahnw|Bakk|Bakt|Bal|Bald|Band|BankBev|Banz|Bapt|Bar|Barb|Barbest|Bark|Barmh|Barpr|Barv|Barverk|Barz|Barzahl|Bas|Bast|Bat|BatI|BauAss|BauAssess|BauDir|BauI|BauIng|Bauaufs|Bauausf|Baubeschr|Baudir|Bauf|Baufiazg|Baufzg|Baugel|Baugen|Bauges|Baugest|Bauk|Baultg|BaupI|Baupfl|Baupol|Baupr|Bautechn|Bautr|Bauverw|Bauw|Bav|Bay|BayBSVFIn|BayBSVJu|BayObLGSt|BayStAnz|BayVerf|Baz|Bb|Bbd|Bbf|Bbl|Bch|Bchst|Bd|Bd--R|BdGes|BdSt|Bdch|Bdchn|Bde|Bdg|Bdr|Bdtg|Bdu|Bdw|Bea|Beamt|Bearb|Bed|Bef|BefSt|Befh|Befr|Beg|BegI|Begl|Begr|Beh|Beibl|Beig|Beigel|Beih|BeihGr|Beil|Bein|Beir|Beis|Beisp|Beist|Beitr|Beiw|Bek|Bekl|Bel|Belg|Bem|Ben|Ber|Berat|Bergass|Bergb|Bergdir|Berging|Bergm|Bergw|Berl|Berufsber|Berufsgen|Bes|BesGr|Besch|BeschSt|Beschl|Beschr|Beschw|Bespr|Best|Bestr|Bet|Betr|Beub|Beurl|Bev|BevoIIm|Bew|Bez|BezABl|BezFürsVerb|Bf|BfArb|Bfg|Bfh|Bfst|Bg|Bgb|Bge|Bgl|Bgld|Bgm|Bgmstr|Bgr|Bgrz|Bgw|Bh|Bhdl|Bhdlg|Bhf|Bhm|Bi|BiI|BiSt|Bib|Bibl|BiblI|Bild|Bill|BillRichtl|Bio|Biogr|Biol|Bisch|Bist|Bit|Bk|BkI|Bkg|Bkm|Bkt|Bkz|Bl|Bld|Bldg|Blickw|Blk|Bln|Blst|Blvd|Blz|Bm|Bmstr|Bn|Bol|Bor|Bot|Botsch|Bp|Br|BrABI|BrMilReg|Brandm|Brandmstr|Brandsch|Brandvers|Branntw|Bras|Brat|Brauiug|Braum|Braumstr|Braur|Braust|Brennst|Bret|Brev|Briefm|Brieft|Briefw|Brill|Brit|Brk|Brm|Brosch|Brt|Bs|Bschr|Bsg|Bsp|Bspr|Bst|Bstg|Bstlg|Bt|Btl|Btm|Btr|BtrOAufs|Btrg|Bttr|Bu|BuIg|BuRi|Buchdr|Buchf|Buchh|Buchhdl|Buchhdlg|Buchst|Bull|Bur|Bvh|Bvt|Bw|Bwg|By|Byz|Bz|BzBm|BzSekr|Bzl|Bzn|Bü|Bürg|Bürgm|Bürgmstr|Bürgsch|C|Ca|CaIIf|CaIv|Cabr|Can|Cant|Cap|Capt|Car|Carm|Cath|Cb|Cd|Cdr|CeIl|Cel|Celloph|Cels|Cemb|Cent|Cert|Ceyl|Ch|ChBPr|ChIr|Cham|Champ|Char|Chefdir|Chefing|Chefkorr|Chefp|Chefpil|Chefpl|Chefred|Chefsekr|Chem|Chemot|Chemoth|Chin|Chir|Chol|Chor|Chq|Chr|Christ|Chron|Chronogr|Cic|Cie|Cist|Cl|Cnt|Cntr|Co|Col|Coll|Colleg|Colo|Com|Comm|Comp|Conc|Conf|Confr|Cons|Cont|Conv|Cop|Corp|Corr|Coul|Coup|Cour|Court|Cous|Cout|Cpt|Cr|Cruz|Ct|Ctr|Cud|D|DAnw|DBest|DCG|DDevErkl|DDr|DOGer|DRAnz|DRPa|DRpfl|DRspr|DStBl|DStZtg|DVBl|DVerwBl|DVfg|DVorschr|DWo|DWohn|Dachg|Dachorg|Dachverb|Dak|Dalm|Dankb|Darl|Darst|Dat|Db|Dbd|Dbl|Dchs|Dct|Dd|Def|Deg|Dek|Dekl|Dekr|Dekur|Del|Deleg|Delinq|Dem|Denkm|Denkschr|Dep|Depos|Depr|Dept|Deput|Der|Derm|Desinf|Desint|Deskr|Desp|Dess|Dest|Desz|Det|Deut|Dev|DevKErkl|Devot|Dez|Df|DfBest|Dg|Dgt|Di|Diab|Diag|Diagn|Diagr|Diak|Dial|Diam|Did|Dienstanw|Dienstber|Dienstpfl|Dienststd|Diff|Diffam|Dil|Dim|Dipl|Dir|Disc|Dish|Disk|Diskr|Disp|Dist|DiszStr|DiszVerf|Div|Dkfm|Dkm|Dl|Dm|Dmr|Do|Dok|Dokt|Dolm|Dom|DoppBestAbk|Doppelz|Dos|Doz|Dp|Dpf|Dpfm|Dpp|Dpt|Dptr|Dr|Drchf|Drchfl|Drchg|Drcks|Dres|Drgt|Drog|Drp|Drs|Drucks|Drucksp|Dst|DtVerwBl|Dtl|Dtld|Dtz|Dtzd|DuVerf|Dubl|Dupl|Durchf|DurchfBest|Durchl|Durchm|Durchschl|Durchschn|Durchst|Durchw|Dvst|Dw|Dyn|Dz|Dzd|Dzt|Dzw|Dän|E|E-Nr|E-Rdschr|E-Schw|E-Str|E-Techn|EB-St|EBek|EGr|EHMietv|EPl|EPr|ESt|Eb|Ebf|Ebst|Ebt|Ec|Eccl|Econ|Ecuad|Ecuador|Ed|Ef|Eff|Egb|Egbd|Egbde|Egbdf|Ehel|Ehf|Ehm|Ehrl|Ehrw|Ehz|Ehzg|Ehzgin|Ehzgn|Ehzgt|Ehzgtm|Eiazelpr|Eichm|Eichmstr|Eidg|Eidgen|Eif|Eigenkap|Eigent|Eigenw|Eign|Eigt|Eilb|Eilg|Eilzust|Einb|Einbauschr|Einbd|Einbr|Eindr|Einf|Einfl|Eing|Eingem|Eingl|Eingr|Einh|Eink|Einl|Einn|Einr|Eins|Einschl|Einschr|Einspr|Einst|Eint|Eintr|Einv|Einvern|Einverst|Einw|Einz|Einzelh|Einzelz|Einzlg|Eisenb|Eisenbr|Eisentr|Eish|Ek|Ekuad|Ekuador|El|Elektr|Elektrotechn|Elev|Ell|Els|Em|Emb|Emgld|Emgldsch|Emp|Empf|En|Endst|Eng|Engl|Ent|Entd|Enteig|Enteign|Enth|Entl|Entn|Ents|Entsch|Entschl|Entspr|Entst|Entw|Entz|Enz|Enzykl|Ep|Epid|Epig|Epigr|Epil|Epis|Episk|Eq|Er|Erb|Erbf|Erbl|Erbsch|ErbschSt|Erbschl|Erbt|Erdg|Erf|Erfr|Erg|ErgBek|ErgBest|ErgVschr|Ergeb|Erh|Erk|Erkl|Erkr|Erl|Erm|Ern|Err|Ers|Ersch|Erschl|Erschw|Erst|Erstatt|Erstauff|Erstausg|Erstaust|Erstkl|Erstp|Erstr|Ert|Ertr|Erw|Erz|Erzb|Erzh|Erzhzg|Esk|Esot|Esp|Espr|Estl|Esttg|Et|Etg|Ethn|Etr|Etym|Euph|Eur|EurArch|Eutf|Ev|Evang|Ew|Ewr|Ex|Exek|Exerz|Exh|Exk|Exkl|Exp|Exped|Exper|Expl|Expr|Ext|Extr|Exz|Ez|Ezg|Eßl|F|FAVorst|FArb|FAss|FGDir|FIex|FIgze|FIp|FIugb|FIugh|FIugsIch|FLSt|FLdw|FMBl|FMin|FNr|FPräs|FPräsSt|FRef|FSt|Fa|Fabr|Fachb|Fachber|Fachbibl|Fachl|Fachlit|Fachsch|Fag|Fahrber|Fahrerl|Fahrw|Fahrz|Fak|Faks|Fallsch|Fam|Fan|Fanf|Fas|Fasch|Fasz|Fav|Fb|Fbd|Fbf|Fbl|Fbr|Fbz|Fd|Fdg|Fdw|Fe|FeIdp|FeIngeh|Febr|Feing|Feinmech|Feldm|Feldw|Fem|Ferm|Fernl|Fernltg|Fernr|Fernschr|Festg|Festpr|Festst|FeststKl|Feuerbest|FeuerschSt|Feuerw|Feuevers|Ffl|Ffr|Fg|Fgbz|Fgd|Fgn|Fgr|Fgst|Fh|Fhr|Fhrw|Fhrz|Fhrzge|Fi|Fig|Fil|Fin|FinAss|FinGPräs|FinMin|FinVertr|Firm|Fj|Fk|Fkta|Fl|Flachdr|Flb|Flbl|Flg|Flgz|Flk|Fllg|Flugb|Flugbew|Flugz|Flw|Flzg|Flzge|Fm|Fn|FoArch|FoMstr|Fod|Fol|Ford|Form|Formal|Forsch|Forstw|Forstwiss|Fortf|Forts|Fot|Fotogr|Fp|Fpl|Fr|FrMilReg|Fragm|Frakt|Franz|Frbr|Frdh|Frdlkt|Frdw|Frdz|FrdzI|FreIgr|FreIw|Freg|Freih|Freim|Freist|Freiw|Freiz|Freizt|Fremdst|Fremdw|Freq|Frfr|Frgt|Frh|Frhf|Frhr|Fris|Frk|Frl|Frm|Frq|Frspr|Frst|Frw|Frwk|Frz|Frzb|Frzt|Frzzbd|Fs|Fsch|Fsch-Jg|Fschr|Fspr|Fss|Fst|Fstg|Fstm|Ft|FtSt|Ftg|FuAnl|FuAss|FuGer|FuSpr|FuSt|FuVerb|Fua|Fugr|FundSAnw|Funkm|Funkt|Fur|Furd|Furn|Furtschr|Fut|Fußn|Fw|Fwb|Fwk|Fz|Fzb|Fzbd|Fzgn|Fzw|Fü|Fürs|G|GABl|GAss|GBVfg|GBeh|GBl|GESt|GG|GHSp|GI|GISchr|GInsp|GKl|GMBl|GSekr|GSt|GSv|GVBl|GVOBl|GVerg|Ga|Gal|Gall|Galv|Gam|Gar|Gard|Garm|Garn|Gas|Gasm|Gastr|Gasw|Gatt|Gav|Gaz|Gb|Gbd|Gbg|Gbl|Gbm|Gbo|Gbrm|Gbst|Gbt|Gd|Gdbgr|Gdbgre|Gde|Gdm|Geb|GebOStrVerk|GebOZeug|Gebl|Gebr|Gebrm|Ged|Gef|Gefdg|Gefg|Gefgach|Gefl|Gefr|Geg|Geh|Gel|Gelbf|Gem|GemSt|GemVerb|Gen|GenAktVfg|Gend|Geod|Geogr|Geol|Geom|Gep|Ger|GerAss|GerRef|GerSchr|Germ|Ges|Gesch|GeschOBReg|GeschVPl|Geschl|Geschw|Gest|Getr|Gew|GewGer|GewOSekr|GewSt|GewStAusglGea|Gez|Gf|Gfge|Gfgs|Gfgsch|Gfl|Gfsch|Gft|Gg|Ggs|Ggw|Ggzg|Gh|Ghg|Ghgt|Ghl|Ghrz|Ghrzgt|Ghz|Ghzg|Ghzgt|Gibr|Gk|Gkde|Gl|Gld|Glde|Gldr|Gled|Glfl|Glgew|Gln|Glstr|Gmde|Gmk|Gn|Go|Gouv|Gr|GrESt|GrErwSt|GrSSt|GrSt|Graa|Grad|Grat|Grav|Grchl|Grd|GrdESt|GrdErwSt|GrdSt|Grdb|Grdf|Grdfl|Grdfr|Grdg|Grdl|Grdlstg|Grdr|Grds|Grdst|GrdstVerkBek|Grdw|Grdwsp|Grdz|Grdzg|Greg|Grfsch|Grhdl|Grhdlg|Grhzg|Grhzgt|Griech|Grim|Grkfm|Grld|Grst|Grundst|Gruudw|Grv|Grz|Gs|Gsch|Gschf|Gsdtr|Gsdtsch|Gsg|Gsge|Gst|Gstb|Gstr|Gstw|Gt|Gtb|Gttg|Gu|Guat|Guay|Guer|Guth|Gvb|Gvz|Gw|GwOSekr|GwSt|Gwb|Gymn|Gyn|Gynäk|Gzl|Gzld|Gzln|Gzn|Gzpgt|Gär|Gült|H|HASt|HAnst|HArb|HAusg|HBAbt|HBBl|HBl|HDv|HEinn|HF-Verschw|HFSt|HFürsSt|HGBl|HGSt|HGr|HKl|HPflVers|HPl|HReg|HRegVfg|HSt|HVBl|HVStd|HVers|HVertr|HVerw|Ha|HaIbf|HaagEhescheidAbk|HaagEheschlAbk|HaagIPRAbk|HaagNeutrAbk|HaagZPAbk|Hab|Habil|Habsb|Haf|Haftentsch|Haftg|Haftpfl|Hagiogr|Halbj|Halbs|Halt|Ham|Handb|Handbed|Handl|Handschr|Handw|Handwb|Harm|Harp|Haub|HausBlVf|Hausbes|Hausgeh|Haush|Hausm|Hausr|Hausverw|Hausw|Hausz|HauszSt|Hav|Hb|Hbd|Hbf|Hbg|Hbhf|Hbj|Hbl|Hbschr|Hd|Hda|Hdb|Hdbr|Hdgr|Hdhbg|Hdl|HdlAbk|Hdlbg|Hdlg|Hdn|Hdschr|Hdt|Hdtsch|Hdtschft|Hdw|Hdwb|Hdz|HeiIst|Heilpr|Heilw|Heimatl|Heimk|Heiml|Heimw|HelIen|Helg|Heliugr|Helv|Herg|Herk|Herm|Herst|Heur|Hex|Hexam|Hf|Hfn|Hfr|Hfrz|Hfrzbd|Hft|Hftg|Hfw|Hfz|Hg|Hgb|Hgbf|Hger|Hgl|Hgr|Hi|Hiaterl|Hind|Hins|Hinterg|Hintergr|Hinterh|Hinw|Hist|Hj|Hjschr|Hl|Hld|Hldbd|Hldr|Hlg|Hlk|Hll|Hln|Hlnbd|Hlwd|Hm|Ho|Hochf|Hochsch|Hochstbel|Hochstgeschw|Hochstgew|Hochstpr|Hochstst|Hochw|Hofl|Hoh|Holl|Hom|Hon|Honor|Honorat|Hor|Hosp|Hot|Hott|Hp|Hperg|Hpergbd|Hpfl|Hpgt|Hpt|Hptb|Hptbd|Hptl|Hptm|Hptq|Hpts|Hptsch|Hptst|Hptverw|Hptw|Hptwm|Hr|Hrg|Hrn|Hrsg|Hrst|Hrzg|Hrzgt|Hs|Hschr|Hse|Hsh|Hsp|Hss|Hst|Htl|Htp|Htwl|Hubbr|Hubr|Hubschr|Hum|Hund|Hw|Hwb|Hwg|Hwm|Hy|Hyaz|Hydr|Hydrogr|Hydrol|Hygr|Hygrom|Hyp|HypD|Hypn|Hypot|Hypoth|Hz|Hzbl|Hzg|Hzgt|Hzgtm|Hzt|Hztm|Hzw|I|IIIustr|IIb|IIbit|IIt|IKrk|IRTU|Ia|Iad|Iadep|Iam|Iandsch|Iandw|Iangfr|Ibd|Ibdg|Id|Ident|Idschl|IeIbI|Iect|Iegg|Ill|Illum|Im|ImIt|Imm|Immatr|Immob|Imp|Imper|Imperf|Impf|Impr|InI|InIt|Ind|Indet|Indiff|Indik|Indiskr|Indiskret|Indisp|Indiv|Indog|Indon|Inf|Infl|Inform|Ing|Ingl|Ingr|Inh|Init|Inj|Ink|Inkomp|Inkons|Inkonv|Inkorp|Inkrafttr|Inq|Ins|InsI|Inschr|Insp|Instr|Int|Integr|Inter|Interj|Interl|Interp|Interpr|Interv|Inton|Intr|Introd|Introsp|Inv|Invent|Io|Ioc|Irh|Irl|Irrl|Is|Isl|Isol|Isr|It|Iudon|Iukons|Iukorp|Iur|Iü|J|JAmtm|JBer|JBl|JD|JInsp|JMBl|JOIasp|JOSekr|JPfl|JVerw|JVw|Jagdh|Jagdw|Jahrb|Jahresber|Jahresz|Jahrg|Jahrt|Jam|Jan|Jap|Jb|Jbb|Jber|Jberr|Jg|Jgd|Jgdl|Jgg|Jh|Jhb|Jhdt|Jhg|Jhgg|Jhh|Jhrb|Jhrg|Jhtsd|Jhtsde|Journ|Jr|Jt|Jtg|Jtsd|Jub|Jug|Jugosl|Jul|Jun|Jur|Just|Juw|Jähgh|K|K-Pr|KABl|KAnw|KD|KGI|KGPräs|KGer|KHl|KHld|KHldr|KId|KIl|KIw|KKhs|KPr|KRBef|KRDir|KRProkl|KSt|KaPflSt|Kab|Kad|Kaj|Kal|Kalf|Kam|Kamb|Kan|Kand|Kans|Kap|KapSt|KapVG|Kapl|Kapt|Kar|Kard|Kart|Kas|Kass|Kast|Kat|Kath|Kaufm|Kaus|Kaut|Kav|Kb|Kblg|Kbrm|Kde|Kdg|Kdgg|Kdo|Kdos|Kdr|Kdre|Kds|Ke|Kea|Kennz|Ker|Kf|KfSt|Kffr|Kfm|Kfz-Anh|Kfz-Gew|Kfz-Hdl|Kfz-Mech|Kfz-Vers|Kfz-Zut|Kg|Kgf|Kgl|Kgn|Kgr|Kh|Khdw|Khf|KiSt|Kiesb|Kinderg|KirchGem|KirchSt|Kis|Kj|Kjm|Kjmstr|Kl|Klar|Klarh|Klass|Klassif|Klav|Klin|Klkw|Klär|Kmdo|Kmdr|Kmdt|Kmdtr|Kn|Ko|Koh|Kok|Kol|Koll|Kolp|Komb|Komf|Komm|Komp|Kompl|Kond|Konf|Konfl|Konfr|Kongl|Kongr|Konj|Konjug|Konjunkt|Konkl|Konkr|Kons|Konsekr|Konseq|Konserv|Konsist|Konsp|Konst|Konstr|Kont|Kontr|Kontrap|Konz|Koord|Kop|Kopr|Koprod|Korp|Korref|Korrep|Kos|Kost|Kot|Kotft|Koup|Koupt|Kp|Kpf|Kpie|Kpl|Kplm|Kpm|Kpmstr|Kpn|Kpsch|Kpt|Kr|Kradf|Kreish|Krem|Krf|Krh|Krhs|Krht|Krim|Krist|Krkhs|Krkschw|Krkw|Ks|Kschr|Ksgr|Kt|Ktg|Ktn|Kto|Ktr|Kts|KuVV|Kub|Kueff|Kult|Kum|Kumpr|Kunk|Kunstgesch|Kunsthist|Kunstm|Kunstst|Kunstw|Kunv|Kur|Kurf|Kurft|Kurh|Kurp|Kurpf|Kurresp|Kursw|Kursz|Kurt|Kurzarb|Kurzber|Kurzgesch|Kurzschl|Kurzschr|Kurzw|Kw|Kwf|Ky|Kyffh|Kz|Kze|Kzf|Kzl|Kä|Käm|Kü|Künd|Kürl|Kürz|L|LAbg|LBI|LBez|LD|LDv|LGBez|LGBl|LGDir|LGPräs|LGer|LGr|LHFürsSt|LIg|LIt|LItt|LKZSt|LL|LNr|LPol|LReg|LSt|LT-Präs|LTGO|LVf|LVfg|La|Lab|Lad|Ladest|Laf|Lag|Landk|Landkr|Landr|Landsch|Landstr|Landw|Langob|Laut|Laz|Lb|Ld|LdGerDir|LdR|Ldbd|Ldg|Ldgr|Ldk|Ldkr|Ldpr|Ldr|Ldrb|Ldrr|Ldsch|Ldstr|Ldtg|Ldw|Leb|Led|Leg|Legg|Lehrg|Lehrj|Lehrl|Leibr|Leit|Leitart|Leitd|Leitf|Leitl|Leits|Lekt|Les|Leserbr|Lev|Lexikogr|Lf|Lfb|Lfzt|Lg|Lgg|Lgn|Lhr|Lhs|Lhwg|Lhz|Li|Liag|Lib|Libr|Lic|Lief|Liefersch|Lig|Ligg|Lim|Lin|Liq|Lit|Lith|Litt|Liz|Lj|Lkr|Lkt|Lm|Lok|LondSchAbk|Los|Loschgeb|Lsg|Lsgg|Lsp|Lstg|Lstr|Lt|LtSt|Ltm|Ltn|Ltq|Ltr|Ltspr|Lu|Luftfed|Lum|Lux|Luz|Lw|LwVG|Lwbdb|Lwd|Lwg|Lyz|Lz|M|MA|MABl|MCh|MDv|MG|MRBef|MSch|MSchr|MVBl|Maa|Mad|Madr|Mag|Magn|Magy|Maj|Mak|Makl|Makr|Mall|Man|Mand|Mar|MarO|Marg|Marktfl|Marm|Mas|Masch|Mask|Mass|Mat|Math|Matr|Matth|Max|May|Maz|Maßn|Mbull|Mchron|Md|Mdt|Me|Mech|Med|Meg|Mehrz|Mel|Meld|Met|Metalik|Meth|Mex|Mf|MfI|Mg|Mgf|Mgl|Mgr|Mh|Mhe|Mhm|Mi|Mia|Mich|Mij|Mill|Min|MinBl|MinBlFin|MinDgt|MinDir|MinDirig|MinEntschl|MinErl|Miner|Minn|Mio|Miss|Miss-St|Misz|Mitben|Mitbest|Mitbew|Mitgl|Mithg|Mitt|MittBl|Mittelfr|Mittelw|Mittw|Mitw|Mixt|Mißb|Mißbr|Mißf|Mißh|Mißhdlg|Mißtr|Mj|Mjr|Mjrt|Mk|Mkfs|Mkg|Mkgr|Mkt|Mktg|Ml|Mld|Mldg|Mlle|Mlles|Mng|Mngr|Mngt|Mo|Moag|Mob|Mobilm|Mobm|Moh|Mol|Mom|Mon|Monatsh|Monog|Monogr|Monok|Monot|Monstr|Mont|Mor|Morb|Mos|Mosk|Mot|Mp|Mr|Mrd|Mrg|Mrh|Mrs|Mrz|Ms|Msch|Mschg|Mschr|Msgr|Mskr|Mss|Mst|Mstb|Mstg|Mstr|Mt|Mte|Mthm|Mtl|Mtr|Mts|Mttl|Mtv|Mu|MuII|MuVBl|Mua|Mud|Mun|Munol|Must|Mw|MwSt|Mx|Myk|Myst|Myth|Mz|Mzg|Mzs|Mzss|Mzst|Mztschr|Mzz|Mühew|N|NBL|NFI|NIchterf|NIederl|NIhII|NIkar|NIv|NTP|Nachb|Nachdr|Nacherz|Nachf|Nachgeb|Nachh|Nachk|Nachl|Nachm|Nachs|Nachtr|Nachw|Nachz|Nat|Nationalsoz|Natr|Nav|Nb|Nbf|Nbfl|Nbk|Nchf|Nchfr|Nd|NdI|NdIg|Ndr|Ndrh|Nds|Ndschr|Neb|Nebens|Nebent|Nebr|Neg|Neolitk|Neuf|Neugl|Neugr|Neuinsz|Neur|Neureg|Neurnl|Neutr|Neuw|Nf|Nff|Nfg|Nfgr|Ngl|Nicar|Nig|Nm|Nn|No|Nom|Nomenkl|Nonp|Norm|Not|Notaufn|Notausg|Notbeh|Notbr|Notd|Notgeb|Notgem|Noth|Notl|Notpf|Notst|Notverb|Nov|Nr|Nrh|Nrn|Ns|Nsch|Nschr|Nsp|Ntf|Ntfg|Ntr|NtzIstg|Ntzg|Ntzl|Num|Nutbel|Nvkn|Nzhlg|Näh|Nüt|O|OB|OFPräs|OGer|OGst|OKBDir|OKrDir|OLGPr|OLGPräs|OLGSt|OLGer|OLt|OLtg|ORVDir|ORh|OStDir|OVGer|OVerk|OVermDir|Ob|ObGer|Obb|Oberfr|Oberg|Oberh|Oberpf|Oberpr|Oberpräs|Obf|Obfr|Obgfr|Obl|Oblt|Obltg|Obm|Odw|Oe|Off|Offz|Ofr|Ofw|Ohz|Ohzg|Okk|Okkl|Okl|Okla|Okt|Ol|Omn|On|Op|Opt|Or|Orat|Orch|Ord|Ordn|Oreg|Org|OrgSta|Orient|Orig|Orn|Orth|Ortskl|Osch|Ostd|Ostfr|Ott|Ouv|Oxf|Oxh|Oxyd|Oz|P|PDir|PDv|PNr|PP|Pa|Pag|Pak|Pal|Pall|Pan|Pap|Papr|Part|Parz|Pass|Past|Pat|PatAnw|PatGer|Path|Patr|Pav|Pbd|Pck|Pckg|Pd|Ped|Pen|Pens|Pent|Per|Perf|Perg|Perm|Pers|PersWiPl|Pet|Pf|Pfl|Pfr|Pg|Pgl|Pgn|Pgt|Ph|Pharm|Phil|Philantr|Philh|Philharm|Philol|Philos|Phon|Phonol|Phonom|Phonotyp|Photogr|Photomont|Photum|Phys|Physiol|Pi|Pil|Pk|Pkg|Pkt|Pkte|Pl|Plant|Pln|Plur|Plv|Pol|Polyg|Polygr|Polym|Pop|Port|Porz|Pos|PostGNov|Postf|Postwz|Pp|Ppb|Ppbd|Ppl|Pr|PrSchAaw|Praem|Prakt|Prf|Prfg|Prim|Prior|Priv|Probl|Prod|Prof|Progn|Progr|Proj|Prok|Prokl|Prol|Prom|Pron|Prop|Pros|Prosp|Prost|Prot|Prov|Pruf|Pruz|Prz|Präd|Präf|Präl|Präm|Präp|Präs|Prät|Prüfz|Ps|Pseud|Pst|Psych|Psychol|Psychop|Psychosom|Psychother|Pt|Ptr|Publ|Pul|Pulyn|Pus|Puss|Putp|Pz|Päd|Q|Qmstr|Qt|Qu|Quadr|Qual|Quant|Quar|Quart|Quat|Quitt|Quäst|R|R-Mitgl|RABI|RAmtm|RAnw|RBDir|RBRef|RBauDir|RBm|RBz|RDir|RGBI|RGeh|RGwOSekr|RI|RIVASt|RSch|RT|RZAnw|Rab|Raff|Rak|Randb|Randbem|Rb|Rbl|Rd|RdErl|RdV|Rdbr|Rdf|Rdfr|Rdn|Rdsch|Rdschr|Re|Rec|Rechtspr|Rechtsspr|Red|Redupl|Ref|Refl|Reg|RegI|Regt|Reimp|Rein|Reiseg|Reisegep|Reiseges|Reisek|Rekr|Rel|Rem|Ren|Reorg|Rep|Repr|Res|Resign|Resoz|Resp|Ress|Rest|Restr|Ret|Rev|Rez|Rf|Rfn|Rfnr|Rfs|Rfz|Rg|Rgl|Rgstr|Rgt|Rh|Rhj|Rhld|Richtl|Rim|Riv|Rj|Rk|Rkt|Rm|Ro|Rontg|Rot|Rp|Rpr|Rr|Rs|Rsb|Rschr|Rsl|Rspr|Rsz|Rt|Rtm|Rtn|Ruf-Nr|Rum|Rundf|Rvj|Rzpt|Rzs|Rzt|Rückg|Rückl|Rückp|Rücks|Rücksp|Rückst|Rückv|Rückvers|Rückz|S|SDevErkl|SI|SOCist|SS|SSt|Sa|Sab|Sachb|Sachbearb|Sachv|Sachverst|Sachw|Sakr|Sal|Salp|Salzst|Samml|San|Sanat|Sb|Sbb|Sbd|Sbst|Sc|Sch|Schallpl|Schatzm|Schdsch|Schdsr|Schdsv|Schdsverf|Schem|SchenkSt|SchiRegVfg|Schiedsr|Schiffb|SchiffsRegVfg|Schl|Schm|Schmp|Schn|Schnp|Schp|Schr|Schriftf|Schriftl|Schrifts|Schriftst|Schubf|Schuldverschr|Schutzz|Schußw|Schw|Schwerb|Schwerbeh|Scr|Sdbd|Sdg|Sdp|Sdr|Sdz|Sdzt|Se|Seef|Seeh|Seek|Seels|Seem|Seevers|Seew|Sek|Sekr|Sekt|Selbstf|Sen|SenDir|SenDirig|Send|SeosetH|Sep|Sept|Seq|Ser|Settlement|Seus|Sez|Sfk|Sfl|Sgt|Sh|Shb|Sib|Sich|Sichtv|Sichtw|Sig|Sign|Sim|Sinf|Sing|Sir|Sit|Sittl|Sk|Skand|Skdt|Skt|Slg|Slgn|Slow|Sm|So|Sol|Son|Sopr|Sort|Soupr|Sout|Souv|Soz|Sozw|Sp|Spark|Spd|Sped|Spek|Spez|Spfr|Spfrde|Spielb|Spielz|Spir|Spk|Spl|Spr|Sprachw|Sprachwiss|Sprd|Sprdr|Sprk|Sps|Spvg|Spvgg|Spw|Sr|Ss|Sskr|St|StKl|StRegBek|Stab|Stad|Staf|Stat|StatGes|Stb|Stckpr|Stckz|Std|Stdg|Stdn|Steig|Stell|Sten|Stg|Stgm|Stift|Still|Stip|Stj|Stk|Stkr|Stm|Stmb|Stmk|Stn|Stp|Stpfl|Stpl|Str|StrRegBek|Strafr|Strafs|Strat|Sts|Stud|StudDir|Stv|Stw|Subd|Subj|Subskr|Subst|Subtr|Subv|Sup|Suppl|Susp|Swk|Synd|Sz|Szgm|T|TIIg|TO|TVAufz|TVz|Ta|Tab|Tabl|Taf|Talm|Tam|Tamb|Tang|Tans|Tant|Tar|TarReg|Tax|Tbl|Tct|Td|Tdr|Tech|Techn|TeeSt|Teel|Teilfin|Teilh|Teiln|Teilz|Tel|Telegr|Temp|Tend|Tenn|Term|Termin|Terr|Tert|Tf|Tfl|Tflg|Tflw|Tg|Tgb|Tgbl|Tgt|Tgw|Th|Theat|Therm|Thw|Thür|Ti|Tit|Tk|Tkr|Tkst|Tl|Tle|Tln|TnangReg|Tog|Toil|Tom|Tonn|Top|Torp|Totp|Tp|Tr|TrIg|TrIk|Trad|Trag|Tragf|Tragkr|Tranap|Trans|TransI|Transf|Transkr|TrauspI|Trbst|Trem|Trfg|TrgI|Trgf|Trgkr|Trib|Tromp|Trp|Trsf|Trsp|TruppVtg|Ts|Tschb|Tschft|Tsd|Tsp|Tu|Tug|Tun|Tunes|Turb|Turn|Tw|Twregrk|Typ|Tz|Tüb|U|U-BeIh|UFr|UGr|UIt|USt|UZg|UZwGBw|Ubr|Uffz|Ufr|Ufw|Ufwg|Ug|Ukr|UmI|Umarb|Umb|Umbr|Umd|Umdr|Umf|Umg|Umgr|Umk|Umkr|Umr|Ums|UmsSt|Umsch|Umst|Umw|UnI-Kl|Unf|Univ|Unk|Unt|Unterbr|Unterfr|Unterg|Unterh|Unterm|Untern|Unterred|Unters|Untersch|Unterz|Unz|Unzufr|Unzul|Uoffz|Up|Urh|Urk|Url|Urs|Urspr|Urt|Urug|Ut|Uw|Uwdlg|Uwk|V|VAnw|VAnz|VBI|VBef|VBeh|VIBest|VIschr|VIsk|VOBl|VOSch|VPr|VPräs|VSt|VVg|VWI|Va|Val|Var|Vb|Vbb|Vbd|Vbdg|Vbem|Ver|VerSt|Veranl|Verb|Verbdg|Verbr|Verd|Vereinb|Verf|Verfg|Verg|Verges|Vergl|Vergn|VergnSt|Vergr|Verh|Verj|Verk|Verkl|Verl|Verm|VermDir|VermInsp|VermOInsp|VermSt|Verp|Verpfl|Verschl|VerstG|Vertr|Verz|Vet|Vf|Vfg|Vg|Vgach|Vgg|Vgl|Vgr|Vgt|Vhdlg|Vhw|Vis|Vit|Vj|Vjber|Vjh|Vjs|Vjschr|Vk|VkBI|Vkde|Vkf|Vkfl|VlBek|VlVorschr|VlVschr|Vlg|Vm|Vn|VoIIstrBef|VoIIstrGkI|Vok|Vollm|Vollstr|Vollz|VorE|Vorarb|Vorb|Vorbed|Vorbeh|Vorbem|Vorber|Vorbest|Vordr|Vorentsch|Vorentw|Vorf|Vorg|Vorj|Vork|Vorl|Vorm|Vorn|Vorp|Vorr|Vors|Vorsch|Vorst|Vortr|Vorw|Vorz|Vp|Vpfl|Vrg|Vrm|Vrz|Vrzs|Vschr|Vst|Vstdg|Vstg|Vt|VtSt|Vtdg|Vtr|VuIIstrKI|Vulg|Vurschr|Vw|VwKG|VwVfg|Vwwt|Vz|Vzg|Vzge|Vzw|W|WAbk|WArb|WBEG|WDIr|WDSen|WGSch|WPVertr|WSt|WStr|WZBl|Wa|Wachst|Wachtm|Wag|Wahlb|Wahlber|Wash|Wb|WbI|Wber|Wbs|WdKl|Wdg|Wdh|Wdhlg|Wdst|Web|Wegeg|Wegf|Wegl|Weiastr|Weim|Weis|Werkst|Wertp|Westf|Westm|Wf|Wfl|Wgdr|Wgl|Wh|Wha|Whg|Whga|Wi|Wirtsch|Wiss|Witt|Wj|Wk|Wkg|Wkm|Wkmstr|Wkr|Wkst|Wkz|Wkzg|Wm|Wo|Wp|Wpl|Wrkg|Ws|Wschr|Wst|Wstr|Wtb|Wtg|Wv|Wvb|Wvz|Ww|Wwe|Wwr|Wy|Wyo|Wz|Wzg|X|Y|Z|ZAbfSt|ZBl|ZFSt|ZGKom|ZIe|ZIg|ZIv|ZSch|ZSekr|ZSprSt|ZSt|ZTar|Zahl|Zahlm|Zahlst|Zbl|Zchg|Zchn|Zdg|Zdh|Zdw|Zeitl|Zeitschr|Zeitw|Zellst|Zentr|Zers|Zerschl|Zerst|Zerstr|Zf|Zfg|Zgf|Zgh|Zgn|Zi|Ziff|Zig|Zim|Zit|ZivAnw|Zk|Zkft|Zl|Zm|Zn|Zool|Zr|Zs|ZsIzg|Zschr|Zsfg|Zshg|Zss|Zst|Zstg|Zt|ZtIn|Ztg|Ztgn|Ztl|Ztm|Ztn|Ztr|Ztrhzg|Ztschr|Ztw|Zub|Zubr|Zuf|Zugew|Zul|Zus|Zusch|Zuschr|Zust|ZustV|Zut|Zuw|Zw|Zündw|a|aIIg|aT-AnlV|aa|abbl|abbr|abbrev|abds|abg|abgeI|abgedr|abgef|abgeg|abgeh|abgek|abger|abges|abgeschl|abgest|abh|abk|abl|abn|abr|abs|abschl|abst|abstr|abug|abw|abz|abzgI|acc|accad|accel|ad|adb|adj|adm|ado|adv|advs|ae|aengl|aequ|aero|aeron|aet|aeuz|aff|affet|afgh|afr|afranz|afries|afrik|afrk|afrz|afränk|ag|agIfrz|agIt|agb|agerm|agr|ags|agz|ahd|ai|al|allg|anbefr|ang|ao|aotgedr|apI|apers|apok|app|appass|appr|approb|approx|apr|apukr|aq|ar|arab|aram|arch|arg|argent|arith|arom|arp|arpegg|arr|arret|art|aruss|as|asI|asIaw|asIh|aserb|asph|ass|assyr|ast|astr|astrol|astron|asym|asyn|asynd|asyuchr|asächs|at|atI|athl|atm|attr|audiom|aufgef|aufh|aufr|aufw|ausI|ausbez|ausg|ausgeg|ausgegl|ausgel|ausgen|ausges|ausgesch|ausgeschl|ausgest|ausgew|ausgez|auskpfl|ausr|ausschl|austr|austral|ausw|aut|auth|autogr|auton|autor|außerger|av|avdp|avest|avu|aw|awdt|awsl|b|baak|bab|bad|bakt|ball|balt|band|bank|bapt|barb|baschk|baschkir|bauf|baupol|bautechn|bayr|bd|bds|bdt|bdtd|bearb|bed|bef|befr|begl|begr|beh|beif|beig|beigeh|beih|beil|beisp|beisph|beispw|bek|bel|belg|bem|ben|beob|ber|berat|berf|berfl|bergefr|bergm|berl|berschl|bersek|bes|besch|beschI|beschr|besp|bespr|best|bestr|betr|bettl|beurl|bev|bevollm|bevrecht|bew|bez|bezb|bezgl|bezw|bf|bfgd|bfgn|bfl|bfrk|bgl|bgr|bgrzt|bh|bhm|bhut|bhutan|bianendt|bibl|bil|bild|bildl|biogr|biol|birm|bisch|bischofl|bisl|bisw|bl|blg|boh|bol|boliv|bot|br|bras|bret|brev|brill|brit|brl|brn|brosch|brschw|brschwg|brt|bräunl|brün|bs|bschr|bsd|bsds|bsph|bspw|bt|btechn|bto|btto|bu|buah|buchst|bur|burg|burm|bus|bvr|bvt|bwf|bwgl|byz|bz|bzf|bzgl|bztl|bzw|bürg|c|ca|cad|cal|calv|camer|cand|cant|cap|capp|caps|capt|carc|cath|cd|cent|cert|cet|ceyl|cf|cfr|ch|chald|cham|chang|char|chem|chiff|chiffr|chil|chin|chir|chol|chq|chr|christl|chrom|chron|chronogr|churw|circ|cl|col|coll|collab|colloq|com|comm|compr|con|conc|cond|conf|coop|cop|coq|cor|corp|corr|cour|cpt|cr|cresc|crt|ct|cts|cud|cult|cur|curr|cutt|cv|cwt|d|dab|dad|daf|dag|dagest|dah|dalm|dam|dan|dankwtw|dargel|dargest|darl|dass|dat|dav|dazw|dch|ddt|deb|dec|decoct|decr|decresc|def|deg|degr|dek|dekl|del|deleg|delin|dem|demn|demz|demzuf|depr|dergl|derj|derm|ders|des|desgI|desgl|desinf|desint|deskr|desp|dess|dest|desz|det|dez|dfg|dgl|dgt|diab|diag|diakr|dial|diam|did|dieI|dienstl|dienstpfl|diesI|diesbez|diess|diff|diffam|dig|dil|dim|dimin|dip|dipt|dir|dish|disj|disk|diskr|disp|diss|dist|distr|disz|div|dkl|dkwtw|do|dok|doktr|dom|dominik|donnerst|dopp|dorth|dos|dpp|dr|drlg|ds|dspr|dstl|dt|dto|du|dub|duminikan|durchl|durchschu|durchw|dyn|dz|dzt|dztg|dän|e|ea|eb|ebd|ebenf|ebf|ebn|ec|econ|ecuad|ecuadur|ed|edd|ef|eff|eh|ehd|ehed|ehel|ehem|ehrl|ehrw|eiaf|eidg|eidgen|eig|eigenh|eigent|eigentl|eigh|eigtl|einb|einbez|eind|eindr|eindrgl|eing|einged|eingedr|eingef|eingeg|eingegl|eingel|eingem|einger|eingesch|eingeschl|{e}eingetr|eingez|eingl|einh|einkstpfl|einl|eins|einschl|einschr|einsp|einst|einstm|einstr|eint|eintr|einverl|einverst|einw|einwdfr|einz|eisenh|eisenverarb|ek|ekuad|ekuador|el|elektr|elektromech|elektromot|elektrumagn|ell|elmag|els|em|emer|empf|endg|energ|eng|engl|ent|entb|entd|enteign|entf|entg|entgges|entgl|enth|entl|entn|ents|entsch|entschl|entspr|entst|entw|enz|enzykl|eo|ep|epid|epigr|epik|epil|erI|erachwgl|erb|erbl|erd|erf|erfdl|erfdlf|erfdlfs|erford|erforderlf|erforderlfs|erg|erh|erk|erkl|erkr|erl|erm|ern|err|ers|ersch|erschl|erschw|erstg|erstgen|erstkl|erstm|ert|ertr|erw|erwähnw|erz|erzb|erzbisch|esk|esot|espr|espress|est|estab|estn|etc|ethn|etr|etw|etym|euph|euphem|eur|europ|euteig|ev|evgl|evt|evtl|ew|ex|exDiv|exalt|exc|exempl|exerz|exh|exk|exkl|exot|exp|expl|expon|expr|ext|exterr|extr|exz|exzept|f|fIn|fahrb|fak|fakt|fallw|fam|fan|farb|farbl|fasch|fasz|fav|fbg|fdl|fe|fec|fem|ferm|fernschr|fernschrftl|feuerg|feuergef|ff|ffd|filt|fin|finn|finnl|firm|fisk|fk|fl|flekt|flex|flg|fluor|fmdl|foI|foIg|fod|fol|folg|forest|form|fortgef|fortges|fortis|fortl|fortschr|forz|fotogr|fps|fr|frag|fragl|fragm|fragw|frank|franz|frb|frbl|frdl|frdsch|frdschtl|frdspr|frdsprl|freig|freih|freiw|freiz|fremdl|fremdspr|fremdsprl|frfr|frger|frgm|frh|frhtl|fries|friesl|fris|fristger|frnhd|fro|frstl|frt|frtr|frw|frz|frzg|fschrl|fstI|ft|ftgn|fud|funkt|furn|fus|fw|fwd|g|gIas|gIeichl|gaI|gaIl|gabun|gal|gall|galv|gar|gastr|gb|gbd|gbr|gds|ge|geIbI|geIeg|geIgg|geb|gebd|gebh|gebl|gebr|ged|gef|gefl|gefr|geg|gegr|geh|geistl|gek|gel|gelt|gem|gen|geod|geogr|geol|geom|geophys|geopol|georg|geoz|gep|gepfl|gepl|gepr|ger|germ|ges|gesch|geschfd|geschl|geschm|geschn|geschr|gesp|gespr|gest|gestr|get|getr|gew|gez|gezw|gf|gfg|gfl|gg|ggI|ggb|ggbfs|ggez|ggf|ggfs|gglb|ggr|ggs|ggz|ghan|gk|gl|gleichld|gleichlfd|gleichltd|gleichn|gleichz|gls|glt|gltd|glz|glzd|gm|gms|got|gp|gpr|gr|grad|gram|gramm|gran|graph|grat|grav|graz|grch|grfl|grhzgI|griech|grus|grusin|gsch|gschftl|gschr|gschtl|gschu|gspr|gstr|gt|guat|guatemaIt|guay|guin|gumm|gymn|gyn|gynäk|gz|gzj|gäI|gütI|h|hKfn-gBr|ha|haas|hab|habil|habsb|haftb|haftpfl|hagiogr|hait|halb|halbj|halbjhg|halbjhl|haltb|ham|hamb|handgeschl|handgeschr|handgest|handgew|handl|handschr|hann|harm|harml|haupts|haus|hausw|hb|hbfl|hbg|hbst|hbstg|hbstl|hd|hdbr|hdgeschl|hdgest|hdgew|hdgm|hdgr|hdgrfl|hdl|hdschr|hdt|hebr|heimatl|heiml|heir|helg|helv|herg|hergest|herm|herv|herz|herzgl|hess|heth|hett|heur|hex|hg|hgb|hgd|hgg|hgm|hind|hindust|hingew|hinr|hins|hint|hinterhltg|hinterlstg|hinth|hisp|hist|hiuw|hj|hl|hlg|hochd|hochst|hochw|hohtl|holl|holländ|holst|hon|hond|hondur|hor|horiz|hort|hott|hptpl|hpts|hptw|hr|hrg|hrsg|hs|hsl|ht|hum|hy|hybr|hydr|hydrogr|hydrol|hydrom|hydromech|hyg|hygrosk|hyp|hyperb|hypfr|hypn|hypoth|hypt|hzb|hzgl|i|iac|iakomm|ib|ibd|id|ident|idg|ill|illum|illus|illustr|im|imit|imm|impr|imst|in|ina|inbegr|indekl|indisk|indiskr|indiv|indogerm|inf|infl|inform|inhaft|init|inkl|innerl|inoff|inq|inquis|ins|insbes|insg|insgeh|insges|insp|instr|int|integr|inter|interk|interm|intern|interpr|interr|interrog|interv|intr|intrm|introsp|introv|intrv|inv|invent|inw|inwf|inww|inzw|ir|irak|iran|ird|irg|irr|irrat|irreg|irrev|is|isl|islam|isländ|isol|isr|it|ital|itell|iter|itr|j|jak|jakut|jap|jem|jemen|jens|jew|jfr|jg|jgdfd|jgdfr|jidd|jmd|jmdm|jmdn|jnr|jord|jordaa|jr|jug|jugosl|jun|jur|jüd|k|kI|kIIn|kIass|kInderl|kais|kal|kalend|kalm|kalmück|kamb|kamer|kan|kanad|kand|kant|kap|karIb|karoI|kart|kast|kat|kath|kaufm|kaus|kdb|kdt|kelt|ken|kennz|ker|kf|kfm|kfr|kgI|kh|kin|kind|kindl|kirg|kl|klf|kmd|kmdt|kmfr|koh|kol|kolp|kolumb|kom|komb|komf|komm|komp|kompl|kompr|kond|konf|konfl|konfr|kong|kongol|kongr|konj|konjug|konjunkt|konk|konkr|kons|konsekr|konserv|konsp|konst|konstr|kont|kontr|kontrap|konvert|konz|koop|koord|kop|kor|korean|korp|korrep|korresp|kot|kouseq|kpl|kr|krfr|krist|krit|krk|krzfr|ksIaw|ksl|kstl|kt|kuI|kub|kuban|kull|kult|kunsek|kunsthist|kunv|kurfl|kurhess|kurpf|kurs|kurzfr|kuw|kv|kw|kymr|kz|kzfg|kzfr|kzfrg|kzh|künstl|l|lak|langob|laot|larg|lb|ldw|leIbh|led|leg|leichtl|leichts|leipz|leit|leitd|let|lett|letztw|lev|lexikogr|lfd|lfde|lg|lgd|lgfr|li|lib|liban|liber|lim|lin|ling|liq|lit|liter|litgesch|lith|lks|loal|lobw|log|lok|lomb|long|lothr|luftd|luftgef|luftgek|luth|lux|lyr|lün|m|ma|maI|mad|madag|magn|mai|mak|maked|makr|mandsch|mar|marc|mark|marm|marokk|martan|mas|masc|masch|maschr|maschtechn|mask|mass|mat|math|maur|mauret|max|maz|mazed|maßg|maßgeb|maßgebl|md|mdal|mdls|mdse|me|meas|mech|meckl|med|mehrj|meistb|mel|melan|melanch|meldepfl|meludr|merc|metall|metaphys|meteor|meth|metverarb|mex|mexikan|mfr|mfranz|mfrk|mfrs|mfrz|mfränk|mfs|mgr|mgriech|mhd|mi|miltechn|min|mind|minderj|mindj|misc|mitget|mitt|mitteld|mitteldt|mittelfr|mittw|mißbr|mj|mk|mkr|mlat|mnd|mndd|mndl|mnl|mob|mobl|mod|mog|mogl|moh|moham|mohammed|mol|mom|mong|mongol|monog|monogr|monok|monol|mont|mor|morg|morph|mos|mosk|mosl|mot|mpers|mrh|mschr|mst|msth|mstl|mtl|mts|mttl|mu|mult|multilat|mus|mut|mutl|mw|mx|myk|myst|myth|n|na|nachdr|nachm|nachst|nachtlg|nachtr|nachwsl|nad|nam|nat|natdem|natfarb|natsoz|natur|naut|nd|ndd|ndl|ndrd|ndrh|ndrl|nds|ndt|ne|neap|neb|nebeus|nebl|neg|nem|nengl|neof|neofasch|neuhebr|neur|neurol|neurot|neuseel|neutat|neutest|neutr|neuw|nf|nfrz|ngr|nhbr|nhd|nhebr|nicar|nicarag|niederl|niem|nig|niger|nigr|nihil|nikar|nikarag|nirg|nl|nlat|nm|nmtl|nnI|nno|nnurd|no|nom|not|notw|npl|nplm|nt|nto|nukl|num|nung|nutl|nw|nwd|nzl|näml|o|ob|obb|obd|oberd|oberh|obfr|obfrk|obgl|obj|obl|oblig|oc|od|oec|off|offiz|offtl|offz|ofrs|oh|ok|okk|okon|okonom|ol|old|oldenb|omn|ono|op|or|orch|ord|org|orient|orig|orn|orth|ortl|oso|ostd|ostdt|osterr|ostfr|ostidg|oxdd|oxyd|oz|ozs|p|pa|paed|pag|pak|pakist|pal|pan|panam|panar|par|parl|parlam|parlament|parz|pass|past|pat|path|patr|pd|per|perf|perm|pers|peruan|pf|pg|pharm|phil|philanthr|philat|philh|philharm|philipp|philol|philos|phon|phonol|phosph|photugr|phys|physiogn|physiol|picc|pinx|pizz|pkg|pl|plak|planm|plm|plotzl|pnxt|poet|pol|polit|poln|polyg|polym|polyn|pop|port|pos|post|posth|postw|pp|ppa|ppb|ppd|ppt|pr|prakt|prbw|prim|prinz|priv|pro|prob|probl|proc|prod|prof|progr|prom|pron|prop|proph|pros|prot|prov|prox|prubw|präd|prädik|präm|präp|präs|psych|psyched|psychiat|psychiatr|psychol|psychop|psychosom|pt|pto|publ|pul|pulv|pv|pw|pzt|päd|q|qr|qu|quadr|qual|quant|quitt|r|ra|raII|rab|rad|raff|rand|rat|rbz|rd|reI|reIt|reIter|rechtsw|red|ref|refl|reform|reg|regeIm|rep|repbed|rer|res|resp|restI|rev|rez|rf|rfz|rgIm|rh|rhet|rhfrk|rinf|rinforz|rip|rit|ritard|riten|rkr|rom|rotw|rs|rumän|russ|rzp|rzptpfl|rztpfl|rückl|rückw|s|sFr|sI|sachk|san|sanskr|sat|sbst|sc|sch|schem|scherz|scherzh|schl|schott|schr|schriftl|schw|schwb|scient|sculps|sd|sdl|sec|seef|seekr|seem|sek|sel|sem|sen|seneg|senkr|sens|sep|seq|seqq|serb|sex|sez|sf|sig|sign|sim|sin|sinf|sing|sinng|sit|sittl|sk|skand|skr|slaw|sm|smorz|soc|sod|sof|sog|sogen|sol|sold|solid|solv|som|somal|sord|sosp|sost|sosten|souv|sow|sowj|soz|sp|spIr|span|spec|spek|spez|spf|spfr|sph|spr|sprachw|sprachwiss|sq|sqq|sqs|sr|sso|ssw|st|staatI|staatl|stacc|stat|std|stdg|stdl|stelIv|sten|stf|stfr|stg|sth|stl|stpfl|str|strIng|strat|stud|stv|stäad|städt|sub|subat|subj|subsp|subtr|subv|sud|sudan|sugg|sugl|summ|sva|sw|symph|synchr|synt|synth|syr|syst|szs|sächs|südd|süddt|südl|südw|t|tab|takt|taktl|taktv|tam|tang|tans|tat|tatar|tats|tax|tct|tdu|techn|technol|teilw|tel|telef|telegr|tem|temp|ten|tend|term|terr|tert|test|tgl|theatr|them|theol|theor|ther|therap|therm|thermon|tib|tibet|tit|tlw|tm|todl|tog|top|topogr|tr|trad|trag|tragb|trans|transf|transkr|trausp|trig|trk|trop|tsch|tt|tunes|tunl|turb|turkm|turkmen|tw|typ|tägl|türk|u|uItim|uachd|uagew|ubpl|ubr|ue|ufl|ug|ugand|ugf|ugr|ugs|ugt|ukr|ul|ult|umb|umd|umf|umfgl|umfgr|umg|umgeb|umged|umgedr|umgek|umgel|umgest|uml|ums|umschr|umstpfl|un|unabl|unang|unb|unbeb|unbed|unbef|unbefl|unbegr|unbeh|unbek|unbem|unber|unbeschr|unbestr|unbet|unbez|unbr|unehel|uneig|uneingeschr|unempf|unentb|unentg|unentsch|unentschl|unerf|unerg|unergr|unerh|unerkl|unerl|unerm|unertr|unf|unfl|unfr|ung|ungar|ungeb|ungebr|unged|ungeh|ungek|ungel|ungen|unges|ungesch|ungest|ungestr|ungez|ungezw|ungl|unh|uniform|unis|univ|unk|unkl|unl|unpag|unpf|unpg|unr|unrat|unreg|unregelm|unrent|unselbst|unstr|unt|unterh|untersch|untgl|untr|unv|unverantw|unverb|unverbr|unverd|unverg|unverh|unverk|unverp|unvers|unversch|unverz|unverzgI|unvollst|unvorb|unvors|unw|unz|unzerbr|unzerst|unzug|unzul|unzurechn|unzust|unzuv|unzw|uotf|up|upt|urgerm|urgesch|uridg|urk|urkdl|urschr|urslaw|urspr|urug|usb|usbek|usf|usw|uubew|uuv|uv|uvsf|v|vIs|va|val|var|vb|vbd|vbdl|vd|vdt|ven|ver|verb|verbr|verchr|verd|vereh|vereinf|verfl|verfr|vergl|vergr|verh|verj|verk|verm|vern|veroff|verp|verpf|verpfl|verschl|verst|vertr|vertrl|verurs|verz|veränd|verändl|vet|vf|vg|vgl|vgm|vh|vid|vitr|viv|vj|vk|vl|vlg|vlgt|vll|vm|vol|volkst|vollst|vollsynchr|vollz|vordr|vorg|vorges|vorgesch|vorl|vorm|vorr|vors|vorz|vpf|vpfl|vrb|vrgr|vrm|vrt|vs|vsl|vst|vstdl|vt|vulk|vurh|vurschm|vz|vzgl|w|wIosI|wahrsch|wall|wallon|wbl|wd|wdt|wehrf|weidm|werkt|west|westd|westf|westgerm|westidg|westl|wf|wg|wgl|wh|wiederh|willk|wiss|wktgs|wl|wm|wnw|woch|wss|wstd|wsw|wt|wtgl|wtgs|wu|x|y|z|zIt|zIv|zKungF|za|zahlr|zeatrip|zeitgen|zeitl|zeitw|zentr|zentrif|zerstr|zfr|zga|zgat|zgl|zgw|zit|zk|zool|zr|zs|zsges|zsgest|ztl|ztw|zuf|zugeI|zuget|zugew|zugl|zuk|zul|zur|zus|zust|zuw|zuz|zuzgI|zw|zwgw|zyl|zypr|zzgl|Ä|Äg|Äq|Äquiv|Ästh|Äth|Ö|Ü|Üam|ä|äg|äq|äquiv|ärztl|ästh|äth|ö|ü|üb|überb|übers|überschl|überst|übertr|überz|übf|übl|üblw|übz)

%s OPEN_QUOTE POLISH_CONDITIONAL_MODE JUST_AFTER_PERIOD CLITIC_MODE

%%
{ENDMARKER}                                             { fileEnd(); }


// dates and fractions

<POLISH_CONDITIONAL_MODE>{POLISH_CONDITIONAL_CLITIC} / {POLISH_CONDITIONAL_ENDING}                                      { yybegin(YYINITIAL); return currentToken(); }
<POLISH_CONDITIONAL_MODE>[^b].                                        { throw new RuntimeException("..." + currentToken());}
{EMDASH}                                                 {return currentToken();}
{URL}                                                         { return currentToken(); }

// special words
{c}an / not                                                      {return currentToken();}
{l}em / me                                                      {return currentToken();}
{g}on / na                                                      {return currentToken();}
{g}im / me                                                      {return currentToken();}
{w}an / na                                                      {return currentToken();}
{g}ot / ta                                                      {return currentToken();}

{LETTER}\.                                                      {return currentToken();}
{LETTER}{2,12} / \.[:uppercase:]                                  {return currentToken();}
{PLUSAMPERSAND}                                                 {return currentToken();}
{SEABBR}\.                                                      {return currentToken();}
{PRAGMA}                                                        {return currentToken();}
{FNAME}                                                         {return currentToken();}

// contractions and other clitics
{INIT_CLITIC}{CLITIC}                                           {return currentToken();}

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
.                                                   { return currentToken();}



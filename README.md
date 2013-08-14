# ScannedDocumentProcessor

This is what this program is for:

1. The "paperless office" still is not a reality (that's why scanning and printing documents still matters)
1. I regularly have to prepare PNG documents for my tax office
1. I scan the documents with fine resolution so I can fix scan errors, enhance quality and such (= the files get big)
1. While most of the time I use Linux, sometimes I prepare documents on Windows (= I need a multi-platform solution)
1. I do not want to send documents to my tax office that are 1-5 MB in size or even larger (because that takes too much time)
1. I do not want to prepare all the docs manually (usually: scale proportionally to 800 px width, reduce colors to 8 without to much dithering)

In the past I have used a simple bash script that was wrapped around ImageMagick. It did the trick most of the time, though
I did not (and do not) want to set up ImageMagick on Windows (if that even is possible or feasible, I don't know).

That's what ScannedDocumentProcessor is for:

It takes a heap of PNG business documents, scales them down proportionally to 800 px width and reduces color while
maintaining readability.

Usually PNG images that are several MBs in size get crunched down to a couple of 100 KBs. They can be transmitted much faster than large files.

## Success story

I have just scanned two documents, one was 1,5 MBs in size, the other 5,9 MB (total 7,4 MB). After processing with ScannedDocumentProcessor, both files were 0,2 MB in total and could still easily be read.

# Author

Me:

1. https://reneschmidt.de/wiki/index.php/page/view/ScannedDocumentProcessor,Start
2. https://reneschmidt.de/

# Licence

GPL v2.

# Download

[Download the jar file](https://github.com/rene-s/ScannedDocumentProcessor/blob/master/ScannedDocumentProcessor.jar?raw=true)

# Installation and how to use

1. Install [Java JRE 7](https://www.java.com/getjava/)
1. Double-click the jar (you might need to "chmod +x" the jar beforehand) or call like this:

```bash
 java -jar ScannedDocumentProcessor.jar
```

Java must be in your PATH.

## Acknowledgements

This software is based on other software:

Name                       | Licence                   | Description
--------                   | --------                  | --------
[Java Imaging Utilities](http://sourceforge.net/projects/jiu/) | GPL | A Java library for loading, editing, analyzing and saving pixel image files.

Have fun.

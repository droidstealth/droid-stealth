#!/bin/bash

echo 'Assuring ./latex-output/ exists'
mkdir -p latex-output

echo 'Copying IEEEtran files to ./latex-output/'
cp IEEEtran.* latex-output/
cp *.{tex,bib} latex-output/
cd latex-output

echo 'Cleaning folder before running'
rm `ls | grep droidstealth | grep -v .tex`

echo 'Running pdflatex'
pdflatex $1
bibtex $1
pdflatex $1
pdflatex $1

echo 'Copying pdf to parent folder'
cp $1.pdf ../

exit 0;

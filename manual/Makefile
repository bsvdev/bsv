# Manual Makefile

SHELL := /bin/bash
PDFLATEX = pdflatex
TARGET = Manual
GHOSTSCRIPT = gs
PUBLISH_TARGET = BSV_Manual

all: $(TARGET).pdf

$(TARGET).pdf: *.tex
	$(PDFLATEX) $(TEX_ARGS) -draftmode $(TARGET)
	$(PDFLATEX) $(TEX_ARGS) $(TARGET)

publish: all
	$(GHOSTSCRIPT) -sDEVICE=pdfwrite -dDOPDFMARKS=true -dMaxSubsetPct=100 -dPDFSETTINGS=/printer -dUseCIEColor=true -sOutputFile="$(PUBLISH_TARGET).pdf" -dNOPAUSE -dBATCH "$(TARGET).pdf" docinfo.txt

clean:
	rm --force *.{aux,log,lof,out,toc} $(TARGET).pdf $(PUBLISH_TARGET).pdf

.PHONY: all publish clean

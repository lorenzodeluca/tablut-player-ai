#!/bin/bash

# entra nella cartella del sorgente
cd Tablut;

# rimuove eventuali build precedenti
ant clean

# compila il codice sorgente
ant compile

# crea il file JAR specifico per il MatrixClient
ant matrix-jar

mv matrix.jar ..
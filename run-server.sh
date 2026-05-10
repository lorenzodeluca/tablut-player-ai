#!/bin/bash

# entra nella cartella del sorgente
cd Tablut;

# rimuove eventuali build precedenti
ant clean

# compila il codice sorgente
ant compile

# esegue il server
ant server;
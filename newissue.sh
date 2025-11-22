#!/usr/bin/env bash
echo "-------------------- NEW GITHUB ISSUE -------------------"
hub issue labels

#echo
#echo
read -p 'Issue Title: ' TITLE

read -p 'Issue Label: ' LABEL

hub issue create -M R1 -l $LABEL -m "$TITLE"

echo "-----------------------------------------------------------"
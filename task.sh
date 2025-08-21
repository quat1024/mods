#!/bin/bash
set -eu

# little busybox script. see https://github.com/adriancooney/Taskfile

collect () {
  rm -rf collect
  mkdir collect

  # only collect from subprojects with a version number in them
  # excludes :vanilla, excludes :floader-only
  cp ./*1.*/build/libs/*.jar collect

  cp CHANGELOG.md collect

  #mv ./collect/*sources*.jar collect/sources

  echo "Yay all done"
}

mktag () {
  VER=v$(TZ="America/New_York" date +%Y.%m.%d)
  echo "making a tag for version" $VER
  git tag -a "$VER" -m "Marks release of version $VER."
}

help () {
  echo "Available functions:"
  compgen -A function
}

echo "--- ${1} ---"
eval "$@"

# TZ="America/New_York" date +%y.%m.%d
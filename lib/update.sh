#!/usr/bin/env bash

pushd lib/
  if [[ `uname` == 'Darwin' ]]; then
    echo *.jar > contents.macosx.txt
  fi
  if [[ `uname` == 'Linux' ]]; then
    echo *.jar > contents.linux.txt
  fi
popd


#!/bin/bash
function gittag() {
  git config --global user.name "Chris Moreton"
  git config --global user.email "chris@netsensia.com"
  README=$(cat README.md | grep "${1}" | wc -l)
  BUILD=$(cat build.gradle | grep "${1}" | wc -l)
  if [ $README -lt 2 ] || [ $BUILD -lt 1 ]
  then
    echo "Please update README and build.gradle"
  else
    git add -A
    git commit -m "Tagging as $1"
    git tag $1
    git push
    git push --tags
    ./gradlew uploadArchives
  fi
}

sed -i "s/version = .*/version = '$1'/g" build.gradle
sed -i "s/<version>.*<\/version>/<version>'$1'<\/version>/g" README.md
sed -i "s/version: '.*'/version: '$1'/g" README.md
./gradlew publishToMavenLocal
gittag $1

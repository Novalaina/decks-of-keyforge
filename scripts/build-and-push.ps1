
$ErrorActionPreference = "Stop"
docker --version
aws --version

./gradlew genSrc

cd ui

npm run build
cd ../src/main/resources
if (test-path static) {
  rm static -r -Force
}
xcopy ..\..\..\ui\build static /i /s
cd ../../../

./gradlew build
./gradlew dockerCopyJar

$VERSION = Get-Content version.txt

cd ./docker

docker build -t "keyswap:$VERSION" .

$env:AWS_PROFILE="dok_deploy"

aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 811687120814.dkr.ecr.us-west-2.amazonaws.com

docker tag "keyswap:$VERSION" 811687120814.dkr.ecr.us-west-2.amazonaws.com/decks-of-keyforge:$VERSION

docker push 811687120814.dkr.ecr.us-west-2.amazonaws.com/decks-of-keyforge:$VERSION

"All done now!"

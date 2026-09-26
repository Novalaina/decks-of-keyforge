
$ErrorActionPreference = "Stop"
docker --version
aws --version

./gradlew genSrc
if ($LASTEXITCODE -ne 0) { throw "Gradle genSrc failed with exit code $LASTEXITCODE" }

cd ui

npm run build
if ($LASTEXITCODE -ne 0) { throw "NPM build failed with exit code $LASTEXITCODE" }
cd ../src/main/resources
if (test-path static) {
  rm static -r -Force
}
xcopy ..\..\..\ui\build static /i /s
cd ../../../

./gradlew build
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
./gradlew dockerCopyJar
if ($LASTEXITCODE -ne 0) { throw "Gradle dockerCopyJar failed with exit code $LASTEXITCODE" }

$VERSION = Get-Content version.txt

cd ./docker

docker build -t "keyswap:$VERSION" .
if ($LASTEXITCODE -ne 0) { throw "Docker build failed with exit code $LASTEXITCODE" }

$env:AWS_PROFILE="dok_deploy"

aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 811687120814.dkr.ecr.us-west-2.amazonaws.com
if ($LASTEXITCODE -ne 0) { throw "AWS ECR login failed with exit code $LASTEXITCODE" }

docker tag "keyswap:$VERSION" 811687120814.dkr.ecr.us-west-2.amazonaws.com/decks-of-keyforge:$VERSION
if ($LASTEXITCODE -ne 0) { throw "Docker tag failed with exit code $LASTEXITCODE" }

docker push 811687120814.dkr.ecr.us-west-2.amazonaws.com/decks-of-keyforge:$VERSION
if ($LASTEXITCODE -ne 0) { throw "Docker push failed with exit code $LASTEXITCODE" }

"All done now!"

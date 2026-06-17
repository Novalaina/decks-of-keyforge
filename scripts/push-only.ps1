
$ErrorActionPreference = "Stop"
docker --version
aws --version

cd ..

$VERSION = Get-Content version.txt

cd ./docker

$env:AWS_PROFILE="dok_deploy"

aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 811687120814.dkr.ecr.us-west-2.amazonaws.com

docker push 811687120814.dkr.ecr.us-west-2.amazonaws.com/decks-of-keyforge:$VERSION

"All done now!"

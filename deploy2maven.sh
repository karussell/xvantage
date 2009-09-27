#mvn install:install-file -DgroupId=de.pannous.xvantage -DartifactId=xvantage-core -Dversion=0.8 -Dpackaging=jar -Dfile=dist/Xvantage.jar
mvn clean install -Dmaven.test.skip=true
mvn clean
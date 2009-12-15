#mvn install:install-file -DgroupId=de.pannous.xvantage -DartifactId=xvantage-core -Dversion=0.8 -Dpackaging=jar -Dfile=dist/Xvantage.jar
mvn clean install -Dmaven.test.skip=true
scp ~/.m2/repository/de/pannous/xvantage/xvantage-core/0.8/* peat_hal@web.sourceforge.net:/home/users/p/pe/peat_hal/userweb/htdocs/m2repository/de/pannous/xvantage/xvantage-core/0.8/
mvn clean
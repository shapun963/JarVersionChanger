# JarVersionChanger
A cli tool to change version of jar file .**Note this tool only changes the version related byte and does not change any other part of class file . This tool doesn't backport any bytecode.** This tool is pretty much useless except in some rare occasions like if you want to run a java 11 jar file and you have JRE 8 (This would would work in most cases as most of the language features are implemented at java compiler level. If it uses packages introduced in java 8+ it wont work.).

### Usage
```
java -jar jar_version_changer-all.jar path_of_jar -major 52
```

### Compiling and usage
```
cd path_of_project
./gradlew shadowJar 
java -jar  app/build/libs/app-all.jar app/build/libs/app-all.jar --major  52
```

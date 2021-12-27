# JarVersionChanger
A CLI tool to change version of jar file .**Note this tool only chages the version related byte and does not change any other part of class file .** This tool is pretty much useless except in some rare occasions like if you want to run a java 11 jar file and you have JRE 8 (This would would work in most cases as most of the language features are implemented at java compiler level. If it uses packages introduced in java 8+ it wont work.).

### Usage


```
java -jar JarVersionChanger.jar JarVersionChanger jar_path -major-version 52
```

# Cakes2

## NOTES
The constructors for both Alice and Bob have a maxSize parameter, in which you can customize to an arbitrary value.
Without this parameter, the program will continue running until it is manually stopped by the IDE.
The max size of the list is set to the same number as Tim's hunger variable, but again this can be changed to an arbitrary value.

##Bugs
task2.jar and task3.jar may throw exceptions when you run them via the command:

``
java -jar task2.jar
``


``
java -jar task3.jar
``

When you run these two commands, you may get the following exception:

``
Exception in thread "main" com.typesafe.config.ConfigException$Missing: No configuration setting found for key 'akka.remote.artery'
...
``

To run these jar files, you may have to create a new project in your IDE, convert the jar files to a .zip file 
and place the cakes directory inside the project's src folder.

## Instructions:
1. Import task2.jar and task3.jar as a project for IntelliJ.
2. Ensure that the jars file's libraries are added to the project's classpath. If not, right click on the jars directory and click "Add as Library... ".
3. Ensure that the akkaCakes, akkaUtils and dataCakes are inside the cakes package inside the src folder.

## Running The Program
### Task 2
Run the main method in Cakes.java as a Java application.

### Task 3
1. Establish an SSH connection with three other machines from the ECS Servers at VUW. Noteable examples incldue: greta-pt, barretts and embassy
2. Run the main method in Cakes.java as a Java application.
3. Ensure that between the four machines, ensure that the four Bob objects are copied and resided on each

Task 3 also contains the option to run either a single Bob object or four Bob objects. 
You have the option to comment out certain parts of the program to allow either Task 2 or Task 3 versions of the program to run.
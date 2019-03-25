Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat

Each Spring Boot server needs to be started in its own terminal/command prompt, replace X with A, B and C:
- Unix/Mac OSX: ./gradlew runPartyXServer
- Windows: gradlew.bat runPartyXServer
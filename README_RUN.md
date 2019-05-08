Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew clean deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat


step 1 
Originate Asset
- Console:
flow start OriginateAssetFlowInitiator assetContainer: {data: extra fresh, owner: PartyA, type: a lot of mango}, gps: {longitude: 10, latitude: 20}, obligation: {owner: PartyA, beneficiary: PartyB, amount: $100}
- Webserver
POST
http://localhost:12223/asset/create
body
{
	"data": "fruit",
    "owner": "O=PartyA,L=London,C=GB",
    "producerID": 1,
    "type": "excelent",
    "longitude": 23,
    "latitude": 23,
    "beneficiary": "O=PartyB,L=New York,C=US",
    "amount": 123
}

step 2
check states
- Console:
run vaultQuery contractStateType: com.template.states.AssetContainerState
run vaultQuery contractStateType: com.template.states.LocationState
run vaultQuery contractStateType: com.template.states.ObligationState
- Webserver
GET 
http://localhost:12223/asset/

take linear id from one of the state and put it into move flow

step 3
Transfer fruits
- Console
flow start MoveFlowInitiator linearId: 1028e3ba-525f-4c28-b56a-a7e06a691a13, longitude: 25.5, latitude: 55.5
- Webserver
POST 
http://localhost:12223/asset/move
body
{
	"longitude": 24,
    "latitude": 24,
    "linearId": "9ea75384-4c09-479b-b411-613f0de4e91d"
}


step 4
- Console
repeat step 2 and check states with new data
-Webserver
GET
http://localhost:12223/asset/trace?linearId=9ea75384-4c09-479b-b411-613f0de4e91d
http://localhost:12223/asset/trace-status?linearId=9ea75384-4c09-479b-b411-613f0de4e91d


- H2 Console (connection url at logs. Default username: sa, Default empty password)
http://localhost:12223/h2-console/
- Swagger
http://localhost:12223/swagger-ui.html

Each Spring Boot server needs to be started in its own terminal/command prompt, replace X with A, B and C:
- Unix/Mac OSX: ./gradlew runPartyXServer
- Windows: gradlew.bat runPartyXServer



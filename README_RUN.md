Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew clean deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat


step 1 
Originate Asset
- Console:
flow start OriginateAssetFlowInitiator assetContainer: {owner: PartyA, type: mango, producerID: 1, physicalContainerID: 2efeb496-f049-4fbd-934f-81e6c200a1aa}, gps: {longitude: 10, latitude: 20}, obligation: {owner: PartyA, beneficiary: PartyB, amount: $100}
- Webserver
POST
http://localhost:12223/asset/create
body
{
    "owner": "O=PartyA,L=London,C=GB",
    "producerID": 1,
    "physicalContainerID": "2efeb496-f049-4fbd-934f-81e6c200a1aa",
    "type": "mango",
    "longitude": 23,
    "latitude": 23,
    "beneficiary": "O=PartyB,L=New York,C=US",
    "amount": 123
}

step 2
check states
- Console:
run vaultQuery contractStateType: twinkle.agriledger.states.AssetContainerState
run vaultQuery contractStateType: twinkle.agriledger.states.LocationState
run vaultQuery contractStateType: twinkle.agriledger.states.ObligationState
- Webserver
GET 
http://localhost:12223/asset/

take linear id from one of the state and put it into move flow

step 3
Transfer fruits
- Console
flow start MoveFlowInitiator linearId: 5816bbe7-82f5-4cf4-b896-8ebdc3d4d3de, gps: {longitude: 78, latitude: 77}
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



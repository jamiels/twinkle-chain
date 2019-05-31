Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew clean deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat


step 1 
Originate Asset
- Console:
flow start OriginateAssetFlowInitiator assetContainer: {owner: PartyA, type: mango, producerID: 1, stage: Ready for Pickup}, gps: {longitude: 10, latitude: 20}, obligation: {owner: PartyA, beneficiary: PartyB, amount: $100}
- Webserver
POST
http://localhost:12223/asset/create
body
{
    "producerID": 1,
    "stage": "Ready for Pickup"
    "physicalContainerID": "2efeb496-f049-4fbd-934f-81e6c200a1ab",
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

step 3-1
- Console
flow start AssetNewStageFlow physicalContainerId: 02a27840-e09c-4f08-92e7-5d86aea83ac6, stage: Harvest Physical Handling
- Webserver
http://localhost:12223/asset/set-stage/d9587bcf-2d02-4e6d-8817-cbd9ac4eabc7/Harvest Physical Handling

step 3
Transfer fruits
- Console
flow start MoveFlowInitiator physicalContainerID: 4e9a9162-983d-4c8b-8d5f-5b91bf9e0b77, gps: {longitude: 78, latitude: 77}
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

step 5
Split assets
flow start SplitAssetContainerFlow physicalContainerID: 2050db30-6c20-4a5a-b7d5-d1fa365ae769, splitNumber: 20
                                                                 
step 6
flow start MergeAssetContainersFlow physicalContainerIDs: [602edb3f-9d54-4d29-94c8-f09aeb18192c, 058e4b58-c632-41a8-950c-693b1b5b88ed]


step 7
Finalize Asset
flow start FinalBuyerPurchaseContainerFlow linearId: 2050db30-6c20-4a5a-b7d5-d1fa365ae769






- H2 Console (connection url at logs. Default username: sa, Default empty password)
http://localhost:12223/h2-console/
- Swagger
http://localhost:12223/swagger-ui.html

Each Spring Boot server needs to be started in its own terminal/command prompt, replace X with A, B and C:
- Unix/Mac OSX: ./gradlew runPartyXServer
- Windows: gradlew.bat runPartyXServer



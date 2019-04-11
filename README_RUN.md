Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew clean deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat


step 1 
Originate Asset
flow start OriginateAssetFlowInitiator assetContainer: {data: extra fresh, owner: PartyA, type: a lot of mango}, gps: {longitude: 10, latitude: 20}, obligation: {owner: PartyA, beneficiary: PartyB, amount: $100}

or Run webserver and for example
POST
http://localhost:12223/create-asset
body
{
	"data": "fruit",
    "owner": "O=PartyA,L=London,C=GB",
    "type": "excelent",
    "longitude": 23,
    "latitude": 23,
    "beneficiary": "O=PartyB,L=New York,C=US",
    "amount": 123
}

step 2
check states
run vaultQuery contractStateType: com.template.states.AssetContainerState
run vaultQuery contractStateType: com.template.states.LocationState
run vaultQuery contractStateType: com.template.states.ObligationState

take linear id from one of the state and put it into move flow

step 3
Transfer fruits
flow start MoveFlowInitiator linearId: 1028e3ba-525f-4c28-b56a-a7e06a691a13, longitude: 25.5, latitude: 55.5

step 4
repeat step 2 and check states with new data



Each Spring Boot server needs to be started in its own terminal/command prompt, replace X with A, B and C:
- Unix/Mac OSX: ./gradlew runPartyXServer
- Windows: gradlew.bat runPartyXServer



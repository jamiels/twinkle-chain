Run the deployNodes Gradle task to build four nodes with our CorDapp already installed on them:
- Unix/Mac OSX: ./gradlew clean deployNodes
- Windows: gradlew.bat deployNodes

Start the nodes by running the following command from the root of the cordapp-example folder:
- Unix/Mac OSX: build/nodes/runnodes
- Windows: call build\nodes\runnodes.bat


step 1 
Originate Asset
flow start OriginateAssetFlowInitiator data: fresh, owner: PartyA, type: avocado, longitude: 10, latitude: 20, beneficiary: PartyB, amount: 100, currency: USD

step 2
check states
run vaultQuery contractStateType: com.template.states.AssetState
run vaultQuery contractStateType: com.template.states.LocationState
run vaultQuery contractStateType: com.template.states.ObligationState

take linear id from one of the state and put it into move flow

step 3
Transfer fruits
flow start MoveFlowInitiator linearId: 1e249f01-5602-4b9c-93f3-5ce25e933087, longitude: 25.5, latitude: 20

step 4
repeat step 2 and check states with new data



Each Spring Boot server needs to be started in its own terminal/command prompt, replace X with A, B and C:
- Unix/Mac OSX: ./gradlew runPartyXServer
- Windows: gradlew.bat runPartyXServer



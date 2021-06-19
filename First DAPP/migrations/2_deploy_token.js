var VToken = artifacts.require("./VToken.sol");
var Voting = artifacts.require("./Voting.sol");

module.exports = function(deployer) {
  deployer.deploy(VToken, "Test", "TST", 100000).then(
    DeployedContract =>{
      deployer.deploy(Voting, DeployedContract.address);
    }
  )
};

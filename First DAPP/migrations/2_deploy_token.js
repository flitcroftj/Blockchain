require('web3');

var VToken = artifacts.require("./VToken.sol");
var Voting = artifacts.require("./Voting.sol");
var Distribution = artifacts.require("./Distribution.sol");

module.exports = async function(deployer, network, accounts) {
  await deployer.deploy(VToken, "Virtual Token", "VTKN", 1000000);
  const token = await VToken.deployed();

  await deployer.deploy(Voting, token.address);

  await deployer.deploy(Distribution, 2, accounts[0], token.address);
  const crowdsale = await Distribution.deployed();

  token.transfer(crowdsale.address, await token.totalSupply());
};

// SPDX-License-Identifier: MIT

pragma solidity >=0.7.0 <0.9.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract VToken is Ownable,ERC20 {
   
    uint256 public _maxSupply;

    constructor (
        string memory name_,
        string memory symbol_,
        uint256 maxSupply_
    ) ERC20(name_, symbol_) {
        _maxSupply = maxSupply_ * (10 ** uint256(decimals()));
    }
   
    function buyToken() external payable {
        _mint(msg.sender, msg.value * 1000); //what is msg.value represent?
    }
   
    function getEthFromSale() public onlyOwner {
        payable(owner()).transfer(address(this).balance);
    }
   
    function killSale() public onlyOwner saleComplete {
        selfdestruct(payable(owner()));
    }
   
    function mintRewards(address to, uint rewards) public onlyOwner {
        _mint(to, rewards);
    }
   
    modifier saleComplete() {
        require(_maxSupply == totalSupply(), "Sale not complete, function cannot be executed");
        _;
    }
   
    function _beforeTokenTransfer(
        address from,
        address to,
        uint256 amount
    ) internal override {
        //_burn(from, amount/50);
    }
   
    receive() external payable{
        revert();
    }
   
    fallback() external {
        revert();
    }
}
// SPDX-License-Identifier: MIT

pragma solidity >=0.7.0 <0.9.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";

contract VToken is ERC20 {
   
    uint256 immutable private _cap;

    constructor (
        string memory name_,
        string memory symbol_,
        uint256 cap_
    ) ERC20(name_, symbol_) {
        require(cap_ > 0, "ERC20Capped: cap is 0");
        _cap = cap_ * (10 ** uint256(decimals()));
        _mint(msg.sender, cap_ * 10 ** uint256(decimals()));
    }

    /**
     * @dev Returns the cap on the token's total supply.
     */
    function cap() public view virtual returns (uint256) {
        return _cap;
    }
   
    /*
    * Only here for curiosity or possibly adding hooks
    */
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
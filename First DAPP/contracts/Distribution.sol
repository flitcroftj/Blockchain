// SPDX-License-Identifier: MIT

pragma solidity >=0.7.0 <0.9.0;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/math/SafeMath.sol";
import "./VToken.sol";
import {Strings} from "@openzeppelin/contracts/utils/Strings.sol";

contract Distribution is Ownable {

    using SafeMath for uint256;

    VToken public _token;
    address payable public _wallet;
    uint256 public _rate;
    uint256 public _weiRaised;

    /**
    * Event for token purchase logging
    * @param purchaser who paid for the tokens
    * @param beneficiary who got the tokens
    * @param value weis paid for purchase
    * @param amount amount of tokens purchased
    */
    event TokenPurchase(
        address indexed purchaser,
        address indexed beneficiary,
        uint256 value,
        uint256 amount
    );

    /*
    * Don't call any function on fallback
    * just report the incorrect function call
    */
    event incorrectFunCall(
        address indexed caller,
        uint256 amount,
        string reason
    );

    /**
    * @param rate_ Number of token units a buyer gets per wei
    * @param wallet_ Address where collected funds will be forwarded to
    * @param token_ Address of the token being sold
    */
    constructor(uint256 rate_, address wallet_, address token_) {
        require(rate_ > 0);
        require(wallet_ != address(0));
        require(address(token_) != address(0));

        _rate = rate_;
        _wallet = payable(wallet_);
        _token = VToken(payable(token_));
    }

    /**
    * @dev low level token purchase
    * @param _beneficiary Address performing the token purchase
    */
    function buyTokens(address _beneficiary) public payable {

        uint256 weiAmount = msg.value;
        _preValidatePurchase(_beneficiary, weiAmount);

        // calculate token amount to be created
        uint256 tokens = _getTokenAmount(weiAmount);

        // ensure contract has enough tokens to sell
        uint256 balance = _token.balanceOf(address(this));
        string memory notEnoughBalance = concat("Contract can only sell ", (Strings.toString(balance)));
        require(balance >= tokens, notEnoughBalance);

        // update state
        _weiRaised = _weiRaised.add(weiAmount);

        _processPurchase(_beneficiary, tokens);
        emit TokenPurchase(
        msg.sender,
        _beneficiary,
        weiAmount,
        tokens
        );

        _forwardFunds();
    }

    // -----------------------------------------
    // Internal interface (extensible)
    // -----------------------------------------

    /**
    * @dev Validation of an incoming purchase. Use require statements to revert state when conditions are not met. Use super to concatenate validations.
    * @param _beneficiary Address performing the token purchase
    * @param _weiAmount Value in wei involved in the purchase
    */
    function _preValidatePurchase(
        address _beneficiary,
        uint256 _weiAmount
    )
        internal pure
    {
        require(_beneficiary != address(0));
        require(_weiAmount != 0);
    }

    /**
    * @dev Source of tokens. Override this method to modify the way in which the crowdsale ultimately gets and sends its tokens.
    * @param _beneficiary Address performing the token purchase
    * @param _tokenAmount Number of tokens to be emitted
    */
    function _deliverTokens(
        address _beneficiary,
        uint256 _tokenAmount
    )
        internal
    {
        _token.transfer(_beneficiary, _tokenAmount);
    }

    /**
    * @dev Executed when a purchase has been validated and is ready to be executed. Not necessarily emits/sends tokens.
    * @param _beneficiary Address receiving the tokens
    * @param _tokenAmount Number of tokens to be purchased
    */
    function _processPurchase(
        address _beneficiary,
        uint256 _tokenAmount
    )
        internal
    {
        _deliverTokens(_beneficiary, _tokenAmount);
    }

    /**
    * @dev Override to extend the way in which ether is converted to tokens.
    * @param _weiAmount Value in wei to be converted into tokens
    * @return Number of tokens that can be purchased with the specified _weiAmount
    */
    function _getTokenAmount(uint256 _weiAmount)
        internal view returns (uint256)
    {
        return _weiAmount.mul(_rate);
    }

    // -----------------------------------------
    // Sale completion commands
    // -----------------------------------------

    /**
    * @dev Determines how ETH is stored/forwarded on purchases.
    */
    function _forwardFunds() internal {
        _wallet.transfer(msg.value);
    }

    /**
     * Should not be necessary, but in case some eth is left in contract
     */
    function getEthFromSale() external onlyOwner {
        payable(owner()).transfer(address(this).balance);
    }

    /**
     * If there is less than 0.1% of total tokens remaining, send to 
     * contract owner so the distribution contract may be destroyed
     */
    function finishSale() external onlyOwner saleNearComplete {
        _deliverTokens(owner(), _token.balanceOf(address(this)));
    }

    modifier saleNearComplete() {
        require(_token.cap().div(1000) > _token.balanceOf(address(this)));
        _;
    }

    modifier saleComplete() {
        require(_token.balanceOf(address(this)) == 0, "Sale not complete, function cannot be executed");
        _;
    }

    function killSale() public onlyOwner saleComplete {
        selfdestruct(payable(owner()));
    }

    receive() external payable{
        emit incorrectFunCall(
            msg.sender,
            msg.value,
            "Incorrect function called, reverting state and payment"
        );
        revert();
    }
   
    fallback() external {
        emit incorrectFunCall(
            msg.sender,
            0,
            "Incorrect function called, reverting state"
        );
        revert();
    }

    // -----------------------------------------
    // Helper functions for basic manipulation
    // -----------------------------------------

    function concat(
        string memory a,
        string memory b)
        internal 
        pure
        returns(string memory) {
            return string(abi.encodePacked(a, b));
    }

}
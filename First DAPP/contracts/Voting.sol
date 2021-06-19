// SPDX-License-Identifier: MIT

pragma solidity >=0.7.0 <0.9.0;

import "./VToken.sol";

contract Voting {
   
    struct Proposal {
        bytes32 name;
        uint voteYes;
        uint voteNo;
    }
   
    struct Voter {
        uint power;
        bool voted;
        bool votedYes;
    }
   
    address public _proposer;
    address public _tokenAddr;
    uint public _openTime;
    mapping(address => Voter) public _voters;
    address[] private yesVoters;
    address[] private noVoters;
    Proposal public _proposal;
    bool private _rewardsGiven;
   
    constructor (
        address tokenAddr_
    ) {
        _tokenAddr = tokenAddr_;
        _openTime = block.timestamp;
        _proposer = msg.sender;
        _proposal = Proposal({name: "TEST", voteYes: 0, voteNo: 0});
        _rewardsGiven = false;
    }
   
    function addVoter() public {
        VToken token = VToken(payable(_tokenAddr));
        _voters[msg.sender] = Voter({
            power: token.balanceOf(msg.sender),
            voted: false,
            votedYes: false
        });
    }
   
    function vote(bool yes) public {
        Voter storage sender = _voters[msg.sender];
        require(sender.power >= 1000 * 10 ** 18, "Voter does not have 1000 tokens");
        require(!sender.voted, "Sender has already voted on the proposal");
        sender.voted = true;
        sender.votedYes = yes;
        if (yes){
            _proposal.voteYes += sender.power;
            yesVoters.push(msg.sender);
        } else {
            _proposal.voteNo += sender.power;
            noVoters.push(msg.sender);
        }
    }
   
    function proposalWin() public voteComplete view returns(bool) {
        return _proposal.voteYes > _proposal.voteNo;
    }
   
    function giveRewards() public voteComplete rewardsNotGiven {
        _rewardsGiven = true;
        if (proposalWin()) {
            for (uint i = 0; i < yesVoters.length; i++) {
                //add 1 percent of tokens somehow
            }
        } else {
            for (uint i = 0; i < yesVoters.length; i++) {
                //add 1 percent of tokens somehow
            }
        }
    }
   
    modifier rewardsNotGiven() {
        require(!_rewardsGiven);
        _;
    }
   
    modifier voteComplete() {
        require(_proposal.voteYes + _proposal.voteNo > 50000 * 10**18);
        //require that sufficient time has passed
        _;
    }
   
}
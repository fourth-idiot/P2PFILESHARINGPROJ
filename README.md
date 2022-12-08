# P2PFILESHARINGPROJ

## GROUP MEMBERS
- Nikhil Mukesh Saoji
- Gauri Pandharinath Bodke
- Prasad Ramakanth Hadkar

## Project Description and working

1. Peer processs reads all configs and starts the peer process
2. There is a peer server and peer client, here the server keeps on listening for clients and the client makes connection with servers started till now.These server and client form the connection endpoints. 
3. After the endpoint is created, handshake message is sent to other peers via the endpoint and then bitfield is sent.
4. Now, if any interested piece is present in the bitfield, then the peer send the INTERESTED message.
5. There are schedulers to select the preferred and the optimistic neighbors.
6. The UNCHOKE message is sent to these selected neighbors and CHOKE message is sent to the unselected remaining neighbors.
7. The next step is to check if the unchoked peer has any intereseted piece and if it has then it REQUEST for the interested piece.
8. Now, when a piece is received and if the neighbor is unchoked then it sends the PIECE message. Also, when the piece is received the bitfield is updated and it sends the HAVE message.
9. If any of the peer receives the have message and doesnot have any interested pieces left, it sends the NOT INTERESTED message otherwise REQUEST for next piece.
10. Finally if a peer gets all the pieces, it joins all the pieces to get complete file.

## Project Highlights
- All peer processes start correctly 
- Connection is established between peers by TCP handshake
- Successfull exchange of bitfield message
- Sending of INTERESTED and NOT INTERESTED message
- Accurate sending of k UNCHOKE and CHOKE messages every p seconds
- Optimistic neighbors selection after every m seconds
- Successful exchange of REQUEST and HAVE messages
- Successfull exchange of PIECE 
- Termination after all peers receive the complete file


## How to Run

## Project Contributions
- Nikhil Mukesh Saoji

- Gauri Pandharinath Bodke
    - Worked on the project structure and implemented the switch case functions like interested, not interested, have, request, piece and their related helper functions.

- Prasad Ramakanth Hadkar
    - Collaborated in projected design and then implemented schedulers which choose k preferred neighbors as per the download rate in every interval(p seconds)and choose optimistically unchoked neighbors and send choke and unchoke messages accordingly. Also wrote scheduler to ask for a certain piece after a timeout if piece request was not fulfiled.



## Demo link
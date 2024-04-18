# COSC 650 Project - UDP Client-Server Communication

## Project Overview

This project implements a basic UDP client-server application in Java, where the server, named `AmudaServer`, fetches data from a specified web server and sends it to the client, named `AmudaClient`, in packets. This simulation includes handling of packet sequencing, loss, and retransmission under timeout conditions.

### Features

- **UDP Communication**: Utilizes Java sockets for UDP communication between client and server.
- **Concurrency**: `AmudaServer` handles multiple client requests concurrently.
- **Error Handling**: Manages packet loss and ensures data integrity through packet sequencing.
- **Timeout Management**: Implements timeout for packet transmission and retransmission.

## Installation

### Prerequisites

### Ensure you have Java installed on your system. This project was developed using Java 11. You can check your Java version by running:

```bash
java -version


# if Java is not installed, you can install it on Ubuntu by running:

bash
Copy code
sudo apt update
sudo apt install default-jdk
Download
Clone the repository to your local machine:

bash
Copy code
git clone https://github.com/yourusername/AmudaCOSC650Project.git
cd AmudaCOSC650Project

#Compilation
#Compile the server and client Java programs with the following commands:

javac AmudaServer.java
javac AmudaClient.java

#Running the Code
#Running the Server
#To start the server, open a terminal and execute:

java AmudaServer

#Running the Client
#To start the client, open a new terminal window and execute:


java AmudaClient

#Follow the on-screen prompts to enter the web server URL and the timeout value.



Usage
AmudaClient initiates communication by sending a request containing a web server URL and a timer value to AmudaServer. The server fetches the requested data, breaks it into packets, and sends these packets back to the client. The client receives and assembles these packets, managing potential packet loss or ordering issues. If all packets are received before the timer expires, it will display the data and a message "OK". Otherwise, it will show "FAIL".

Contributing
Contributions to this project are welcome! Here are a few ways you can help:

Report bugs.
Add new features.
Improve the existing code to handle more edge cases.
Please feel free to fork the repository and submit pull requests.

License
This project is licensed under the MIT License - see the LICENSE.md file for details.

Acknowledgments
Special thanks to the COSC 650 course staff and classmates for their support and suggestions.
Inspired by common networking challenges and real-world applications of client-server architectures.

### Notes:
- **Repository URL**: Replace `https://github.com/binwakil/AmudaCOSC650Project.git` with the actual URL of your GitHub repository.
- **File Names**: Ensure the Java source files in your project directory are correctly named `AmudaServer.java` and `AmudaClient.java`.
- **Customization**: You may need to adjust paths, dependencies, or other specific details according to your project setup.

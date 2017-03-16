Install Pi4j on your Raspberry platform

Information on http://pi4j.com

Connect your Raspberry to the the Internet
Login to your Raspberry.
Execute the following command:

curl -s get.pi4j.com | sudo bash

This method will download and launch an installation script that perform the following steps:

adds the Pi4J APT repository to the local APT repositories
downloads and installs the Pi4J GPG public key for signature validation
invokes the 'apt-get update' command on the Pi4J APT repository to update the local package database
invokes the 'apt-get install pi4j' command to perform the download and installation


Upgrade
Execute the following command:

curl -s get.pi4j.com | sudo bash

If you wish to force an upgrade of the Pi4J package only.

sudo apt-get install pi4j or pi4j --update

Then open menu Preferences > Raspberry Pi Configuration
Select Tab Interfaces:
Only Enable SSH:  SPI: and Serial:
Click on OK

The Raspberry is now ready to work with a Lora board using the SX1276
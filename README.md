# LoraRaspberryPiDragino
Simple Lora Gateway and Lora Node on RaspberryPi using the Dragino Lora/GPS HAT and Java(pi4j)

There are a lot of C++ examples but I rather use Java to communicate with the sx1276 on the Dragino Hat.

Most C++ examples used different pin setting.
So I did some rewiring on the Lora/GPS HAT

Lora/GPS HAT <--> RaspBerryPi

3.3v <--> 3.3v

5v <--> 5v

GND <--> GND

DIO0 <--> GPI07

DIO1 <--> 16

DIO2 <--> 17

RX <--> GPIO15/TX

TX <--> GPIO15/RX

RESET <--> GPIO0

NSS <--> 24

MISO <--> 21

MOSI <--> 19

SCK <--> 23

All java code is experimental.

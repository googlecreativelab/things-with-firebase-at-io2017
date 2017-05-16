Quick, Draw! Multiplayer Podiums
===

Code to control the contestant podiums for the Quick, Draw! experience at Google I/O 2017.

There are two main components: an [Android Things app](app/) that runs on a Raspberry Pi and an [Arduino sketch](arduino/) that actually drives the lights.

The code running on the Raspberry Pi listens to a Firebase database for commands and passes them along (via serial) to an Arduino which in turn controls the LED lights in the contestant podiums.


<table>
  <tr>
    <td>
      This project is no longer actively maintained by the Google Creative Lab but remains here in a read-only Archive mode so that it can continue to assist developers that may find the examples helpful. We aren’t able to address all pull requests or bug reports but outstanding issues will remain in read-only mode for reference purposes. Also, please note that some of the dependencies may not be up to date and there hasn’t been any QA done in a while so your mileage may vary.
      <br><br>
      For more details on how Archiving affects Github repositories see <a href="https://docs.github.com/en/github/creating-cloning-and-archiving-repositories/about-archiving-repositories">this documentation </a>.
      <br><br>
      <b>We welcome users to fork this repository</b> should there be more useful, community-driven efforts that can help continue what this project began.
    </td>
  </tr>
</table>


Things &amp; Firebase
===

While building our Experiments Tent for Google I/O 2017, we realized that 
on-screen controls were <i>so</i> 2016. 

So we built a couple things. This repository contains the code that powers the knobs 
and sliders controlling The Spirit, as well as the LED lights in the podiums for our
Quick, Draw! multiplayer experience.

The Spirit
---

![google i/o 2017](controller-app/imgs/spirit-1.jpg)

The Spirit controls, located in [controller-app/](controller-app/), uses a newly 
created driver for Android Things, the [ADCV2x analog controller](). It reads
data from the sliders and dials and updates a
[Firebase Realtime Database](https://firebase.google.com/docs/database/). 
  
To make it even easier for developers, we created a simple library named 
[dat.fire](https://github.com/googlecreativelab/dat.fire), which connects 
[dat.gui](https://github.com/dataarts/dat.gui) controllers to any Firebase database 
in just a few lines of code. 

Check out [controller-app/](controller-app/) for a more detailed look, and look at this fork
of The Spirit, [here](https://github.com/trippedout/The-Spirit/blob/master/src/index.js#L55) 
and [here](https://github.com/trippedout/The-Spirit/blob/master/src/index.js#L185-L191), 
where we implemented [dat.fire](https://github.com/googlecreativelab/dat.fire).

Quick, Draw! Multiplayer
---

![google i/o 2017](quickdraw-podiums/imgs/io-setup.jpg)

The contestant podiums for Quick, Draw! function in the opposite direction.
 
Rather than send data out, they listen for Firebase Realtime Database value change
events, and update their state accordingly. This was especially easy to implement
since Quick, Draw! was <i>already</i> using Firebase. 

They were way ahead of their time. #trendsetters

Check out the implementation details in [quickdraw-podiums/](quickdraw-podiums/)

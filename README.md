# Android Thermal Monitor

## Description
Android application to monitor temperatures and CPU clock of the SoC and other components as an overlay.

## Examples
<p float="left">
<img src="https://i.imgur.com/ZjNq5Vh.png" width="300"/>
<img src="https://i.imgur.com/8mYo6Ym.png" width="300"/>
<img src="https://i.imgur.com/SnPomPQ.png" width="300"/>
</p>
<img src="https://i.imgur.com/nvf9NTi.png" width="600"/>

### Video demo
[![Video demo](http://img.youtube.com/vi/53mw3wq6m5I/0.jpg)](http://www.youtube.com/watch?v=53mw3wq6m5I)

## Features
* Monitor thermals of different components
* Monitor CPU core frequencies
* Display this information as an overlay over other apps
* Works without root (where available)
* Supports using root as a fallback
* Customizable overlay

## Device support
The exact device support can vary greatly, some devices don't allow access to thermal sensors without root, and some devices don't even expose them at all.

The app has been tested on the following devices:
Device | Support | Details | Software
------ | ------- | ------- | --------
Amazon Fire 7 (2017) | ✔ | Everything works | Android 5.1.1 (Lineage 12.1-20181218-UNOFFICIAL-austin)
Bq Aquaris U Plus | ✔ | Everything works | Android 7.1.1 (2.8.0_20180625-1854)
LG Nexus 5 | ⚠  | Root required  for thermal monitoring | Android 7.1.2 (Lineage 14.1-20180726-NIGHTLY-hammerhead )
Motorola Moto G (2nd Gen) | ✔ | Everything works | Android 7.1.2 (Lineage 14.1-20180729-NIGHTLY-thea)
OnePlus 3 | ✔ | Everything works | Android 8.0.0 (ONEPLUS A3003_16_180914)
Samsung Galaxy Nexus | ❌ | Thermal sensor has different path (Monitoring CPU frequency is supported) | Android 6.0.1 (Lineage 13.0-20180610-2353-UNOFFICAL-maguro)
Samsung Nexus 10 | ❌ | Thermal sensors not exposed (Monitoring CPU frequency is supported) | Android 5.1.1 (LMY49J)
Sony Xperia Z | ✔ | Everything works | Android 5.1.1 (10.7.A.0.228)

## Implementation details
Thermal data and CPU frequencies are read through the Linux sysfs.
The root component uses Java and is implemented using [libRootJava](https://github.com/Chainfire/librootjava).

## Building
Application can be built using Gradle and Android Studio.


# Motoman
![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/425276_10201474466443234_450159264_n.jpg)

This is an android motorcycle racing game developed by myself alone during mid 2013.
This repository is the result of 1 month of basic development and 1 month of fine tuning.
The project is only build on top of a cross platform OpenGL wrapper, [libgdx](https://github.com/libgdx/libgdx), no game engine is based on.
All 3d models, audio, game maths is completed on my own.
Blender was used to create the 3d models.

### Features are:
- Random seed generated racing tracks, unlimited tracks as you progress the game.
- Incoming corner type notification like rally car games.
- Active camera that looks into the incoming corner apex.
- Two seperated steering control, counter steering and leaning.
- Supports using tilting to lean (disabled in source).
- Supports simple traditional steering by combining the two steering methods.
- A physical strength system to reduce the effectiveness of steering if too much action inputed in a row.
- GLSL shader effects.

### Development screenshots:
![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/603757_10201266638567667_1167871821_n.jpg)

![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/538038_10201266638527666_537886606_n.jpg)

![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/37046_10201266638487665_36700105_n.jpg)

![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/913734_10201266638447664_1988336069_o.jpg)

![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/37046_10201266638487665_36700105_n.jpg)

![screenshot](https://raw.githubusercontent.com/mammalwong/motoman/master/screenshots/936192_10201317985131299_43361273_n.jpg)

### Youtube demos:
[Demo on PC](https://www.youtube.com/watch?v=eoEkVXiIgFU)

[Demo on Mobile](https://www.youtube.com/watch?v=AR22-CrtBdw)

### Prebuilt android APK:
[Prebuilt latest version with all shader effect and simple steering control](https://github.com/mammalwong/motoman/blob/master/motoman-android/bin/motoman-android.apk)

[Prebuilt old version with shader effect off and separated steering control](https://github.com/mammalwong/motoman/blob/master/motoman-android/motoman-android.apk)

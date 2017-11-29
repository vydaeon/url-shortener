# url-shortener
Simple URL-shortener app using [Lagom Framework](https://www.lagomframework.com/)

## To Run (Locally)
1. Install Software
   1. (Optional) [SDKMAN](http://sdkman.io/)
   1. JDK 8.x
   1. Scala 2.11.8+ (2.11.x)
   1. SBT 0.13.15+ (0.13.x)
1. Run `sbt runAll` from terminal
1. Open `front-end/index.html` in browser to use
1. Hit `Enter` in terminal to shut down when finished

## To Develop
1. Install Software
   1. Same as "To Run" software list above, plus:
   1. one of Scala IDE, Eclipse with Scala plugin, or IntelliJ with Scala plugin
1. For Scala IDE / Eclipse:
   1. Run `sbt eclipse` from terminal
   1. From IDE workspace:
      1. "File" menu > "Import..." and select "Projects from Folder or Archive"
      1. Select project root directory, and ensure nested projects are detected & selected
   1. Ensure each nested project uses Scala 2.11.x (right-click > Scala > Set the Scala Installation)
1. For IntelliJ, should be able to import project/modules from the build.sbt file (untested)

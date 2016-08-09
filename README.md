# Teleoperate_ROS
Install Java JDK 8
1. sudo add-apt-repository ppa:webupd8team/java
2. sudo apt-get update
3. sudo apt-get install oracle-java8-installer

Install Android Studio:
1. sudo add-apt-repository ppa:paolorotolo/android-studio
2. sudo apt-get update
3. sudo apt-get install android-studio

Update Android studio from inside Android studio and install sdk 10, 13, 15 & 18
Put android sdk folder in /home, not int /opt/..(need write permission)

To compile extra dependencies( like those in build.gradle(Module: app)):
1. Go to https://github.com/rosjava/rosjava_mvn_repo and find your desired dependency
2. Follow the structure for the already added dependecies in build.gradle(Module: app)
Note that messages (e.g. move_base_msgs) are located under rosjava_mvn_repo/org/ros/rosjava_messages





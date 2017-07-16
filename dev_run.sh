#!/bin/sh
java -Xms10M -Xmx500M -XX:+UseG1GC -server -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:MaxGCPauseMillis=2000 -Xverify:none -cp build/classes/main:build/libs/bluestone-1.0-SNAPSHOT.jar:build/libs/bluestone-1.0-SNAPSHOT-all.jar -Dapple.awt.UIElement=true com.khronodragon.bluestone.Start

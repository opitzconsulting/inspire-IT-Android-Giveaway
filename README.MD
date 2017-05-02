# inspire|IT Giveaway App

Diese App stellt eine Beispielanwendung zur Nutzung von Bluetooth Beacons unter Android dar.
Mit Hilfe der App kann gezählt werden wie häufig ein Ort besucht wurde.
Dazu wird ein Beacon an dem Ort platziert, die App zählt dann wie oft dieser Ort besucht wurde.
Ein Beispiel dazu ist die Anzahl der Besuche einer Kaffeemaschiene.
Enstanden ist sie im Rahmen der inspire|IT Konferenz als Zusatz zu den dort verteilten Beacons.


## Vorrausetzungen
Diese App setzt ein BLE (Bluetooth Low Energy) fähiges Smartphone mit Android 4.3 oder höher vorraus. 
Zudem wird für die Nutzung ein Bluetooth Beacon, welches im iBeacon Format sendet, benötigt.


## Installation

Das Projekt lässt sich in Android Studio bauen und installieren oder über den Play Store herunterladen.
(Link folgt)

## Benutzung

In den Einstellungen der App lassen sich folgende Konfigurationen vornehmen:

* Bezeichnung: Hier kann ein Name für den Zähler festgelegt werden.
* BeaconId: Die MinorId des Beacons.
* Zeit: Zeit in Sekunden, die mindestens vergehen muss bis der Zähler wieder hochzählt.
* Distanz: Wird diese Distanz unterschritten, löst der Zähler aus.
* Scannen im Hintergrund: Es gibt die Möglichkeit den Scanner auch im Hintergrund aktiv zu halten, um Besuche auch dann zu erfassen, wenn die App nicht im Vordergrund ist.
* Scannintervall (Hintergrund): Abstand in Sekunden, in dem nach Beacons gesucht wird, wenn die App im Hintergrund aktiv ist. Die Standardeinstellung liegt bei 30s.
* Scannintervall (Vordergrund): Abstand in Sekunden, in dem nach Beacons gesucht wird, wenn die App aktiv ist. Die Standardeinstellung liegt bei 1s.



## Hacking


Die Anwendung bietet bereits einige Einstellungsmöglichkeiten.
Für weitere Informationen zu der Nutzung von Bluetooth Beacons lohnt sich ein Blick in die 
Android Beacon Library, die zur Kommunikation mit den Beacons verwendet wird.

	https://altbeacon.github.io/android-beacon-library/index.html




## Lizenz 

Für diese Anwendung gilt die MIT Lizenz.
	
Es werden folgende Open Source Bibliotheken verwendet:

* Android Beacon Library: Apache2 Lizenz

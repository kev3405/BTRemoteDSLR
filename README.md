BTRemoteDSLR
============

Fernsteuerung eine Canon DSLR (Stereo Klinkenstecker) über ein Bundle aus einen Arduino mit Bluetooth und einer Android App.

Protokoll
---------------------------------------------------------------------
Es werden 3 Byte übertragen.
* 1. Byte: Zeitintervall
* 2. & 3. Byte. Anzahl der Auslösungen

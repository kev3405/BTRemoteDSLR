BTRemoteDSLR
============

Fernsteuerung eine Canon DSLR (Stereo Klinkenstecker) über ein Bundle aus einen Arduino mit Bluetooth und einer Android App.

Protokoll
---------------------------------------------------------------------
Es werden 4 Byte übertragen.
* 1. Byte: Optionen (0x1 - Fokusieren?)
* 2. Byte: Zeitintervall
* 3. & 4. Byte. Anzahl der Auslösungen

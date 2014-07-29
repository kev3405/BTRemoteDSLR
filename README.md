BTRemoteDSLR
============

Fernsteuerung eine Canon DSLR (Stereo Klinkenstecker) über ein Bundle aus einen Arduino mit Bluetooth und einer Android App.

**Protokoll**
Es werden stets 4 Byte übertragen. Dies dient einer einfachen Schnittstellenimplementierung!
1. Byte: Definition des Inhaltes
3. - 4. Byte. Inhalt

*** bisherige Festlegungen ***
* allgemeine Einstellungen
	- 1. Byte: 0x1
	- 2. - 4. Byte: 0x1 (Log aktiv) / 0x0 (Log inaktiv)
* 1. Modus
	- 1. Byte: 0x2
	- 2. Byte: Zeitintervall (1 - 255 Sekunden)
	- 3. - 4. Byte: Anzahl der Bilder (0x0 = keine Begrenzung)
* 2. Modus
	- 1. Byte: 0x3
	- 2. - 4. Byte: Zeit in Sekunden bis zum Auslösen
* aktuellen Auftrage beenden:
	- 1. Byte: 0xFF
	- 2. - 4. Byte: nicht definiert

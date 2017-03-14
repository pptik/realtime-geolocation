# realtime-geolocation
* Realtime osmdroid marker positioning based on geolocation

Menampilkan lokasi dari GPS-Tracker device yang dipasang dikendaraan pada OSMDroid Map (`OSM Bonuspack`) secara realtime.

### Skema

* GPS-Tracker mengirmkan data `lokasi`, `kecepatan`, `tanggal & jam` dan `mac address` ke `AMQP`/`MQTT` server via `topic`.

* Service `consume` ke `topic` dimana data dari GPS-Tracker dikirim via `MQTT` dan mengolahnya ke `database`.

* Service `publish` via `fanout` dan mengirimkan data GPS-Tracker setiap `n` detik

* `Android Client` consume ke `fanout` dan mengolah setiap data yang diterima kemudian menampilkannya di `OSMDroid`

## SS

![alt tag](https://github.com/pptik/realtime-geolocation/blob/master/01.gif?raw=true)

![alt tag](https://github.com/pptik/realtime-geolocation/blob/master/02.gif?raw=true)

# realtime-geolocation
* Realtime osmdroid marker positioning based on geolocation

Menampilkan lokasi dari GPS-Tracker device yang dipasang dikendaraan pada OSMDroid Map (`OSM Bonuspack`) secara realtime.

### Skema

* GPS-Tracker mengirmkan data `lokasi`, `kecepatan`, `tanggal & jam` dan `mac address` ke `AMQP`/`MQTT` server via `topic`.

* Service `consume` ke `topic` dimana data dari GPS-Tracker dikirim via `MQTT` dan mengolahnya ke `database`.

* Service `publish` via `fanout` dan mengirimkan data GPS-Tracker setiap `n` detik

* `Android Client` consume ke `fanout` dan mengolah setiap data yang diterima kemudian menampilkannya di `OSMDroid`

## JSON MSG (for test)

```json
{
	"success": true,
	"data": [{
		"_id": "588a0d242ca4be3698424d63",
		"Mac": "60: 1:94: 0:51:C8",
		"Speed": 0,
		"Date": "17/02/2017",
		"Time": "11:14:15",
		"Data": [-6.886998, 107.608222],
		"Lokasi": "Sasana Budaya Ganesha, Jalan Siliwangi, Hegarmanah, Jawa Barat, 40132, Indonesia",
		"Keterangan": "GPS Tracker Test"
	}, {
		"_id": "58a5c220139f4b626cf7172d",
		"Mac": "5C:CF:7F:1D:B9:2C",
		"Speed": 10.96,
		"Date": "21/02/2017",
		"Time": "20:34:11",
		"Data": [-5.378157, 105.251014],
		"Lokasi": "Dunkin' Donuts, Zainal Abidin Pagar Alam, Labuhan Ratu, Lampung, 35119, Indonesia",
		"Keterangan": "GPS Tracker 06"
	}, {
		"_id": "58ad0ae6139f4b626cf71870",
		"Mac": "60: 1:94: B:A5:E0",
		"Speed": 0,
		"Date": "02/03/2017",
		"Time": "09:55:09",
		"Data": [-6.88676, 107.608757],
		"Lokasi": "Sasana Budaya Ganesha, Jalan Siliwangi, Hegarmanah, Jawa Barat, 40132, Indonesia",
		"Keterangan": "GPS Tracker 007"
	}, {
		"_id": "58bf7a42139f4b626cf71ad6",
		"Mac": "5C:CF:7F:1D:A1:AE",
		"Speed": 0.15,
		"Date": "13/03/2017",
		"Time": "25:32:03",
		"Data": [-6.88707, 107.623451],
		"Lokasi": "Jalan Tubagus Ismail, Dago, Jawa Barat, 40135, Indonesia",
		"Keterangan": "Angkot D1979AE Sadang Serang - Caringin"
	}]
}
```

## SS

![alt tag](https://github.com/pptik/realtime-geolocation/blob/master/01.gif?raw=true)

![alt tag](https://github.com/pptik/realtime-geolocation/blob/master/02.gif?raw=true)

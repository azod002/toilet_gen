package com.toiletgen.feature.entertainment.data

data class RadioStation(
    val name: String,
    val genre: String,
    val streamUrl: String,
)

val radioStations = listOf(
    RadioStation("Европа Плюс", "Поп", "https://ep128.hostingradio.ru:8030/europaplus128.mp3"),
    RadioStation("Русское Радио", "Поп", "https://rusradio.hostingradio.ru/rusradio128.mp3"),
    RadioStation("Радио Energy", "Dance", "https://nrj128.hostingradio.ru:8030/nrj128.mp3"),
    RadioStation("Ретро FM", "Ретро", "https://retro128.hostingradio.ru:8030/retrofm128.mp3"),
    RadioStation("DFM", "Dance", "https://dfm128.hostingradio.ru:8030/dfm128.mp3"),
    RadioStation("Радио Шансон", "Шансон", "https://chanson128.hostingradio.ru:8030/chanson128.mp3"),
    RadioStation("Юмор FM", "Юмор", "https://humor128.hostingradio.ru:8030/humorfm128.mp3"),
    RadioStation("Радио Дача", "Поп", "https://dacha128.hostingradio.ru:8030/dacha128.mp3"),
)

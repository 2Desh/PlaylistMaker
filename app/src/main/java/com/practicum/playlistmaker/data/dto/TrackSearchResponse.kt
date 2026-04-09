package com.practicum.playlistmaker.data.dto

// список найденных треков, приходящий от тунца
class TrackSearchResponse(
    val results: List<TrackDto>
) : Response()
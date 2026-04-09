package nicholos.tyler.dugout.data.api

import nicholos.tyler.dugout.data.api.dto.GameFeedResponseDto
import nicholos.tyler.dugout.data.api.dto.RosterResponseDto
import nicholos.tyler.dugout.data.api.dto.ScheduleResponseDto
import nicholos.tyler.dugout.data.api.dto.StandingsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MlbApiService {

    @GET("api/v1/schedule")
    suspend fun getSchedule(
        @Query("sportId") sportId: Int = 1,
        @Query("teamId") teamId: Int,
        @Query("season") season: Int,
        @Query("hydrate") hydrate: String = "team,venue,linescore"
    ): ScheduleResponseDto

    @GET("api/v1/schedule")
    suspend fun getScheduleByDate(
        @Query("sportId") sportId: Int = 1,
        @Query("teamId") teamId: Int,
        @Query("date") date: String
    ): ScheduleResponseDto

    @GET("api/v1/schedule")
    suspend fun getScheduleRange(
        @Query("sportId") sportId: Int = 1,
        @Query("teamId") teamId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ScheduleResponseDto

    @GET("api/v1.1/game/{gamePk}/feed/live")
    suspend fun getGameFeed(
        @Path("gamePk") gamePk: Int
    ): GameFeedResponseDto

    @GET("api/v1/teams/{teamId}/roster")
    suspend fun getRosterWithStats(
        @Path("teamId") teamId: Int,
        @Query("rosterType") rosterType: String = "active",
        @Query("hydrate") hydrate: String =
            "person(stats(group=[hitting,pitching],type=[season]))"
    ): RosterResponseDto

    @GET("api/v1/standings")
    suspend fun getStandings(
        @Query("sportId") sportId: Int = 1,
        @Query("leagueId") leagueId: String = "103,104",
        @Query("season") season: Int,
        @Query("standingsTypes") standingsTypes: String = "regularSeason"
    ): StandingsResponseDto
}
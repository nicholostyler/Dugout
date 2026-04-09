package nicholos.tyler.dugout.model.domain


data class TeamDisplayInfo(
    val id: Int,
    val fullName: String,
    val shortName: String,
    val abbreviation: String
)

object MlbTeams {

    val byId = mapOf(
        108 to TeamDisplayInfo(108, "Los Angeles Angels", "Angels", "LAA"),
        109 to TeamDisplayInfo(109, "Arizona Diamondbacks", "Diamondbacks", "ARI"),
        110 to TeamDisplayInfo(110, "Baltimore Orioles", "Orioles", "BAL"),
        111 to TeamDisplayInfo(111, "Boston Red Sox", "Red Sox", "BOS"),
        112 to TeamDisplayInfo(112, "Chicago Cubs", "Cubs", "CHC"),
        113 to TeamDisplayInfo(113, "Cincinnati Reds", "Reds", "CIN"),
        114 to TeamDisplayInfo(114, "Cleveland Guardians", "Guardians", "CLE"),
        115 to TeamDisplayInfo(115, "Colorado Rockies", "Rockies", "COL"),
        116 to TeamDisplayInfo(116, "Detroit Tigers", "Tigers", "DET"),
        117 to TeamDisplayInfo(117, "Houston Astros", "Astros", "HOU"),
        118 to TeamDisplayInfo(118, "Kansas City Royals", "Royals", "KC"),
        119 to TeamDisplayInfo(119, "Los Angeles Dodgers", "Dodgers", "LAD"),
        120 to TeamDisplayInfo(120, "Washington Nationals", "Nationals", "WSH"),
        121 to TeamDisplayInfo(121, "New York Mets", "Mets", "NYM"),
        133 to TeamDisplayInfo(133, "Oakland Athletics", "Athletics", "OAK"),
        134 to TeamDisplayInfo(134, "Pittsburgh Pirates", "Pirates", "PIT"),
        135 to TeamDisplayInfo(135, "San Diego Padres", "Padres", "SD"),
        136 to TeamDisplayInfo(136, "Seattle Mariners", "Mariners", "SEA"),
        137 to TeamDisplayInfo(137, "San Francisco Giants", "Giants", "SF"),
        138 to TeamDisplayInfo(138, "St. Louis Cardinals", "Cardinals", "STL"),
        139 to TeamDisplayInfo(139, "Tampa Bay Rays", "Rays", "TB"),
        140 to TeamDisplayInfo(140, "Texas Rangers", "Rangers", "TEX"),
        141 to TeamDisplayInfo(141, "Toronto Blue Jays", "Blue Jays", "TOR"),
        142 to TeamDisplayInfo(142, "Minnesota Twins", "Twins", "MIN"),
        143 to TeamDisplayInfo(143, "Philadelphia Phillies", "Phillies", "PHI"),
        144 to TeamDisplayInfo(144, "Atlanta Braves", "Braves", "ATL"),
        145 to TeamDisplayInfo(145, "Chicago White Sox", "White Sox", "CWS"),
        146 to TeamDisplayInfo(146, "Miami Marlins", "Marlins", "MIA"),
        147 to TeamDisplayInfo(147, "New York Yankees", "Yankees", "NYY"),
        158 to TeamDisplayInfo(158, "Milwaukee Brewers", "Brewers", "MIL")
    )

    fun get(teamId: Int, fallbackName: String? = null): TeamDisplayInfo {
        return byId[teamId]
            ?: TeamDisplayInfo(
                id = teamId,
                fullName = fallbackName ?: "Unknown Team",
                shortName = fallbackName ?: "",
                abbreviation = fallbackName?.take(3)?.uppercase() ?: ""
            )
    }
}
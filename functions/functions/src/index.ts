import {onSchedule} from "firebase-functions/v2/scheduler";
import {initializeApp} from "firebase-admin/app";
import {FieldValue, getFirestore} from "firebase-admin/firestore";

initializeApp();

const db = getFirestore();

export const syncTodayMlbGames = onSchedule(
  {
    schedule: "every 5 minutes",
    timeZone: "America/New_York",
  },
  async () => {
    const today = new Date().toISOString().slice(0, 10);

    const url =
      `https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=${today}`;

    const response = await fetch(url);
    const data = await response.json();

    const games = data.dates?.[0]?.games ?? [];

    const batch = db.batch();

    for (const game of games) {
      const gamePk = game.gamePk.toString();

      const gameRef = db.collection("games").doc(gamePk);

      batch.set(
        gameRef,
        {
          gamePk: game.gamePk,
          gameDate: game.gameDate,

          status: game.status?.detailedState ?? "",
          abstractGameState: game.status?.abstractGameState ?? "",

          awayTeamId: game.teams?.away?.team?.id ?? 0,
          awayTeamName: game.teams?.away?.team?.name ?? "",
          awayScore: game.teams?.away?.score ?? 0,

          homeTeamId: game.teams?.home?.team?.id ?? 0,
          homeTeamName: game.teams?.home?.team?.name ?? "",
          homeScore: game.teams?.home?.score ?? 0,

          updatedAt: FieldValue.serverTimestamp(),
        },
        {merge: true}
      );
    }

    await batch.commit();

    await db.collection("functionTests").doc("latestMlbSync").set(
      {
        message: `Synced ${games.length} MLB games`,
        date: today,
        updatedAt: FieldValue.serverTimestamp(),
      },
      {merge: true}
    );
  }
);

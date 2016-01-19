SOURCE=$(dirname $0)
echo $SOURCE
mongoexport --db nba-databank --collection playerstats --csv --fieldFile ${SOURCE}/field_files/playerstats.fields --out playerstats.csv
mongoexport --db nba-databank --collection gamelogs --csv --fieldFile ${SOURCE}/field_files/gamelog.fields --out gamelogs.csv
mongoexport --db nba-databank --collection commonteamroster --csv --fieldFile ${SOURCE}/field_files/commonteamroster.fields --out commonteamroster.csv
mongoexport --db nba-databank --collection coachroster --csv --fieldFile ${SOURCE}/field_files/coachroster.fields --out coachroster.csv
mongoexport --db nba-databank --collection commonplayers --csv --fieldFile ${SOURCE}/field_files/commonplayer.fields --out commonplayer.csv
mongoexport --db nba-databank --collection teams --csv --fieldFile ${SOURCE}/field_files/team.fields --out team.csv

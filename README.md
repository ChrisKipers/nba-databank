# NBA Databank

## Summary
A Scala program that retrieves information from stats.nba.com and builds a MongoDB database. Scripts are included
to export the MongoDB collections to CSV. Project was inspired by [nba_py](https://github.com/seemethere/nba_py).

## Installation
1. Make sure local MongoDB is running.
2. Make sure Scala and SBT are installed on your machine.
3. Clone this repo.
4. Change directory to nba-databank.
5. Run the command "sbt run" in the terminal.

## NBA data collected
The NBA data that is collected is stored in six MongoDB collections: gamelog, playerstats, coachroster, commonplayer,
commonteamroster and teams.

### gamelog
Field       | Type
=========== | ====
FTA         | Integer
STL         | Integer
OREB        | Integer
MATCHUP     | String
WL          | String
Game_ID     | String
FG_PCT      | Float
TOV         | Integer
MIN         | Integer
BLK         | Integer
FGM         | Integer
FTM         | Integer
FT_PCT      | Float
REB         | Integer
PF          | Integer
PTS         | Integer
FGA         | Integer
FG3A        | Integer
DREB        | Integer
FG3M        | Integer
AST         | Integer
GAME_DATE   | String
FG3_PCT     | Float
Team_ID     | Integer

### playerstats
Field               | Type
=================== | ====
FTA                 | Integer
STL                 | Integer
OREB                | Integer
TEAM_CITY           | String
TEAM_ABBREVIATION   | String
COMMENT             | String
PLAYER_NAME         | String
FG_PCT              | Float
MIN                 | String
BLK                 | Integer
GAME_ID             | String
START_POSITION      | String
FGM                 | Integer
FTM                 | Integer
FT_PCT              | Float
PLUS_MINUS          | Integer
TEAM_ID             | Integer
REB                 | Integer
PF                  | Integer
PTS                 | Integer
FGA                 | Integer
FG3A                | Integer
PLAYER_ID           | Integer
DREB                | Integer
FG3M                | Integer
AST                 | Integer
TO                  | Integer
FG3_PCT             | Float

### coachroster
Field           | Type
=============== | ====
SCHOOL          | String
FIRST_NAME      | String
COACH_ID        | String
LAST_NAME       | String
COACH_TYPE      | String
TEAM_ID         | Integer
COACH_NAME      | String
COACH_CODE      | String
SORT_SEQUENCE   | Integer
SEASON          | String
IS_ASSISTANT    | Integer

### commonplayer
Field                       | Type
=========================== | ====
TEAM_CITY                   | String
TEAM_CODE                   | String
TO_YEAR                     | String
TEAM_ABBREVIATION           | String
DISPLAY_LAST_COMMA_FIRST    | String
GAMES_PLAYED_FLAG           | String
FROM_YEAR                   | String
PLAYERCODE                  | String
ROSTERSTATUS                | Integer
TEAM_ID                     | Integer
TEAM_NAME                   | String
PERSON_ID                   | Integer

### commonteamroster
Field       | Type
=========== | ====
EXP         | String
BIRTH_DATE  | String
SCHOOL      | String
HEIGHT      | String
AGE         | Integer
TeamID      | Integer
WEIGHT      | String
NUM         | String
LeagueID    | String
POSITION    | String
PLAYER_ID   | Integer
PLAYER      | String
SEASON      | String

### teams
Field           | Type
=============== | ====
ABBREVIATION    | String
TEAM_ID         | Integer
LEAGUE_ID       | String
MIN_YEAR        | String
MAX_YEAR        | String

## Todo
1. Store more information gathered from stats.nba.com.
2. Normalize data.
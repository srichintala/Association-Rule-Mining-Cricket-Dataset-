import yaml
import os
import csv
from bunch import bunchify
from geopy.geocoders import Nominatim

#Path to the directory in which all the files exist
path = "K://IUB Sem-4//Data mining- B565//odis//"
file_names = os.listdir(path) #stores the file names (with extensions) in a list (file_names)

#Iterates over each file to get the binary values of the features and ignores the matches in
#which the match outcome is not there (if the match is cancelled (due to rain or other reasons)
for files in file_names:
    print("filename", files)
    fpath = "K://IUB Sem-4//Data mining- B565//odis//" + files
    with open(fpath, 'r') as stream:
        datamap = yaml.safe_load(stream)
    x = bunchify(datamap)
    if(x.info.outcome.has_key('winner')):
        team1_bat_first = 0
        team1_bat_second = 0
        team2_bat_first = 0
        team2_bat_second = 0
        first_bat_team = ""
        team1_winner = 0
        team1_runs = 0
        team2_runs = 0
        team1_abv300 = 0
        team1_less200 = 0
        team1_between200n300 = 0
        team2_abv300 = 0
        team2_less200 = 0
        team2_between200n300 = 0
        team1_wicket_count = 0
        team2_wicket_count = 0
        team1_wicket = 0
        team2_wicket = 0
        home_match = 0
        features_list = []

        if(x['info']['toss']['winner'] == x.info.teams[0]):
            if(x['info']['toss']['decision'] == "bat"):
                team1_bat_first = 1
                team2_bat_second = 1
                first_bat_team = x.info.teams[0]
            else:
                team1_bat_second = 1
                team2_bat_first = 1
                first_bat_team = x.info.teams[1]
        elif(x['info']['toss']['winner'] == x.info.teams[1]):
            if(x['info']['toss']['decision'] == "bat"):
                team2_bat_first = 1
                team1_bat_second = 1
                first_bat_team = x.info.teams[1]
            else:
                team2_bat_second = 1
                team1_bat_first = 1
                first_bat_team = x.info.teams[0]
        if((team1_bat_first & team2_bat_second) & (x.info.teams[0] == x.innings[0]['1st innings']['team'])):
            for d in x.innings[0]['1st innings']['deliveries']:
                for k in d:
                    team1_runs = team1_runs + d[k]['runs']['total']
                    if d[k].has_key('wicket'):
                        team2_wicket_count = team2_wicket_count + 1
            for d in x.innings[1]['2nd innings']['deliveries']:
                for k in d:
                    team2_runs = team2_runs + d[k]['runs']['total']
                    if d[k].has_key('wicket'):
                        team1_wicket_count = team1_wicket_count + 1
        elif((team2_bat_first & team1_bat_second) & (x.info.teams[1] == x.innings[0]['1st innings']['team'])):
            for d in x.innings[0]['1st innings']['deliveries']:
                for k in d:
                    team2_runs = team2_runs + d[k]['runs']['total']
                    if d[k].has_key('wicket'):
                        team1_wicket_count = team1_wicket_count + 1
            for d in x.innings[1]['2nd innings']['deliveries']:
                for k in d:
                    team1_runs = team1_runs + d[k]['runs']['total']
                    if d[k].has_key('wicket'):
                        team2_wicket_count = team2_wicket_count + 1

        #to get the team1 and team2 runs
        if(team1_runs >= 300):
            team1_abv300 = 1
        elif(team1_runs <= 200):
            team1_less200 = 1
        elif(team1_runs >= 200 & team1_runs <= 300):
            team1_between200n300 = 1
        if(team2_runs >= 300):
            team2_abv300 = 1
        elif(team2_runs <= 200):
            team2_less200 = 1
        elif(team2_runs >= 200 & team2_runs <= 300):
            team2_between200n300 = 1

        #to get the binary value of wickets taken by both teams
        if(team1_wicket_count >= 5):
            team1_wicket = 1
        elif(team2_wicket_count >= 5):
            team2_wicket = 1

        #to get the binary value of the winner (team batting first win or loose)
        if(first_bat_team == x.info.outcome.winner):
            team1_winner = 1
        else:
            team1_winner = 0

        #to get information of home or away match
        geolocator = Nominatim()
        #city = x['info']['venue'].replace(',', '')
        if(x.info.has_key('city')):
            location = geolocator.geocode(x['info']['city'])
            address = [x.strip() for x in location._address.split(',')]
            if(address[-1]== "United Kingdom"):
                if(address[2] == "England") & (first_bat_team == "England"):
                    home_match = 1
            elif(address[-1] == first_bat_team):
                home_match = 1
        else:
            location = geolocator.geocode(x['info']['venue'])
            address = [x.strip() for x in location._address.split(',')]
            if(address[-1]== "United Kingdom"):
                if(address[2] == "England") & (first_bat_team == "England"):
                    home_match = 1
            elif(address[-1] == first_bat_team):
                home_match = 1
        features_list.append(team1_abv300)
        features_list.append(team1_less200)
        features_list.append(team1_between200n300)
        features_list.append(team1_wicket)
        features_list.append(team2_abv300)
        features_list.append(team2_less200)
        features_list.append(team2_between200n300)
        features_list.append(team2_wicket)
        features_list.append(team1_winner)
        features_list.append(home_match)
        print(first_bat_team, home_match, location._address)
        out = csv.writer(open('K://IUB Sem-4//Data mining- B565//features_dataset.csv',"ab"), delimiter=',',quoting=csv.QUOTE_MINIMAL)
        out.writerow(features_list)

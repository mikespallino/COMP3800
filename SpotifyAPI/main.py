import requests
import json
import configparser

config = configparser.ConfigParser()
config.read('/sapiws/.conf')

CLIENT_ID = config['API_SETTINGS']['API_ID']
CLIENT_SECRET = config['API_SETTINGS']['API_SECRET']

token_url = 'https://accounts.spotify.com/api/token'
search_url = 'https://api.spotify.com/v1/search?{}'
id_url = 'https://api.spotify.com/v1/me'
token_payload = {'grant_type': 'client_credentials', 'scope': 'playlist-modify-public playlist-modify-private'}


def get_access_token():
    resp = requests.post(token_url,  data=token_payload, auth=(CLIENT_ID, CLIENT_SECRET))
    return json.loads(resp.text)


def search_song(song_title, access_type, access_token):
    url = search_url.format('q={song}&type=track'.format(song=song_title))
    header = {'Authorization': '{token_type} {token}'.format(token_type=access_type, token=access_token)}
    resp = requests.get(url, headers=header)
    return json.loads(resp.text)


def get_spotify_id(access_type, access_token):
    header = {'Authorization': '{token_type} {token}'.format(token_type=access_type, token=access_token)}
    resp = requests.get(id_url, headers=header)
    return json.loads(resp.text)

if __name__ == '__main__':
    token = get_access_token()
    print(token)

    song = 'Killing%20in%20the%20name'
    search_result = search_song(song, token['token_type'], token['access_token'])
    print(search_result)

    spotify_id = get_spotify_id(token['token_type'], token['access_token'])
    print(spotify_id)
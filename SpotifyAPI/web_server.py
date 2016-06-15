import bottle
import configparser
import urllib.parse
import json

config = configparser.ConfigParser()
config.read('/sapiws/dama.conf')

CLIENT_ID = config['API_SETTINGS']['API_ID']
CLIENT_SECRET = config['API_SETTINGS']['API_SECRET']
SCOPES = urllib.parse.quote_plus(config['API_SETTINGS']['SCOPE'])
WEBSERVER_URL = urllib.parse.quote_plus(config['WEBSERVER_SETTINGS']['URL'])
AUTHORIZE_URL = 'https://accounts.spotify.com/authorize?{params}'
AUTHORIZE_PARAMS = 'response_type=code&client_id={cid}&scopes={scopes}&redirect_uri={redirect}'


@bottle.route('/login')
def login():
    params = AUTHORIZE_PARAMS.format(cid=CLIENT_ID, scopes=SCOPES, redirect='{}/authorized'.format(WEBSERVER_URL))
    bottle.redirect(AUTHORIZE_URL.format(params=params))


@bottle.route('/authorized', method='GET')
def logged_in():
    query_string = bottle.request.query_string
    query_dict = urllib.parse.parse_qs(query_string)
    query_dict['code'] = query_dict['code'][0]
    return json.dumps(query_dict)


if __name__ == "__main__":
    bottle.run(host='127.0.0.1', port=5000)
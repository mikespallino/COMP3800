import flask
import configparser
import urllib.parse

config = configparser.ConfigParser()
config.read('/sapiws/dama.conf')

CLIENT_ID = config['API_SETTINGS']['API_ID']
CLIENT_SECRET = config['API_SETTINGS']['API_SECRET']
SCOPES = urllib.parse.quote_plus(config['API_SETTINGS']['SCOPE'])
WEBSERVER_URL = urllib.parse.quote_plus(config['WEBSERVER_SETTINGS']['URL'])
AUTHORIZE_URL = 'https://accounts.spotify.com/authorize?{params}'
AUTHORIZE_PARAMS = 'response_type=code&client_id={cid}&scopes={scopes}&redirect_uri={redirect}'
app = flask.Flask('SAPI Web Server')


@app.route('/login')
def login():
    params = AUTHORIZE_PARAMS.format(cid=CLIENT_ID, scopes=SCOPES, redirect='{}/authorized'.format(WEBSERVER_URL))
    flask.redirect(AUTHORIZE_URL.format(params=params))


@app.route('/authorized')
def logged_in():
    return None

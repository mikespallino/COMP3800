import bottle
import pymysql
import json
import configparser

import queries


config = configparser.ConfigParser(interpolation=None)
config.read('dama.ini')
db_creds = {
    'host': 'localhost',
    'user': config['db_creds']['user'],
    'password': config['db_creds']['passwd'],
    'db': 'DAMA',
    'cursorclass': pymysql.cursors.DictCursor
}

errors = {
    'database': '1 (Database error)',
    'server': '2 (Web server error): {}'
}


@bottle.route('/host', method='POST')
def host():
    valid_keys = ['event_name', 'event_password', 'playlist_name', 'playlist_songs', 'all_songs', 'location', 'key',
                  'playlist_id']
    post_data = bottle.request.json
    validated = validate_response(post_data, valid_keys)
    if isinstance(validated, str):
        return bottle.redirect('/error/{}'.format(errors['server'].format(validated)))
    else:
        del post_data['key']
        try:
            con = pymysql.connect(**db_creds)
            cur = con.cursor()
            cur.execute(queries.CREATE_EVENT, (post_data['event_name'], post_data['event_password'],
                                               post_data['playlist_name'], ';'.join(post_data['playlist_songs']),
                                               ';'.join(post_data['all_songs']), post_data['location'],
                                               post_data['playlist_id'],))
            con.commit()
        except Exception as e:
            print(e)
            bottle.redirect('/error/{}'.format(errors['database']))
        finally:
            cur.close()
            con.close()

    return {'THANKS!': 123}


@bottle.route('/join', method='POST')
def join():
    valid_keys = ['event_name', 'event_password', 'key']
    post_data = bottle.request.json
    validated = validate_response(post_data, valid_keys)
    event_data = {'playlist_name': '', 'playlist_songs': [''], 'all_songs': ['']}
    if isinstance(validated, str):
        return bottle.redirect('/error/{}'.format(errors['server'].format(validated)))
    else:
        del post_data['key']
        try:
            if post_data['event_password'] is not None:
                post_data['event_password'] = '= ("{}")'.format(post_data['event_password'])
            else:
                post_data['event_password'] = 'IS NULL'
            query = queries.JOIN_EVENT.format(**post_data)
            con = pymysql.connect(**db_creds)
            cur = con.cursor()
            cur.execute(query)
            result = cur.fetchone()
            event_data['playlist_name'] = result['playlist_name']
            event_data['playlist_songs'] = result['playlist_songs'].split(';')
            event_data['all_songs'] = result['all_songs'].split(';')
        except Exception as e:
            print(e)
            bottle.redirect('/error/{}'.format(errors['database']))
        finally:
            cur.close()
            con.close()
    return json.dumps(event_data)


@bottle.route('/discover', method='POST')
def discover():
    valid_keys = ['location', 'key']
    post_data = bottle.request.json
    validated = validate_response(post_data, valid_keys)
    event_list = {'event_list': []}
    if isinstance(validated, str):
        return bottle.redirect('/error/{}'.format(errors['server'].format(validated)))
    else:
        del post_data['key']
        try:
            con = pymysql.connect(**db_creds)
            cur = con.cursor()
            cur.execute(queries.DISCOVER_EVENT, (post_data['location'],))
            results = cur.fetchall()
            for result in results:
                event_list['event_list'].append(result['event_name'])
        except Exception as e:
            print(e)
            bottle.redirect('/error/{}'.format(errors['database']))
        finally:
            cur.close()
            con.close()
    return json.dumps(event_list)


@bottle.route('/delete_event', method='POST')
def delete_event():
    valid_keys = ['event_name', 'event_password', 'key']
    post_data = bottle.request.json
    validated = validate_response(post_data, valid_keys)
    if isinstance(validated, str):
        return bottle.redirect('/error/{}'.format(errors['server'].format(validated)))
    else:
        try:
            del post_data['key']
            if post_data['event_password'] is not None:
                post_data['event_password'] = ' = ("{}")'.format(post_data['event_password'])
            else:
                post_data['event_password'] = 'IS NULL'
            query = queries.DELETE_EVENT.format(**post_data)
            con = pymysql.connect(**db_creds)
            cur = con.cursor()
            cur.execute(query)
            con.commit()
        except Exception as e:
            print(e)
            bottle.redirect('/error/{}'.format(errors['database']))
        finally:
            cur.close()
            con.close()
        return 'Event Deleted!'


@bottle.route('/error/<code>')
def error(code):
    return json.dumps({'error': code})


def validate_response(post_data, valid_keys):
    if post_data.get('key', None) != 'DAMA':
        return 'Bad Request: not a valid request'
    else:
        for key in post_data:
            if key not in valid_keys:
                return 'Bad Request: unknown key'
        if len(post_data.keys()) != len(valid_keys):
            return 'Bad Request: not enough keys'

    return True


if __name__ == "__main__":
    bottle.run(host='192.241.149.243', server='paste')

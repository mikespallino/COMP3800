CREATE_EVENT = """
               INSERT INTO DAMA.events (event_uuid, event_name, event_password, playlist_name, playlist_songs, all_songs, location, cur_play)
               VALUES (%s,%s,%s,%s,%s,%s,%s,%s);
               """
DISCOVER_EVENT = """
                 SELECT event_name FROM DAMA.events WHERE location = (%s);
                 """
DELETE_EVENT = """
               DELETE FROM DAMA.events WHERE event_name = ("{event_name}") AND event_password {event_password};
               """
JOIN_EVENT = """
             SELECT playlist_name, playlist_songs, all_songs, event_uuid FROM DAMA.events
             WHERE event_name = ("{event_name}") AND event_password {event_password};
             """
SET_CUR_PLAY = """
               UPDATE DAMA.events SET cur_play=%s WHERE event_uuid=%s;
               """
GET_CUR_PLAY = """
               SELECT cur_play FROM DAMA.events WHERE event_uuid=%s;
               """
MAKE_REQUEST = """
               INSERT INTO DAMA.requests (event_uuid, song_title) VALUES (%s,%s);
               """
GET_REQUESTS = """
               SELECT song_title FROM DAMA.requests WHERE event_uuid=%s;
               """
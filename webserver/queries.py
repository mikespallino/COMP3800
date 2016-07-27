CREATE_EVENT = """
               INSERT INTO DAMA.events (event_name, event_password, playlist_name, playlist_songs, all_songs, location, playlist_id)
               VALUES (%s,%s,%s,%s,%s,%s,%s);
               """
DISCOVER_EVENT = """
                 SELECT event_name FROM DAMA.events WHERE location = (%s);
                 """
DELETE_EVENT = """
               DELETE FROM DAMA.events WHERE event_name = ("{event_name}") AND event_password {event_password};
               """
JOIN_EVENT = """
             SELECT playlist_name, playlist_songs, all_songs FROM DAMA.events
             WHERE event_name = ("{event_name}") AND event_password {event_password};
             """
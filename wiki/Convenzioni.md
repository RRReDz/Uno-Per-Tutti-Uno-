Convenzioni di progetto
=====
Non fare confusione tra stanza e partita: la stanza contiene una lista di
partite e giocatori, la partita contiene solamente giocatori e sarà il luogo
dove verranno giocate le manches.

Le classi che si riferiscono alla stanza devono avere il suffisso "Room",
le classi che invece riguardano la partita devono avere il suffisso "Match".

La logica della della e di tutte le partita in essa contenute viene eseguita
dal proprietario della stanza (quindi, in un certo senso, il proprietario
di una partita non è altro che un client con qualche piccolo privilegio).

Tutti i giocatori (quindi proprietari compresi) comunicano con stanze e partite
tramite le classi "Remote*", che in un certo senso fanno da wrapper ed eseguono
la connessione vera e propria. In questo modo non si devono fare ragionamenti
strani e/o contorti del tipo "Se sono io il proprietario, mando un messaggio
al mio thread, altrimenti mando un messaggio tramite TCP/IP".

È molto importante, in ogni momento, non confondere i termini Stanza/Room e
Partita/Match.

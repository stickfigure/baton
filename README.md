# baton

Baton is a simple proxy which accepts SMTP connections and routes them to one or more backend
SMTP servers based on the envelope sender or recipients.  The conversation is proxied in
realtime without storing the mail content.

There are no downloads but the code works - it is simple enough to be self-explanatory,
especially with the test cases.  Code derived from this project is used to implement the
"fallback host" feature of the [SubEtha](http://code.google.com/p/subetha/) mailing list server.

Automatically exported from code.google.com/p/baton

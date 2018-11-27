This project is a simple framework intended for obtaining diagnostic information from remote nodes through JMS.

Components:

Utility    - an utility used to synchronously request diagnostic info from remote nodes
             which hostname contains a substring given as an argument to the utility.

TestApp    - a multithreaded application simulating multiple remote nodes to obtain diagnostic
             information from.

DiagCollectorLib - a library intended to be used in applications which provide diagnostic info.

MsgLibrary - a simple convenience wrapper to ActiveMQ.

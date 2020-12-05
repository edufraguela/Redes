Sistema Operativo : Kali linux (debian)


EJECUCUION PRACTICA WEBSERVER

Lanzar WebServer desde netbeans(yo lo hice así con el custom config)/terminal

Se ejecuta desde el terminal el httptester.jar 

Se comprueba que da 0 failures(tendría que dar 0)


PROBAR HEAD/GET

Lanzar WebServer desde netbeans/terminal

Desde el terminal se escribe: nc localhost 5000

Desde el navegador http://localhost:5000/udc.gif (por ejemplo)

Por ultimo desde el terminal de nc localhost 5000, se escribe:
	
	HEAD /udc.gif HTTP/1.1


Se vuelve a ejecutar el comando nc localhost 5000

	GET /index.html HTTP/1.1


